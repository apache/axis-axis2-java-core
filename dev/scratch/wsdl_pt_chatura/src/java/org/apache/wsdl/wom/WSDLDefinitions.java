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

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.xmlbeans.XmlObject;

/**
 * @author chathura@opensource.lk
 *
 */
public interface WSDLDefinitions {
    /**
     * Returns a Map of <code>WSDLBindings</code> Objects keyed by the <code>QName</code>
     * of the Binding. 
     */
    public HashMap getBindings();

    /**
     * Sets the whole new set of Bindings to the WSDLDefinition.
     */
    public void setBindings(HashMap bindings);

    /**
     * The WSDLBinding Will be added to the map keyed  with its own QName.
     * If the WSDLBinding is null it will not be added.
     * If the WSDLBinding is not null and  Binding name is null then 
     * WSDLProcessingException will be thrown
     * @param binding <code>WSDLBinding</code> Object
     */
    public void addBinding(WSDLBinding binding);

    /**
     * Retrives the <code>WSDLBinding</code> by its QName. Will throw an exception 
     * if the Binding is not found in the  <code>WSDLBinding</code>s map it will throw an
     * WSDLProcessingException.
     * @param qName THe QName of the Binding.
     */
    public WSDLBinding getBinding(QName qName);

    /**
     * The Interface component will be added to the map keyed with its own name.
     * If the Interface is null it will not be added.
     * If the interface name is null an WSDLProcessingException will be thrown
     * @param interfaceComponent
     */
    public void addInterface(WSDLInterface interfaceComponent);

    /**
     * The Interface Component will be returned if it exsists, otherwise will throw an 
     * WSDLException.
     * @param qName qName of the Interface.
     * @return The Interface Component with the relavent QName w
     */
    public WSDLInterface getInterface(QName qName);

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

    public String getTargetNameSpace();

    public void setTargetNameSpace(String targetNameSpace);

    public HashMap getWsdlInterfaces();

    public void setWsdlInterfaces(HashMap wsdlInterfaces);

    public XmlObject[] getTypes();

    public void setTypes(XmlObject[] types);

    /**
     * Gets the name attrebute of the WSDL 1.1 Definitions Element 
     * @return
     */
    public QName getWSDL1DefinitionName();

    /**
     * Sets the name attrebute of the WSDL 1.1 Definitions Element
     * @param wsdl1DefinitionName
     */
    public void setWSDL1DefinitionName(QName wsdl1DefinitionName);

    /**
     * Will return all the Namespaces associated with the Definition
     * Component and will be keyed by the Napespace Prefix.
     * @return
     */
    public HashMap getNamespaces();

    /**
     * Sets the Namespaces associated with the Difinition Component
     * and they should be keyed by its Namespace Prefix.
     * @param namespaces
     */
    public void setNamespaces(HashMap namespaces);

    /**
     * Will return the Namespace URI as a String if there exists an 
     * Namespace URI associated with the given prefix, in the Definition
     * Component, Will return null if not found.
     * @param prefix Prefix defined in the Definitions elemet in the WSDL file
     * @return The Namespace URI for the prefix.
     */
    public String getNamespace(String prefix);
}