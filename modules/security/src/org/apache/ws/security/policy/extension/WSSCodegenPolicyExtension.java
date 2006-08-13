/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.ws.security.policy.extension;

import javax.xml.namespace.QName;

import org.apache.axis2.modules.PolicyExtension;
import org.apache.rampart.handler.config.OutflowConfiguration;
import org.apache.ws.policy.Policy;
import org.apache.ws.security.policy.WSS4JConfig;
import org.apache.ws.security.policy.WSS4JConfigBuilder;
import org.apache.ws.security.policy.model.RootPolicyEngineData;
import org.apache.ws.security.policy.parser.WSSPolicyProcessor;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public class WSSCodegenPolicyExtension implements PolicyExtension {

    public void addMethodsToStub(Document document, Element element,
            QName methodName, Policy policy) {
        try {
            WSSPolicyProcessor processor = new WSSPolicyProcessor();
            processor.setup();
            
            if (!policy.isNormalized()) {
                policy = (Policy) policy.normalize();
            }
            
            processor.processPolicy(policy);
                
            RootPolicyEngineData data = processor.getRootPED();
            
            WSS4JConfig wss4jConfig = WSS4JConfigBuilder.build(data.getTopLevelPEDs(), false);
            OutflowConfiguration outflowConfiguration = wss4jConfig.getOutflowConfiguration();
            
            Element secElement = document.createElement("security-codegen-policy-extensions");
            
            String actionItems = outflowConfiguration.getActionItems();
            
            if (actionItems != null) {
                String[] actions = actionItems.split(" ");
                
                Element child;
                String localname;
                for (int i = 0; i < actions.length; i++) {
                    localname = actions[i].toLowerCase() + "-action";
                    child = document.createElement(localname);
                    secElement.appendChild(child);
                }
            }
            
            element.appendChild(secElement);
            
        } catch (Exception nsme) {
            throw new RuntimeException("error in WSSPolicyExtension " , nsme);
            
        }
    }

}
