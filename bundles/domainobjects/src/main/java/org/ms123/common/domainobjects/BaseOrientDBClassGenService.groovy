/*
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2017] [Manfred Sattler] <manfred@ms123.org>
 *
 * SIMPL4 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SIMPL4 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SIMPL4.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.ms123.common.domainobjects;

import flexjson.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Comparator;
import java.util.Collections;
import java.lang.reflect.Method;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.utils.Inflector;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Bundle;
import org.ms123.common.entity.api.EntityService;
import org.ms123.common.system.orientdb.OrientDBService;
import com.tinkerpop.blueprints.impls.orient.OrientGraphFactory;
import com.orientechnologies.orient.core.db.document.ODatabaseDocumentTx;

import com.orientechnologies.orient.core.metadata.schema.OType;
import groovy.transform.CompileStatic;
import org.ms123.groovy.orient.graph.Vertex;
import org.ms123.groovy.orient.graph.Edge;

import java.io.File;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;

/**
 *
 */
@groovy.transform.CompileStatic
@groovy.transform.TypeChecked
abstract class BaseOrientDBClassGenService implements org.ms123.common.domainobjects.api.ClassGenService{

	protected Inflector m_inflector = Inflector.getInstance();
	protected EntityService m_entityService;
	protected OrientDBService m_orientdbService;

	public List<String> generate(StoreDesc sdesc, List<Map> entities, String outDir) throws Exception {
		info(this,"--->generate:" + sdesc.getString());
		List<String> classnameList = new ArrayList();
		String javaPackage=null;
		for (int i = 0; i < entities.size(); i++) { //Make empty classes to resolve relations
			Map m = entities.get(i);
			String fqn = getFQN(sdesc, m);
			info(this,"\tmakeClass:" + fqn);
			if( i==0){
				javaPackage = getJavaPackage(sdesc, m);
			}
		}
		info(this,"entities:"+entities);
		ClassBuilder builder = new ClassBuilder(this.getClass().getClassLoader());
		builder.setPack( javaPackage );
		builder.addImport( OType);
		builder.addImport( CompileStatic);
		builder.addImport( Vertex);
		builder.addImport( Edge);
		for (int i = 0; i < entities.size(); i++) {
			Map entMap = entities.get(i);
			String name = (String) entMap.get("name");
			String fqn = getFQN(sdesc, entMap);
			String classname = getClassName(sdesc, entMap);
			boolean isVertex = isVertex(entMap);
			String  superclass = getSuperclass(entMap);

			builder.newClazz(classname);
			builder.setAnnotation("@"+superclass+"(initSchema = true, value = '"+classname+"')");
			List<Map> fields = getEntityMetaData(sdesc, name);
			for( int j=0; j< fields.size(); j++){
				Map field = fields.get(j);
				String fieldname = getName(field);
				if( "id".equals(fieldname)){
					continue;
				}
				OType otype = getType(field);
				Class javaClazz = otype.getDefaultJavaType()
				boolean isMulti = otype.isMultiValue();
				boolean isLink = otype.isLink();
				String linkedClass = getLinkedClassName(sdesc, field);
				if( isMulti ){
					if( isLink ){
						builder.addField(fieldname, linkedClass);
						builder.addMapping(fieldname, "edge: "+ linkedClass )
					}else{
						if( linkedClass != null ){
							builder.addField(fieldname, linkedClass);
						}else{
							builder.addField(fieldname, getLinkedType(field).getDefaultJavaType());
							builder.addMapping(fieldname, "type: "+ getLinkedType(field).toString() )
						}
					}
				}else{
					builder.addField(fieldname, javaClazz)
				}
			}

//			builder.addMapping('field1', 'index: \'unique\'' )
//			builder.addMapping('field2', 'edge: Lives' )*/


			classnameList.add(classname);
			List<String> pkNameList = (List) entMap.get("primaryKeys");
			if (pkNameList != null && pkNameList.size() > 1) {
				String classnamePK = classname + "_PK";
				//classnameList.add(classnamePK);
			}
		}

		File _outDir = new File(outDir,  javaPackage.replace(".","/"));
		if (!_outDir.exists()) {
			_outDir.mkdirs();
		}

		builder.createClasses()
		OrientGraphFactory f = m_orientdbService.getFactory(sdesc.getNamespace());
		ODatabaseDocumentTx db = f.getNoTx().getRawGraph();
		for( int i=0; i < classnameList.size();i++){
			String cn = classnameList.get(i);
			builder.writeClassFile( new File(_outDir, cn+ ".class"), cn );
			Class clazz = builder.getClazz( cn );
			info(this,"class("+cn+"):"+clazz);
			callInit( db, clazz, "initSchema");
			callInit( db, clazz, "initSchemaLinks");
		}
		info(this,"classnameList:"+classnameList);
		db.commit();
		return classnameList;
	}

