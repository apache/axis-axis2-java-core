/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.cluster.tribes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.catalina.tribes.Channel;

public class ContextManager {

	private Channel channel;

	private long timeout = 1000;

	private ClassLoader classLoader;

	private Map<String, HashMap> serviceCtxProps = new HashMap<String, HashMap>();

	private Map<String, HashMap> serviceGrpCtxProps = new HashMap<String, HashMap>();

	public ContextManager(Channel channel, long timeout, ClassLoader classLoader) {
		this.channel = channel;
		this.timeout = timeout;
		this.classLoader = classLoader;
	}

	public void addServiceContext(String parentId, String serviceCtxName) {
		String key = parentId + "_" + serviceCtxName;
		/*
		 * serviceCtxProps.put(key,new ReplicatedMap(this, channel, timeout,
		 * key, new ClassLoader[]{classLoader} ));
		 */

		serviceCtxProps.put(key, new HashMap());
	}

	public void addServiceGroupContext(String groupId) {
		String key = groupId;
		/*
		 * serviceGrpCtxProps.put(key,new ReplicatedMap(this, channel, timeout,
		 * key, new ClassLoader[]{classLoader} ));
		 */
		serviceGrpCtxProps.put(key, new HashMap());
	}

	public void removeServiceContext(String parentId, String serviceCtxName) {
		String key = parentId + "_" + serviceCtxName;
		HashMap map = (HashMap) serviceCtxProps.get(key);
		// map.breakdown();
		serviceCtxProps.remove(key);
	}

	public void removeServiceGroupContext(String groupId) {
		String key = groupId;
		HashMap map = (HashMap) serviceGrpCtxProps.get(key);
		// map.breakdown();
		serviceGrpCtxProps.remove(key);
	}

	public void addPropToServiceContext(String parentId, String serviceCtxName,
			String propName, Object value) {
		String key = parentId + "_" + serviceCtxName;
		HashMap map = (HashMap) serviceCtxProps.get(key);
		map.put(propName, value);
	}

	public void addPropToServiceGroupContext(String groupId, String propName,
			Object value) {
		String key = groupId;
		HashMap map = (HashMap) serviceGrpCtxProps.get(key);
		map.put(propName, value);
	}

	public void removePropFromServiceContext(String parentId,
			String serviceCtxName, String propName) {
		String key = parentId + "_" + serviceCtxName;
		HashMap map = (HashMap) serviceCtxProps.get(key);
		map.remove(propName);
	}

	public void removePropFromServiceGroupContext(String groupId,
			String propName) {
		String key = groupId;
		HashMap map = (HashMap) serviceGrpCtxProps.get(key);
		map.remove(propName);
	}

	public void updatePropOnServiceContext(String parentId,
			String serviceCtxName, String propName, Object value) {
		String key = parentId + "_" + serviceCtxName;
		HashMap map = (HashMap) serviceCtxProps.get(key);
		map.put(propName, value);
	}

	public void updatePropOnServiceGroupContext(String groupId,
			String propName, Object value) {
		String key = groupId;
		HashMap map = (HashMap) serviceGrpCtxProps.get(key);
		map.put(propName, value);
	}

	public Map getServiceGroupProps(String groupId) {
		return serviceGrpCtxProps.get(groupId);
	}

	public Map getServiceProps(String parentId, String serviceCtxName) {
		String key = parentId + "_" + serviceCtxName;
		return serviceCtxProps.get(key);
	}

