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


package org.apache.axis2.jaxws.description.impl;

import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

import javax.xml.namespace.QName;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import static org.apache.axis2.jaxws.description.builder.MDQConstants.CONSTRUCTOR_METHOD;

/**
 * Utilities used throughout the Description package.
 */
class DescriptionUtils {
    private static final Log log = LogFactory.getLog(DescriptionUtils.class);
    
    static boolean isEmpty(String string) {
        return (string == null || "".equals(string));
    }
    
    static boolean isEmpty(QName qname) {
        return qname == null || isEmpty(qname.getLocalPart());
    }

    /**
     * Creat a java class name given a java method name (i.e. capitalize the first letter)
     * @param name
     * @return
     */
    static String javaMethodtoClassName(String methodName) {
        String className = null;
        if(methodName != null){
            StringBuffer buildClassName = new StringBuffer(methodName);
            buildClassName.replace(0, 1, methodName.substring(0,1).toUpperCase());
            className = buildClassName.toString();
        }
        return className;
    }
    
	/**
	 * @return Returns TRUE if we find just one WebMethod Annotation with exclude flag
	 * set to false
	 */
	static boolean falseExclusionsExist(DescriptionBuilderComposite dbc) {
		
		MethodDescriptionComposite mdc = null;
		Iterator<MethodDescriptionComposite> iter = dbc.getMethodDescriptionsList().iterator();
		
		while (iter.hasNext()) {
			mdc = iter.next();

			WebMethodAnnot wma = mdc.getWebMethodAnnot();
			if (wma != null) {
				if (wma.exclude() == false)
					return true;
			}
		}
		
		return false;
	}
	
	/**
	 * Gathers all MethodDescriptionCompsite's that contain a WebMethod Annotation with the
	 * exclude set to FALSE
	 * @return Returns List<MethodDescriptionComposite> 
	 */
	static ArrayList<MethodDescriptionComposite> getMethodsWithFalseExclusions(DescriptionBuilderComposite dbc) {
		
		ArrayList<MethodDescriptionComposite> mdcList = new ArrayList<MethodDescriptionComposite>();
		Iterator<MethodDescriptionComposite> iter = dbc.getMethodDescriptionsList().iterator(); 
		
		if (DescriptionUtils.falseExclusionsExist(dbc)) {
			while (iter.hasNext()) {
				MethodDescriptionComposite mdc = iter.next();
				if (mdc.getWebMethodAnnot() != null) {
					if (mdc.getWebMethodAnnot().exclude() == false) {
						mdc.setDeclaringClass(dbc.getClassName());
						mdcList.add(mdc);
					}
				}
			}
		}
		
		return mdcList;
	}
	
	/*
	 * Check whether a MethodDescriptionComposite contains a WebMethod annotation with 
	 * exlude set to true
	 */
	static boolean isExcludeTrue(MethodDescriptionComposite mdc) {
	
		if (mdc.getWebMethodAnnot() != null) {
			if (mdc.getWebMethodAnnot().exclude() == true) {
					return true;
			}
		}
		
		return false;
	}
	
    static String javifyClassName(String className) {
    	if(className.indexOf("/") != -1) {
    		return className.replaceAll("/", ".");
    	}
    	return className;
    }
    /**
     * Return the name of the class without any package qualifier.
     * This method should be DEPRECATED when DBC support is complete
     * @param theClass
     * @return the name of the class sans package qualification.
     */
    static String getSimpleJavaClassName(Class theClass) {
        String returnName = null;
        if (theClass != null) {
            String fqName = theClass.getName();
            // We need the "simple name", so strip off any package information from the name
            int endOfPackageIndex = fqName.lastIndexOf('.');
            int startOfClassIndex = endOfPackageIndex + 1;
            returnName = fqName.substring(startOfClassIndex);
        }
        return returnName;
    }
    
    /**
     * Return the name of the class without any package qualifier.
     * @param theClass
     * @return the name of the class sans package qualification.
     */
    static String getSimpleJavaClassName(String name) {
        String returnName = null;
        
        if (name != null) {
            String fqName = name;
            
            // We need the "simple name", so strip off any package information from the name
            int endOfPackageIndex = fqName.lastIndexOf('.');
            int startOfClassIndex = endOfPackageIndex + 1;
            returnName = fqName.substring(startOfClassIndex);
        }
        return returnName;
    }
    
