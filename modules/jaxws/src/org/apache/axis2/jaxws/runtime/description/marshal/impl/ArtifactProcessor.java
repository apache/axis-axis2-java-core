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
package org.apache.axis2.jaxws.runtime.description.marshal.impl;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Examines a ServiceDesc and locates and/or builds the 
 * JAX-WS artifacts.
 * The JAX-WS artifacts are:
 *    - request wrapper classes
 *    - response wrapper classes
 *    - fault beans for non-JAX-WS compliant exceptions
 */
class ArtifactProcessor {

    private static final Log log = LogFactory.getLog(ArtifactProcessor.class);
    
    private ServiceDescription serviceDesc;
    private Map<OperationDescription, String> requestWrapperMap = new HashMap<OperationDescription, String> ();
    private Map<OperationDescription, String> responseWrapperMap = new HashMap<OperationDescription, String> ();

    /**
     * Artifact Processor
     * @param serviceDesc
     */
    ArtifactProcessor(ServiceDescription serviceDesc) {
        this.serviceDesc = serviceDesc;
    }
    
    Map<OperationDescription, String> getRequestWrapperMap() {
        return requestWrapperMap;
    }

    Map<OperationDescription, String> getResponseWrapperMap() {
        return responseWrapperMap;
    }
    
    void build() {
        for (EndpointDescription ed:serviceDesc.getEndpointDescriptions()) {
            if (ed.getEndpointInterfaceDescription() != null) {
                for (OperationDescription opDesc:ed.getEndpointInterfaceDescription().getOperations()) {
                    
                    String declaringClassName = opDesc.getJavaDeclaringClassName();
                    String packageName = getPackageName(declaringClassName);
                    String simpleName = getSimpleClassName(declaringClassName);
                    String methodName = opDesc.getJavaMethodName();
                    
                    // There is no default for @RequestWrapper/@ResponseWrapper classname  None is listed in Sec. 7.3 on p. 80 of
                    // the JAX-WS spec, BUT Conformance(Using javax.xml.ws.RequestWrapper) in Sec 2.3.1.2 on p. 13
                    // says the entire annotation "...MAY be omitted if all its properties would have default vaules."
                    // We will assume that this statement gives us the liberty to find a wrapper class/build a wrapper class or 
                    // implement an engine w/o the wrapper class.
                    
                    // @RequestWrapper className processing
                    String requestWrapperName = opDesc.getRequestWrapperClassName();
                    if (requestWrapperName == null) {
                        if (packageName.length() > 0) {
                            requestWrapperName = packageName + "." + javaMethodToClassName(methodName);
                        } else {
                            requestWrapperName = javaMethodToClassName(methodName);
                        }
                    }
                    String foundRequestWrapperName  = findArtifact(requestWrapperName);
                    if (foundRequestWrapperName == null) {
                        foundRequestWrapperName = missingArtifact(requestWrapperName);
                    }
                    if (foundRequestWrapperName != null) {
                        requestWrapperMap.put(opDesc, foundRequestWrapperName);
                    }
                    
                    // @ResponseWrapper className processing
                    String responseWrapperName = opDesc.getResponseWrapperClassName();
                    if (responseWrapperName == null) {
                        if (packageName.length() > 0) {
                            responseWrapperName = packageName + "." + javaMethodToClassName(methodName) + "Response";
                        } else {
                            responseWrapperName = javaMethodToClassName(methodName) + "Response";
                        }
                    }
                    String foundResponseWrapperName  = findArtifact(responseWrapperName);
                    if (foundResponseWrapperName == null) {
                        foundResponseWrapperName = missingArtifact(responseWrapperName);
                    }
                    if (foundResponseWrapperName != null) {
                        responseWrapperMap.put(opDesc, foundResponseWrapperName);
                    }
                    
                    
                }
            }
        }
    }
    
    /**
     * @param className
     * @return package name
     */
    private static String getPackageName(String className) {
        int index = className.lastIndexOf(".");
        if (index == 0) {
            return "";
        } else {
            return className.substring(0,index);
        }
    }
    
