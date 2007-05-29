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

import javax.xml.ws.Binding;
import javax.xml.ws.soap.MTOMFeature;

import org.apache.axis2.jaxws.binding.SOAPBinding;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.feature.WebServiceFeatureValidator;
import org.apache.axis2.jaxws.feature.util.WebServiceFeatureConfigurator;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.spi.BindingProvider;

/**
 *
 */
public class MTOMConfigurator implements WebServiceFeatureConfigurator {

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.util.WebServiceFeatureConfigurator#performConfiguration(org.apache.axis2.jaxws.core.MessageContext, org.apache.axis2.jaxws.spi.BindingProvider)
     */
    public void performConfiguration(MessageContext messageContext, BindingProvider provider) {
        Binding bnd = provider.getBinding();
        if (bnd instanceof SOAPBinding) {
            WebServiceFeatureValidator validator = provider.getWebServiceFeatureValidator();
            MTOMFeature mtomFeature = (MTOMFeature) validator.get(MTOMFeature.ID);
            Message requestMsg = messageContext.getMessage();
            
            if (mtomFeature.isEnabled()) {
                requestMsg.setMTOMEnabled(true);
                
                //TODO: Make use of the threshold somehow.
                int threshold = mtomFeature.getThreshold();
            }
            
            // If the user has enabled MTOM on the SOAPBinding, we need
            // to make sure that gets pushed to the Message object.
            if (((SOAPBinding)bnd).isMTOMEnabled()) {
                requestMsg.setMTOMEnabled(true);
            }

            // Check if the user enabled MTOM using the SOAP binding 
            // properties for MTOM
            String bindingID = messageContext.getEndpointDescription().getClientBindingID();
            if ((bindingID.equalsIgnoreCase(SOAPBinding.SOAP11HTTP_MTOM_BINDING) ||
                    bindingID.equalsIgnoreCase(SOAPBinding.SOAP12HTTP_MTOM_BINDING)) &&
                    !requestMsg.isMTOMEnabled()) {
                requestMsg.setMTOMEnabled(true);
            }
        } 
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.util.WebServiceFeatureConfigurator#performConfiguration(org.apache.axis2.jaxws.description.ServiceDescription)
     */
    public void performConfiguration(ServiceDescription serviceDescription) {
        // TODO Auto-generated method stub
        
    }
}
