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
package org.ms123.common.data.dupcheck;

import org.ms123.common.data.api.SessionContext;
import java.util.Map;
import java.util.List;
import org.ms123.common.store.StoreDesc;

public interface DublettenCheckService {

	public DupCheckContext getContext();
	public DupCheckContext getContext(SessionContext sc, String entityName);
	public DupCheckContext getContext(SessionContext sc, String entityName, String idField);
	public Map dublettenCheck(DupCheckContext dcc, Object dataObject);

	public Map dublettenCheck(DupCheckContext dcc, Map dataMap);

	public Map dublettenCheck(DupCheckContext dcc, List<Map> dataList);

	public void dublettenCheckOne(DupCheckContext dcc, List<Map> compareList, Map dataMap);
	public boolean compare(DupCheckContext dcc, String valueInput, String valueCompareTo,String[] algos);
}
