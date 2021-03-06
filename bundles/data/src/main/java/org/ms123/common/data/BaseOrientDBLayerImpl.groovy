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
package org.ms123.common.data;

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
import org.ms123.common.libhelper.Inflector;
import java.lang.reflect.Method;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.utils.Inflector;
import org.ms123.common.data.api.SessionContext;


import groovy.transform.CompileStatic;
import groovy.transform.TypeCheckingMode

import java.io.File;
import static com.jcabi.log.Logger.info;
import static com.jcabi.log.Logger.error;
import static com.jcabi.log.Logger.debug;

/**
 *
 */
//@groovy.transform.CompileStatic
abstract class BaseOrientDBLayerImpl implements org.ms123.common.data.api.DataLayer{

	protected Inflector inflector = Inflector.getInstance();
	public List<Map> executeQuery(SessionContext sc, Class clazz,String className, String where){
		info(this,"executeQuery.sql:"+"select from "+className + " where "+ where);
		List list = clazz.graphQuery("select from "+className + " where "+ where,false);
		List result = [];
		list.each{ obj ->
			result.add( _objToMap( sc, obj));
		}
		info(this,"List:"+result);
		return result;
	}
	protected Map _objToMap(SessionContext sc, Object obj ){
		def entityName = this.inflector.lowerFirst(obj.getClass().getSimpleName());
		Map fields = sc.getPermittedFields(entityName, "read");
		def cleanData = [:];
		fields.each{ k, v ->
			if( isSimple( v.datatype ) && obj[k] != null){
				cleanData[k] = obj[k];
			}else if( isObj(v.datatype) && obj[k] != null){
				info(this,"Obj("+entityName+":"+k+"):"+obj[k]);
				cleanData[k] = _objToMap(sc, obj[k]);
			}else if( isList( v.datatype ) && obj[k] != null){
				info(this,"List("+entityName+":"+k+"):"+obj[k]);
				def mapList = [];
				for( Object child : obj[k] ){
					mapList.add( _objToMap(sc,child) );
				}
				cleanData[k] = mapList;
			}
		}
		cleanData["_id"] = obj.getId().toString();
		cleanData["@class"] = obj.vertex.getRecord().getClassName();
		return cleanData;
	}

	public Map executeInsertObject(SessionContext sc, String entityName,Map data){
		def obj = _executeInsertObject(sc, entityName, data);
		return [ id : obj.getIdentity() ];
	}
	protected Object _executeInsertObject(SessionContext sc, String entityName,Map data){
		Map fields = sc.getPermittedFields(entityName, "write");
		def cleanData = [:];
		fields.each{ k, v ->
			info(this,"K:"+k+"/"+v.datatype);
			if( isSimple( v.datatype ) && data[k] != null){
				info(this,"Simple("+entityName+":"+k+"):"+data[k]);
				if( isDate( v.datatype) && data[k] instanceof String){
					cleanData[k] = parseFromString(data[k]);
				}else if( isDate( v.datatype) && data[k] instanceof Long){
					cleanData[k] = new Date(data[k]);
				}else{
					cleanData[k] = data[k];
				}
			}else if( isLinkedObj(v.datatype) && data[k] != null){
				info(this,"LinkedObj("+entityName+":"+k+"):"+data[k]);
				Class clazz = getClass(sc, v.linkedclass);
				def id = data[k]._id;
				if( id && id.startsWith("#") ){
					cleanData[k] = _getObject(clazz, id);
				}
			}else if( isEmbeddedObj(v.datatype) && data[k] != null){
				info(this,"EmbeddedObj("+entityName+":"+k+"):"+data[k]);
				cleanData[k] = _executeInsertObject(sc, v.linkedclass, data[k] );
			}else if( (isLinkedSet(v.datatype) || isLinkedList( v.datatype )) && data[k] != null){
				info(this,"LinkedMulti("+entityName+":"+k+"):"+data[k]);
				def objList = [];
				Class clazz = getClass(sc, v.linkedclass);
				for( Map childData : data[k] ){
					if( childData._id && childData._id.startsWith("#") ){
						def child = _getObject(clazz, childData._id);
						objList.add( child );
					}
				}
				cleanData[k] = objList;
			}else if( (isEmbeddedSet(v.datatype) || isEmbeddedList( v.datatype )) && data[k] != null){
				info(this,"EmbeddedMulti("+entityName+":"+k+"):"+data[k]);
				def cleanList = [];
				for( Map map : data[k] ){
					info(this,"map:"+map);
					def ret = _executeInsertObject(sc, v.linkedclass, map );
					cleanList.add(ret);
				}
				cleanData[k] = cleanList;
			}
		}
		Class clazz = getClass(sc, entityName);
		return clazz.newInstance( cleanData );
	}

