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
package org.apache.axis2.maven2.repo;

public class GeneratedAxis2Xml {
    private Parameter[] parameters;
    private Transport[] transportReceivers;
    private Transport[] transportSenders;
    private MessageHandler[] messageBuilders;
    private MessageHandler[] messageFormatters;
    private Handler[] handlers;
    private String[] modules;
    
    public Parameter[] getParameters() {
        return parameters;
    }

    public void setParameters(Parameter[] parameters) {
        this.parameters = parameters;
    }

    public Transport[] getTransportReceivers() {
        return transportReceivers;
    }

    public void setTransportReceivers(Transport[] transportReceivers) {
        this.transportReceivers = transportReceivers;
    }

    public Transport[] getTransportSenders() {
        return transportSenders;
    }

    public void setTransportSenders(Transport[] transportSenders) {
        this.transportSenders = transportSenders;
    }

    public MessageHandler[] getMessageBuilders() {
        return messageBuilders;
    }
    
    public void setMessageBuilders(MessageHandler[] messageBuilders) {
        this.messageBuilders = messageBuilders;
    }
    
    public MessageHandler[] getMessageFormatters() {
        return messageFormatters;
    }
    
    public void setMessageFormatters(MessageHandler[] messageFormatters) {
        this.messageFormatters = messageFormatters;
    }

    public Handler[] getHandlers() {
        return handlers;
    }

    public void setHandlers(Handler[] handlers) {
        this.handlers = handlers;
    }
    
    public String[] getModules() {
        return modules;
    }

    public void setModules(String[] modules) {
        this.modules = modules;
    }
}
