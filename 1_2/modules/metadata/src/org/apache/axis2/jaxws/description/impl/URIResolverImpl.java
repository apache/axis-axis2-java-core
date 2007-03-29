/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *      
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.description.impl;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.ws.commons.schema.resolver.URIResolver;
import org.xml.sax.InputSource;

/**
 * This class is used to locate xml schemas that are imported by wsdl documents.
 * 
 */
public class URIResolverImpl implements URIResolver {
	
	private final String HTTP_PROTOCOL = "http";
	
	private final String HTTPS_PROTOCOL = "https";
	
	private final String FILE_PROTOCOL = "file";

    private ClassLoader classLoader;

    public URIResolverImpl() {
    }

    public URIResolverImpl(ClassLoader cl) {
        classLoader = cl;
    }

    public InputSource resolveEntity(String namespace, String schemaLocation,
            String baseUri) {

        InputStream is = null;
        URI pathURI = null;
        if (baseUri != null) {
            try {
                // if the location is an absolute path, build a URL directly
                // from it
            	
                if (isAbsolute(schemaLocation)) {
                    is = getInputStreamForURI(schemaLocation);
                }

                // Try baseURI + relavtive schema path combo
                else {
           			pathURI = new URI(baseUri);
                    String pathURIStr = schemaLocation;
                    // If this is absolute we need to resolve the path without the 
                    // scheme information
                    if(pathURI.isAbsolute()) {
                    	URL url = new URL(baseUri);
                    	if(url != null) {
                    		URI tempURI = new URI(url.getPath());
                        	URI resolvedURI = tempURI.resolve(schemaLocation);
                        	// Add back the scheme to the resolved path
                        	pathURIStr = constructPath(url, resolvedURI);
                    	}
                    }
                    else {
                    	pathURI = pathURI.resolve(schemaLocation);
                    	pathURIStr = pathURI.toString();
                    }
                    // If path is absolute, build URL directly from it
                    if (isAbsolute(pathURIStr)) {
                        is = getInputStreamForURI(pathURIStr);
                    }

                    // if the location is relative, we need to resolve the
                    // location using
                    // the baseURI, then use the loadStrategy to gain an input
                    // stream
                    // because the URI will still be relative to the module
                    else {
                        is = classLoader
                                .getResourceAsStream(pathURI.toString());
                    }
                }
            } catch (Exception e) {
					
            }
        }
        return new InputSource(is);
    }

    /**
     * Checks to see if the location given is an absolute (actual) or relative
     * path.
     * 
     * @param location
     * @return
     */
    private boolean isAbsolute(String location) {
        boolean absolute = false;
        if (location.indexOf(":/") != -1) {
            absolute = true;
        } else if (location.indexOf(":\\") != -1) {
            absolute = true;
        }
        else if(location.indexOf("file:") != -1) {
        	absolute = true;
        }
        return absolute;
    }

    /**
     * Gets input stream from the uri given. If we cannot find the stream,
     * <code>null</code> is returned.
     * 
     * @param uri
     * @return
     */
    private InputStream getInputStreamForURI(String uri) {
        URL streamURL = null;
        InputStream is = null;
        URI pathURI = null;

        try {
            streamURL = new URL(uri);
            is = streamURL.openStream();
        } catch (Throwable t) {
			//Exception handling not needed
        }

        if (is == null) {
            try {
                pathURI = new URI(uri);
                streamURL = pathURI.toURL();
                is = streamURL.openStream();
            } catch (Throwable t) {
				//Exception handling not needed
            }
        }

        if (is == null) {
            try {
                File file = new File(uri);
                streamURL = file.toURL();
                is = streamURL.openStream();
            } catch (Throwable t) {
                //Exception handling not needed
            }
        }
        return is;
    }
    
    private String constructPath(URL baseURL, URI resolvedURI) {
    	String importLocation = null;
    	URL url = null;
    	try {
    		// Allow for http or https
    		if(baseURL.getProtocol() != null && (baseURL.getProtocol().equals(
    				HTTP_PROTOCOL) || baseURL.getProtocol().equals(HTTPS_PROTOCOL))) {
        		url = new URL(baseURL.getProtocol(), baseURL.getHost(), baseURL.getPort(),
        				resolvedURI.toString());
        	}
    		// Check for file
    		else if(baseURL.getProtocol()!= null && baseURL.getProtocol().equals(FILE_PROTOCOL)) {
        		url = new URL(baseURL.getProtocol(), baseURL.getHost(), resolvedURI.toString());
        	}
    	}
    	catch(MalformedURLException e) {
    		throw ExceptionFactory.makeWebServiceException(Messages.getMessage("schemaImportError", 
    				resolvedURI.toString(), baseURL.toString()), e);
    	}
    	if(url == null) {
    		throw ExceptionFactory.makeWebServiceException(Messages.getMessage("schemaImportError", 
    				resolvedURI.toString(), baseURL.toString()));
    	}
    	importLocation = url.toString();
    	return importLocation;
    }
    
}
