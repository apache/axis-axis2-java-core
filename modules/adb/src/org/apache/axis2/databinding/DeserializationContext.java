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

package org.apache.axis2.databinding;

import org.apache.axis2.databinding.deserializers.BeanDeserializer;
import org.apache.axis2.databinding.metadata.BeanManager;
import org.apache.axis2.databinding.metadata.TypeDesc;
import org.apache.ws.commons.om.OMAttribute;
import org.apache.ws.commons.om.OMElement;
import org.apache.ws.commons.om.OMNode;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * DeserializationContext
 */
public class DeserializationContext {
    public static final String SOAP12_REF_ATTR = "ref";
    public static final String SOAP11_REF_ATTR = "href";

    private Map idToElementMap = new HashMap();

    Map waitingDescs = new HashMap();
    String refAttr = SOAP12_REF_ATTR;

    public OMElement getMultiref(String id) {
        return (OMElement)idToElementMap.get(id);
    }

    public void addMultiref(String id, OMElement element) {
        idToElementMap.put(id, element);
    }

    public void addWaitingDesc(String id, Deserializer dser) {
        ArrayList descs = (ArrayList)waitingDescs.get(id);
        if (descs == null) {
            descs = new ArrayList();
            waitingDescs.put(id, descs);
        }
        descs.add(dser);
    }

    public void idFound(String id, OMElement element) throws Exception {
        ArrayList descs = (ArrayList)waitingDescs.get(id);
        if (descs == null)
            return; // nobody cares
        for (Iterator i = descs.iterator(); i.hasNext();) {
            Deserializer dser = (Deserializer) i.next();
            dser.deserialize(element.getXMLStreamReader(), this);
        }
        waitingDescs.remove(id);
    }

    public void deserialize(XMLStreamReader reader, Deserializer dser)
            throws Exception {
        int event = reader.getEventType();
        if (event == XMLStreamConstants.START_DOCUMENT) {
            event = reader.next();
        }
        if (event == XMLStreamConstants.START_ELEMENT) {
            int numAttrs = reader.getAttributeCount();
            if (numAttrs > 0) {
                String ref = reader.getAttributeValue("", refAttr);
                if (ref != null) {
                    // this is a reference.
                    if (ref.charAt(0) == '#') {
                        // It's local
                        ref = ref.substring(1);
                    }
                    OMElement refElement = getMultiref(ref);
                    if (refElement != null) {
                        // Deserialize this one
                        reader = refElement.getXMLStreamReader();
                    } else {
                        addWaitingDesc(ref, dser);
                        return;
                    }
                }
            }
        }

        dser.deserialize(reader, this);
    }

    public boolean isIncomplete() {
        return !waitingDescs.isEmpty();
    }

    public void processRest(OMElement rpcElement) throws Exception {
        // The siblings of this should be SOAP 1.1 independent elements
        OMNode nextElement = rpcElement.getNextOMSibling();
        while (nextElement != null) {
            if (nextElement instanceof OMElement) {
                OMAttribute idAttr =
                        ((OMElement)nextElement).getAttribute(new QName("id"));
                if (idAttr != null) {
                    idFound(idAttr.getAttributeValue(), (OMElement)nextElement);
                }
            }
            nextElement = nextElement.getNextOMSibling();
        }
    }

    public Object deserializeToClass(XMLStreamReader reader,
                                     Class javaClass) throws Exception {

        TypeDesc typeDesc = BeanManager.getTypeDesc(javaClass);
        Deserializer dser = new BeanDeserializer(typeDesc);

        // Now make sure there's somewhere to put the deserialized object
        SimpleTarget target = new SimpleTarget();
        dser.setTarget(target);

        // Good to go.
        dser.deserialize(reader, this);
        return target.getValue();
    }

}
