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

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.axis2.jaxws.i18n.Messages;
import org.apache.axis2.jaxws.message.databinding.impl.JAXBBlockImpl;
import org.apache.axis2.jaxws.util.ClassUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * JAXB Utilites to pool JAXBContext and related objects.
 * Currently the JAXBContext is pooled by Class name.  We may change this to 
 * create and pool by package name.
 */
public class JAXBUtils {
	
    private static final Log log = LogFactory.getLog(JAXBUtils.class);
    
	// Create a synchronized map to get the JAXBObject: keys are ClassLoader and Set<String>.
    // TODO We should change the key from Set<String> to an actual package ContextPath
    private static Map<ClassLoader, Map<Set<String>, JAXBContext> > jaxbMap =
			Collections.synchronizedMap(new WeakHashMap<ClassLoader, Map<Set<String>, JAXBContext> >());
	private static JAXBContext genericJAXBContext = null;
	
	private static Map<JAXBContext,Unmarshaller> umap = 
        Collections.synchronizedMap(new WeakHashMap<JAXBContext, Unmarshaller>());
    
    private static Map<JAXBContext,Marshaller> mmap = 
        Collections.synchronizedMap(new WeakHashMap<JAXBContext, Marshaller>());
    
    private static Map<JAXBContext,JAXBIntrospector> imap = 
        Collections.synchronizedMap(new WeakHashMap<JAXBContext, JAXBIntrospector>());
	
    private static boolean ENABLE_ADV_POOLING = false;
	
