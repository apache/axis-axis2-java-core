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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBElement;

import org.apache.axis2.java.security.AccessController;
import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.description.EndpointInterfaceDescription;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ParameterDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.description.ServiceDescriptionWSDL;
import org.apache.axis2.jaxws.runtime.description.marshal.AnnotationDesc;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.axis2.jaxws.utility.PropertyDescriptorPlus;
import org.apache.axis2.jaxws.utility.XMLRootElementUtil;
import org.apache.axis2.jaxws.wsdl.SchemaReader;
import org.apache.axis2.jaxws.wsdl.SchemaReaderException;
import org.apache.axis2.jaxws.wsdl.impl.SchemaReaderImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Walks the ServiceDescription and its child *Description classes
 * to find all of the types.  An AnnotationDesc is built for each of the types
 */
public class PropertyDescriptorMapBuilder {
    
    private static Log log = LogFactory.getLog(PropertyDescriptorMapBuilder.class);
    

    /**
     * This is a static utility class.  The constructor is intentionally private
     */
    private PropertyDescriptorMapBuilder() {
    }
    
   
    
    /**
     * @param serviceDescription ServiceDescription
     * @param ap ArtifactProcessor which found the artifact classes
     * @return PropertyDescriptor Map
     */
    public static Map<Class,Map<String, PropertyDescriptorPlus>> getPropertyDescMaps(
            ServiceDescription serviceDesc,
            ArtifactProcessor ap) {
        Map<Class,Map<String, PropertyDescriptorPlus>> map = new HashMap<Class,Map<String, PropertyDescriptorPlus>>();
        EndpointDescription[] endpointDescs = serviceDesc.getEndpointDescriptions();
        
        // Build a set of packages from all of the endpoints
        if (endpointDescs != null) {
            for (int i=0; i< endpointDescs.length; i++) {
                getPropertyDescMaps(endpointDescs[i], ap, map);
            }
        }
        return map;
    }
    
    
    /**
     * @param endpointDesc
     * @param ap ArtifactProcessor which found the artifact classes
     * @param map
     */
    private static void getPropertyDescMaps(EndpointDescription endpointDesc, 
            ArtifactProcessor ap,
            Map<Class,Map<String, PropertyDescriptorPlus>> map) {
        EndpointInterfaceDescription endpointInterfaceDesc = 
            endpointDesc.getEndpointInterfaceDescription();
        if (endpointInterfaceDesc != null) {
            getPropertyDescMaps(endpointInterfaceDesc, ap, map);
        }
    }
    
   
    /**
     * @param endpointInterfaceDesc
     * @param ap ArtifactProcessor which found the artifact classes
     * @param map
     */
    private static void getPropertyDescMaps(EndpointInterfaceDescription endpointInterfaceDesc, 
            ArtifactProcessor ap, 
            Map<Class,Map<String, PropertyDescriptorPlus>> map) {
        OperationDescription[] opDescs = endpointInterfaceDesc.getOperations();
        
        // Build a set of packages from all of the opertions
        if (opDescs != null) {
            for (int i=0; i< opDescs.length; i++) {
                getPropertyDescMaps(opDescs[i], ap, map);
            }
        }
    }
    
    
    
    /**
     * @param opDesc
     * @param map
     */
    private static void getPropertyDescMaps(OperationDescription opDesc, 
            ArtifactProcessor ap,
            Map<Class,Map<String, PropertyDescriptorPlus>> map) {
       
       
       // Walk the fault information
       FaultDescription[] faultDescs = opDesc.getFaultDescriptions();
       if (faultDescs != null) {
           for (int i=0; i <faultDescs.length; i++) {
               getPropertyDescMaps(faultDescs[i], map);
           }
       }
       
       // Also consider the request and response wrappers
       String wrapperName = ap.getRequestWrapperMap().get(opDesc);
       if (wrapperName != null) {
           addPropertyDesc(wrapperName, map);
       }
       wrapperName = ap.getResponseWrapperMap().get(opDesc);
       if (wrapperName != null) {
           addPropertyDesc(wrapperName, map);
       }
    }
    
    /**
     * @param opDesc
     * @param map
     */
    private static void getPropertyDescMaps(FaultDescription faultDesc, Map<Class,Map<String, PropertyDescriptorPlus>> map) {
        // TODO The property descriptors for legacy exceptions and the corresponding fault beans could be cached at this point.
        String faultDescExceptionName = faultDesc.getExceptionClassName();
        Class faultDescException = loadClass(faultDescExceptionName);
        if (faultDescException != null && isLegacyException(faultDescException)) {
            
            // For legacy exceptions, the fault bean is a "wrapper class" that has the same properties
            // as the exception.  To marshal an exception:
            //    1) the values are obtained from the legacy exception (using the exception's property desc map)
            //    2) the values are placed on the fault bean (using the bean's property desc map)
            //    3) the bean is marshalled.
            // 
            // Unmarshalling is outside the spec, but we do provide proprietary support.
            // The algorithm is basically
            //    1) unmarshall into the fault bean
            //    2) the values on the fault bean are obtained (using the bean's property desc map)
            //    3) use heuristics to find a matching constructor on the exception class.  If found it is instantiated.
            
            // To accomplish the above marshalling and unmarshalling we need the property descriptor maps
            // for the exception and the bean.
            String faultDescBeanName = faultDesc.getFaultBean();
            Class faultDescBean = loadClass(faultDescBeanName);
            if (faultDescBean != null) {
                addPropertyDesc(faultDescBeanName, map);
                addPropertyDesc(faultDescExceptionName, map);
            }
            
        }
    }
    
    private static void addPropertyDesc(String clsName, Map<Class,Map<String, PropertyDescriptorPlus>> map) {
        
        Class cls = loadClass(clsName);
        if (cls == null) {
            return;
        }
        
        if (map.get(cls) == null) {
            try {
                Map<String, PropertyDescriptorPlus> pdMap = XMLRootElementUtil.createPropertyDescriptorMap(cls);
                map.put(cls, pdMap);
            } catch (Throwable t) {
                ExceptionFactory.makeWebServiceException(t);
            }
        }
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
            
            return forName(className, true, 
                   getContextClassLoader());
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
    * A compliant exception has a @WebFault annotation and a getFaultInfo method.
    * Legacy exceptions do not.
    * @param cls
    * @return true if legacy exception
    * REVIEW perhaps this detection should be in FaultDescription
    */
   static boolean isLegacyException(Class cls) {
       boolean legacyException = false;
       
       try {
           Method getFaultInfo = cls.getMethod("getFaultInfo", null);
       } catch (Exception e) {
           // Failure indicates that this is not a legacy exception
           legacyException = true;
       }
       if (legacyException) {
           if (log.isDebugEnabled()) {
               log.debug("Detected Legacy Exception = " + cls);
           }
       }
       return legacyException;
   }
}
