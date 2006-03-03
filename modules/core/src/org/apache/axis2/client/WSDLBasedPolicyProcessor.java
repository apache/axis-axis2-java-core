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

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.ws.policy.AndCompositeAssertion;
import org.apache.ws.policy.Policy;
import org.apache.ws.policy.PrimitiveAssertion;
import org.apache.ws.policy.XorCompositeAssertion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class WSDLBasedPolicyProcessor {
    private HashMap ns2modules = new HashMap();

    public WSDLBasedPolicyProcessor(ConfigurationContext configctx) {
        //create the Map for namespaces to modules this is 1:N mapping
        // so we got to use array lists

        AxisConfiguration axisConfiguration = configctx.getAxisConfiguration();
        for (Iterator iterator = axisConfiguration.getModules().values()
                .iterator(); iterator.hasNext();) {
            AxisModule axisModule = (AxisModule) iterator.next();
            String[] namespaces = axisModule.getSupportedPolicyNamespaces();

            if (namespaces == null) {
                continue;
            }

            for (int i = 0; i < namespaces.length; i++) {
                ArrayList moduleList;
                Object obj = ns2modules.get(namespaces[i]);
                if (obj == null) {
                    moduleList = new ArrayList(5);
                    ns2modules.put(namespaces[i], moduleList);
                } else {
                    moduleList = (ArrayList) obj;
                }
                moduleList.add(axisModule);

            }
        }

    }

    public void configureOperationPolices(AxisOperation op, Policy policy) {
        if (!policy.isNormalized()) {
            policy = (Policy) policy.normalize();
        }

        HashMap map = new HashMap();

        XorCompositeAssertion XOR = (XorCompositeAssertion) policy.getTerms()
                .get(0);
        AndCompositeAssertion AND = (AndCompositeAssertion) XOR.getTerms().get(
                0);

        for (Iterator iterator = AND.getTerms().iterator(); iterator.hasNext();) {

            AndCompositeAssertion nAND = new AndCompositeAssertion();
            PrimitiveAssertion pa = (PrimitiveAssertion) iterator.next();

            String namespace = pa.getName().getNamespaceURI();
            nAND.addTerm(pa);

            while (iterator.hasNext()) {
                pa = (PrimitiveAssertion) iterator.next();

                if (namespace.equals(pa.getName().getNamespaceURI())) {
                    nAND.addTerm(pa);
                }
            }

            map.put(namespace, nAND);
            AND.getTerms().removeAll(nAND.getTerms());

            iterator = AND.getTerms().iterator();
        }

        for (Iterator iterator = map.keySet().iterator(); iterator.hasNext();) {
            String namespace = (String) iterator.next();

            //for each module intersted in
            ArrayList moduleList = (ArrayList) ns2modules.get(namespace);

            if (moduleList == null) {
                System.err.println(Messages
                        .getMessage("cannotfindamoduletoprocess", namespace));
            } else {
                for (int i = 0; i < moduleList.size(); i++) {
                    AxisModule axisModule = (AxisModule) moduleList.get(i);
                    //we should be notifying/consulting modules here
                    throw new UnsupportedOperationException();

                }
            }
        }
    }
}
