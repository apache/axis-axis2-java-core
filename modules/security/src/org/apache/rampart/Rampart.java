/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.rampart;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.modules.Module;
import org.apache.axis2.modules.ModulePolicyExtension;
import org.apache.axis2.modules.PolicyExtension;
import org.apache.rampart.util.HandlerParameterDecoder;
import org.apache.rampart.handler.WSSHandlerConstants;
import org.apache.rampart.handler.config.InflowConfiguration;
import org.apache.rampart.handler.config.OutflowConfiguration;
import org.apache.ws.policy.Policy;
import org.apache.ws.security.policy.WSS4JConfig;
import org.apache.ws.security.policy.WSS4JConfigBuilder;
import org.apache.ws.security.policy.extension.WSSCodegenPolicyExtension;
import org.apache.ws.security.policy.parser.WSSPolicyProcessor;

public class Rampart implements Module, ModulePolicyExtension  {
    private AxisModule module;

    public void init(ConfigurationContext configContext, AxisModule module)
            throws AxisFault {
        this.module = module;
    }

    public void engageNotify(AxisDescription axisDescription) throws AxisFault {
        Policy policy = axisDescription.getPolicyInclude().getEffectivePolicy();
        if (axisDescription instanceof AxisOperation && policy != null) {
            try {
                WSSPolicyProcessor wssPolicyProcessor = new WSSPolicyProcessor();
                wssPolicyProcessor.setup();
                wssPolicyProcessor.processPolicy((Policy)policy.normalize(axisDescription.getPolicyInclude().getPolicyRegistry()));

                //create server side config
                WSS4JConfig serverConfig = WSS4JConfigBuilder
                        .build(wssPolicyProcessor.getRootPED()
                                .getTopLevelPEDs());

                InflowConfiguration policyInflowConfig = serverConfig
                        .getInflowConfiguration();

                Parameter infp = calcuateCurrentInflowConfiguration(policyInflowConfig,axisDescription).getProperty();
                infp.setName(WSSHandlerConstants.INFLOW_SECURITY_SERVER);
                axisDescription.addParameter(infp);
                
                OutflowConfiguration policyOutflowConfig = serverConfig.getOutflowConfiguration();
                Parameter outfp = calcuateCurrentOutflowConfiguration(policyOutflowConfig,axisDescription).getProperty();
                outfp.setName(WSSHandlerConstants.OUTFLOW_SECURITY_SERVER);
                axisDescription.addParameter(outfp);
                
                
                //create client side config
                wssPolicyProcessor = new WSSPolicyProcessor();
                wssPolicyProcessor.setup();
                wssPolicyProcessor.processPolicy(policy);
                
                WSS4JConfig clientConfig = WSS4JConfigBuilder
                .build(wssPolicyProcessor.getRootPED()
                        .getTopLevelPEDs(), isServerSide(axisDescription));

                policyInflowConfig = clientConfig.getInflowConfiguration();

                infp = calcuateCurrentInflowConfiguration(policyInflowConfig,axisDescription).getProperty();
                infp.setName(WSSHandlerConstants.INFLOW_SECURITY_CLIENT);
                axisDescription.addParameter(infp);
        
                 policyOutflowConfig = clientConfig.getOutflowConfiguration();
                 outfp = calcuateCurrentOutflowConfiguration(policyOutflowConfig,axisDescription).getProperty();
                 outfp.setName(WSSHandlerConstants.OUTFLOW_SECURITY_CLIENT);
                 axisDescription.addParameter(outfp);
            } catch (Exception e) {
                throw new AxisFault(e.getMessage(), e);
            }
        }
    }

    public void shutdown(ConfigurationContext configurationContext) throws AxisFault {
        // Do nothing
    }

    public PolicyExtension getPolicyExtension() {
        return new WSSCodegenPolicyExtension();
    }