    /**
     * Returns the package name from the class.  If no package, then returns null
     * This method should be DEPRECATED when DBC support is complete
     * @param theClass
     * @return
     */
    static String getJavaPackageName(Class theClass) {
        String returnPackage = null;
        if (theClass != null) {
            String fqName = theClass.getName();
            // Get the package name, if there is one
            int endOfPackageIndex = fqName.lastIndexOf('.');
            if (endOfPackageIndex >= 0) {
                returnPackage = fqName.substring(0, endOfPackageIndex);
            }
        }
        return returnPackage;
    }
    
    /**
     * Returns the package name from the class.  If no package, then returns null
     * @param theClassName
     * @return
     */
    static String getJavaPackageName(String theClassName) {
        String returnPackage = null;
        if (theClassName != null) {
            String fqName = theClassName;
            // Get the package name, if there is one
            int endOfPackageIndex = fqName.lastIndexOf('.');
            if (endOfPackageIndex >= 0) {
                returnPackage = fqName.substring(0, endOfPackageIndex);
            }
        }
        return returnPackage;
    }
    
    /**
     * Create a JAX-WS namespace based on the package name
     * @param packageName
     * @param protocol
     * @return
     */
    static final String NO_PACKAGE_HOST_NAME = "DefaultNamespace";

    static String makeNamespaceFromPackageName(String packageName, String protocol) {
        if (DescriptionUtils.isEmpty(protocol)) {
            protocol = "http";
        }
        if (DescriptionUtils.isEmpty(packageName)) {
            return protocol + "://" + NO_PACKAGE_HOST_NAME;
        }
        StringTokenizer st = new StringTokenizer( packageName, "." );
        String[] words = new String[ st.countTokens() ];
        for(int i = 0; i < words.length; ++i)
            words[i] = st.nextToken();

        StringBuffer sb = new StringBuffer(80);
        for(int i = words.length-1; i >= 0; --i) {
            String word = words[i];
            // seperate with dot
            if( i != words.length-1 )
                sb.append('.');
            sb.append( word );
        }
        return protocol + "://" + sb.toString() + "/";
    }
    
    static Class loadClass(String className)throws ClassNotFoundException {
        // Don't make this public, its a security exposure
        return forName(className, true, getContextClassLoader());
    }
    
    /**
     * Return the class for this name
     * @return Class
     */
    static Class forName(final String className, final boolean initialize, final ClassLoader classloader) throws ClassNotFoundException {
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
    static ClassLoader getContextClassLoader() {
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
    
    
    /**
     * Determines whether a method should have an OperationDescription created for it
     * based on the name. This is a convenience method to allow us to exlude methods
     * such as constructors.
     * @param methodName
     * @return 
     */
    static boolean createOperationDescription(String methodName) {
    	if(methodName.equals(CONSTRUCTOR_METHOD)) {
    		return false;
    	}
    	return true;
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
     * @param requestWrapperClassName
     * @return
     */
    static final String JAXWS_SUBPACKAGE = "jaxws";
    static String determineActualAritfactPackage(String wrapperClassName) {
        String returnWrapperClassName = null;
        if (wrapperClassName == null) {
            return returnWrapperClassName;
        }
    
        // Try to load the class that was passed in
        try {
            loadClass(wrapperClassName);
            returnWrapperClassName = wrapperClassName;
        }
        catch (ClassNotFoundException e) {
            // Couldn't load the class; we'll try another one below.
        }
    
        // If the original class couldn't be loaded, try adding ".jaxws." to the package
        if (returnWrapperClassName == null) {
            String originalPackage = getJavaPackageName(wrapperClassName);
            if (originalPackage != null) {
                String alternatePackage = originalPackage + "." + DescriptionUtils.JAXWS_SUBPACKAGE;
                String className = getSimpleJavaClassName(wrapperClassName);
                String alternateWrapperClass = alternatePackage + "." + className;
                try {
                    loadClass(alternateWrapperClass);
                    returnWrapperClassName = alternateWrapperClass;
                }
                catch (ClassNotFoundException e) {
                    // Couldn't load the class
                }
            }
        }

        return returnWrapperClassName;
    }

    
}
