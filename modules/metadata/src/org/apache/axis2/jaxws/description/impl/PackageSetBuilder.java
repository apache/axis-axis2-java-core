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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBElement;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.util.JavaUtils;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.jaxws.util.XMLRootElementUtil;
import org.apache.axis2.jaxws.wsdl.SchemaReader;
import org.apache.axis2.jaxws.wsdl.SchemaReaderException;
import org.apache.axis2.jaxws.wsdl.impl.SchemaReaderImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * In order to marshal or unmarshal the user data, we need to know
 * the set of packages involved.  The set of packages is used to construct
 * an appropriate JAXBContext object during the marshalling/unmarshalling.
 * 
 * There are two ways to get this data.
 * 
 * Schema Walk (preferred):  Get the list of packages by walking the schemas
 * that are referenced by the wsdl (or generated wsdl).  Each schema
 * represents a different package.  The package is obtained using the
 * jaxb customization or JAXB default ns<->package rule. 
 * 
 * Annotation Walk(secondary) : Walk the list of Endpoints, Operations,
 * Parameters, etc. and build a list of packages by looking at the
 * classes involved.  
 * 
 * The Schema Walk is faster and more complete, but relies on the 
 * presence of the schema or wsdl.
 * 
 * The Annotation Walk is slower and is not complete.  For example,
 * the annotation walk may not discover the packages for derived types
 * that are defined in a different schema than the formal parameter types.
 */
public class PackageSetBuilder {
    
    private static Log log = LogFactory.getLog(PackageSetBuilder.class);

    /**
     * This is a static utility class.  The constructor is intentionally private
     */
    private PackageSetBuilder() {}
    
    /**
     * Walks the schemas of the serviceDesc's wsdl (or generated wsdl) to determine the list of 
     * packages.
     * This is the preferred algorithm for discovering the package set.
     * 
     * @param serviceDesc ServiceDescription
     * @return Set of Packages
     */
    public static Set<String> getPackagesFromSchema(ServiceDescription serviceDesc) {
    	boolean annotationWalking = true;
    	Set<String> set = new HashSet<String>();
    	//If we are on client side we will get wsdl definition from ServiceDescription. If we are on server side we will have to 
    	//read wsdlLocation from @WebService Annotation.
    	ServiceDescriptionWSDL sdw = (ServiceDescriptionWSDL) serviceDesc;
    	Definition wsdlDefinition = sdw.getWSDLDefinition(); 
    	EndpointDescription[] endpointDescs = serviceDesc.getEndpointDescriptions();
    	if (endpointDescs != null) {
            for (int i=0; i< endpointDescs.length; i++) {
            	EndpointDescriptionImpl ed = (EndpointDescriptionImpl)endpointDescs[i];
            	if(wsdlDefinition == null){
            		//Let see if we can get wsdl definition from endpoint @WebService annotation.
	            	String wsdlLocation = ed.getAnnoWebServiceWSDLLocation();
	            	wsdlDefinition = getWSDLDefinition(wsdlLocation);
            	}
            	//So at this point either we got wsdl definition from ServiceDescription (which means we are running this code
            	//on client side) or we got it from the @WebService annotation (which means we are running this code on server side)
           		if(wsdlDefinition !=null){
           			SchemaReader sr = new SchemaReaderImpl();
           			try{
           				Set<String> pkgSet = sr.readPackagesFromSchema(wsdlDefinition);
           				set.addAll(pkgSet);
           			}catch(SchemaReaderException e){
           				ExceptionFactory.makeWebServiceException(e);
           			}
           			//FIXME: For now lets read packages from Annotation too. We will remove this once we are confident that 
           			//Schema Walk thru works.
           			if(annotationWalking){
           				set.addAll(getPackagesFromAnnotations(serviceDesc));
           			}
           		}
           		//if on client side, there is no wsdl provided by client application. if on server side there is no WSDLLocation on @WebService. 
           		//let read all the required package from SEI Annotation.
           		else{
           			set.addAll(getPackagesFromAnnotations(serviceDesc));
           		}    
              
            }
    	}
    	return set;
    }
    
    /**
     * @param serviceDescription ServiceDescription
     * @return Set of Packages
     */
    public static Set<String> getPackagesFromAnnotations(ServiceDescription serviceDesc) {
        HashSet<String> set = new HashSet<String>();
        EndpointDescription[] endpointDescs = serviceDesc.getEndpointDescriptions();
        
        // Build a set of packages from all of the endpoints
        if (endpointDescs != null) {
            for (int i=0; i< endpointDescs.length; i++) {
                set.addAll(getPackagesFromAnnotations(endpointDescs[i]));
            }
        }
        return set;
    }
    