    /**
     * @param className
     * @return simple class name
     */
    private static String getSimpleClassName(String className) {
        int index = className.lastIndexOf(".");
        if (index == 0) {
            return className;
        } else {
            return className.substring(index+1);
        }
    }
    
    /**
     * @param methodName
     * @return method name converted into a class name
     */
    private static String javaMethodToClassName(String methodName) {
        String className = null;
        if(methodName != null){
            StringBuffer buildClassName = new StringBuffer(methodName);
            buildClassName.replace(0, 1, methodName.substring(0,1).toUpperCase());
            className = buildClassName.toString();
        }
        return className;
    }
        
    /**
     * This method is invoked if the artifact is missing
     * @param artifactName
     * @return newly constructed name or null
     */
    private String missingArtifact(String artifactName) {
        
        // TODO Could we contstruct a proxy of the artifact at this point ?
        if (log.isDebugEnabled()) {
            log.debug("The following class is missing: " + artifactName +" Processing continues.");
        }
        return null;
    }
    
    /**
     * Determine the actual packager name for the generated artifacts by trying to load the class from one of two
     * packages.  This is necessary because the RI implementations of WSGen and WSImport generate the artifacts 
     * in different packages:
     * - WSImport generates the artifacts in the same package as the SEI
     * - WSGen generates the artifacts in a "jaxws" sub package under the SEI package.
     * Note that from reading the JAX-WS spec, it seems that WSGen is doing that correctly; See
     * the conformance requirement in JAX-WS 2.0 Spec Section 3.6.2.1 Document Wrapped on page 36:
     *     Conformance (Default wrapper bean package): In the absence of customizations, the wrapper beans package
     *     MUST be a generated jaxws subpackage of the SEI package.
     *                         ^^^^^^^^^^^^^^^^
     * @param artifactClassName
     * @return
     */
    static final String JAXWS_SUBPACKAGE = "jaxws";
    private static String findArtifact(String artifactClassName) {
        String returnArtifactClassName = null;
        if (artifactClassName == null) {
            return returnArtifactClassName;
        }
    
        // Try to load the class that was passed in
        try {
            loadClass(artifactClassName);
            returnArtifactClassName = artifactClassName;
        }
        catch (ClassNotFoundException e) {
            // Couldn't load the class; we'll try another one below.
        }
    
        // If the original class couldn't be loaded, try adding ".jaxws." to the package
        if (returnArtifactClassName == null) {
            String originalPackage = getPackageName(artifactClassName);
            if (originalPackage.length() > 0) {
                String alternatePackage = originalPackage + "." + JAXWS_SUBPACKAGE;
                String className = getSimpleClassName(artifactClassName);
                String alternateWrapperClass = alternatePackage + "." + className;
                try {
                    loadClass(alternateWrapperClass);
                    returnArtifactClassName = alternateWrapperClass;
                }
                catch (ClassNotFoundException e) {
                    // Couldn't load the class
                }
            }
        }

        return returnArtifactClassName;
    }
    
    private static Class loadClass(String className)throws ClassNotFoundException {
        // Don't make this public, its a security exposure
        return forName(className, true, getContextClassLoader());
    }
    
    /**
     * Return the class for this name
     * @return Class
     */
    private static Class forName(final String className, final boolean initialize, final ClassLoader classloader) throws ClassNotFoundException {
        // NOTE: This method must remain protected because it uses AccessController
        Class cl = null;
        try {
            cl = (Class) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Class.forName(className, initialize, classloader);    
                        }
                    }
                  );  
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw (ClassNotFoundException) e.getException();
        } 
        
        return cl;
    }
    
    /**
     * @return ClassLoader
     */
    private static ClassLoader getContextClassLoader() {
        // NOTE: This method must remain private because it uses AccessController
        ClassLoader cl = null;
        try {
            cl = (ClassLoader) AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            return Thread.currentThread().getContextClassLoader();   
                        }
                    }
                  );  
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw (RuntimeException) e.getException();
        }
        
        return cl;
    }
}
