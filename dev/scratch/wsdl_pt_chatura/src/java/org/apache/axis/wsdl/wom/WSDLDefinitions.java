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
package org.apache.axis.wsdl.wom;

import java.net.URI;
import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;

/**
 * @author chathura@opensource.lk
 *
 */
public interface WSDLDefinitions {
    public HashMap getBindings();

    public void setBindings(HashMap bindings);

    /**
     * The binding will be added despite its namespace being either of
     * that specified in the WSDLConstants class(WSDL_NAMESPACES) otherwise the checkValidityOfNamespaceWRTWSDLContext() 
     * method will throw an exception.
     * @param qName The QName of the binding
     * @param binding Binding Object
     */
    public void addBinding(QName qName, WSDLBinding binding);

    /**
     * Inserts a WSDLBinding to the Collection by keying its NCName.
     * @param nCName NCName of the Binding
     * @param binding Binding Object
     */
    public void addBinding(String nCName, WSDLBinding binding);

    /**
     * The binding will be retrived despite its namespace being either of
     * that specified in the WSDLConstants class(WSDL_NAMESPACES) otherwise the checkValidityOfNamespaceWRTWSDLContext() 
     * method will throw an exception.
     * @param qName THe Namespace of the QName should be either of the WSDL_NAMESPACES
     * mentioned in the WSDLConstants interface.
     * @return The Binding with the relavent QName which have a namespace
     * that qualifies that of the versions in the WSDLConstants interface.
     */
    public WSDLBinding getBinding(QName qName);

    /**
     * Binding will be retrived by its NCName and the Namespace of the QName
     * is assumed to be in line with that of the WSDL_NAMESPACES in the WSDLConstants
     * interface, Thus no namespace checking will be done.
     * @param nCName NCName of the Binding
     * @return WSDLBinding Object or will throw an WSDLProcessingException in the case of object not found. 
     */
    public WSDLBinding getBinding(String nCName);

    /**
     * The Inteface will be added despite its namespace being either of
     * that specified in the WSDLConstants class(WSDL_NAMESPACES) otherwise the checkValidityOfNamespaceWRTWSDLContext() 
     * method will throw an exception.
     * @param qName The QName of the Inteface
     * @param interfaceComponent WSDLInterface Object
     */
    public void addInterface(QName qName, WSDLInterface interfaceComponent);

    /**
     * Inserts a WSDLInterface to the Collection by keying its NCName.
     * @param nCName NCName of the WSDLInterface
     * @param interfaceComponent WSDLInterface Object
     */
    public void addInterface(String nCName, WSDLInterface interfaceComponent);

    /**
     * The Interface Component will be retrived despite its namespace being either of
     * that specified in the WSDLConstants class(WSDL_NAMESPACES) otherwise the checkValidityOfNamespaceWRTWSDLContext() 
     * method will throw an exception.
     * @param qName THe Namespace of the QName should be either of the WSDL_NAMESPACES
     * mentioned in the WSDLConstants interface.
     * @return The Interface Component with the relavent QName which have a namespace
     * that qualifies that of the versions in the WSDLConstants interface.
     */
    public WSDLInterface getInterface(QName qName);

    /**
     * Interface Component will be retrived by its NCName and the Namespace of the QName
     * is assumed to be in line with that of the WSDL_NAMESPACES in the WSDLConstants
     * interface, Thus no namespace checking will be done.
     * @param nCName NCName of the Interface Component
     * @return WSDLInterface Object or will throw an WSDLProcessingException in the case of object not found. 
     */
    public WSDLInterface getInterface(String nCName);

    public HashMap getServices();

    public void setServices(HashMap services);

    /**
     * The Service will be retrived despite its namespace being either of
     * that specified in the WSDLConstants class(WSDL_NAMESPACES) otherwise the checkValidityOfNamespaceWRTWSDLContext() 
     * method will throw an exception.
     * @param qName THe Namespace of the QName should be either of the WSDL_NAMESPACES
     * mentioned in the WSDLConstants interface.
     * @return The Service with the relavent QName which have a namespace
     * that qualifies that of the versions in the WSDLConstants interface.
     */
    public WSDLService getService(QName qName);

    /**
     * Service will be retrived by its NCName and the Namespace of the QName
     * is assumed to be in line with that of the WSDL_NAMESPACES in the WSDLConstants
     * interface, Thus no namespace checking will be done.
     * @param nCName NCName of the Service
     * @return WSDLService Object or will throw an WSDLProcessingException in the case of object not found. 
     */
    public WSDLService getService(String nCName);

    public URI getTargetNameSpace();

    public void setTargetNameSpace(URI targetNameSpace);

    public HashMap getWsdlInterfaces();

    public void setWsdlInterfaces(HashMap wsdlInterfaces);

    public XmlObject[] getTypes();

    public void setTypes(XmlObject[] types);
}