	public Map executeUpdateObject(SessionContext sc, String entityName, String id, Map data){
		Class clazz = getClass(sc, entityName);
		def obj = clazz.graphQuery("select from "+id,true);
		if( obj == null){
			throw new RuntimeException("executeUpdateObject("+id+"):not found");
		}
		_executeUpdateObject( sc, obj, data);
		return [ id : id ];
	}

	protected Map _executeUpdateObject(SessionContext sc, Object obj, Map data){
		if( obj == null){
			info(this,"_executeUpdateObject is null:"+data);
			return;
		}
		def entityName = obj.getClass().getSimpleName();
		entityName = this.inflector.lowerFirst(entityName);
		info(this,"_executeUpdateObject("+entityName+"):"+data);
		Map fields = sc.getPermittedFields(entityName, "write");
		fields.each{ k, v ->
			info(this,"K:"+k+"/"+v.datatype);
			if( isSimple( v.datatype) && data[k] != null){
				info(this,"Simple("+entityName+":"+k+"):"+data[k]);
				if( isDate( v.datatype) && data[k] instanceof String){
					obj[k] = parseFromString(data[k]);
				}else if( isDate( v.datatype) && data[k] instanceof Long){
					obj[k] = new Date(data[k]);
				}else{
					obj[k] = data[k];
				}
			}else if( isLinkedObj(v.datatype) && data[k] != null){
				info(this,"LinkedObj("+entityName+":"+k+"):"+data[k]);
				Class clazz = getClass(sc, v.linkedclass);
				def id = data[k]._id;
				if( id && id.startsWith("#") ){
					obj[k] = _getObject(clazz, id);
				}
			}else if( isEmbeddedObj(v.datatype) && data[k] != null){
				info(this,"EmbeddedObj("+entityName+":"+k+"):"+data[k]);
				Class clazz = getClass(sc, v.linkedclass);
				def child = clazz.newInstance(  );
				_executeUpdateObject(sc, child, data[k] );
				obj[k] = child;
			}else if( (isLinkedSet(v.datatype) || isLinkedList( v.datatype )) && data[k] != null){
				info(this,"LinkedMulti("+entityName+":"+k+"):"+data[k]);
				def objList = [];
				Class clazz = getClass(sc, v.linkedclass);
				for( Map childData : data[k] ){
					if( childData._id && childData._id.startsWith("#") ){
						def child = _getObject(clazz, childData._id);
						objList.add( child );
					}
				}
				info(this,"objList:"+objList);
				obj[k] = objList;
			}else if( (isEmbeddedSet(v.datatype) || isEmbeddedList( v.datatype )) && data[k] != null){
				info(this,"EmbeddedMulti("+entityName+":"+k+"):"+data[k]);
				def objList = [];
				Class clazz = getClass(sc, v.linkedclass);
				for( Map childData : data[k] ){
					def child = clazz.newInstance(  );
					_executeUpdateObject(sc, child, childData );
					objList.add( child );
				}
				info(this,"objList:"+objList);
				obj[k] = objList;
			}
		}
	}

	public Map executeDeleteObject(Class clazz,String id){
		def obj = clazz.graphQuery("select from "+id,true);
		if( obj == null){
			throw new RuntimeException("executeDeleteObject("+id+"):not found");
		}
		obj.remove();
		return [ id : id ];
	}

	public Map executeGet(Class clazz,String id){
		info(this,"executeGet:"+id);
		Map row = clazz.graphQueryMap("select from "+id,true);
		return row;
	}
	protected Object _getObject(Class clazz,String id){
		def obj = clazz.graphQuery("select from "+id,true);
		info(this,"_getObject("+id+"):"+obj);
		return obj;
	}
	def isLinkedObj( def dt ){
		def list = ["13"]
		return list.contains( dt );
	}
	def isLinkedList( def dt ){
		def list = ["14"]
		return list.contains( dt );
	}
	def isLinkedSet( def dt ){
		def list = ["15"]
		return list.contains( dt );
	}
	def isList ( dt ){
		def list = ["10", "11", "14", "15"]
		return list.contains( dt );
	}

	def isEmbeddedObj( def dt ){
		def list = ["9"]
		return list.contains( dt );
	}
	def isEmbeddedList( def dt ){
		def list = ["10"]
		return list.contains( dt );
	}
	def isEmbeddedSet( def dt ){
		def list = ["11"]
		return list.contains( dt );
	}

	def isObj ( dt ){
		def list = ["9", "13"]
		return list.contains( dt );
	}

	def isDate ( dt ){
		def list = ["19", "6"]
		return list.contains( dt );
	}

	def isSimple( def dt ){
		def simpleList = ["0", "1", "2", "3", "4", "5", "6", "7", "17", "19", "21"]
		return simpleList.contains( dt );
	}
	def parseFromString( String str ){
		return com.mdimension.jchronic.Chronic.parse(str).beginCalendar.time
	}
}