	protected List getEntityMetaData(StoreDesc sdesc, String entity) throws Exception {
		List list = m_entityService.getFields(sdesc, entity, false);
		info(this,"getEntityMetaData:"+list);
		return list;
	}

	private void callInit( ODatabaseDocumentTx db, Class clazz, String meth ){
		Class[] cargs = new Class[1];
		cargs[0] = ODatabaseDocumentTx.class;
		try {
			Method _meth = clazz.getDeclaredMethod(meth, cargs);
			Object[] args = new Object[1];
			args[0] = db;
			_meth.invoke(null, args);
			info(this, "Init("+clazz+":" + meth);
		} catch (java.lang.reflect.InvocationTargetException e) {
			throw e.getCause();
		}
	}

	private boolean isVertex( Map<String,String> m){
		return "vertex".equals(m.get("superclass"));
	}

	private boolean isEdge( Map<String,String> m){
		return "edge".equals(m.get("superclass"));
	}

	private String getSuperclass( Map<String,String> m){
		return firstToUpper(m.get("superclass"));
	}
	private OType getType( Map<String,String> m){
		return OType.getById(Byte.parseByte(m.get("datatype")));
	}
	private OType getLinkedType( Map<String,String> m){
		return OType.getById(Byte.parseByte(m.get("linkedtype")));
	}

	private String getName( Map<String,String> m){
		return m.get("name");
	}

	private String firstToUpper(String s) {
		String fc = s.substring(0, 1);
		return fc.toUpperCase() + s.substring(1);
	}

	private String firstToLower(String s) {
		String fc = s.substring(0, 1);
		return fc.toLowerCase() + s.substring(1);
	}

	private boolean getBoolean(Object o, boolean _def) {
		if (o == null)
			return _def;
		try {
			boolean b = (Boolean) o;
			return b;
		} catch (Exception e) {
		}
		return _def;
	}
	private String getLinkedClassName(StoreDesc sdesc, Map entity) {
		String name = (String)entity.get("linkedclass");
		if( name == null) return null;
		name = StoreDesc.getSimpleEntityName(name);
		return m_inflector.getClassName((String) name);
	}
	private String getClassName(StoreDesc sdesc, Map entity) {
		String name = (String)entity.get("name");
		if( name == null) return null;
		name = StoreDesc.getSimpleEntityName(name);
		return m_inflector.getClassName((String) name);
	}

	private String getJavaPackage(StoreDesc sdesc, Map entity) {
		String name = (String)entity.get("name");
		String pack = StoreDesc.getPackName(name,sdesc.getPack());
		return sdesc.getJavaPackage(pack);
	}
	private String getFQN(StoreDesc sdesc, Map entity) {
		String name = (String)entity.get("name");
		String pack = StoreDesc.getPackName(name,sdesc.getPack());
		name = StoreDesc.getSimpleEntityName(name);
		String className = m_inflector.getClassName((String) name);
		return sdesc.getJavaPackage(pack) + "." + className;
	}
}

