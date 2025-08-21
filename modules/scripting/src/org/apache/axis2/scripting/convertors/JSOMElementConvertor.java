/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.scripting.convertors;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axiom.om.OMXMLParserWrapper;
import org.apache.xmlbeans.XmlObject;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;
import org.mozilla.javascript.ScriptableObject;
import org.mozilla.javascript.Wrapper;
import org.mozilla.javascript.xml.XMLObject;

import java.io.StringReader;

/**
 * JSObjectConvertor converts between OMElements and JavaScript E4X XML objects
 */
public class JSOMElementConvertor extends DefaultOMElementConvertor {

    protected Scriptable scope;

    public JSOMElementConvertor() {
        Context cx = Context.enter();
        try {
            this.scope = cx.initStandardObjects();
        } finally {
            Context.exit();
        }
    }

    public Object toScript(OMElement o) {
        try {
            XmlObject xml = XmlObject.Factory.parse(o.getXMLStreamReader());
            
            Context cx = Context.enter();
            try {
                // Enable E4X support
                cx.setLanguageVersion(Context.VERSION_1_6);
                Scriptable tempScope = cx.initStandardObjects();
                
                // Wrap the XmlObject directly
                return cx.getWrapFactory().wrap(cx, tempScope, xml, XmlObject.class);
                
            } finally {
                Context.exit();
            }
        } catch (Exception e) {
            throw new RuntimeException("exception getting message XML: " + e);
        }
    }

    public OMElement fromScript(Object o) {
        if (!(o instanceof XMLObject) && !(o instanceof Wrapper)) {
            return super.fromScript(o);
        }

        try {
            XmlObject xmlObject = null;
            
            // Handle wrapped XmlObject
            if (o instanceof Wrapper) {
                Object unwrapped = ((Wrapper) o).unwrap();
                if (unwrapped instanceof XmlObject) {
                    xmlObject = (XmlObject) unwrapped;
                }
            }
            
            // If we have an XMLObject but not a wrapped XmlObject, try the old approach
            if (xmlObject == null && o instanceof XMLObject) {
                // TODO: E4X Bug? Shouldn't need this copy, but without it the outer element gets lost. See Mozilla bugzilla 361722
                XMLObject jsXML = (XMLObject) ScriptableObject.callMethod((XMLObject) o, "copy", new Object[0]);

                // get proper XML representation from toXMLString()
                String xmlString;
                try {
                    // Try toXMLString() method first
                    xmlString = (String) ScriptableObject.callMethod(jsXML, "toXMLString", new Object[0]);
                } catch (Exception toXMLException) {
                    // If toXMLString() doesn't work, try toString()
                    xmlString = jsXML.toString();
                }

                // Remove extra whitespace to match expected format
                String normalizedXML = xmlString.replaceAll(">\\s+<", "><").trim();
                return OMXMLBuilderFactory
                    .createOMBuilder(new java.io.StringReader(normalizedXML))
                    .getDocumentElement();
            }
            
            if (xmlObject != null) {
                return OMXMLBuilderFactory
                    .createOMBuilder(xmlObject.newInputStream())
                    .getDocumentElement();
            } else {
                throw new RuntimeException("Unable to extract XmlObject from JavaScript object");
            }
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert JavaScript XML to OMElement: " + e.getMessage(), e);
        }
    }
}
