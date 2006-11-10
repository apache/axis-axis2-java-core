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
package org.apache.axis2.jaxws.message.databinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.axis2.jaxws.marshaller.ClassUtils;
import org.apache.axis2.jaxws.message.databinding.impl.JAXBBlockImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JAXB Utilites to pool JAXBContext and related objects.
 * Currently the JAXBContext is pooled by Class name.  We may change this to 
 * create and pool by package name.
 */
public class JAXBUtils {
	
    private static final Log log = LogFactory.getLog(JAXBUtils.class);
    
	// Create a synchronized weak hashmap key=class name, value= JAXBContext
	private static Map<String, JAXBContext> map =
			Collections.synchronizedMap(new WeakHashMap<String, JAXBContext>());
	private static JAXBContext genericJAXBContext = null;
	
	private static Map<JAXBContext,Unmarshaller> umap = 
        Collections.synchronizedMap(new WeakHashMap<JAXBContext, Unmarshaller>());
    
    private static Map<JAXBContext,Marshaller> mmap = 
        Collections.synchronizedMap(new WeakHashMap<JAXBContext, Marshaller>());
    
    private static Map<JAXBContext,JAXBIntrospector> imap = 
        Collections.synchronizedMap(new WeakHashMap<JAXBContext, JAXBIntrospector>());
	
    private static boolean ENABLE_ADV_POOLING = false;
    
    /**
	 * Get a generic JAXBContext (that can be used for primitives)
	 * @throws JAXBException
	 */
	public static JAXBContext getGenericJAXBContext() throws JAXBException {
		
		// JAXBContexts can be reused and are supposed to be thread-safe
		if (genericJAXBContext == null) {
            if (log.isDebugEnabled()) {
                log.debug("Generic JAXBContext [created]");
            }
            genericJAXBContext = JAXBContext.newInstance(int.class);
            return genericJAXBContext;
		} else {
            if (log.isDebugEnabled()) {
                log.debug("Generic JAXBContext [from pool]");
            }
        }
		return genericJAXBContext;
	}
	
	/**
	 * Get a JAXBContext for the class
	 * @param cls Class
	 * @return JAXBContext
	 * @throws JAXBException
	 */
	public static JAXBContext getJAXBContext(Class[] classes) throws JAXBException {
		// JAXBContexts for the same class can be reused and are supposed to be thread-safe
		// TODO Can we cache by package name ?
		Class cls = classes[0];
        if (cls.isPrimitive()) {
            return getGenericJAXBContext();
        }
		JAXBContext context = map.get(cls.getName());
		if (context == null) {
            synchronized(map) {
                try{
                	// TODO
                	// For now we are generating a list of all of the classes in each
                	// of the referenced packages.  We have plans to use a contextPath instead
                	List<Class> fullList = new ArrayList<Class>();
                	for (int i=0; i<classes.length; i++) {
                		Package pkg = classes[i].getPackage();
                		if (log.isDebugEnabled()) {
                			log.debug("Package for " + classes[i].getName() + " "+pkg.getName());
                		}
                		if (log.isDebugEnabled()) {
                			log.debug("Attempting to read all classes from package " + pkg.getName());
                		}
                		fullList.addAll(ClassUtils.getAllClassesFromPackage(pkg.getName()));
                	}
                	Class[] classArray = fullList.toArray(new Class[0]);
                	context = JAXBContext.newInstance(classArray);
                    map.put(cls.getName(), context);	
                }catch(ClassNotFoundException e){
                	throw new JAXBException(e);
                }
                if (log.isDebugEnabled()) {
                    log.debug("JAXBContext [created] for" + cls.getName());
                }
            }
		} else {
            if (log.isDebugEnabled()) {
                log.debug("JAXBContext [from pool] for" + cls.getName());
            }
        }
		return context;
	}
	
	/**
	 * Get the unmarshaller.  You must call releaseUnmarshaller to put it back into the pool
	 * @param context JAXBContext
	 * @return Unmarshaller
	 * @throws JAXBException
	 */
	public static Unmarshaller getJAXBUnmarshaller(JAXBContext context) throws JAXBException {
		if (!ENABLE_ADV_POOLING) {
            if (log.isDebugEnabled()) {
                log.debug("Unmarshaller created [no pooling]");
            }
		    return context.createUnmarshaller();
        } 
        Unmarshaller u = umap.get(context);
        if (u == null) {
            if (log.isDebugEnabled()) {
                log.debug("Unmarshaller created [not in pool]");
            }
            u = context.createUnmarshaller();
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Unmarshaller obtained [from  pool]");
            }
        }
        return u;
	}
	
	/**
	 * Release Unmarshaller
	 * Do not call this method if an exception occurred while using the Unmarshaller.
	 * We object my be in an invalid state.
	 * @param context JAXBContext
	 * @param unmarshaller Unmarshaller
	 */
	public static void releaseJAXBUnmarshaller(JAXBContext context, Unmarshaller unmarshaller) {
        if (log.isDebugEnabled()) {
            log.debug("Unmarshaller placed back into pool");
        }
		umap.put(context, unmarshaller);
	}
	
	/**
	 * Get JAXBMarshaller
	 * @param context JAXBContext
	 * @return Marshaller
	 * @throws JAXBException
	 */
	public static Marshaller getJAXBMarshaller(JAXBContext context) throws JAXBException {
        Marshaller m = null;
        if (!ENABLE_ADV_POOLING) {
            if (log.isDebugEnabled()) {
                log.debug("Marshaller created [no pooling]");
            }
            m = context.createMarshaller();
        } else { 
            m = mmap.get(context);
            if (m == null) {
                if (log.isDebugEnabled()) {
                    log.debug("Marshaller created [not in pool]");
                }
                m = context.createMarshaller();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("Marshaller obtained [from  pool]");
                }
            }
        }
		m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE); // No PIs
		return m;
	}
	
	/**
	 * releaseJAXBMarshalller
	 * Do not call this method if an exception occurred while using the Marshaller.
	 * We object my be in an invalid state.
	 * @param context JAXBContext
	 * @param marshaller Marshaller
	 */
	public static void releaseJAXBMarshaller(JAXBContext context, Marshaller marshaller) {
        if (log.isDebugEnabled()) {
            log.debug("Marshaller placed back into pool");
        }
        mmap.put(context, marshaller);
	}
	
	/**
	 * get JAXB Introspector
	 * @param context JAXBContext
	 * @return JAXBIntrospector
	 * @throws JAXBException
	 */
	public static JAXBIntrospector getJAXBIntrospector(JAXBContext context) throws JAXBException {
        JAXBIntrospector i = null;
        if (!ENABLE_ADV_POOLING) {
            if (log.isDebugEnabled()) {
                log.debug("JAXBIntrospector created [no pooling]");
            }
            i = context.createJAXBIntrospector();
        } else { 
            i = imap.get(context);
            if (i == null) {
                if (log.isDebugEnabled()) {
                    log.debug("JAXBIntrospector created [not in pool]");
                }
                i = context.createJAXBIntrospector();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("JAXBIntrospector obtained [from  pool]");
                }
            }
        }
        return i;
	}
	
	/**
	 * Release JAXBIntrospector
	 * Do not call this method if an exception occurred while using the JAXBIntrospector.
	 * We object my be in an invalid state.
	 * @param context JAXBContext
	 * @param introspector JAXBIntrospector
	 */
	public static void releaseJAXBIntrospector(JAXBContext context, JAXBIntrospector introspector) {
        if (log.isDebugEnabled()) {
            log.debug("JAXBIntrospector placed back into pool");
        }
        imap.put(context, introspector);
	}
}