    /**
     * @param endpointDesc EndpointDescription
     * @return Set of Packages
     */
    public static Set<String> getPackagesFromAnnotations(EndpointDescription endpointDesc) {
        EndpointInterfaceDescription endpointInterfaceDesc = 
            endpointDesc.getEndpointInterfaceDescription();
        if (endpointInterfaceDesc == null) {
            return new HashSet<String>(); 
        } else {
            return getPackagesFromAnnotations(endpointInterfaceDesc);
        }
    }
    
    /**
     * @param endpointInterfaceDescription EndpointInterfaceDescription
     * @return Set of Packages
     */
    public static Set<String> getPackagesFromAnnotations(EndpointInterfaceDescription endpointInterfaceDesc) {
        HashSet<String> set = new HashSet<String>();
        OperationDescription[] opDescs = endpointInterfaceDesc.getOperations();
        
        // Build a set of packages from all of the opertions
        if (opDescs != null) {
            for (int i=0; i< opDescs.length; i++) {
                getPackagesFromAnnotations(opDescs[i], set);
            }
        }
        return set;
    }
    
    /**
     * Update the package set with the packages referenced by this OperationDesc
     * @param opDesc OperationDescription
     * @param set Set<Package> that is updated
     */
    private static void getPackagesFromAnnotations(OperationDescription opDesc, Set<String> set) {
       
       // Walk the parameter information
       ParameterDescription[] parameterDescs = opDesc.getParameterDescriptions();
       if (parameterDescs != null) {
           for (int i=0; i <parameterDescs.length; i++) {
               getPackagesFromAnnotations(parameterDescs[i], set);
           }
       }
       
       // Walk the fault information
       FaultDescription[] faultDescs = opDesc.getFaultDescriptions();
       if (faultDescs != null) {
           for (int i=0; i <faultDescs.length; i++) {
               getPackagesFromAnnotations(faultDescs[i], set);
           }
       }
       
       // Also consider the request and response wrappers
       String pkg = getPackageFromClassName(opDesc.getRequestWrapperClassName());
       if(log.isDebugEnabled()){
    	   log.debug("Package from Request Wrapper annotation = "+ pkg);
       }
       if (pkg != null) {
           set.add(pkg);
       }
       pkg = getPackageFromClassName(opDesc.getResponseWrapperClassName());
       if(log.isDebugEnabled()){
    	   log.debug("Package from Response Wrapper annotation = "+ pkg);
       }
       if (pkg != null) {
           set.add(pkg);
       }
       
       // Finally consider the result type
       Class cls = opDesc.getResultActualType();
       if (cls != null && cls != void.class && cls != Void.class) {
           Package returnTypePkg = cls.getPackage();
           if(log.isDebugEnabled()){
        	   log.debug("Package from Return Type = "+ pkg);
           }
           if (returnTypePkg != null) {
           	   pkg = returnTypePkg.getName();
               set.add(pkg);
           }
       }
    }
    
    /**
     * Update the package set with the packages referenced by this ParameterDescription
     * @param paramDesc ParameterDesc
     * @param set Set<Package> that is updated
     */
    private static void getPackagesFromAnnotations(ParameterDescription paramDesc, Set<String> set) {
       
       // Get the type that defines the actual data.  (this is never a holder )
       Class paramClass = paramDesc.getParameterActualType();
       
       if (paramClass != null) {
           setTypeAndElementPackages(paramClass, paramDesc.getTargetNamespace(), paramDesc.getPartName(), set);
       }
       
    }
    
    /**
     * Update the package set with the packages referenced by this FaultDescription
     * @param faultDesc FaultDescription
     * @param set Set<Package> that is updated
     */
    private static void getPackagesFromAnnotations(FaultDescription faultDesc, Set<String> set) {
      
      Class faultBean = loadClass(faultDesc.getFaultBean());  
      if (faultBean != null) {
          setTypeAndElementPackages(faultBean, faultDesc.getTargetNamespace(), faultDesc.getName(), set);
      }
    }
    
