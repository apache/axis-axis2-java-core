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

import java.util.Map;

import javax.xml.ws.Binding;
import javax.xml.ws.soap.AddressingFeature;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.addressing.AddressingConstants.Submission;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.SubmissionAddressingFeature;
import org.apache.axis2.jaxws.binding.SOAPBinding;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.feature.WebServiceFeatureValidator;
import org.apache.axis2.jaxws.feature.util.WebServiceFeatureConfigurator;
import org.apache.axis2.jaxws.spi.BindingProvider;

/**
 *
 */
public class W3CAndSubmissionAddressingConfigurator implements WebServiceFeatureConfigurator {

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.util.WebServiceFeatureConfigurator#performConfiguration(org.apache.axis2.jaxws.core.MessageContext, org.apache.axis2.jaxws.spi.BindingProvider)
     */
    public void performConfiguration(MessageContext messageContext, BindingProvider provider) {
        Binding bnd = provider.getBinding();
        if (bnd instanceof SOAPBinding) {
            WebServiceFeatureValidator validator = provider.getWebServiceFeatureValidator();
            AddressingFeature addressingFeature =
                (AddressingFeature) validator.get(AddressingFeature.ID);
            SubmissionAddressingFeature submissionAddressingFeature =
                (SubmissionAddressingFeature) validator.get(SubmissionAddressingFeature.ID);
            Map<String, Object> properties = messageContext.getProperties();
            
            if (addressingFeature.isEnabled() || submissionAddressingFeature.isEnabled()) {
                String addressingNamespace = provider.getAddressingNamespace();
                
                //Make sure that the feature that has been enabled and the addressing namespace
                //are consistent with each other.
                if (addressingFeature.isEnabled() && !submissionAddressingFeature.isEnabled()) {
                    //TODO NLS enable.
                    if (!Final.WSA_NAMESPACE.equals(addressingNamespace))
                        throw ExceptionFactory.makeWebServiceException("The namespace of the endpoint reference is different to the namespace of the addressing WebServiceFeature.");
                }
                
                if (submissionAddressingFeature.isEnabled() && !addressingFeature.isEnabled()) {
                    //TODO NLS enable.
                    if (!Submission.WSA_NAMESPACE.equals(addressingNamespace))
                        throw ExceptionFactory.makeWebServiceException("The namespace of the endpoint reference is different to the namespace of the submission addressing WebServiceFeature.");
                }
                
                org.apache.axis2.context.MessageContext msgContext = messageContext.getAxisMessageContext();
                msgContext.setTo(provider.getAxis2EndpointReference());
                
                try {
                    ServiceDescription sd = messageContext.getEndpointDescription().getServiceDescription();
                    AxisConfiguration axisConfig = sd.getAxisConfigContext().getAxisConfiguration();
                    if (!axisConfig.isEngaged(Constants.MODULE_ADDRESSING))
                        axisConfig.engageModule(Constants.MODULE_ADDRESSING);
                }
                catch (Exception e) {
                    //TODO NLS enable.
                    throw ExceptionFactory.makeWebServiceException("Unable to engage the addressing module.", e);
                }
                
                properties.put(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.FALSE);
                properties.put(AddressingConstants.WS_ADDRESSING_VERSION, addressingNamespace);                                
            }
            else {
                properties.put(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, Boolean.TRUE);
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
