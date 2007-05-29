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
import org.apache.axis2.jaxws.addressing.SubmissionAddressingFeature;

import javax.xml.ws.RespectBindingFeature;
import javax.xml.ws.WebServiceFeature;
import javax.xml.ws.soap.AddressingFeature;
import javax.xml.ws.soap.MTOMFeature;

import java.util.IdentityHashMap;
import java.util.Map;

public class WebServiceFeatureValidator {
    private static final WebServiceFeature DEFAULT_ADDRESSING_FEATURE = new AddressingFeature();
    private static final WebServiceFeature DEFAULT_SUBMISSION_ADDRESSING_FEATURE = new SubmissionAddressingFeature();
    private static final WebServiceFeature DEFAULT_MTOM_FEATURE = new MTOMFeature();
    private static final WebServiceFeature DEFAULT_RESPECT_BINDING_FEATURE = new RespectBindingFeature();

    private static final WebServiceFeature DEFAULT_CLIENT_SIDE_ADDRESSING_FEATURE = new AddressingFeature(false);
    private static final WebServiceFeature DEFAULT_CLIENT_SIDE_SUBMISSION_ADDRESSING_FEATURE = new SubmissionAddressingFeature(false);
    private static final WebServiceFeature DEFAULT_CLIENT_SIDE_MTOM_FEATURE = new MTOMFeature(false);

    private static final WebServiceFeature[] ZERO_LENGTH_ARRAY = new WebServiceFeature[0];
    
    private Map<String, WebServiceFeature> featureMap;
    
    public WebServiceFeatureValidator(boolean isServerSide) {
        //Set up default WebServiceFeatures.
        featureMap = new IdentityHashMap<String, WebServiceFeature>();
        
        if (isServerSide) {
            featureMap.put(AddressingFeature.ID, DEFAULT_ADDRESSING_FEATURE);
            featureMap.put(SubmissionAddressingFeature.ID, DEFAULT_SUBMISSION_ADDRESSING_FEATURE);
            featureMap.put(MTOMFeature.ID, DEFAULT_MTOM_FEATURE);
        }
        else {
            featureMap.put(AddressingFeature.ID, DEFAULT_CLIENT_SIDE_ADDRESSING_FEATURE);
            featureMap.put(SubmissionAddressingFeature.ID, DEFAULT_CLIENT_SIDE_SUBMISSION_ADDRESSING_FEATURE);
            featureMap.put(MTOMFeature.ID, DEFAULT_CLIENT_SIDE_MTOM_FEATURE);            
        }
        
        featureMap.put(RespectBindingFeature.ID, DEFAULT_RESPECT_BINDING_FEATURE);        
    }

    public WebServiceFeatureValidator(boolean isServerSide, WebServiceFeature... features) {
        this(isServerSide);
        
        if (features != null) {
            for (WebServiceFeature feature : features) {
                put(feature);
            }
        }
    }
    
    public boolean isValid(WebServiceFeature feature) {
        return featureMap.containsKey(feature.getID());
    }

    public void put(WebServiceFeature feature) {
        if (feature != null) {
            //TODO NLS enable.
            if (!isValid(feature))
                throw ExceptionFactory.makeWebServiceException("Unrecognized WebServiceFeature " + feature.getID());
            
            featureMap.put(feature.getID(), feature);            
        }
    }
    
    public WebServiceFeature get(String featureID) {
        return featureMap.get(featureID);
    }
    
    public WebServiceFeature[] getAll() {
        return featureMap.values().toArray(ZERO_LENGTH_ARRAY);
    }
}
