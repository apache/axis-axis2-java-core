package org.apache.axis.impl.llom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.namespace.QName;

import org.apache.axis.om.OMAttribute;
import org.apache.axis.om.OMNamespace;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 */
public class OMAttributeImpl implements OMAttribute {

    private String localName;
    private String value;
    private OMNamespace namespace;

    private static String QUOTE_ENTITY = "&quot;";
    private static Matcher matcher = Pattern.compile("\"").matcher(null);

    public OMAttributeImpl(String localName, OMNamespace ns, String value) {
        setLocalName(localName);
        setValue(value);
        setOMNamespace(ns);
    }

    synchronized static String replaceQuoteWithEntity(String value) {
        matcher.reset(value);
        return matcher.replaceAll(QUOTE_ENTITY);
    }

    public QName getQName(){
        String namespaceName = namespace != null ? namespace.getName() : null;
        return new QName(namespaceName, localName);
    }

    // -------- Getters and Setters
    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setOMNamespace(OMNamespace omNamespace){
        this.namespace = omNamespace;
    }

    public OMNamespace getNamespace(){
        return namespace;
    }



}
