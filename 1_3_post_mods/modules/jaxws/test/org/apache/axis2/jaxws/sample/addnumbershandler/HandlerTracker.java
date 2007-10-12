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
package org.apache.axis2.jaxws.sample.addnumbershandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.ws.handler.MessageContext;

public class HandlerTracker {

    static Map<String, HandlerTracker> trackers = new HashMap<String, HandlerTracker>();
    
    List<Methods> calledMethods = new ArrayList<Methods>();
    
    enum Methods { CLOSE, GET_HEADERS, HANDLE_FAULT, HANDLE_MESSAGE };
    
    public HandlerTracker(String name) {        
    }
    
    public void close(MessageContext context) {
        calledMethods.add(Methods.CLOSE);
    }

    public void getHeaders() {
        calledMethods.add(Methods.GET_HEADERS);
    }
    
    public void handleFault(MessageContext context) {
        calledMethods.add(Methods.HANDLE_FAULT);
    }

    public void handleMessage(MessageContext context) {
       calledMethods.add(Methods.HANDLE_MESSAGE);
    }

    public boolean isCalled(Methods method) {
        return calledMethods.contains(method);
    }
    
    public static HandlerTracker getHandlerTracker(Class clazz) {
        HandlerTracker tracker = trackers.get(clazz.getName());
        if (tracker == null) {
            tracker = new HandlerTracker(clazz.getName());
            trackers.put(clazz.getName(), tracker);                       
        }
        return tracker;
    }
    
    public String toString() {
        return this.calledMethods.toString();
    }
    
}
