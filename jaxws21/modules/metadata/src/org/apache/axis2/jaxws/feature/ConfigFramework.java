/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.feature;

import org.apache.axis2.jaxws.ExceptionFactory;

import javax.xml.ws.WebServiceFeature;

import java.util.IdentityHashMap;
import java.util.Map;

public abstract class ConfigFramework {
    private static final WebServiceFeature[] ZERO_LENGTH_ARRAY = new WebServiceFeature[0];
    
    protected Map<String, WebServiceFeature> featureMap;
    
    public ConfigFramework() {
        featureMap = new IdentityHashMap<String, WebServiceFeature>();
    }
    
    public abstract boolean isValid(WebServiceFeature feature);
    
    public void addFeature(WebServiceFeature feature) {
        //TODO NLS enable.
        if (!isValid(feature))
            throw ExceptionFactory.makeWebServiceException("Unrecognized WebServiceFeature " + feature.getID());
        
        featureMap.put(feature.getID(), feature);
    }
    
    public WebServiceFeature getFeature(String id) {
        return featureMap.get(id);
    }
    
    public WebServiceFeature[] getAllFeatures() {
        return featureMap.values().toArray(ZERO_LENGTH_ARRAY);
    }
}
