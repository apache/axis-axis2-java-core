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

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.catalog.JAXWSCatalogManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.apache.xml.resolver.Catalog;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.PrivilegedAction;


/**
 * This resolver provides the means of resolving the imports and includes of a
 * given schema document. It allows the use of the Apache Commons Resolver API
 * to redirect resource requests to alternative locations.
 */
public class CatalogURIResolver implements URIResolver {

    private static Log log = LogFactory.getLog(CatalogWSDLLocator.class);
    private Catalog catalogResolver;
    
    /**
     * CatalogURIResolver constructor.  Resolves WSDL URIs using Apache Commons Resolver API.
     * @param catalogManager
     *            the OASISCatalogManager which will determine the settings for the XML catalog
     */
    
    public CatalogURIResolver(JAXWSCatalogManager catalogManager) {
        this.catalogResolver = catalogManager.getCatalog();
    }
    
    /**
     * Resolves URIs using Apache Commons Resolver API.
     * 
     * @param importURI a URI specifying the document to import
     * @param parent a URI specifying the location of the parent document doing
     * the importing
     * @return the resolved import location, or null if no indirection is performed
     */
    public String getRedirectedURI(String importURI, String parent) {
        String resolvedImportLocation = null;
        try {
            resolvedImportLocation = this.catalogResolver.resolveSystem(importURI);
            if (resolvedImportLocation == null) {
                resolvedImportLocation = catalogResolver.resolveURI(importURI);
            }
            if (resolvedImportLocation == null) {
                resolvedImportLocation = catalogResolver.resolvePublic(importURI, parent);
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
                                     String baseUri){

        if (baseUri!=null) 
        {
        	String redirectedURI = getRedirectedURI(namespace, baseUri);
        	if (redirectedURI != null)	
        	   schemaLocation = redirectedURI;
            try
            {
                File baseFile = new File(baseUri);
                
                
                if (fileExists(baseFile)) baseUri = baseFile.toURI().toString();
                
                String ref = new URI(baseUri).resolve(new URI(schemaLocation)).toString();

                return new InputSource(ref);
            }
            catch (URISyntaxException e1)
            {
                throw new RuntimeException(e1);
            }

        }
        return new InputSource(schemaLocation);

    }

    /**
     * Find whether a given uri is relative or not
     *
     * @param uri
     * @return boolean
     */
    protected boolean isAbsolute(String uri) {
        return uri.startsWith("http://");
    }

    /**
     * This is essentially a call to "new URL(contextURL, spec)"
     * with extra handling in case spec is
     * a file.
     *
     * @param contextURL
     * @param spec
     * @throws java.io.IOException
     */
    protected URL getURL(URL contextURL, String spec) throws IOException {

        // First, fix the slashes as windows filenames may have backslashes
        // in them, but the URL class wont do the right thing when we later
        // process this URL as the contextURL.
        String path = spec.replace('\\', '/');

        // See if we have a good URL.
        URL url;

        try {

            // first, try to treat spec as a full URL
            url = new URL(contextURL, path);

            // if we are deail with files in both cases, create a url
            // by using the directory of the context URL.
            if ((contextURL != null) && url.getProtocol().equals("file")
                    && contextURL.getProtocol().equals("file")) {
                url = getFileURL(contextURL, path);
            }
        } catch (MalformedURLException me) {

            // try treating is as a file pathname
            url = getFileURL(contextURL, path);
        }

        // Everything is OK with this URL, although a file url constructed
        // above may not exist.  This will be caught later when the URL is
        // accessed.
        return url;
    }    // getURL

    /**
     * Method getFileURL
     *
     * @param contextURL
     * @param path
     * @throws IOException
     */
    protected URL getFileURL(URL contextURL, String path)
            throws IOException {

        if (contextURL != null) {

            // get the parent directory of the contextURL, and append
            // the spec string to the end.
            String contextFileName = contextURL.getFile();
            URL parent = null;
            //the logic for finding the parent file is this.
            //1.if the contextURI represents a file then take the parent file
            //of it
            //2. If the contextURI represents a directory, then take that as
            //the parent
            File parentFile;
            File contextFile = new File(contextFileName);
            if (contextFile.isDirectory()){
                parentFile = contextFile;
            }else{
                parentFile = contextFile.getParentFile();
            }

            if (parentFile != null) {
                parent = parentFile.toURL();
            }
            if (parent != null) {
                return new URL(parent, path);
            }
        }

        return new URL("file", "", path);
    }    // getFileURL
    
    
    private Boolean fileExists (final File file) {
        Boolean exists = (Boolean) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return new Boolean(file.exists());
                    }
                }
        );
        return exists;
    }
    
    private Boolean fileIsDirectory(final File file) {
        Boolean isDir = (Boolean) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return new Boolean(file.isDirectory());
                    }
                }
        );
        return isDir;
    }


}
