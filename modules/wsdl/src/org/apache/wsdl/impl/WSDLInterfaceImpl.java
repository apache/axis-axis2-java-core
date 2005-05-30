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
package org.apache.wsdl.impl;

import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Chathura Herath
 */
public class WSDLInterfaceImpl extends ExtensibleComponentImpl
        implements WSDLInterface {
    /**
     * Field name
     */
    private QName name;

    /**
     * Field superInterfaces
     */
    private HashMap superInterfaces = new HashMap();

    /**
     * Field faults
     */
    private List faults = new LinkedList();

    /**
     * Field operations
     */
    private HashMap operations = new HashMap();

    /**
     * Field styleDefault
     */
    private String styleDefault;

    /**
     * Method getDefinedOperations
     *
     * @return
     */
    public HashMap getDefinedOperations() {
        return this.operations;
    }

    /**
     * Will return a map of all this <code>WSDLOperation</code>s that
     * are defined and inherited from super interfaces.
     *
     * @return
     */
    public HashMap getAllOperations() {
        HashMap all =  this.operations;
        if (this.superInterfaces.size() == 0) {
            return all;
        } else {
            Iterator superIterator =
                    this.superInterfaces.values().iterator();
            Iterator operationIterator;
            WSDLInterface superInterface;
            WSDLOperation superInterfaceOperation;
            Iterator thisIterator = all.values().iterator();
            WSDLOperation thisOperation;
            boolean tobeAdded = false;
            while (superIterator.hasNext()) {
                superInterface = (WSDLInterface) superIterator.next();
                operationIterator =
                superInterface.getAllOperations().values().iterator();
                while (operationIterator.hasNext()) {
                    superInterfaceOperation =
                    (WSDLOperation) operationIterator.next();
                    tobeAdded = true;
                    while (thisIterator.hasNext()) {
                        thisOperation = (WSDLOperation) thisIterator.next();
                        if ((thisOperation.getName() == superInterfaceOperation.getName())
                                && !tobeAdded) {
                            if (thisOperation.getTargetnamespace().equals(
                                    superInterfaceOperation.getTargetnamespace())) {

                                // Both are the same Operation; the one inherited and
                                // the one that is already in the map(may or maynot be inherited)
                                tobeAdded = false;
                            } else {

                                // same name but target namespces dont match
                                // TODO Think this is an error
                                throw new WSDLProcessingException(
                                        "The Interface " + this.getName()
                                                + " has more than one Operation that has the same name but not the same interface ");
                            }
                        }
                    }
                    if (tobeAdded) {

                        // This one is not in the list already developped
                        all.put(superInterfaceOperation.getName().getLocalPart(),
                                superInterfaceOperation);
                    }
                }
            }
            return all;
        }
    }

    /**
     * @return
     */
    public List getFaults() {
        return faults;
    }

    /**
     * @return
     */
    public QName getName() {
        return name;
    }

    /**
     * @return
     */
    public HashMap getOperations() {
        return operations;
    }

    /**
     * Retruns the <code>WSDLOperation</code>
     *
     * @param nCName
     * @return
     */
    public WSDLOperation getOperation(String nCName) {
        return (WSDLOperation)this.operations.get(nCName);
        
    }

    /**
     * @return
     */
    public HashMap getSuperInterfaces() {
        return superInterfaces;
    }

    /**
     * Method getSuperInterface
     *
     * @param qName
     * @return
     */
    public WSDLInterface getSuperInterface(QName qName) {
        return (WSDLInterface) this.superInterfaces.get(qName);
    }

    /**
     * The Targetnamespace is that of the namespace URI of the QName of
     * this component.
     *
     * @return URI as a String if the name is set otherwise will return null.
     */
    public String getTargetnamespace() {
        if (null == this.name) {
            return null;
        }
        return this.name.getNamespaceURI();
    }

    /**
     * @param list
     */
    public void setFaults(List list) {
        faults = list;
    }

    /**
     * @param qName
     */
    public void setName(QName qName) {
        name = qName;
    }

    /**
     * @param list
     */
    public void setOperations(HashMap list) {
        operations = list;
    }

    /**
     * The operation is added by its ncname. If operation is null
     * it will not be added. If the Operation name is null a
     * <code>WSDLProcessingException</code> will be thrown.
     *
     * @param operation
     */
    public void setOperation(WSDLOperation operation) {
        if (null == operation) {
            return;
        }
        if (null == operation.getName()) {
            throw new WSDLProcessingException(
                    "The Operation name cannot be null (required)");
        }
        this.operations.put(operation.getName().getLocalPart(), operation);
    }

    /**
     * @param list
     */
    public void setSuperInterfaces(HashMap list) {
        superInterfaces = list;
    }

    /**
     * The Inteface will be added to the list of super interfaces keyed with
     * the QName.
     *
     * @param interfaceComponent WSDLInterface Object
     */
    public void addSuperInterface(WSDLInterface interfaceComponent) {
        this.superInterfaces.put(interfaceComponent.getName(),
                interfaceComponent);
    }

    /**
     * Will return the StyleDefault if exist , otherwise will return null
     *
     * @return
     */
    public String getStyleDefault() {
        return styleDefault;
    }

    /**
     * Method setStyleDefault
     *
     * @param styleDefault
     */
    public void setStyleDefault(String styleDefault) {
        this.styleDefault = styleDefault;
    }
}
