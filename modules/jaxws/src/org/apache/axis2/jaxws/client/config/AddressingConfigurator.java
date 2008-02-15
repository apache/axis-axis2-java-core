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
package org.apache.axis2.jaxws.client.config;

import javax.xml.ws.soap.AddressingFeature;

import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.addressing.AddressingConstants.Final;
import org.apache.axis2.addressing.AddressingConstants.Submission;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.addressing.SubmissionAddressingFeature;
import org.apache.axis2.jaxws.binding.SOAPBinding;
import org.apache.axis2.jaxws.core.MessageContext;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.feature.ClientConfigurator;
import org.apache.axis2.jaxws.spi.Binding;
import org.apache.axis2.jaxws.spi.BindingProvider;

/**
 *
 */
public class AddressingConfigurator implements ClientConfigurator {

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.WebServiceFeatureConfigurator#configure(org.apache.axis2.jaxws.core.MessageContext, org.apache.axis2.jaxws.spi.BindingProvider)
     */
    public void configure(MessageContext messageContext, BindingProvider provider) {
        Binding bnd = (Binding) provider.getBinding();
        AddressingFeature addressingFeature =
            (AddressingFeature) bnd.getWebServiceFeature(AddressingFeature.ID);
        SubmissionAddressingFeature submissionAddressingFeature =
            (SubmissionAddressingFeature) bnd.getWebServiceFeature(SubmissionAddressingFeature.ID);
        String addressingNamespace =
            (String) messageContext.getProperty(AddressingConstants.WS_ADDRESSING_VERSION);
        Boolean disableAddressing =
            (Boolean) messageContext.getProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES);
        
        //Figure out which WS-Addressing feature was specified causing this configurator to run. 
        if (addressingFeature != null && submissionAddressingFeature != null) {
            //Both features must have been specified.
            boolean w3cAddressingEnabled = addressingFeature.isEnabled();
            boolean submissionAddressingEnabled = submissionAddressingFeature.isEnabled();
            
            if (w3cAddressingEnabled && submissionAddressingEnabled) {
                //Use the addressing namespace of the specified EPR,
                //else default to 2005/08
                addressingNamespace = provider.getAddressingNamespace();
                if (addressingNamespace == null)
                    addressingNamespace = Final.WSA_NAMESPACE;
                disableAddressing = Boolean.FALSE;
            }
            else if (w3cAddressingEnabled) {
                //Enable only 2005/08 addressing
                addressingNamespace = Final.WSA_NAMESPACE;
                disableAddressing = Boolean.FALSE;
            }
            else if (submissionAddressingEnabled) {
                //Enable only 2004/08 addressing
                addressingNamespace = Submission.WSA_NAMESPACE;
                disableAddressing = Boolean.FALSE;
            }
            else {
                //Disable 2005/08 and 2004/08 addressing
                disableAddressing = Boolean.TRUE;
            }                
        }
        else if (addressingFeature != null) {
            //The AddressingFeature must have been specified.
            boolean w3cAddressingEnabled = addressingFeature.isEnabled();

            if (w3cAddressingEnabled) {
                //Enable 2005/08 addressing
                addressingNamespace = Final.WSA_NAMESPACE;
                disableAddressing = Boolean.FALSE;
            }
            else {
                //Disable 2005/08 addressing
                if (addressingNamespace == null ||
                        Final.WSA_NAMESPACE.equals(addressingNamespace))
                    disableAddressing = Boolean.TRUE;
                else
                    disableAddressing = Boolean.FALSE;
            }                
        }
        else if (submissionAddressingFeature != null) {
            //The SubmissionAddressingFeature must have been specified.
            boolean submissionAddressingEnabled = submissionAddressingFeature.isEnabled();
            
            if (submissionAddressingEnabled) {
                //Enable 2004/08 addressing
                addressingNamespace = Submission.WSA_NAMESPACE;
                disableAddressing = Boolean.FALSE;
            }
            else {
                //Disable 2004/08 addressing
                if (addressingNamespace == null ||
                        Submission.WSA_NAMESPACE.equals(addressingNamespace))
                    disableAddressing = Boolean.TRUE;
                else
                    disableAddressing = Boolean.FALSE;
            }                
        }
        else {
            //If neither were specified then this configurator should never run.
            throw ExceptionFactory.makeWebServiceException("Both WS-Addressing features were unspecified.");
        }
        
        if (!disableAddressing) {
            try {
                EndpointReference epr = provider.getAxis2EndpointReference(addressingNamespace);
                org.apache.axis2.context.MessageContext axis2MessageContext = messageContext.getAxisMessageContext();
                axis2MessageContext.setTo(epr);
                
                ServiceDescription sd = messageContext.getEndpointDescription().getServiceDescription();
                AxisConfiguration axisConfig = sd.getAxisConfigContext().getAxisConfiguration();
                if (!axisConfig.isEngaged(Constants.MODULE_ADDRESSING))
                    axisConfig.engageModule(Constants.MODULE_ADDRESSING);
            }
            catch (Exception e) {
                //TODO NLS enable.
                throw ExceptionFactory.makeWebServiceException("Unable to engage the addressing module.", e);
            }
        }

        messageContext.setProperty(AddressingConstants.WS_ADDRESSING_VERSION, addressingNamespace);                        
        messageContext.setProperty(AddressingConstants.DISABLE_ADDRESSING_FOR_OUT_MESSAGES, disableAddressing);
    }

    /*
     *  (non-Javadoc)
     * @see org.apache.axis2.jaxws.feature.ClientConfigurator#supports(org.apache.axis2.jaxws.spi.Binding)
     */
    public boolean supports(Binding binding) {
        return binding instanceof SOAPBinding;
    }
}
