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
package org.apache.axis2.jaxws.feature.config;

import javax.xml.ws.soap.MTOMFeature;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.feature.WebServiceFeatureConfigurator;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.spi.Binding;
import org.apache.axis2.jaxws.spi.BindingProvider;

/**
 *
 */
public class MTOMConfigurator implements WebServiceFeatureConfigurator {

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.util.WebServiceFeatureConfigurator#performConfiguration(org.apache.axis2.jaxws.core.MessageContext, org.apache.axis2.jaxws.spi.BindingProvider)
     */
    public void configure(MessageContext messageContext, BindingProvider provider) {
        Binding bnd = (Binding) provider.getBinding();
        MTOMFeature mtomFeature = (MTOMFeature) bnd.getWebServiceFeature(MTOMFeature.ID);
        Message requestMsg = messageContext.getMessage();
        
        if (mtomFeature == null)
            throw ExceptionFactory.makeWebServiceException("The MTOM features was unspecified.");
        
        if (mtomFeature.isEnabled()) {
            requestMsg.setMTOMEnabled(true);
            
            //TODO: Make use of the threshold somehow.
            int threshold = mtomFeature.getThreshold();
        }
        else {
            requestMsg.setMTOMEnabled(false);
        }
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.WebServiceFeatureConfigurator#configure(org.apache.axis2.jaxws.description.EndpointDescription)
     */
    public void configure(EndpointDescription endpointDescription) {
        // TODO Auto-generated method stub
        
    }    
}
