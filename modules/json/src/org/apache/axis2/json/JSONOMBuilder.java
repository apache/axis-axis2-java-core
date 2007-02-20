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

import java.io.IOException;
import java.io.InputStream;

import org.apache.axiom.om.*;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.builder.OMBuilder;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.om.impl.llom.factory.OMLinkedListImplFactory;

/**
 * Makes the OMSourcedElementImpl object with the JSONDataSource inside.
 */

public class JSONOMBuilder implements OMBuilder {
    InputStream jsonInputStream = null;
    String localName = null;
    String prefix = "";

    public JSONOMBuilder() {
    }

	public void init(InputStream inputStream, String chatSetEncoding, String url, String contentType) {
		this.jsonInputStream = inputStream;

    }

    //returns the OMSourcedElementImpl with JSONDataSource inside
    public OMElement getDocumentElement() {
        OMFactory factory = OMAbstractFactory.getOMFactory();
        OMNamespace ns = new OMNamespaceImpl("", "");
        if (localName == null) {
            localName = getLocalName();
        }
        JSONDataSource jsonDataSource = getDataSource();
        return new OMSourcedElementImpl(localName, ns, factory, jsonDataSource);
    }

    protected JSONDataSource getDataSource() {
        return new JSONDataSource(this.jsonInputStream, "\"" + prefix + localName + "\"");
    }

    private String getLocalName() {
        String localName = "";
        try {
            char temp = (char) jsonInputStream.read();
            while (temp != ':') {
                if (temp != ' ' && temp != '{') {
                    localName += temp;
                }
                temp = (char) jsonInputStream.read();
            }

            if (localName.charAt(0) == '"') {
                if (localName.charAt(localName.length() - 1) == '"') {
                    localName = localName.substring(1, localName.length() - 1);
                } else {
                    prefix = localName.substring(1, localName.length()) + ":";
                    localName = "";
                    temp = (char) jsonInputStream.read();
                    while (temp != ':') {
                        if (temp != ' ') {
                            localName += temp;
                        }
                        temp = (char) jsonInputStream.read();
                    }
                    localName = localName.substring(0, localName.length() - 1);
                }
            }
        } catch (IOException e) {
            throw new OMException(e);
        }
        return localName;
    }

    public String getCharsetEncoding() {
        return "UTF-8";
    }

}
