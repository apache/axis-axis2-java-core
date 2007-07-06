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
package org.apache.axis2.jaxws.message.databinding;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.ws.Holder;
import java.util.TreeSet;

/*
 * A JAXBBlockContext controls access to the JAXB Context
 * In addition the JAXBBlockContext contains additional contextural information needed
 * by the JAX-WS component
 * 
 * This class is immutable after construction.
 */

public class JAXBBlockContext {

    private static final Log log = LogFactory.getLog(JAXBBlockContext.class);

    private TreeSet<String> contextPackages;  // List of packages needed by the context
    private String contextPackagesKey;        // Unique key that represents the set of contextPackages (usually toString)
    private JAXBContext jaxbContext = null;   // JAXBContext
    private JAXBUtils.CONSTRUCTION_TYPE       // How the JAXBContext is constructed
            constructionType = JAXBUtils.CONSTRUCTION_TYPE.UNKNOWN;

    // There are two modes of marshalling and unmarshalling: "by java type" and "by schema element".
    // The prefered mode is "by schema element" because it is safe and xml-centric.
    // However there are some circumstances when "by schema element" is not available.
    //    Examples: RPC Lit processing (the wire element is defined by a wsdl:part...not schema)
    //              Doc/Lit Bare "Minimal" Processing (JAXB ObjectFactories are missing...and thus we must use "by type" for primitives/String)
    // Please don't use "by java type" processing to get around errors.

    private Class processType = null;
    private boolean isxmlList =false;

    /**
     * Full Constructor JAXBBlockContext (most performant)
     *
     * @param packages Set of packages needed by the JAXBContext.
     */
    public JAXBBlockContext(TreeSet<String> packages, String packagesKey) {
        this.contextPackages = packages;
        this.contextPackagesKey = packagesKey;
    }

    /**
     * Slightly slower constructor
     *
     * @param packages
     */
    public JAXBBlockContext(TreeSet<String> packages) {
        this(packages, packages.toString());
    }

    /**
     * Normal Constructor JAXBBlockContext
     *
     * @param contextPackage
     * @deprecated
     */
    public JAXBBlockContext(String contextPackage) {
        this.contextPackages = new TreeSet();
        this.contextPackages.add(contextPackage);
        this.contextPackagesKey = this.contextPackages.toString();
    }

    /**
     * "Dispatch" Constructor Use this full constructor when the JAXBContent is provided by the
     * customer.
     *
     * @param jaxbContext
     */
    public JAXBBlockContext(JAXBContext jaxbContext) {
        this.jaxbContext = jaxbContext;
    }

    /** @return Class representing type of the element */
    public TreeSet<String> getContextPackages() {
        return contextPackages;
    }

    /**
     * @return get the JAXBContext
     * @throws JAXBException
     */
    public JAXBContext getJAXBContext() throws JAXBException {
        if (jaxbContext == null) {
            if (log.isDebugEnabled()) {
                log.debug(
                        "A JAXBContext did not exist, creating a new one with the context packages.");
            }
            Holder<JAXBUtils.CONSTRUCTION_TYPE> constructType =
                    new Holder<JAXBUtils.CONSTRUCTION_TYPE>();
            jaxbContext =
                    JAXBUtils.getJAXBContext(contextPackages, constructType, contextPackagesKey);
            constructionType = constructType.value;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Using an existing JAXBContext");
            }
        }
        return jaxbContext;
    }

    /** @return RPC Declared Type */
    public Class getProcessType() {
        return processType;
    }

    /**
     * Set RPC Declared Type.  The use of use this property if the message is style=document is
     * discouraged.
     *
     * @param type
     */
    public void setProcessType(Class type) {
        processType = type;
    }

    public JAXBUtils.CONSTRUCTION_TYPE getConstructionType() {
        return constructionType;
    }

    public boolean isxmlList() {
        return isxmlList;
    }

    public void setIsxmlList(boolean isxmlList) {
        this.isxmlList = isxmlList;
    }
    
}
