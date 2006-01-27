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

package org.apache.axis2.security;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisDescription;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.modules.Module;
import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.axis2.security.handler.config.InflowConfiguration;
import org.apache.axis2.security.handler.config.OutflowConfiguration;
import org.apache.axis2.security.util.HandlerParameterDecoder;
import org.apache.ws.policy.Policy;
import org.apache.ws.security.policy.WSS4JConfig;
import org.apache.ws.security.policy.WSS4JConfigBuilder;
import org.apache.ws.security.policy.parser.WSSPolicyProcessor;

public class SecurityModule implements Module {

    public void engageNotify(AxisDescription axisDescription) throws AxisFault {
        Policy policy = axisDescription.getPolicyInclude().getEffectivePolicy();
        if(axisDescription instanceof AxisOperation && policy != null) {
//            PolicyWriter writer = PolicyFactory.getPolicyWriter(PolicyFactory.StAX_POLICY_WRITER);
//            writer.writePolicy(policy, System.out);
            try {
                WSSPolicyProcessor wssPolicyProcessor = new WSSPolicyProcessor();
                wssPolicyProcessor.setup();
                wssPolicyProcessor.processPolicy(policy);
                
                WSS4JConfig config = WSS4JConfigBuilder.build(wssPolicyProcessor.getRootPED().getTopLevelPEDs());
                
                InflowConfiguration policyInflowConfig = config.getInflowConfiguration();
                OutflowConfiguration policyOutflowConfig = config.getOutflowConfiguration();
                
                Parameter inflowSecParam = axisDescription.getParameter(WSSHandlerConstants.INFLOW_SECURITY);
                Parameter outflowSecParam = axisDescription.getParameter(WSSHandlerConstants.OUTFLOW_SECURITY);
                
                InflowConfiguration staticInflowConfig = HandlerParameterDecoder.getInflowConfiguration(inflowSecParam);
                OutflowConfiguration staticOutflowConfig = HandlerParameterDecoder.getOutflowConfiguration(outflowSecParam);

//                if(staticInflowConfig == null || staticOutflowConfig == null) {
//                    throw new Exception("Static configuration not available!!!");
//                }
                if(staticOutflowConfig != null) {
                    OutflowConfiguration mergedOutflowConfig = this
                            .mergeStaticAndPolicyOutflowConfiguration(
                                    staticOutflowConfig, policyOutflowConfig);
                    axisDescription.addParameter(mergedOutflowConfig.getProperty());
                }
                
                if(staticInflowConfig != null) {
                    InflowConfiguration mergedInflowConfig = this.mergeStaticAndPolicyInflowConfiguration(staticInflowConfig, policyInflowConfig);
                    axisDescription.addParameter(mergedInflowConfig.getProperty());
                }
            } catch (Exception e) {
                throw new AxisFault(e.getMessage(),e);
            }
        }
    }

    public void shutdown(AxisConfiguration axisSystem) throws AxisFault {
        //Do nothing
    }

    public void init(ConfigurationContext configContext, AxisModule module) throws AxisFault {
        //DO nothing 
    }

    private OutflowConfiguration mergeStaticAndPolicyOutflowConfiguration(
            OutflowConfiguration staticConfig, OutflowConfiguration policyConfig) {
        policyConfig.setPasswordCallbackClass(staticConfig.getPasswordCallbackClass());
        policyConfig.setSignaturePropFile(staticConfig.getSignaturePropFile());
        policyConfig.setEncryptionPropFile(staticConfig.getEncryptionPropFile());
        policyConfig.setEmbeddedKeyCallbackClass(staticConfig.getEmbeddedKeyCallbackClass());
        policyConfig.setUser(staticConfig.getUser());
        policyConfig.setEncryptionUser(staticConfig.getEncryptionUser());
        return policyConfig;
    }
    
    private InflowConfiguration mergeStaticAndPolicyInflowConfiguration(
            InflowConfiguration staticConfig, InflowConfiguration policyConfig) {
        policyConfig.setPasswordCallbackClass(staticConfig.getPasswordCallbackClass());
        policyConfig.setDecryptionPropFile(staticConfig.getDecryptionPropFile());
        policyConfig.setSignaturePropFile(staticConfig.getSignaturePropFile());
        return policyConfig;
    }
}
