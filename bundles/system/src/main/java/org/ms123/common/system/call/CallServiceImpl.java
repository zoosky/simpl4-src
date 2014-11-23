/**
 * This file is part of SIMPL4(http://simpl4.org).
 *
 * 	Copyright [2014] [Manfred Sattler] <manfred@ms123.org>
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
package org.ms123.common.system.call;

import aQute.bnd.annotation.component.*;
import aQute.bnd.annotation.metatype.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.camel.Route;
import org.ms123.common.rpc.PName;
import org.ms123.common.rpc.POptional;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.store.StoreDesc;
import org.ms123.common.permission.api.PermissionService;
import org.ms123.common.camel.api.CamelService;
import org.ms123.common.git.GitService;
import org.osgi.framework.BundleContext;
import org.ms123.common.rpc.RpcException;
import org.ms123.common.rpc.JsonRpcServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import static org.ms123.common.rpc.JsonRpcServlet.ERROR_FROM_METHOD;
import static org.ms123.common.rpc.JsonRpcServlet.INTERNAL_SERVER_ERROR;
import static org.ms123.common.rpc.JsonRpcServlet.PERMISSION_DENIED;

/** CallService implementation
 */
@SuppressWarnings("unchecked")
@Component(enabled = true, configurationPolicy = ConfigurationPolicy.optional, immediate = true, properties = { "rpc.prefix=call" })
public class CallServiceImpl extends BaseCallServiceImpl implements org.ms123.common.rpc.CallService {

	public CallServiceImpl() {
	}

	protected void activate(BundleContext bundleContext, Map<?, ?> props) {
	}

	public void update(Map<String, Object> props) {
	}

	protected void deactivate() throws Exception {
	}

	public Object callCamel(String pathInfo, String serviceName, String methodName, Object _methodParams, HttpServletRequest request, HttpServletResponse response){
		Map methodParams = (Map)_methodParams;
		String ns = getNamespace(methodParams);
		if (ns == null) {
			throw new RpcException(JsonRpcServlet.ERROR_FROM_SERVER, JsonRpcServlet.PARAMETER_MISMATCH, "Namespace not found");
		}
		Map shape = getCamelShape(ns, methodName);
		boolean logExceptionsOnly = getBoolean(shape,"logExceptionsOnly", false);
		List<String> permittedRoleList = getStringList(shape,"startableGroups");
		List<String> permittedUserList = getStringList(shape,"startableUsers");
		List<Map> paramList = getItemList(shape, "rpcParameter");
		String userName = getUserName();
		List<String> userRoleList = null;
		try{
			userRoleList = m_permissionService.getUserRoles(userName);
		}catch(Exception e){
			userRoleList=new ArrayList();
		}

		Map<String,Object> properties = new HashMap();
		info("logExceptionsOnly:"+logExceptionsOnly);
		properties.put("__logExceptionsOnly", logExceptionsOnly);
		Map<String,Object> headers = new HashMap();

		for( Map param : paramList){
			String destination = (String)param.get("destination");
			String name = (String)param.get("name");
			Object def = param.get("defaultvalue");
			if( "property".equals(destination)){
				properties.put(name, getValue(methodParams.get(name), def));	
			}else if( "header".equals(destination)){
				headers.put(name, getValue(methodParams.get(name), def));	
			}
		}
		info("methodParams:"+methodParams);
		info("userRoleList:"+userRoleList);
		info("permittedRoleList:"+permittedRoleList);
		info("permittedUserList:"+permittedUserList);
		info("paramList:"+paramList);
		if(!isPermitted( userName, userRoleList, permittedUserList, permittedRoleList)){
			throw new RpcException(JsonRpcServlet.ERROR_FROM_METHOD, JsonRpcServlet.PERMISSION_DENIED, "User("+userName+") has no permission");
		}
		Route route = m_camelService.getCamelContext(ns, getString(shape,"camelcontext", CamelService.DEFAULT_CONTEXT)).getRoute(methodName);
		System.out.println("Enpoint:"+route.getEndpoint());
		Object answer = null;
		try{
			answer = camelSend( ns,route.getEndpoint(),null,headers,properties);
			System.out.println("Answer:"+answer);
		}catch(Exception e){
			e.printStackTrace();
			throw new RpcException(JsonRpcServlet.ERROR_FROM_METHOD, JsonRpcServlet.INTERNAL_SERVER_ERROR,"CamelRouteService",e );
		}
		return answer;
	}

	public void callHooks(Map props) {
		String serviceName = (String) props.get(SERVICENAME);
		String methodName = (String) props.get(METHODNAME);
		Object methodParams = props.get(METHODPARAMS);
		Object at = props.get(AT);
		Object result = props.get(METHODRESULT);
		String ns = getNamespace(methodParams);
		if (ns == null) {
			return;
		}
		List<Map> hookList = getHooks(ns);
		if (hookList == null)
			return;
		for (Map<String, Object> call : hookList) {
			if (at.equals(call.get(AT)) && ns.equals(call.get(StoreDesc.NAMESPACE)) && serviceName.equals(call.get(SERVICENAME)) && methodName.equals(call.get(METHODNAME))) {
				String preCondition = (String) call.get(PRECONDITION);
				if (preCondition != null) {
					boolean isok = isHookPreConditionOk(preCondition, methodParams);
					info("preCondition:" + preCondition + ":" + isok);
					if (!isok)
						return;
				}
				String action = (String) call.get(ACTION);
				Boolean sync = (Boolean) call.get(SYNC);
				info("CallServiceImpl.camelAction: service:" + serviceName + ",Method:" + methodName + "/params:" + methodParams);
				try {
					camelHook(ns, getUserName(), serviceName, methodName, action, sync, methodParams, result);
				} catch (Exception e) {
					System.out.println("callHooks:" + e);
					throw new RpcException(ERROR_FROM_METHOD, INTERNAL_SERVER_ERROR, "CallRemote:", e);
				}
			}
		}
	}


	@Reference(dynamic = true, optional = true)
	public void setCamelService(CamelService paramCamelService) {
		this.m_camelService = paramCamelService;
		System.out.println("CallServiceImpl.setCamelService:" + paramCamelService);
	}

	@Reference(dynamic = true, optional = true)
	public void setGitService(GitService gitService) {
		System.out.println("CallServiceImpl.setGitService:" + gitService);
		this.m_gitService = gitService;
	}

	@Reference(dynamic = true, optional = true)
	public void setPermissionService(PermissionService paramPermissionService) {
		this.m_permissionService = paramPermissionService;
		System.out.println("CallServiceImpl.setPermissionService:" + paramPermissionService);
	}
}
