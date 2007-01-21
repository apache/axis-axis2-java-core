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

package org.apache.axis2.jaxws.marshaller.impl.alt;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.ws.WebServiceException;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.description.FaultDescription;
import org.apache.axis2.jaxws.util.ClassUtils;
import org.apache.axis2.jaxws.wrapper.JAXBWrapperTool;
import org.apache.axis2.jaxws.wrapper.PropertyInfo;
import org.apache.axis2.jaxws.wrapper.impl.JAXBWrapperToolImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * The JAX-WS Specification (chapter 3.7) indicates that JAX-WS
 * supports exceptions that do not match the normal pattern (the normal
 * pattern is defined in chapter 2.5.  
 * 
 * These non-matching exceptions are the result of running WSGen
 * on a pre-existing webservice.  I am going to use the term, legacy exception,
 * to describe these non-matching exceptions.
 * 
 * The JAX-WS marshaller (server) must marshal a legacy exception thrown from
 * the web service impl.  The marshalling/mapping algorithm is defined in chapter 3.7.
 * 
 * On the client, the JAX-WS engine will need to demarshal exceptions.  However
 * the specification assumes that the client is always created via wsimport; therefore
 * the assumption is that all exceptions on the client are compliant (never legacy exceptions).
 * I have included some code here in case we have to deal with legacy exceptions on the client...this is non-spec.
 * 
 *
 */
/**
 * @author scheu
 *
 */
class LegacyExceptionUtil {

    private static Log log = LogFactory.getLog(LegacyExceptionUtil.class);
    
    private static Set<String> ignore = new HashSet<String>();
    static {
        // Per Chap 3.7 rule 3, ignore these properties on the exception
        ignore.add("localizedMessage");
        ignore.add("stackTrace");
        ignore.add("class");
        ignore.add("cause");
    }
    
    /**
     * Static class.  Constructor is intentionally private
     */
    private LegacyExceptionUtil() {}
    
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
    
    /**
     * Create a FaultBean populated with the data from the Exception t
     * The algorithm used to populate the FaultBean is described in 
     * JAX-WS 3.7
     * @param t
     * @param fd
     * @return faultBean
     */
    static Object createFaultBean(Throwable t, FaultDescription fd) throws WebServiceException {
        
        Object faultBean = null;
        try {
            // Get the fault bean name from the fault description.
            // REVIEW The default name should be:
            //      Package = <SEI package> or <SEI package>.jaxws
            //      Name = <exception name> + Bean
            String faultBeanName = fd.getFaultBean();
            
            // TODO Add check that faultBeanName is correct
            if (log.isDebugEnabled()) {
                log.debug("Legacy Exception Bean name is = " + faultBeanName);
            }
            
            // Load the FaultBean Class
            Class faultBeanClass = MethodMarshallerUtils.loadClass(faultBeanName);
            
            // We need to assign the legacy exception data to the java bean class.
            // We will use the JAXBWrapperTool.wrap utility to do this.
            
            // Get the map of child objects
            Map<String, Object> childObjects = getChildObjectsMap(t);
            
            List<String> childNames = new ArrayList<String>(childObjects.keySet());
            
            if (log.isErrorEnabled()) {
                log.debug("List of properties on the Legacy Exception is " + childNames);
            }
            // Use the wrapper tool to get the child objects.
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            faultBean = wrapperTool.wrap(faultBeanClass, childNames, childObjects);
            if (log.isErrorEnabled()) {
                log.debug("Completed creation of the fault bean.");
            }
            
        } catch (Exception e) {
            throw ExceptionFactory.makeWebServiceException(e);
        }
        return faultBean;
    }
    
    /**
     * Create an Exception using the data in the JAXB object.  
     * The specification is silent on this issue.
     * @param exceptionClass
     * @param jaxb
     * @return
     */
    static Exception createFaultException(Class exceptionClass, Object jaxb) {
        Exception e = null;
        try {
            if (log.isErrorEnabled()) {
                log.debug("Create Legacy Exception for " + exceptionClass.getName());
            }
            // Get the properties names from the exception class
            Map<String, PropertyInfo> piMap = getPropertyInfoMap(exceptionClass);
            
            // Now get a list of PropertyInfo objects that map to the jaxb bean properties
            Iterator<Entry<String, PropertyInfo>> it = piMap.entrySet().iterator();
            List<PropertyInfo> piList= new ArrayList<PropertyInfo>();
            while (it.hasNext()) {
                Entry<String, PropertyInfo> entry = it.next();
                String propertyName = entry.getKey();
                // Some propertyNames should be ignored.
                if (!ignore.contains(propertyName)) {
                    piList.add(entry.getValue());
                }
            }
            
            // Find a matching constructor
            List<String>  childNames = new ArrayList<String>();
            if (log.isErrorEnabled()) {
                log.debug("List of childNames on legacy exception is " + childNames);
            }
            Constructor constructor = findConstructor(exceptionClass, piList, childNames);
            
            if (log.isErrorEnabled()) {
                log.debug("The constructor used to create the exception is " + constructor);
            }
            // Use the wrapper tool to unwrap the jaxb object
            JAXBWrapperTool wrapperTool = new JAXBWrapperToolImpl();
            Object[] childObjects = wrapperTool.unWrap(jaxb, childNames);
            
            if (log.isErrorEnabled()) {
                log.debug("Calling newInstance on the constructor " + constructor);
            }
            e = (Exception) constructor.newInstance(childObjects);
        } catch (Exception ex) {
            throw ExceptionFactory.makeWebServiceException(ex);
        }
        return e;
    }
    
    /**
     * Find a construcor that matches this set of properties
     * @param cls
     * @param piList
     * @param childNames returned in the order that they occur in the constructor
     * @return Constructor or null
     */
    private static Constructor findConstructor(Class cls, List<PropertyInfo> piList, List<String> childNames) {
        Constructor[] constructors = cls.getConstructors();
        Constructor constructor = null;
        if (constructors != null) {
            for (int i=0; i<constructors.length && constructor == null; i++) {
                Constructor tryConstructor = constructors[i];
                if (tryConstructor.getParameterTypes().length  == piList.size()) {
                    // Try and find the best match using the property types
                    List<PropertyInfo> list = new ArrayList<PropertyInfo>(piList);
                    List<PropertyInfo> args = new ArrayList<PropertyInfo>();
                    
                    Class[] parms = tryConstructor.getParameterTypes();
                    boolean valid= true;
                    
                    // Assume the message is first in the constructor
                    for (int j=0; j<list.size(); j++) {
                        if ("message".equals(list.get(j).getPropertyName())) {
                            args.add(list.remove(j));
                        }
                    }
                    if (args.size() != 1 ||
                        !parms[0].isAssignableFrom(args.get(0).getPropertyType())) {
                        valid = false;
                    }
                    
                    // Now process the rest of the args
                    for (int j=1; j<parms.length && valid; j++) {
                        // Find a compatible argument
                        Class parm = parms[j];
                        boolean found = false;
                        for (int k=0; k<list.size() && !found; k++) {
                            Class arg = list.get(k).getPropertyType();
                            if (parm.isAssignableFrom(arg)) {
                                found = true;
                                args.add(list.remove(k));
                            }
                        }
                        // If no compatible argument then this constructor is not valid
                        if (!found) {
                            valid = false;   
                        }
                    }
                    // A constructor is found
                    if (valid) {
                        constructor = tryConstructor;
                        for (int index = 0; index<args.size(); index++) {
                            childNames.add(args.get(index).getPropertyName());
                        }
                    }
                }
            }
        }
        
        return constructor;
    }
    
    
    /**
     * Get the child objects map that is required by the wrapper tool.
     * @param t Exception
     * @return Map with key is bean property names and values are objects from the Exception
     * @throws IntrospectionException
     */
    private static Map<String, Object> getChildObjectsMap(Throwable t) 
        throws IntrospectionException, InvocationTargetException, IllegalAccessException {
        
        Map<String, PropertyInfo> piMap = getPropertyInfoMap(t.getClass());
        
        Map<String, Object> coMap = new HashMap<String, Object>();
        
        Iterator<Entry<String, PropertyInfo>> it = piMap.entrySet().iterator();
        while (it.hasNext()) {
            Entry<String, PropertyInfo> entry = it.next();
            String propertyName = entry.getKey();
            // Some propertyNames should be ignored.
            if (!ignore.contains(propertyName)) {
                Object value = entry.getValue().get(t);
                coMap.put(propertyName, value);
            }
        }
        return coMap;
    }
    
    /**
     * Get a Map<String, PropertyInfo> for the specified exception
     * @param t
     * @return Map<String, PropertyInfo>
     */
    private static Map<String, PropertyInfo> getPropertyInfoMap(Class cls) throws IntrospectionException {
        // TODO Performance Alert
        // Get a PropertyInfo Map.  Perhaps this should be cached on the FaultDesc for performance
        PropertyDescriptor[] pds = Introspector.getBeanInfo(cls).getPropertyDescriptors();
        
        Map<String, PropertyInfo> piMap = new HashMap<String, PropertyInfo>();
        if (pds != null) {
            for (int i=0; i< pds.length; i++) {
                PropertyInfo pi = new PropertyInfo(pds[i]);
                piMap.put(pds[i].getName(), pi);
            }
        }
        return piMap;
    }
    
    
}
