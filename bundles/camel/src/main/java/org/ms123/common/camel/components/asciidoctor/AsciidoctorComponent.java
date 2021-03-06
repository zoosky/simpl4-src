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
package org.ms123.common.camel.components.asciidoctor;

import java.util.Map;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.ResourceHelper;

/**
 * @version 
 */
public class AsciidoctorComponent extends DefaultComponent {

	protected Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		AsciidoctorEndpoint endpoint = new AsciidoctorEndpoint(uri, this, remaining);
//		endpoint.setTemplateEngineKind(TemplateEngineKind.valueOf(remaining));
//		endpoint.setHeaderfields((String) parameters.get("headerfields"));
//		endpoint.setOutputformat((String) parameters.get("outputformat"));
		setProperties(endpoint, parameters);
		return endpoint;
	}
}
