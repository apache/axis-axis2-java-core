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
package org.apache.axis2.jaxws.message.databinding;

import org.apache.axis2.java.security.AccessController;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.namespace.QName;

import java.awt.Image;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

/**
 * This class provides a utility method, newInstance, which
 * builds a valid JAXBContext from a series of classes.
 */
public class JAXBContextFromClasses {

    private static final Log log = LogFactory.getLog(JAXBContextFromClasses.class);
    /**
     * Static utility class.  Constructor is intentionally private
     */
    private JAXBContextFromClasses() {
    }

    /**
     * Create a JAXBContext from the given class array and class loader.
     * If errors occur, then the JAXBContext is created from the 
     * minimal set of valid classes.
     * 
     * Note: Sometimes users will intermingle JAXB classes and other
     * non-JAXB utility classes.  This is not a good practice, but does
     * happen.  The purpose of this method is to try and build a valid
     * JAXBContext from only the 'valid' classes. 
     * 
     * @param classArray
     * @param cl
     * @return JAXBContext
     * @throws JAXBException
     */
    public static JAXBContext newInstance(Class[] classArray, 
                                          ClassLoader cl,
                                          Map<String, ?> properties) 
        throws JAXBException {
        JAXBContext jaxbContext = null;
        try {
            if (log.isDebugEnabled()) {
                if (classArray == null || classArray.length == 0) {
                    log.debug("Try to construct JAXBContext with 0 input classes.");
                } else {
                    log.debug("Try to construct JAXBContext with " + classArray.length +
                            " input classes.");
                }
            }
            jaxbContext = _newInstance(classArray, cl, properties);

            if (log.isDebugEnabled()) {
                log.debug("Successfully constructed JAXBContext " + jaxbContext);
            }
        } catch (Throwable t) {
            // Try finding the best set of classes
            ArrayList<Class> original = new ArrayList<Class>();
            for (int i=0; i < classArray.length; i++) {
                original.add(classArray[i]);
            }
            ArrayList<Class> best = new ArrayList<Class>();
            jaxbContext = findBestSet(original, cl, best, properties);
            
        }

        return jaxbContext;
    }
    
    /**
     * Utility method that creates a JAXBContext from the 
     * class[] and ClassLoader.
     * 
     * @param classArray
     * @param cl
     * @return JAXBContext
     * @throws Throwable
     */
    private static JAXBContext _newInstance(final Class[] classArray, 
                                            final ClassLoader cl,
                                            final Map<String, ?> properties) 
        throws Throwable {
        JAXBContext jaxbContext;
        try {
            jaxbContext = (JAXBContext)AccessController.doPrivileged(
               new PrivilegedExceptionAction() {
                   public Object run() throws JAXBException {
                       // Unlike the JAXBContext.newInstance(Class[]) method
                       // does now accept a classloader.  To workaround this
                       // issue, the classloader is temporarily changed to cl
                       Thread currentThread = Thread.currentThread();
                       ClassLoader savedClassLoader = currentThread.getContextClassLoader();
                       try {
                           currentThread.setContextClassLoader(cl);
                           return JAXBContext.newInstance(classArray, properties);
                       } finally {
                           currentThread.setContextClassLoader(savedClassLoader);
                       }
                   }
               }
            );
        } catch (PrivilegedActionException e) {
            throw ((PrivilegedActionException) e).getException();
        } catch (Throwable t) {
            throw t;
        }
        return jaxbContext;
    }
    
