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
package org.apache.wsdl;

import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.namespace.QName;


/**
 * @author chathura@opensource.lk
 *
 */
public interface WSDLDescription extends Component{
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
     * Retrives the <code>WSDLBinding</code> by its QName. Wil return null
     * if <code>WSDLBinding</code> is not found.
     * @param qName The QName of the Binding.
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
     * The Interface Component will be returned if it exsists, 
     * otherwise null will be returned.
     * @param qName qName of the Interface.
     * @return The Interface Component with the relavent QName 
     */
    public WSDLInterface getInterface(QName qName);

    public HashMap getServices();

    public void setServices(HashMap services);

    /**
     * Will return the <code>WSDLService </code> if found otherwise return null.
     * @param qName <code>QName</code> of the Service
     * @return The Service with the relavent QName 
     */
    public WSDLService getService(QName qName);

    /**
     * Will add the <code>WSDLService</code> to the Map.
     * If object is null it will not be added.
     * If the <code>WSDLService</code> name is null a <code>WSDLProcessingException</code>
     * will be thrown.(its required)
     * @param service
     */
    public void addService(WSDLService service);

    public String getTargetNameSpace();

    public void setTargetNameSpace(String targetNameSpace);

    public HashMap getWsdlInterfaces();

    public void setWsdlInterfaces(HashMap wsdlInterfaces);

    public Object[] getTypes();

    public void setTypes(Object[] types);

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

    /**
     * Returns the WSDL Imports in an <code>ArrayList</code>
     * @return
     */
    public ArrayList getImports();

    /**
     * Sets the imports as an <code>ArrayList</code>
     * @param imports
     */
    public void setImports(ArrayList imports);

    /**
     * Adds an import to the list.
     * @param wsdlImport
     */
    public void addImports(WSDLImport wsdlImport);

    /**
     * Returns the Includes as an <code>ArrayList</code>
     * @return
     */
    public ArrayList getIncludes();

    /**
     * Sets the includes as an <code>Arraylist</code>
     * @param includes
     */
    public void setIncludes(ArrayList includes);

    /**
     * Adds the WSDL Include to the list.
     * @param wsdlInclude
     */
    public void addInclude(WSDLInclude wsdlInclude);

    /**
     * 
     * @return A new instance of type <code>WSDLDescription</code>
     */
    public WSDLDescription createDescription();

    /**
     * 
     * @return A new instance of type <code>WSDLService</code>
     */
    public WSDLService createService();

    /**
     * 
     * @return A new instance of type <code>WSDLInterface</code>
     */
    public WSDLInterface createInterface();

    /**
     * 
     * @return A new instance of type <code>WSDLTypes</code>
     */
    public WSDLTypes createTypes();

    /**
     * 
     * @return A new instance of type <code>WSDLBinding</code>
     */
    public WSDLBinding createBinding();

    /**
     * 
     * @return A new instance of type <code>WSDLOperation</code>
     */
    public WSDLOperation createOperation();

    /**
     * 
     * @return A new instance of type <code>WSDLEndpoint</code>
     */
    public WSDLEndpoint createEndpoint();

    /**
     * 
     * @return A new instance of type <code>WSDLFault</code>
     */
    public WSDLFault createFault();

    /**
     * 
     * @return A new instance of type <code>WSDLFeature</code>
     */
    public WSDLFeature createFeature();

    /**
     * 
     * @return A new instance of type <code>WSDLImport</code>
     */
    public WSDLImport createImport();

    /**
     * 
     * @return A new instance of type <code>WSDLInclude</code>
     */
    public WSDLInclude createInclude();

    public WSDLProperty createProperty();
}