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

package org.apache.axis2.cluster.tribes.context;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ContextUpdater {

    private Map serviceCtxProps = null;
    private Map serviceGrpCtxProps = null;

    public Map getServiceCtxProps() {
        return serviceCtxProps;
    }

    public Map getServiceGrpCtxProps() {
        return serviceGrpCtxProps;
    }

    public ContextUpdater() {
        serviceCtxProps = new HashMap ();
        serviceGrpCtxProps = new HashMap ();
    }

    public void addServiceContext(String parentId, String serviceCtxName) {
        String key = parentId + "_" + serviceCtxName;
        serviceCtxProps.put(key, new HashMap());
    }

    public void addServiceGroupContext(String groupId) {
        String key = groupId;
        serviceGrpCtxProps.put(key, new HashMap());
    }

    public void removeServiceContext(String parentId, String serviceCtxName) {
        String key = parentId + "_" + serviceCtxName;
        serviceCtxProps.remove(key);
    }

    public void removeServiceGroupContext(String groupId) {
        String key = groupId;
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
        return (Map) serviceGrpCtxProps.get(groupId);
    }

    public Map getServiceProps(String parentId, String serviceCtxName) {
        String key = parentId + "_" + serviceCtxName;
        return (Map) serviceCtxProps.get(key);
    }

    public List updateStateOnServiceContext(String parentId,
                                            String serviceCtxName, Map newProps) {
        String key = parentId + "_" + serviceCtxName;
        HashMap oldProps = (HashMap) serviceCtxProps.get(key);
        if (oldProps == null) {
            oldProps = new HashMap();
            serviceCtxProps.put(key, oldProps);
        }

        List commandList = new ArrayList();

        try {
            // using set operations to figure out the diffs

            // figuring out entries to remove
            Set diffForRemove = new HashSet();
            diffForRemove.addAll(oldProps.keySet());
            diffForRemove.removeAll(newProps.keySet());

            // figuring out new entires
            Set diffForAddOrUpdate = new HashSet();
            diffForAddOrUpdate.addAll(newProps.keySet());
            diffForAddOrUpdate.removeAll(oldProps.keySet());

            // figuring out entries to update
            for (Iterator it= newProps.keySet().iterator();it.hasNext();) {

                String paramKey = (String) it.next();

                Object oldValue = oldProps.get(paramKey);
                Object newValue = newProps.get(paramKey);

                if (oldValue != null && !oldValue.equals(newValue)) {
                    diffForAddOrUpdate.add(paramKey);
                }
            }

            for (Iterator it= diffForAddOrUpdate.iterator();it.hasNext();) {

                String paramKey = (String) it.next();

                Object value = newProps.get(paramKey);
                if (value instanceof Serializable) {
                    oldProps.put(paramKey, value);
                    commandList.add(new ContextUpdateEntryCommandMessage(
                            parentId,
                            serviceCtxName, serviceCtxName, paramKey,
                            (Serializable) value,
                            ContextUpdateEntryCommandMessage.SERVICE_CONTEXT,
                            ContextUpdateEntryCommandMessage.ADD_OR_UPDATE_ENTRY));
                }
            }

            for (Iterator it= diffForRemove.iterator();it.hasNext();) {

                String paramKey = (String) it.next();

                oldProps.remove(paramKey);
                commandList.add(new ContextUpdateEntryCommandMessage(
                        parentId,
                        serviceCtxName, serviceCtxName, paramKey, "",
                        ContextUpdateEntryCommandMessage.SERVICE_CONTEXT,
                        ContextUpdateEntryCommandMessage.REMOVE_ENTRY));

            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return commandList;
    }

    public List updateStateOnServiceGroupContext(String ctxId,
                                                 Map newProps) {
        HashMap oldProps = (HashMap) serviceGrpCtxProps.get(ctxId);
        if (oldProps == null) {
            oldProps = new HashMap();
            serviceCtxProps.put(ctxId, oldProps);
        }

        List commandList = new ArrayList ();

        try {
            // using set operations to figure out the diffs

            // figuring out entries to remove
            Set diffForRemove = new HashSet();
            diffForRemove.addAll(oldProps.keySet());
            diffForRemove.removeAll(newProps.keySet());

            // figuring out entries to update
            Set diffForAddOrUpdate = new HashSet ();
            diffForAddOrUpdate.addAll(newProps.keySet());
            diffForAddOrUpdate.removeAll(oldProps.keySet());

            // figuring out entries to update
            for (Iterator it=newProps.keySet().iterator();it.hasNext();) {

                String paramKey = (String) it.next();

                Object oldValue = oldProps.get(paramKey);
                Object newValue = newProps.get(paramKey);

                if (oldValue != null && !oldValue.equals(newValue)) {
                    diffForAddOrUpdate.add(paramKey);
                }
            }

            for (Iterator it=diffForAddOrUpdate.iterator();it.hasNext();) {

                String paramKey = (String) it.next();

                Object value = newProps.get(paramKey);
                if (value instanceof Serializable) {
                    oldProps.put(paramKey, value);
                    commandList.add(new ContextUpdateEntryCommandMessage(
                            "",
                            ctxId,
                            ctxId, paramKey, (Serializable) value,
                            ContextUpdateEntryCommandMessage.SERVICE_GROUP_CONTEXT,
                            ContextUpdateEntryCommandMessage.ADD_OR_UPDATE_ENTRY));
                    // oldProps.replicate(paramKey, true); //
                    // map.replicate(true) will replicate all
                }
            }

            for (Iterator it=diffForRemove.iterator();it.hasNext();) {

                String paramKey = (String) it.next();

                commandList.add(new ContextUpdateEntryCommandMessage(
                        "", ctxId, ctxId,
                        paramKey, "",
                        ContextUpdateEntryCommandMessage.SERVICE_GROUP_CONTEXT,
                        ContextUpdateEntryCommandMessage.REMOVE_ENTRY));
                // oldProps.remove(paramKey);
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }

        return commandList;
    }
}
