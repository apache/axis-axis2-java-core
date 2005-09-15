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
package org.apache.axis2.databinding.deserializers;

import org.apache.axis2.databinding.DeserializationContext;
import org.apache.axis2.databinding.Deserializer;
import org.apache.axis2.databinding.DeserializationTarget;
import org.apache.axis2.databinding.metadata.TypeDesc;
import org.apache.axis2.databinding.metadata.AttributeDesc;
import org.apache.axis2.databinding.metadata.ElementDesc;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * BeanDeserializer
 */
public class BeanDeserializer implements Deserializer {
    private DeserializationTarget target;

    TypeDesc typeDesc;
    Object targetObject;
    Class javaClass;

    public BeanDeserializer(TypeDesc typeDesc) {
        this.typeDesc = typeDesc;
        this.javaClass = typeDesc.getJavaClass();
    }

    public void deserialize(XMLStreamReader reader,
                            DeserializationContext context) throws Exception {
        targetObject = createTargetObject();
        Map elementCounts = new HashMap();

        // Get the attributes
        for (Iterator i = typeDesc.getAttributeDescs(); i.hasNext();) {
            AttributeDesc attrDesc = (AttributeDesc)i.next();
            QName qname = attrDesc.getQName();
            String val = reader.getAttributeValue(qname.getNamespaceURI(),
                                                  qname.getLocalPart());
            if (val != null) {
                Object value =
                        ((SimpleDeserializer)attrDesc.getDeserializer(0)).
                        makeValue(val);
                attrDesc.setValue(targetObject, value);
            }
        }

        boolean done = false;
        while (!done) {
            int event = reader.next();
            if (event == XMLStreamConstants.END_ELEMENT) {
                target.setValue(targetObject);
                return;
            }
            if (event == XMLStreamConstants.END_DOCUMENT) {
                throw new Exception("Unfinished element!");
            }

            if (event == XMLStreamConstants.START_ELEMENT) {
                // Work through the child elements
                QName elementName = reader.getName();
                ElementDesc desc = typeDesc.getElementDesc(elementName);
                if (desc != null) {
                    Integer count = (Integer)elementCounts.get(elementName);
                    if (count == null) count = new Integer(0);
                    elementCounts.put(elementName,
                                      new Integer(count.intValue() + 1));
                    Deserializer dser =
                            desc.getDeserializer(count.intValue(), targetObject);
                    context.deserialize(reader, dser);
                }
            }
        }
    }

    private Object createTargetObject() throws Exception {
        return javaClass.newInstance();
    }

    public void setTarget(DeserializationTarget target) {
        this.target = target;
    }
}
