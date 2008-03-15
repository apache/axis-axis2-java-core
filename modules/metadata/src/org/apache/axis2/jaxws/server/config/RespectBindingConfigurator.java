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
package org.apache.axis2.jaxws.server.config;

import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.feature.ServerConfigurator;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.ws.RespectBinding;
import javax.xml.ws.RespectBindingFeature;

/**
 * An implementation of the <code>ServerConfigurator</code> interface that will
 * configure the endpoint based on the presence of a <code>RespectBinding</code>
 * attribute.
 */
public class RespectBindingConfigurator implements ServerConfigurator {

    private static final Log log = LogFactory.getLog(RespectBindingConfigurator.class); 
    
    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.WebServiceFeatureConfigurator#configure(org.apache.axis2.jaxws.description.EndpointDescription)
     */
    public void configure(EndpointDescription endpointDescription) {
    	RespectBinding annotation =
    		(RespectBinding) ((EndpointDescriptionJava) endpointDescription).getAnnoFeature(RespectBindingFeature.ID);
    	
        if (annotation != null) {
            if (log.isDebugEnabled()) {
                log.debug("Setting respectBinding to " + annotation.enabled());
            }
            endpointDescription.setRespectBinding(annotation.enabled());
        }
        else {
            if (log.isDebugEnabled()) {
                log.debug("No @RespectBinding annotation was found.");
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.ServerConfigurator#supports(java.lang.String)
     */
    public boolean supports(String bindingId) {
        return true;
    }
}
