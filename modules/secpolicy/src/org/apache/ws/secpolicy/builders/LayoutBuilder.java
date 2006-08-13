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
package org.apache.ws.security.secpolicy.builders;

import java.util.Iterator;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.neethi.Assertion;
import org.apache.neethi.AssertionBuilderFactory;
import org.apache.neethi.Policy;
import org.apache.neethi.PolicyEngine;
import org.apache.neethi.builders.AssertionBuilder;
import org.apache.ws.security.secpolicy.Constants;
import org.apache.ws.security.secpolicy.model.Layout;

public class LayoutBuilder implements AssertionBuilder {
    
    private static final QName STRICT = new QName(Constants.SP_NS, "Strict");
    private static final QName LAX = new QName(Constants.SP_NS, "Lax");
    private static final QName LAXTSFIRST = new QName(Constants.SP_NS, "LaxTsFirst");
    private static final QName LAXTSLAST = new QName(Constants.SP_NS,"LaxTsLast");

    public Assertion build(OMElement element, AssertionBuilderFactory factory) throws IllegalArgumentException {
        Layout layout = new Layout();
        
        Policy policy = PolicyEngine.getPolicy(element);
        policy = (Policy) policy.normalize(false);
        
        for (Iterator iterator = policy.getAlternatives(); iterator.hasNext(); ) {
            processAlternative((List) iterator.next(), layout);                        
        }
                        
        return layout;
    }
    
    public void processAlternative(List assertions, Layout parent) {
        Layout layout = new Layout();
        
        for (Iterator iterator = assertions.iterator(); iterator.hasNext();) {
            Assertion assertion = (Assertion) iterator.next();
            QName qname = assertion.getName();
            
            if (STRICT.equals(qname)) {
                layout.setValue(Constants.LAYOUT_STRICT);
            } else if (LAX.equals(qname)) {
                layout.setValue(Constants.LAYOUT_LAX);
            } else if (LAXTSFIRST.equals(qname)) {
                layout.setValue(Constants.LAYOUT_LAX_TIMESTAMP_FIRST);
            } else if (LAXTSLAST.equals(qname)) {
                layout.setValue(Constants.LAYOUT_LAX_TIMESTAMP_LAST);
            }
            
        }
        parent.addOption(layout);
    }

}