	/**
	 * Get a JAXBContext for the class
	 * @param contextPackage Set<Package>
	 * @return JAXBContext
	 * @throws JAXBException
	 */
	public static JAXBContext getJAXBContext(Set<String> contextPackages) throws JAXBException {
		// JAXBContexts for the same class can be reused and are supposed to be thread-safe
        
	    // The JAXBContexts are keyed by ClassLoader and the set of Strings
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        
        // Get the innerMap 
        Map<Set<String>, JAXBContext> innerMap = jaxbMap.get(cl);
        if (innerMap == null) {
            synchronized(jaxbMap) {
                innerMap = new WeakHashMap<Set<String>, JAXBContext>();
                jaxbMap.put(cl, Collections.synchronizedMap(innerMap));
            }
        }
        
        if (contextPackages == null) {
            contextPackages = new HashSet<String>();
        }
        
		JAXBContext context = innerMap.get(contextPackages);
		if (context == null) {
            synchronized(innerMap) {
                try{
                    // There are two ways to construct the context.
                    // 1) USE A CONTEXTPATH, which is a string containing
                    //    all of the packages separated by colons.
                    // 2) USE A CLASS[], which is an array of all of the classes
                    //    involved in the marshal/unmarshal.
                    //   
                    // There are pros/cons with both approaches.
                    // USE A CONTEXTPATH: 
                    //    Pros: preferred way of doing this.  
                    //          performant
                    //          most dynamic
                    //    Cons: Each package in context path must have an ObjectFactory
                    //        
                    //
                    // USE CLASS[]:
                    //    Pros: Doesn't require ObjectFactory in each package
                    //    Cons: Hard to set up, must account for JAX-WS classes, etc.
                    //          Does not work if arrays of classes are needed
                    //          slower
                    //
                    //  The following code attempts to build a context path.  It then
                    //  choose one of the two constructions above (prefer USE A CONTEXT_PATH)
                    //
                    
                    if (log.isDebugEnabled()) {
                        log.debug("First try to create JAXBContext with contextPath");
                    }
                    if (useJAXBContextWithContextPath(contextPackages, cl)) {   
                        context = createJAXBContextUsingContextPath(contextPackages, cl);
                    }
                    
                    if (context == null) {
                        // Unsuccessful, USE CLASS[]
                        if (log.isDebugEnabled()) {
                            log.debug("Unsuccessful.. Now attempting to create JAXBContext with Class[]");
                        }
                        Iterator<String> it = contextPackages.iterator();
                        List<Class> fullList = new ArrayList<Class>();
                        while (it.hasNext()) {
                            String pkg = it.next();
                    		fullList.addAll(getAllClassesFromPackage(pkg, cl));
                    	}
                    	Class[] classArray = fullList.toArray(new Class[0]);
                        context = JAXBContext.newInstance(classArray);
                    }
                    if (log.isDebugEnabled()) {
                        log.debug("Successfully created JAXBContext " + context.toString());
                    }
                    innerMap.put(contextPackages, context);	
                }catch(ClassNotFoundException e){
                	throw new JAXBException(e);
                }
                if (log.isDebugEnabled()) {
                    log.debug("JAXBContext [created] for " + contextPackages.toString());
                }
            }
		} else {
            if (log.isDebugEnabled()) {
                log.debug("JAXBContext [from pool] for " + contextPackages.toString());
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
    
    /**
     * @param packages
     * @param cl
     * @return
     */
    private static boolean useJAXBContextWithContextPath(Set<String> packages, ClassLoader cl) {
        
        // Each package must have 
        Iterator<String> it = packages.iterator();
        while(it.hasNext()) {
            String p = it.next();
            if (p.startsWith("java.") ||
                    p.startsWith("javax.")) {
                   ; // Assume that these packages don't need an object factory
            } else {
                try {
                    Class cls = Class.forName(p + ".ObjectFactory",false, cl);
                    if (cl == null) {
                        return false;
                    }
                    // REVIEW: Do we need to check for .package-info
                    //cls = Class.forName(p + ".package-info",false, cl);
                    //if (cl == null) {
                    //    return false;
                    //}
                } catch (Exception e) {
                    if (log.isDebugEnabled()) {
                        log.debug(e);
                    }
                    return false;
                }
                
            }
        }
        return true;
    }
    /**
     * Create a JAXBContext using the contextpath approach
     * @param packages
     * @param cl ClassLoader
     * @return JAXBContext or null if unsuccessful
     */
    private static JAXBContext createJAXBContextUsingContextPath(Set<String> packages, ClassLoader cl) {
        JAXBContext context = null;
        String contextpath = "";
        
        // Iterate through the classes and build the contextpath
        Iterator<String> it = packages.iterator();
        while(it.hasNext()) {
            String p = it.next();
            if (p.startsWith("java.") ||
                p.startsWith("javax.")) {
               ; // Assume that these packages don't have an object factory
            } else {
                if (contextpath.length() != 0) {
                    contextpath +=":";
                }
                contextpath += p;
            }
        }
        try {
            if (log.isDebugEnabled()) {
                log.debug("Attempting to create JAXBContext with contextPath=" + contextpath);
            }
            context = JAXBContext.newInstance(contextpath, cl);
            if (log.isDebugEnabled()) {
                log.debug("  Successfully created JAXBContext:" + context);
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.debug("  Unsuccessful: We will now use an alterative JAXBConstruct construction");
                log.debug("  Reason " + e.toString());
            }
        }
        return context;
    }

    /**
     * This method will return all the Class names needed to construct a JAXBContext
     * @param pkg Package
     * @param ClassLoader cl
     * @return
     * @throws ClassNotFoundException
     */
    private static List<Class> getAllClassesFromPackage(String pkg, ClassLoader cl) throws ClassNotFoundException {
        if (pkg == null) {
            return new ArrayList<Class>();
        }   
        
        // TODO This code does not work if the classes are in a jar !
        
        // This will hold a list of directories matching the pckgname. There may be more than one if a package is split over multiple jars/paths
        String pckgname = pkg;
        ArrayList<File> directories = new ArrayList<File>();
        try {
            String path = pckgname.replace('.', '/');
            // Ask for all resources for the path
            Enumeration<URL> resources = cl.getResources(path);
            while (resources.hasMoreElements()) {
                directories.add(new File(URLDecoder.decode(resources.nextElement().getPath(), "UTF-8")));
            }
        } catch (UnsupportedEncodingException e) {
            if(log.isDebugEnabled()){
                log.debug(pckgname + " does not appear to be a valid package (Unsupported encoding)");
            }
            throw new ClassNotFoundException(Messages.getMessage("ClassUtilsErr2", pckgname));
        } catch (IOException e) {
            if(log.isDebugEnabled()){
                log.debug("IOException was thrown when trying to get all resources for "+ pckgname);
            }
            throw new ClassNotFoundException(Messages.getMessage("ClassUtilsErr3", pckgname));
        }
        
        ArrayList<Class> classes = new ArrayList<Class>();
        // For every directory identified capture all the .class files
        for (File directory : directories) {
            if (log.isDebugEnabled()) {
                log.debug("Adding classes from: " + directory.getName());
            }
            if (directory.exists()) {
                // Get the list of the files contained in the package
                String[] files = directory.list();
                for (String file : files) {
                    // we are only interested in .class files
                    if (file.endsWith(".class")) {
                        // removes the .class extension
                        // TODO Java2 Sec
                        String className = pckgname + '.' + file.substring(0, file.length() - 6);
                        try {
                            Class clazz = Class.forName(className, 
                                    false, 
                                    Thread.currentThread().getContextClassLoader());
                            // Don't add any interfaces or JAXWS specific classes.  
                            // Only classes that represent data and can be marshalled 
                            // by JAXB should be added.
                            if(!clazz.isInterface()
                                    && ClassUtils.getDefaultPublicConstructor(clazz) != null
                                    && !ClassUtils.isJAXWSClass(clazz)){
                                if (log.isDebugEnabled()) {
                                    log.debug("Adding class: " + file);
                                }
                                classes.add(clazz);
                                
                                // REVIEW:
                                // Support of RPC list (and possibly other scenarios) requires that the array classes should also be present.
                                // This is a hack until we can determine how to get this information.
                                
                                // The arrayName and loadable name are different.  Get the loadable
                                // name, load the array class, and add it to our list
                                //className += "[]";
                                //String loadableName = ClassUtils.getLoadableClassName(className);
                                
                                //Class aClazz = Class.forName(loadableName, false, Thread.currentThread().getContextClassLoader());
                            }
                        } catch (Exception e) {
                            if (log.isDebugEnabled()) {
                                log.debug("Tried to load class " + className + " while constructing a JAXBContext.  This class will be skipped.  Processing Continues." );
                                log.debug("  The reason that class could not be loaded:" + e.toString());
                            }
                            e.printStackTrace();
                        }
                        
                    }
                }
                
                // REVIEW Load and add the common array classes
                // Support of RPC list (and possibly other scenarios) requires that the array classes should also be present.
                // This is a hack until we can determine how to get this information.
                //addCommonArrayClasses(classes);
            }
        }
        return classes;
    }
    
    private static  String[] commonArrayClasses = new String[] { 
        // primitives
        "boolean[]",
        "byte[]",
        "char[][]",
        "double[]",
        "float[]",
        "int[]",
        "long[]",
        "short[]",
        "java.lang.String[]",
        // Others
        "java.lang.Object[]",
        "java.awt.Image[]",
        "java.math.BigDecimal[]",
        "java.math.BigInteger[]",
        "java.util.Calendar[]",
        "javax.xml.namespace.QName[]" };
    
    private static void addCommonArrayClasses(List<Class> list) {
        // Add common primitives arrays (necessary for RPC list type support)
        ClassLoader cl = Thread.currentThread().getContextClassLoader();

       
        for (int i=0; i<commonArrayClasses.length; i++) {
            String className = commonArrayClasses[i];
            try {
                // Load and add the class
                Class cls = Class.forName(ClassUtils.getLoadableClassName(className), false, cl);
                list.add(cls);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.debug("Tried to load class " + className + " while constructing a JAXBContext.  This class will be skipped.  Processing Continues." );
                    log.debug("  The reason that class could not be loaded:" + e.toString());
                }
                e.printStackTrace();
            }
        }
    }
    
    
    
}