    private InflowConfiguration calcuateCurrentInflowConfiguration(
            InflowConfiguration policyInflowConfig,
            AxisDescription axisDescription) throws AxisFault {
        // merge inflow configuration
        Parameter inflowModuleParam = module
                .getParameter(WSSHandlerConstants.INFLOW_SECURITY);
        InflowConfiguration moduleInflowConfig = HandlerParameterDecoder
                .getInflowConfiguration(inflowModuleParam);

        Parameter inflowSecParam = axisDescription
                .getParameter(WSSHandlerConstants.INFLOW_SECURITY);
        InflowConfiguration staticInflowConfig = HandlerParameterDecoder
                .getInflowConfiguration(inflowSecParam);

        InflowConfiguration mergedInConf = mergeInflowConfiguration(
                staticInflowConfig, moduleInflowConfig);
        InflowConfiguration finalInConf = mergeInflowConfiguration(mergedInConf,policyInflowConfig);
        return finalInConf;
    }     
        
    private OutflowConfiguration calcuateCurrentOutflowConfiguration(
            OutflowConfiguration policyOutflowConfig,
            AxisDescription axisDescription) throws AxisFault {
        // merge outflow configuration
        Parameter outfloModuleParam = module
                .getParameter(WSSHandlerConstants.OUTFLOW_SECURITY);
        OutflowConfiguration moduleOutflowConfig = HandlerParameterDecoder
                .getOutflowConfiguration(outfloModuleParam);
        Parameter outflowSecParam = axisDescription
                .getParameter(WSSHandlerConstants.OUTFLOW_SECURITY);
        OutflowConfiguration staticOutflowConfig = HandlerParameterDecoder
                .getOutflowConfiguration(outflowSecParam);

        OutflowConfiguration mergedOutFlowConf = mergeOutflowConfiguration(
                staticOutflowConfig, moduleOutflowConfig);
        OutflowConfiguration finalOutFlowConf = mergeOutflowConfiguration(mergedOutFlowConf,policyOutflowConfig);
        return finalOutFlowConf;
    }
    
    
    
    

    // overide secondry configuration with primry configuration
    private OutflowConfiguration mergeOutflowConfiguration(
            OutflowConfiguration primaryConfig,
            OutflowConfiguration secondryConf) {
        if (secondryConf == null && primaryConfig != null) {
            return primaryConfig;
        } else if (primaryConfig == null && secondryConf != null) {
            return secondryConf;
        } else if (primaryConfig == null && secondryConf == null) {
            return null;
        }

        secondryConf.setPasswordCallbackClass(primaryConfig
                .getPasswordCallbackClass());
        secondryConf.setSignaturePropFile(primaryConfig.getSignaturePropFile());
        secondryConf.setEncryptionPropFile(primaryConfig
                .getEncryptionPropFile());
        secondryConf.setEmbeddedKeyCallbackClass(primaryConfig
                .getEmbeddedKeyCallbackClass());
        secondryConf.setUser(primaryConfig.getUser());
        secondryConf.setEncryptionUser(primaryConfig.getEncryptionUser());
        return secondryConf;
    }

    // overide secondry configuration with primry configuration
    private InflowConfiguration mergeInflowConfiguration(
            InflowConfiguration primaryConfig, InflowConfiguration secondryConf) {
        if (secondryConf == null && primaryConfig != null) {
            return primaryConfig;
        } else if (primaryConfig == null && secondryConf != null) {
            return secondryConf;
        } else if (primaryConfig == null && secondryConf == null) {
            return null;
        }

        secondryConf.setPasswordCallbackClass(primaryConfig
                .getPasswordCallbackClass());
        secondryConf.setDecryptionPropFile(primaryConfig
                .getDecryptionPropFile());
        secondryConf.setSignaturePropFile(primaryConfig.getSignaturePropFile());
        if(primaryConfig.getEnableSignatureConfirmation() != null && "false".equals(primaryConfig.getEnableSignatureConfirmation())){
            secondryConf.setEnableSignatureConfirmation(false);
        }
        return secondryConf;
    }
    
    private boolean isServerSide(AxisDescription axisDescription) {
        
        if (axisDescription instanceof AxisService) {
            return !((AxisService) axisDescription).isClientSide();
            
        } else if (axisDescription instanceof AxisOperation) {
            return !((AxisService) axisDescription.getParent()).isClientSide();
            
        } else {
            // we assume that the default is the client-side
            return false;            
        }
    }
    
    
}
