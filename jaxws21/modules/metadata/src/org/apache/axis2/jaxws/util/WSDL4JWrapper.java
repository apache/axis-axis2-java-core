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
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.metadata.factory.ResourceFinderFactory;
import org.apache.axis2.metadata.registry.MetadataFactoryRegistry;
import org.apache.axis2.metadata.resource.ResourceFinder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.Binding;
import javax.wsdl.BindingOperation;
import javax.wsdl.Definition;
import javax.wsdl.Port;
import javax.wsdl.Service;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import javax.xml.namespace.QName;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class WSDL4JWrapper implements WSDLWrapper {
    private static final Log log = LogFactory.getLog(WSDL4JWrapper.class);
    private Definition wsdlDefinition = null;
    private URL wsdlURL;

    public WSDL4JWrapper(URL wsdlURL) throws FileNotFoundException, UnknownHostException,
            ConnectException, IOException, WSDLException {
        super();
        if(log.isDebugEnabled()) {
            log.debug("Looking for wsdl file on client: " + (wsdlURL != null ? 
                    wsdlURL.getPath():null));
        }
        ClassLoader classLoader = (ClassLoader) AccessController.doPrivileged(
                new PrivilegedAction() {
                    public Object run() {
                        return Thread.currentThread().getContextClassLoader();
                    }
                });
        this.wsdlURL = wsdlURL;
        try {
            URL url = wsdlURL;
            String filePath = null;
            boolean isFileProtocol =
                    (url != null && "file".equals(url.getProtocol())) ? true : false;
            if (isFileProtocol) {
                filePath = (url != null) ? url.getPath() : null;
                URI uri = null;
                if(url != null) {
                    uri = url.toURI();
                }
                //Check is the uri has relative path i.e path is not absolute and is not starting with a "/"
                boolean isRelativePath =
                        (filePath != null && !new File(filePath).isAbsolute()) ? true : false;
                if (isRelativePath) {
                    if (log.isDebugEnabled()) {
                        log.debug("WSDL URL has a relative path");
                    }
                    //Lets read the complete WSDL URL for relative path from class loader
                    //Use relative path of url to fetch complete URL.
                    url = getAbsoluteURL(classLoader, filePath);
                    if (url == null) {
                        if (log.isDebugEnabled()) {
                            log.debug("WSDL URL for relative path not found in ClassLoader");
                            log.warn(
                                    "Unable to read WSDL from relative path, check the relative path");
                            log.info("Relative path example: file:/WEB-INF/wsdl/<wsdlfilename>");
                            log.warn(
                                    "Using relative path as default wsdl URL to create wsdl Definition.");
                        }
                        url = wsdlURL;
                    }
                    else {
                        if(log.isDebugEnabled()) {
                            log.debug("WSDL URL found for relative path: " + filePath + " scheme: " +
                                    uri.getScheme());
                        }
                    }
                }
            }

            URLConnection urlCon = url.openConnection();
            InputStream is = null;
            try {
                is = getInputStream(urlCon);
            }
            catch(IOException e) {
                if(log.isDebugEnabled()) {
                    log.debug("Could not open url connection. Trying to use " +
                    "classloader to get another URL.");
                }
                if(filePath != null) {
                    url = getAbsoluteURL(classLoader, filePath);
                    if(url == null) {
                        if(log.isDebugEnabled()) {
                            log.debug("Could not locate URL for wsdl. Reporting error");
                        }
                            throw new WSDLException("WSDL4JWrapper : ", e.getMessage(), e);
                        }
                    else {
                        urlCon = url.openConnection();
                        if(log.isDebugEnabled()) {
                             log.debug("Found URL for WSDL from jar");
                        }
                    }
                }
                else {
                    if(log.isDebugEnabled()) {
                        log.debug("Could not get URL from classloader. Reporting " +
                        "error due to no file path.");
                    }
                    throw new WSDLException("WSDL4JWrapper : ", e.getMessage(), e);
                }
            }
            if(is != null) {
                is.close();
            }
            final String explicitWsdl = urlCon.getURL().toString();
            try {
                wsdlDefinition = (Definition)AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run() throws WSDLException {
                                WSDLReader reader = getWSDLReader();
                                return reader.readWSDL(explicitWsdl);
                            }
                        }
                );
            } catch (PrivilegedActionException e) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception thrown from AccessController: " + e);
                }
                throw ExceptionFactory.makeWebServiceException(e.getException());
            }

        } catch (FileNotFoundException ex) {
            throw ex;
        } catch (UnknownHostException ex) {
            throw ex;
        } catch (ConnectException ex) {
            throw ex;
        } catch(IOException ex)  {
            throw ex;
        } catch (Exception ex) {
            throw new WSDLException("WSDL4JWrapper : ", ex.getMessage());
        }
    }

    private URL getAbsoluteURL(ClassLoader classLoader, String filePath){
    	URL url = classLoader.getResource(filePath);
        if(url == null) {
            if(log.isDebugEnabled()) {
                log.debug("Could not get URL from classloader. Looking in a jar.");
            }
            if(classLoader instanceof URLClassLoader){
                URLClassLoader urlLoader = (URLClassLoader)classLoader;
                url = getURLFromJAR(urlLoader, wsdlURL);
            }
        }
        return url;    
    }
    private URL getURLFromJAR(URLClassLoader urlLoader, URL relativeURL) {

    	URL[] urlList = null;
    	ResourceFinderFactory rff =(ResourceFinderFactory)MetadataFactoryRegistry.getFactory(ResourceFinderFactory.class);
    	ResourceFinder cf = rff.getResourceFinder();
    	urlList = cf.getURLs(urlLoader);
    	if(urlList == null){
    	    if(log.isDebugEnabled()){
    	        log.debug("No URL's found in URL ClassLoader");
    	    }
    	    ExceptionFactory.makeWebServiceException(Messages.getMessage("WSDL4JWrapperErr1"));
    	}

        for (URL url : urlList) {
            if ("file".equals(url.getProtocol())) {
                File f = new File(url.getPath());
                // If file is not of type directory then its a jar file
                if (f.exists() && !f.isDirectory()) {
                    try {
                        JarFile jf = new JarFile(f);
                        Enumeration<JarEntry> entries = jf.entries();
                        // read all entries in jar file and return the first
                        // wsdl file that matches
                        // the relative path
                        while (entries.hasMoreElements()) {
                            JarEntry je = entries.nextElement();
                            String name = je.getName();
                            if (name.endsWith(".wsdl")) {
                                String relativePath = relativeURL.getPath();
                                if (relativePath.endsWith(name)) {
                                    String path = f.getAbsolutePath();
                                    // This check is necessary because Unix/Linux file paths begin
                                    // with a '/'. When adding the prefix 'jar:file:/' we may end
                                    // up with '//' after the 'file:' part. This causes the URL 
                                    // object to treat this like a remote resource
                                    if(path != null && path.indexOf("/") == 0) {
                                        path = path.substring(1, path.length());
                                    }

                                    URL absoluteUrl = new URL("jar:file:/"
                                            + path + "!/"
                                            + je.getName());
                                    return absoluteUrl;
                                }
                            }
                        }
                    } catch (Exception e) {
                        throw ExceptionFactory.makeWebServiceException(e);
                    }
                }
            }
        }

        return null;
    }

    private static WSDLReader getWSDLReader() throws WSDLException {
        // Keep this method private
        WSDLReader reader;
        try {
            reader = (WSDLReader)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws WSDLException {
                            WSDLFactory factory = WSDLFactory.newInstance();
                            return factory.newWSDLReader();
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw (WSDLException)e.getException();
        }
        return reader;
    }

    public WSDL4JWrapper(URL wsdlURL, Definition wsdlDefinition) throws WSDLException {
        super();
        this.wsdlURL = wsdlURL;
        this.wsdlDefinition = wsdlDefinition;

    }
    //TODO: Perform validations for each method to check for null parameters on QName.

    public Definition getDefinition() {
        return wsdlDefinition;
    }


    public Binding getFirstPortBinding(QName serviceQname) {
        // TODO Auto-generated method stub
        Service service = getService(serviceQname);
        if (service == null) {
            return null;
        }
        Map map = getService(serviceQname).getPorts();
        if (map == null || map.isEmpty()) {
            return null;
        }
        for (Object listObject : map.values()) {
            Port wsdlPort = (Port)listObject;
            return wsdlPort.getBinding();

        }
        return null;

    }

    public String getOperationName(QName serviceQname, QName portQname) {
        Port port = getPort(serviceQname, portQname);
        Binding binding = port.getBinding();
        if (binding == null) {
            return null;
        }

        List operations = binding.getBindingOperations();
        for (Object opObj : operations) {
            BindingOperation operation = (BindingOperation)opObj;
            return operation.getName();
        }
        return null;
    }

    private Port getPort(QName serviceQname, QName eprQname) {
        Service service = getService(serviceQname);
        if (service == null) {
            return null;
        }
        return service.getPort(eprQname.getLocalPart());

    }

    public ArrayList getPortBinding(QName serviceQname) {
        // TODO Auto-generated method stub
        Map map = this.getService(serviceQname).getPorts();
        if (map == null || map.isEmpty()) {
            return null;
        }
        ArrayList<Binding> portBindings = new ArrayList<Binding>();
        for (Object listObject : map.values()) {
            Port wsdlPort = (Port)listObject;
            Binding binding = wsdlPort.getBinding();
            if (binding != null) {
                portBindings.add(binding);
            }

        }
        return portBindings;

    }

    public String getPortBinding(QName serviceQname, QName portQname) {
        Port port = getPort(serviceQname, portQname);
        if (port == null) {
            return null;
        }
        Binding binding = port.getBinding();
        return binding.getQName().getLocalPart();
    }

    public String[] getPorts(QName serviceQname) {
        String[] portNames = null;
        Service service = this.getService(serviceQname);
        if (service == null) {
            return null;
        }
        Map map = service.getPorts();
        if (map == null || map.isEmpty()) {
            return null;
        }
        portNames = new String[map.values().size()];
        Iterator iter = map.values().iterator();
        for (int i = 0; iter.hasNext(); i++) {
            Port wsdlPort = (Port)iter.next();
            if (wsdlPort != null) {
                portNames[i] = wsdlPort.getName();
            }
        }
        return portNames;
    }

    public Service getService(QName serviceQname) {
        // TODO Auto-generated method stub
        if (serviceQname == null) {
            return null;
        }
        return wsdlDefinition.getService(serviceQname);

    }

    public String getSOAPAction(QName serviceQname) {
        // TODO Auto-generated method stub
        Binding binding = getFirstPortBinding(serviceQname);
        if (binding == null) {
            return null;
        }
        List operations = binding.getBindingOperations();
        for (Object opObj : operations) {
            BindingOperation operation = (BindingOperation)opObj;
            List exElements = operation.getExtensibilityElements();
            for (Object elObj : exElements) {
                ExtensibilityElement exElement = (ExtensibilityElement)elObj;
                if (isSoapOperation(exElement)) {
                    SOAPOperation soapOperation = (SOAPOperation)exElement;
                    return soapOperation.getSoapActionURI();
                }
            }
        }
        return null;
    }

    public String getSOAPAction(QName serviceQname, QName portQname) {
        // TODO Auto-generated method stub
        Port port = getPort(serviceQname, portQname);
        if (port == null) {
            return null;
        }
        Binding binding = port.getBinding();
        if (binding == null) {
            return null;
        }
        List operations = binding.getBindingOperations();
        for (Object opObj : operations) {
            BindingOperation operation = (BindingOperation)opObj;
            List exElements = operation.getExtensibilityElements();
            for (Object elObj : exElements) {
                ExtensibilityElement exElement = (ExtensibilityElement)elObj;
                if (isSoapOperation(exElement)) {
                    SOAPOperation soapOperation = (SOAPOperation)exElement;
                    return soapOperation.getSoapActionURI();
                }
            }
        }
        return null;
    }

    public String getSOAPAction(QName serviceQname, QName portQname, QName operationQname) {
        Port port = getPort(serviceQname, portQname);
        if (port == null) {
            return null;
        }
        Binding binding = port.getBinding();
        if (binding == null) {
            return null;
        }
        List operations = binding.getBindingOperations();
        if (operations == null) {
            return null;
        }
        BindingOperation operation = null;
        for (Object opObj : operations) {
            operation = (BindingOperation)opObj;
        }
        List exElements = operation.getExtensibilityElements();
        for (Object elObj : exElements) {
            ExtensibilityElement exElement = (ExtensibilityElement)elObj;
            if (isSoapOperation(exElement)) {
                SOAPOperation soapOperation = (SOAPOperation)exElement;
                if (soapOperation.getElementType().equals(operationQname)) {
                    return soapOperation.getSoapActionURI();
                }
            }
        }

        return null;
    }

    public URL getWSDLLocation() {
        // TODO Auto-generated method stub
        return this.wsdlURL;
    }

    private boolean isSoapOperation(ExtensibilityElement exElement) {
        return WSDLWrapper.SOAP_11_OPERATION.equals(exElement.getElementType());
        //TODO: Add Soap12 support later
        // || WSDLWrapper.SOAP_12_OPERATION.equals(exElement.getElementType());
    }

    public String getTargetNamespace() {
        // TODO Auto-generated method stub
        return wsdlDefinition.getTargetNamespace();
    }
    
    /**
     * This method provides a Java2 Security compliant way to obtain the InputStream
     * for a given URLConnection object. This is needed as a given URLConnection object
     * may be an instance of a FileURLConnection object which would require access 
     * permissions if Java2 Security was enabled.
     */
    private InputStream getInputStream(URLConnection urlCon) throws Exception {
    	final URLConnection finalURLCon = urlCon;
    	InputStream is = null;
    	try {
    		is = (InputStream) AccessController.doPrivileged(
        			new PrivilegedExceptionAction() {
    					public Object run() throws IOException {
    						return finalURLCon.getInputStream();
    					}
        			});
    	}
    	catch(PrivilegedActionException e) {
    		throw e.getException();
    	}
    	return is;
    }

}
