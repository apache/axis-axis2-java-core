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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.OMNamespaceImpl;
import org.apache.axiom.om.impl.builder.OMBuilder;
import org.apache.axiom.om.impl.llom.OMSourcedElementImpl;
import org.apache.axiom.om.impl.llom.factory.OMLinkedListImplFactory;
import org.apache.axis2.JScriptConstants;

public class JSONOMBuilder implements OMBuilder {
    InputStream jsonInputStream = null;
    String localName = null;

    public JSONOMBuilder() {
    }

	public void init(InputStream inputStream) {
		this.jsonInputStream = inputStream;
		
	}

    public OMElement getDocumentElement() {

        OMLinkedListImplFactory factory = new OMLinkedListImplFactory();
        OMNamespace ns = new OMNamespaceImpl("", "");
        if (localName == null) {
            localName = getLocalName();
        }
        JSONDataSource jsonDataSource = getDataSource();
        return new OMSourcedElementImpl(localName.substring(1, localName.length() - 1), ns, factory, jsonDataSource);
    }

    protected JSONDataSource getDataSource(){
        return new JSONDataSource(this.jsonInputStream, localName);
    }

    private String getLocalName() {
        String localName = "";
        try {
            char temp = (char) jsonInputStream.read();
            while (temp != ':') {
                if (temp != '{' && temp != ' ') {
                    localName += temp;
                }
                temp = (char) jsonInputStream.read();
            }
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return localName.trim();
    }

	public String getCharsetEncoding() {
		return "UTF-8";
	}

	public String getMessageType() {
		return JScriptConstants.MEDIA_TYPE_APPLICATION_JSON;
	}

}