    /**
     * For each data element, we need the package for both the element and its type.
     * @param cls Class representing element, type or both
     * @param namespace of the element
     * @param localPart of the element
     * @param set with both type and element packages set
     */
    private static void setTypeAndElementPackages(Class cls, String namespace, String localPart, Set<String> set) {
        
        // Get the element and type classes
        Class eClass = getElement(cls);
        Class tClass = getType(cls);
        
        // Set the package for the type
        if (tClass != null) {
        	Package typePkg = tClass.getPackage();
        	//For primitive types there is no package
        	String pkg = (typePkg!=null) ? typePkg.getName() : null;
        	
            if (pkg != null) {
                set.add(pkg);
            }
        }
        
        // Set the package for the element
        if (tClass != eClass) {
            if (eClass == null) {
                // A null or empty namespace indicates that the element is
                // unqualified.  This can occur if the parameter is represented as a child element 
                // in doc/lit wrapped.  The package is determined from the wrapper element in such casses.
                if (namespace != null && namespace.length() > 0) {
                    // Use default namespace to package algorithm
                    String pkg = makePackage(namespace);
                    if (pkg != null) {
                        set.add(pkg);
                    }
                }
            } else {
            	Package elementPkg = eClass.getPackage();
                String pkg = (elementPkg!=null) ? elementPkg.getName() : null;
                if (pkg != null) {
                    set.add(pkg);
                }
            }
        }
    }
    
    /**
     * If cls represents an xml element then cls is returned.
     * Otherwise null is returned
     * @param cls Class
     * @return Class or null
     */
    private static Class getElement(Class cls) {
        if (!XMLRootElementUtil.isElementEnabled(cls)) {
            return null;
        } 
        return cls;
    }
    
    private final static Class[] noClass = new Class[] {};
    /** Returns the class that defines the type.
     * @param cls
     * @return
     */
    private static Class getType(Class cls) {
        if (JAXBElement.class.isAssignableFrom(cls)) {
            try {
                Method m = cls.getMethod("getValue", noClass);
                return m.getReturnType();
            } catch (Exception e) {
                // We should never get here
                if (log.isDebugEnabled()) {
                    log.debug("Cannot find JAXBElement.getValue method.");
                }
                return null;
            }
        } else {
            return cls;
        }
    }
    
    /**
     * Default Namespace to Package algorithm
     * @param ns
     * @return
     */
    private static String makePackage(String ns) {
        String pkgName = JavaUtils.getPackageFromNamespace(ns);
        return pkgName;
    }
    
    /**
     * Return the package associated with the class name.  The className may 
     * not be specified (in which case a null Package is returned).
     * if class has unnamed package return ""
     * @param className String (may be null or empty)
     * @return Package or null if problems occur
     */
    private static String getPackageFromClassName(String className) {
        Class clz = loadClass(className);
        String pkg = (clz == null) ? null :(clz.getPackage() == null) ? "" : clz.getPackage().getName();
        return pkg;
    }
    
    /**
     * Loads the class 
     * @param className
     * @return Class (or null if the class cannot be loaded)
     */
    private static Class loadClass(String className) {
        // Don't make this public, its a security exposure
        if (className == null || className.length() == 0) {
            return null;
        }
        try {
            
            return DescriptionUtils.forName(className, true, 
                    DescriptionUtils.getContextClassLoader());
	        //Catch Throwable as ClassLoader can throw an NoClassDefFoundError that
	        //does not extend Exception, so lets catch everything that extends Throwable
            //rather than just Exception.
        } catch (Throwable e) {
            // TODO Should the exception be swallowed ?
            if (log.isDebugEnabled()) {
                log.debug("PackageSetBuilder cannot load the following class:" + className);
            }
        }
        return null;
    }
    
   private static Definition getWSDLDefinition(String wsdlLocation){
	   Definition wsdlDefinition = null;
	   if(wsdlLocation !=null && wsdlLocation.trim().length()>0){
		   try{
	  	       	String baseDir = new File(".").getCanonicalPath();
	  	       	wsdlLocation = new File(baseDir +File.separator+ wsdlLocation).getAbsolutePath();
	  	       	File file = new File(wsdlLocation);
	  	       	URL url = file.toURL();
	  	       	if(log.isDebugEnabled()){
	  	       		log.debug("Reading WSDL from URL:" +url.toString());
	  	       	}
	          	WSDLWrapper wsdlWrapper = new WSDL4JWrapper(url);
	         	wsdlDefinition = wsdlWrapper.getDefinition();
	          	
		   }catch(MalformedURLException e){
	   	   		ExceptionFactory.makeWebServiceException(e);
		   }catch(IOException e){
	    	  ExceptionFactory.makeWebServiceException(e);
		   }catch(WSDLException e){
	    	  ExceptionFactory.makeWebServiceException(e);
		   }
	  }
	  	  
      return wsdlDefinition;
   }
}
