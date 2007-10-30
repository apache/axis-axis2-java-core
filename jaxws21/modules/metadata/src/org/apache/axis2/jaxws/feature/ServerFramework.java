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
import org.apache.axis2.jaxws.description.EndpointDescription;

import javax.xml.ws.spi.WebServiceFeatureAnnotation;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

public class ServerFramework {
    private static final Annotation[] ZERO_LENGTH_ARRAY = new Annotation[0];

    private Map<String, ServerConfigurator> configuratorMap;
    private Map<String, Annotation> annotationMap;
    
    public ServerFramework() {
    	super();
        configuratorMap = new HashMap<String, ServerConfigurator>();
        annotationMap = new HashMap<String, Annotation>();
    }
    
    public void addConfigurator(String id, ServerConfigurator configurator) {
    	System.out.println("[" + id + "], " + configurator.toString());
        configuratorMap.put(id, configurator);
    }
    
    public boolean isValid(Annotation annotation) {
        if (annotation == null)
            return false;
        
        WebServiceFeatureAnnotation wsfAnnotation =
        	annotation.annotationType().getAnnotation(WebServiceFeatureAnnotation.class);
        
        String id = null;
        if (wsfAnnotation != null)
        	id = wsfAnnotation.id();
        
        System.out.println("[" + id + "]");
        
        return configuratorMap.containsKey(id);
    }
    
    public void addAnnotation(Annotation annotation) {
        //TODO NLS enable.
        if (!isValid(annotation))
            throw ExceptionFactory.makeWebServiceException("Invalid or unsupported WebServiceFeature annotation, " + annotation);
        
        WebServiceFeatureAnnotation wsfAnnotation =
        	annotation.annotationType().getAnnotation(WebServiceFeatureAnnotation.class);

        annotationMap.put(wsfAnnotation.id(), annotation);
    }
    
    public Annotation getAnnotation(String id) {
        return annotationMap.get(id);
    }
    
    public Annotation[] getAllAnnotations() {
        return annotationMap.values().toArray(ZERO_LENGTH_ARRAY);
    }
    
    public void configure(EndpointDescription endpointDescription) {
        for (Annotation annotation : getAllAnnotations()) {
            WebServiceFeatureAnnotation wsfAnnotation =
            	annotation.annotationType().getAnnotation(WebServiceFeatureAnnotation.class);
            
            ServerConfigurator configurator = configuratorMap.get(wsfAnnotation.id());
            configurator.configure(endpointDescription);
        }
    }
}
