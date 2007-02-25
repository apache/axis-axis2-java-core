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
package org.apache.axis2.util;

import org.apache.neethi.Assertion;
import org.apache.neethi.Constants;
import org.apache.neethi.Policy;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ExternalPolicySerializer {

    private List assertions2Filter = new ArrayList();

    public void addAssertionToFilter(QName name) {
        assertions2Filter.add(name);
    }

    public void setAssertionsToFilter(List assertions2Filter) {
        this.assertions2Filter = assertions2Filter;
    }
    
    public List getAssertionsToFilter() {
        return assertions2Filter;
    }
    

    public void serialize(Policy policy, OutputStream os) {

        try {
            XMLStreamWriter writer = XMLOutputFactory.newInstance()
                    .createXMLStreamWriter(os);
            policy = (Policy) policy.normalize(false);

            String prefix = writer.getPrefix(Constants.ATTR_WSP);
            String wsuPrefix = writer.getPrefix(Constants.URI_WSU_NS);

            if (prefix == null) {
                prefix = Constants.ATTR_WSP;
                writer.setPrefix(prefix, Constants.URI_POLICY_NS);
            }

            if (wsuPrefix == null) {
                // TODO move this 'wsu' value to Neethi2 constants
                wsuPrefix = "wsu";
                writer.setPrefix(wsuPrefix, Constants.URI_WSU_NS);
            }

            // write <wsp:Policy tag

            writer.writeStartElement(prefix, Constants.ELEM_POLICY,
                    Constants.URI_POLICY_NS);
            // write xmlns:wsp=".."
            writer.writeNamespace(prefix, Constants.URI_POLICY_NS);

            String name = policy.getName();
            if (name != null) {
                // write Name=".."
                writer.writeAttribute(Constants.ATTR_NAME, name);
            }

            String id = policy.getId();
            if (id != null) {
                // write wsu:Id=".."
                writer.writeAttribute(Constants.ATTR_ID, id);
            }

            writer.writeStartElement(Constants.ATTR_WSP,
                    Constants.ELEM_EXACTLYONE, Constants.URI_POLICY_NS);
            // write <wsp:ExactlyOne>

            List assertionList;

            for (Iterator iterator = policy.getAlternatives(); iterator
                    .hasNext();) {
               
                assertionList = (List) iterator.next();
                
                // write <wsp:All>
                writer.writeStartElement(Constants.ATTR_WSP, Constants.ELEM_ALL, Constants.URI_POLICY_NS);
                
                Assertion assertion;

                for (Iterator assertions = assertionList.iterator(); assertions
                        .hasNext();) {
                    assertion = (Assertion) assertions.next();
                    if (assertions2Filter.contains(assertion.getName())) {
                        // since this is an assertion to filter, we will not serialize this
                        continue;
                    }
                    assertion.serialize(writer);
                }
                
                // write </wsp:All>
                writer.writeEndElement();
            }
            
            // write </wsp:ExactlyOne>
            writer.writeEndElement();
            // write </wsp:Policy>
            writer.writeEndElement();
            
            writer.flush();

        } catch (Exception ex) {
            
            throw new RuntimeException(ex);

        }
    }
}
