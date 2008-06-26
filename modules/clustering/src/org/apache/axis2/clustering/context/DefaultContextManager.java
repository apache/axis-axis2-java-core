/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.clustering.context;

import org.apache.axiom.om.OMElement;
import org.apache.axis2.AxisFault;
import org.apache.axis2.clustering.ClusteringFault;
import org.apache.axis2.clustering.context.commands.ContextClusteringCommandCollection;
import org.apache.axis2.clustering.tribes.ChannelSender;
import org.apache.axis2.context.AbstractContext;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.description.Parameter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultContextManager implements ContextManager {

    private ConfigurationContext configContext;
    private ContextManagerListener listener;

    private Map<String, Parameter> parameters = new HashMap<String, Parameter>();

    private ChannelSender sender;

    private Map<String, List> excludedReplicationPatterns = new HashMap<String, List>();

    //TODO: Try how to use an interface
    public void setSender(ChannelSender sender) {
        this.sender = sender;
    }

    public DefaultContextManager() {
    }

    public void updateContext(AbstractContext context) throws ClusteringFault {
        ContextClusteringCommand cmd =
                ContextClusteringCommandFactory.getUpdateCommand(context,
                                                                 excludedReplicationPatterns,
                                                                 false);
        if (cmd != null) {
            sender.sendToGroup(cmd);
        }
    }

    public void updateContext(AbstractContext context,
                                String[] propertyNames) throws ClusteringFault {
        ContextClusteringCommand cmd =
                ContextClusteringCommandFactory.getUpdateCommand(context, propertyNames);
        if (cmd != null) {
            sender.sendToGroup(cmd);
        }
    }

    public void updateContexts(AbstractContext[] contexts) throws ClusteringFault {
        ContextClusteringCommandCollection cmd =
                ContextClusteringCommandFactory.getCommandCollection(contexts,
                                                                     excludedReplicationPatterns);
        sender.sendToGroup(cmd);
    }

    public void removeContext(AbstractContext context) throws ClusteringFault {
        ContextClusteringCommand cmd = ContextClusteringCommandFactory.getRemoveCommand(context);
        sender.sendToGroup(cmd);
    }

    public boolean isContextClusterable(AbstractContext context) {
        return (context instanceof ConfigurationContext) ||
               (context instanceof ServiceContext) ||
               (context instanceof ServiceGroupContext);
    }

    public void setContextManagerListener(ContextManagerListener listener) {
        this.listener = listener;
        if (configContext != null) {
            this.listener.setConfigurationContext(configContext);
        }
    }

    public void setConfigurationContext(ConfigurationContext configurationContext) {
        this.configContext = configurationContext;
        if (listener != null) {
            listener.setConfigurationContext(configContext);
        }
    }

    public void setReplicationExcludePatterns(String contextType, List patterns) {
        excludedReplicationPatterns.put(contextType, patterns);
    }

    public Map getReplicationExcludePatterns() {
        return excludedReplicationPatterns;
    }

    // ---------------------- Methods from ParameterInclude ----------------------------------------
    public void addParameter(Parameter param) throws AxisFault {
        parameters.put(param.getName(), param);
    }

    public void removeParameter(Parameter param) throws AxisFault {
        parameters.remove(param.getName());
    }

    public Parameter getParameter(String name) {
        return parameters.get(name);
    }

    public ArrayList getParameters() {
        ArrayList<Parameter> list = new ArrayList<Parameter>();
        for (String msg : parameters.keySet()) {
            list.add(parameters.get(msg));
        }
        return list;
    }

    public boolean isParameterLocked(String parameterName) {
        return getParameter(parameterName).isLocked();
    }

    public void deserializeParameters(OMElement parameterElement) throws AxisFault {
        throw new UnsupportedOperationException();
    }
    // ---------------------------------------------------------------------------------------------
}
