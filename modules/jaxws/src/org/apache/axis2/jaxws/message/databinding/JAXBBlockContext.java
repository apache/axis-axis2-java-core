/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
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
package org.apache.axis2.jaxws.message.databinding;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/*
 * A JAXBBlockContext controls access to the JAXB Context
 * In addition the JAXBBlockContext contains additional contextural information needed
 * by the JAX-WS component
 * 
 * This class is immutable after construction.
 */
public class JAXBBlockContext {
    
    private static final Log log = LogFactory.getLog(JAXBBlockContext.class);
    
	private Set<Package> contextPackages;  // List of packages needed by the context
	private JAXBContext jaxbContext = null;
	
	/**
	 * Normal Constructor JAXBBlockContext
	 * @param packages Set of packages needed by the JAXBContext.
	 */
	public JAXBBlockContext(Set<Package> packages) {
        this.contextPackages = packages;
	}
    
    /**
     * Normal Constructor JAXBBlockContext
     * @param contextPackage
     */
    public JAXBBlockContext(Package contextPackage) {
        this.contextPackages = new HashSet();
        this.contextPackages.add(contextPackage);
    }

	/**
	 * "Dispatch" Constructor
	 * Use this full constructor when the JAXBContent is provided by
	 * the customer.  
	 * @param jaxbContext
	 */
	public JAXBBlockContext(JAXBContext jaxbContext) {
		this.jaxbContext = jaxbContext;
	}

	/**
	 * @return Class representing type of the element
	 */
	public Set<Package> getContextPackages() {
		return contextPackages;
	}
    
	/**
	 * @return get the JAXBContext
	 * @throws JAXBException
	 */
	public JAXBContext getJAXBContext() throws JAXBException {
		if (jaxbContext == null) {	
		    if (log.isDebugEnabled()) {
		        log.debug("A JAXBContext did not exist, creating a new one with the context packages.");
            }
            jaxbContext = JAXBUtils.getJAXBContext(contextPackages);
		}
        else {
            if (log.isDebugEnabled()) {
                log.debug("Using an existing JAXBContext");
            }
        }
		return jaxbContext;
	}
}