	public List updateStateOnServiceContext(String parentId,
			String serviceCtxName, Map<String, ?> newProps) {
		String key = parentId + "_" + serviceCtxName;
		HashMap oldProps = (HashMap) serviceCtxProps.get(key);
		if (oldProps == null) {
			oldProps = new HashMap();
			serviceCtxProps.put(key, oldProps);
		}

		List<TribesMapEntryMessage> commandList = new ArrayList<TribesMapEntryMessage>();

		try {
			// using set operations to figure out the diffs

			// figuring out entries to remove
			Set<String> diffForRemove = new HashSet<String>();
			diffForRemove.addAll(oldProps.keySet());
			diffForRemove.removeAll(newProps.keySet());

			// figuring out new entires
			Set<String> diffForAddOrUpdate = new HashSet<String>();
			diffForAddOrUpdate.addAll(newProps.keySet());
			diffForAddOrUpdate.removeAll(oldProps.keySet());

			// figuring out entries to update
			for (String paramKey : newProps.keySet()) {
				Object oldValue = oldProps.get(paramKey);
				Object newValue = newProps.get(paramKey);

				if (oldValue != null && !oldValue.equals(newValue)) {
					diffForAddOrUpdate.add(paramKey);
				}
			}

			for (String paramKey : diffForAddOrUpdate) {
				Object value = newProps.get(paramKey);
				if (value instanceof Serializable) {
					oldProps.put(paramKey, value);
					commandList.add(new TribesMapEntryMessage(
							CommandType.UPDATE_STATE_MAP_ENTRY, parentId,
							serviceCtxName, serviceCtxName, paramKey,
							(Serializable) value,
							TribesMapEntryMessage.SERVICE_CONTEXT,
							TribesMapEntryMessage.ADD_OR_UPDATE_ENTRY));
					// oldProps.replicate(paramKey, true);
				}
			}

			for (String paramKey : diffForRemove) {
				oldProps.remove(paramKey);
				commandList.add(new TribesMapEntryMessage(
						CommandType.UPDATE_STATE_MAP_ENTRY, parentId,
						serviceCtxName, serviceCtxName, paramKey, "",
						TribesMapEntryMessage.SERVICE_CONTEXT,
						TribesMapEntryMessage.REMOVE_ENTRY));

				// oldProps.replicate(paramKey, true);
			}
		} catch (RuntimeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return commandList;
	}

	@SuppressWarnings("unchecked")
	public List updateStateOnServiceGroupContext(String ctxId,
			Map<String, ?> newProps) {
		HashMap oldProps = (HashMap) serviceGrpCtxProps.get(ctxId);
		if (oldProps == null) {
			oldProps = new HashMap();
			serviceCtxProps.put(ctxId, oldProps);
		}

		List<TribesMapEntryMessage> commandList = new ArrayList<TribesMapEntryMessage>();

		try {
			// using set operations to figure out the diffs

			// figuring out entries to remove
			Set<String> diffForRemove = new HashSet<String>();
			diffForRemove.addAll(oldProps.keySet());
			diffForRemove.removeAll(newProps.keySet());

			// figuring out entries to update
			Set<String> diffForAddOrUpdate = new HashSet<String>();
			diffForAddOrUpdate.addAll(newProps.keySet());
			diffForAddOrUpdate.removeAll(oldProps.keySet());

			// figuring out entries to update
			for (String paramKey : newProps.keySet()) {
				Object oldValue = oldProps.get(paramKey);
				Object newValue = newProps.get(paramKey);

				if (oldValue != null && !oldValue.equals(newValue)) {
					diffForAddOrUpdate.add(paramKey);
				}
			}

			for (String paramKey : diffForAddOrUpdate) {
				Object value = newProps.get(paramKey);
				if (value instanceof Serializable) {
					oldProps.put(paramKey, value);
					commandList.add(new TribesMapEntryMessage(
							CommandType.UPDATE_STATE_MAP_ENTRY, "", ctxId,
							ctxId, paramKey, (Serializable) value,
							TribesMapEntryMessage.SERVICE_GROUP_CONTEXT,
							TribesMapEntryMessage.ADD_OR_UPDATE_ENTRY));
					// oldProps.replicate(paramKey, true); //
					// map.replicate(true) will replicate all
				}
			}

			for (String paramKey : diffForRemove) {
				commandList.add(new TribesMapEntryMessage(
						CommandType.UPDATE_STATE_MAP_ENTRY, "", ctxId, ctxId,
						paramKey, "",
						TribesMapEntryMessage.SERVICE_GROUP_CONTEXT,
						TribesMapEntryMessage.REMOVE_ENTRY));
				// oldProps.remove(paramKey);
			}
		} catch (RuntimeException e) {
			e.printStackTrace();
		}

		return commandList;
	}
}