    /**
     * Utility class that quickly divides a list of classes into two categories.
     * The primary category classes contain JAXB annotations.
     * The secondary category classes do not contain JAXB annotations
     * @param original
     * @param primary
     * @param secondary
     */
    static void separate(List<Class> original, List<Class> primary, List<Class> secondary) {
        for (int i=0; i<original.size(); i++) {
            Class cls = original.get(i);
            if (commonArrayClasses.contains(cls)) {
                if (log.isDebugEnabled()) {
                    log.debug("This looks like a JAXB common class. Adding it to primary list:" + 
                                       cls.getName());
                }
                primary.add(cls);
            } else if (getAnnotation(cls, XmlType.class) != null ||
                    getAnnotation(cls, XmlRootElement.class) != null) {
                if (log.isDebugEnabled()) {
                    log.debug("This looks like a JAXB class. Adding it to primary list:" + 
                                       cls.getName());
                }
                primary.add(cls);  // This looks like a JAXB class...add it
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("This may not be a JAXB class. Adding it to secondary list:" + 
                                       cls.getName());
                }
                secondary.add(cls);  // This looks like it might be something else...
            }            
        }
    }
    
    /**
     * Given a list of classes, this method determines the best minimal set
     * of classes and returns the JAXBContext for this minimal set.
     * @param original List<Class>
     * @param cl ClassLoader
     * @param ListMClass> is populated with the minimal, best set of classes
     * @return JAXBContext
     */
    static JAXBContext findBestSet(List<Class> original,
                                   ClassLoader cl,
                                   List<Class> best, 
                                   Map<String, ?> properties) {
        
        if (log.isDebugEnabled()) {
            log.debug("Could not construct JAXBContext with the initial list.");
            log.debug("Now trying to construct JAXBContext with only the valid classes in the list");
        }
        JAXBContext jc = null;
        Class[] clsArray = new Class[0];
            
        // Divide the list into the classes that have JAXB annotations (primary)
        // and those that do not (secondary)
        ArrayList<Class> primary = new ArrayList<Class> ();
        ArrayList<Class> secondary = new ArrayList<Class> ();
        separate(original, primary, secondary);
        
        // Prime the pump
        // Build a JAXBContext with the primary classes
        best.addAll(primary);
        if (best.size() > 0) {
            try {
                jc = _newInstance(best.toArray(clsArray), cl, properties);
            } catch (Throwable t) {
                if (log.isDebugEnabled()) {
                    log.debug("The JAXBContext creation failed with the primary list");
                    log.debug("Will try a more brute force algorithm");
                    log.debug("  The reason is " + t);
                }
                // Add all of the primary classes to the secondary list so 
                // that we can walk them one by one.
                secondary.addAll(primary);
                best.clear(); // Clear out the best list
            }
        }
        
        // Now add secondary classes one at a time.
        // If the JAXBContext creation is successful, add the class
        // to the best list.  Otherwise continue.
        // 
        // @REVIEW One optimization is to do a toString() on the JAXBContext
        // and check for the presense of the secondary class.  This would
        // save time.  However, this also assumes that the toString() 
        // of JAXBContext does not change.
        for (int i = 0; i<secondary.size(); i++) {
            Class cls = secondary.get(i);
            best.add(cls);
            try {
                jc = _newInstance(best.toArray(clsArray), cl, properties);
            } catch (Throwable t) {
                if (log.isDebugEnabled()) {
                    log.debug("The following class is not a JAXB class: " +
                              cls.getCanonicalName());
                    log.debug("  JAXBContext creation continues without this class.");
                    log.debug("  The reason is " + t);
                }
                best.remove(cls);
                
                // @REVIEW
                // We could save the exceptions (perhaps in a list) and
                // return the exceptions.  Then if problems occur later
                // (i.e. do to missing exceptions) then we could throw
                // this exception.
            }
        }
        
        return jc;
  
    }
    
    /**
     * Get an annotation.  This is wrappered to avoid a Java2Security violation.
     * @param cls Class that contains annotation 
     * @param annotation Class of requrested Annotation
     * @return annotation or null
     */
    private static Annotation getAnnotation(final AnnotatedElement element, final Class annotation) {
        return (Annotation) AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                return element.getAnnotation(annotation);
            }
        });
    }
    
    private static List<Class> commonArrayClasses = new ArrayList<Class>();
    static {
        commonArrayClasses.add(boolean[].class);
        commonArrayClasses.add(byte[].class);
        commonArrayClasses.add(char[].class);
        commonArrayClasses.add(double[].class);
        commonArrayClasses.add(float[].class);
        commonArrayClasses.add(int[].class);
        commonArrayClasses.add(long[].class);
        commonArrayClasses.add(short[].class);
        commonArrayClasses.add(String[].class);
        commonArrayClasses.add(Object[].class);
        commonArrayClasses.add(Image[].class);
        commonArrayClasses.add(BigDecimal[].class);
        commonArrayClasses.add(BigInteger[].class);
        commonArrayClasses.add(Calendar[].class);
        commonArrayClasses.add(QName[].class);
    }
}
