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
package org.apache.wsdl.wom;

import java.net.URI;
import java.util.HashMap;
import java.util.List;

import javax.xml.namespace.QName;


/**
 * @author chathura@opensource.lk
 *
 */
public interface WSDLInterface {
    public List getFeatures();

    public void setFeatures(List features);

    public List getProperties();

    public void setProperties(List properties);

    public HashMap getDefinedOperations(WSDLInterface wsdlInterface);

    public HashMap getDefinedOperations();

    /**
     * Will return a map of all this <code>WSDLOperation</code>s that 
     * are defined and inherited from super interfaces.
     */
    public HashMap getAllOperations();

    /**
     * @return
     */
    public List getFaults();

    /**
     * @return
     */
    public QName getName();

    /**
     * @return
     */
    public HashMap getOperations();

    public WSDLOperation getOperation(QName qName);

    public WSDLOperation getOperation(String nCName);

    /**
     * @return
     */
    public HashMap getSuperInterfaces();

    public WSDLInterface getSuperInterface(QName qName);

    /**
     * @return
     */
    public URI getTargetnamespace();

    /**
     * @param list
     */
    public void setFaults(List list);

    /**
     * @param string
     */
    public void setName(QName qName);

    /**
     * @param list
     */
    public void setOperations(HashMap list);

    /**
     * The Operation will be added to the interfce's operations.
     * Though the Qname is required the actual storage will be from the 
     * NCName of the operation, but the namespace URI of the QName 
     * should match that of the Namespaces defined in the WSDLConstants interface. 
     * @param qName
     * @param operation
     */
    public void setOperation(QName qName, WSDLOperation operation);

    /**
     * The operation is added by its ncname.
     * @param nCName
     * @param operation
     */
    public void setOperation(String nCName, WSDLOperation operation);

    /**
     * @param list
     */
    public void setSuperInterfaces(HashMap list);

    /**
     * The Inteface will be added to the list of super interfaces keyed with 
     * the QName.
     * @param qName The QName of the Inteface
     * @param interfaceComponent WSDLInterface Object
     */
    public void addSuperInterface(QName qName, WSDLInterface interfaceComponent);

    /**
     * @param uri
     */
    public void setTargetnamespace(URI uri);
}