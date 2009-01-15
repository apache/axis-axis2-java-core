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

package org.apache.axis2.jaxws.util;

import java.io.IOException;

import org.apache.axis2.jaxws.catalog.JAXWSCatalogManager;
import org.apache.axis2.jaxws.description.impl.URIResolverImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.xml.resolver.Catalog;
import org.xml.sax.InputSource;

/**
 * This resolver provides the means of resolving the imports and includes of a
 * given schema document. It allows the use of the Apache Commons Resolver API
 * to redirect resource requests to alternative locations.
 */
public class CatalogURIResolver extends URIResolverImpl {

    private static Log log = LogFactory.getLog(CatalogWSDLLocator.class);
    private Catalog catalogResolver;
    
    /**
     * CatalogURIResolver constructor.  Resolves WSDL URIs using Apache Commons Resolver API.
     * @param catalogManager
     *            the OASISCatalogManager which will determine the settings for the XML catalog
     */
    public CatalogURIResolver(JAXWSCatalogManager catalogManager) {
        this(catalogManager, null);
    }
    
    /**
     * CatalogURIResolver constructor.  Resolves WSDL URIs using Apache Commons Resolver API.
     * @param catalogManager
     *            the OASISCatalogManager which will determine the settings for the XML catalog
     * @param classLoader
     */    
    public CatalogURIResolver(JAXWSCatalogManager catalogManager, ClassLoader classLoader) {
        super(classLoader);
        if (catalogManager != null) {
            this.catalogResolver = catalogManager.getCatalog();
        }
    }
    
    /**
     * Resolves URIs using Apache Commons Resolver API.
     * 
     * @param importURI a URI specifying the document to import
     * @param parent a URI specifying the location of the parent document doing
     * the importing
     * @return the resolved import location, or null if no indirection is performed
     */
    public String getRedirectedURI(String namespace,
                                   String schemaLocation,
                                   String baseUri) {
        String resolvedImportLocation = null;
        try {
            resolvedImportLocation = this.catalogResolver.resolveSystem(namespace);
            if (resolvedImportLocation == null) {
                resolvedImportLocation = catalogResolver.resolveURI(schemaLocation);
            }
            if (resolvedImportLocation == null) {
                resolvedImportLocation = catalogResolver.resolvePublic(namespace, namespace);
            }
        
        } catch (IOException e) {
            throw new RuntimeException("Catalog resolution failed", e);
        }

        return resolvedImportLocation;
    }
    
    /**
     * As for the resolver the public ID is the target namespace of the
     * schema and the schemaLocation is the value of the schema location
     * @param namespace
     * @param schemaLocation
     * @param baseUri
     */
    public InputSource resolveEntity(String namespace,
                                     String schemaLocation,
                                     String baseUri) {
        String location = schemaLocation;     
        
        if (this.catalogResolver != null) {
            String redirectedURI = getRedirectedURI(namespace, schemaLocation, baseUri);
            if (redirectedURI != null) {
                location = redirectedURI;
            }
        }
        
        return super.resolveEntity(namespace, location, baseUri);
    }

}
