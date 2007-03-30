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

package org.apache.axis2.json;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axis2.AxisFault;
import org.apache.axis2.builder.Builder;
import org.apache.axis2.context.MessageContext;

import java.io.IOException;
import java.io.InputStream;

/** Makes the OMSourcedElementImpl object with the JSONDataSource inside. */

public class JSONOMBuilder implements Builder {


    public JSONOMBuilder() {
    }

    //returns the OMSourcedElementImpl with JSONDataSource inside
    public OMElement processDocument(InputStream inputStream, String contentType,
                                     MessageContext messageContext) throws AxisFault {
        String localName = "";
        String prefix = "";
        OMNamespace ns = new OMNamespaceImpl("", "");

        OMFactory factory = OMAbstractFactory.getOMFactory();
        try {
            char temp = (char)inputStream.read();
            while (temp != ':') {
                if (temp != ' ' && temp != '{') {
                    localName += temp;
                }
                temp = (char)inputStream.read();
            }

            if (localName.charAt(0) == '"') {
                if (localName.charAt(localName.length() - 1) == '"') {
                    localName = localName.substring(1, localName.length() - 1);
                } else {
                    prefix = localName.substring(1, localName.length()) + ":";
                    localName = "";
                    temp = (char)inputStream.read();
                    while (temp != ':') {
                        if (temp != ' ') {
                            localName += temp;
                        }
                        temp = (char)inputStream.read();
                    }
                    localName = localName.substring(0, localName.length() - 1);
                }
            }
        } catch (IOException e) {
            throw new AxisFault(e);
        }
        JSONDataSource jsonDataSource = getDataSource(inputStream, prefix, localName);
        return new OMSourcedElementImpl(localName, ns, factory, jsonDataSource);
    }

    protected JSONDataSource getDataSource(InputStream jsonInputStream, String prefix,
                                           String localName) {
        return new JSONDataSource(jsonInputStream, "\"" + prefix + localName + "\"");
    }
}
