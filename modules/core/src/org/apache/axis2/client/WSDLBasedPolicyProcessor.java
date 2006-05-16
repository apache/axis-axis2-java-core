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

package org.apache.axis2.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.PolicyInclude;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.ws.policy.All;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.policy.ExactlyOne;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class WSDLBasedPolicyProcessor {
	private HashMap ns2modules = new HashMap();
	private ConfigurationContext configctx;
	
	
	public WSDLBasedPolicyProcessor(ConfigurationContext configctx){
		//create the Map for namespaces to modules this is 1:N mapping
		// so we got to use array lists
		this.configctx = configctx;
		AxisConfiguration axisConfiguration = configctx.getAxisConfiguration();
		for (Iterator iterator = axisConfiguration.getModules().values()
				.iterator(); iterator.hasNext();) {
			AxisModule axisModule = (AxisModule) iterator.next();
			String[] namespaces = axisModule.getSupportedPolicyNamespaces();

			if (namespaces == null) {
				continue;
			}

			for (int i = 0; i < namespaces.length; i++) {
				ArrayList moduleList = null;
				Object obj = ns2modules.get(namespaces[i]);
				if(obj == null){
					moduleList = new ArrayList(5);
					ns2modules.put(namespaces[i], moduleList);
				}else{
					moduleList = (ArrayList)obj;
				}
				moduleList.add(axisModule);
				
			}
		}
		
	}
	
	
	
	public void configureServicePolices(AxisService axisService) throws AxisFault{
		Iterator operations = axisService.getOperations();
		while(operations.hasNext()){
			AxisOperation axisOp = (AxisOperation)operations.next();
			//TODO we support only Operation level Policy now
			configureOperationPolices(axisOp);
		}
	}
	
	
	public void configureOperationPolices(AxisOperation op) throws AxisFault {
        PolicyInclude policyInclude = op.getPolicyInclude();

        if (policyInclude != null) {
            Policy policy = policyInclude.getEffectivePolicy();
            if (policy != null) {
                if (!policy.isNormalized()) {
                    policy = (Policy) policy.normalize();
                }

                ExactlyOne XOR = (ExactlyOne) policy
                        .getTerms().get(0);
                All AND = (All) XOR
                        .getTerms().get(0);

                Iterator pAsserations = AND.getTerms().iterator();
                while (pAsserations.hasNext()) {
                    PrimitiveAssertion pa = (PrimitiveAssertion) pAsserations
                            .next();
                    String namespace = pa.getName().getNamespaceURI();
                    ArrayList moduleList = (ArrayList) ns2modules
                            .get(namespace);

                    if (moduleList == null) {
                        System.err.println("cannot find a module to process "
                                + namespace + "type assertions");
                        continue;
                    } else {
                        for (int i = 0; i < moduleList.size(); i++) {
                            AxisModule axisModule = (AxisModule) moduleList
                                    .get(i);
                            Iterator engagedModules = op.getEngagedModules()
                                    .iterator();
                            boolean found = false;
                            while (engagedModules.hasNext()) {
                                AxisModule module = (AxisModule) engagedModules
                                        .next();
                                if (module.getName().equals(
                                        axisModule.getName())) {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                op.engageModule(axisModule, configctx
                                        .getAxisConfiguration());
                                axisModule.getModule().engageNotify(op);
                            }
                        }
                    }
                }
            }
        }
    }
}
