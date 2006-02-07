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
package org.apache.axis2.om.impl.dom.jaxp;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class DocumentBuilderFactoryImpl extends DocumentBuilderFactory {

    /**
     * Temporary solution until DOOM's DocumentBuilder module is done.
     * Use ThreadLocal to determine whether or not DOOM implementation is required.
     * By default (isDOOMRequired() == false), we will use the one from JDK (Crimson)
     */
    private static DocumentBuilderFactory originalDocumentBuilderFactory = null;
    private static String originalDocumentBuilderFactoryClassName = null;
    private static ThreadLocal documentBuilderFactoryTracker = new ThreadLocal();
    
    public static boolean isDOOMRequired() {
        Object value = documentBuilderFactoryTracker.get();
        return (value != null);
    }
    
    public static void setDOOMRequired(boolean isDOOMRequired) {
        String systemKey = DocumentBuilderFactory.class.getName();
        if (isDOOMRequired) {
            if (!isDOOMRequired()) {
                originalDocumentBuilderFactory = DocumentBuilderFactory.newInstance();
                originalDocumentBuilderFactoryClassName = originalDocumentBuilderFactory.getClass().getName();
                documentBuilderFactoryTracker.set(Boolean.TRUE);
                System.setProperty(systemKey, DocumentBuilderFactoryImpl.class.getName());
            }
        } else {
            String currentFactoryClassName = DocumentBuilderFactory.newInstance().getClass().getName();
            if (currentFactoryClassName != null && currentFactoryClassName.equals(DocumentBuilderFactoryImpl.class.getName())) {
                System.getProperties().remove(systemKey);
                if (originalDocumentBuilderFactoryClassName != null) {
                    System.setProperty(DocumentBuilderFactory.class.getName(), originalDocumentBuilderFactoryClassName);
                }
            }
            documentBuilderFactoryTracker.set(null);
            originalDocumentBuilderFactory = null;
        }
    }
    

    public DocumentBuilderFactoryImpl() {
        super();
    }

    public DocumentBuilder newDocumentBuilder()
            throws ParserConfigurationException {
        /**
         * Determine which DocumentBuilder implementation should be returned
         */
        return isDOOMRequired()
                ? new DocumentBuilderImpl()
                : originalDocumentBuilderFactory.newDocumentBuilder();
    }

    public Object getAttribute(String arg0) throws IllegalArgumentException {
        // TODO
        throw new UnsupportedOperationException("TODO");
    }

    public void setAttribute(String arg0, Object arg1)
            throws IllegalArgumentException {
        // // TODO
        // throw new UnsupportedOperationException("TODO");
    }

    public static DocumentBuilderFactory newInstance() {
        return new DocumentBuilderFactoryImpl();
    }

    public void setFeature(String arg0, boolean arg1)
            throws ParserConfigurationException {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }

    public boolean getFeature(String arg0) throws ParserConfigurationException {
        // TODO TODO
        throw new UnsupportedOperationException("TODO");
    }
}
