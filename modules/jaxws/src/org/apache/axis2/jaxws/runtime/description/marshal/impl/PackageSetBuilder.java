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

package org.apache.axis2.jaxws.runtime.description.marshal.impl;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
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
import org.apache.axis2.jaxws.runtime.description.marshal.FaultBeanDesc;
import org.apache.axis2.jaxws.runtime.description.marshal.MarshalServiceRuntimeDescription;
import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.util.WSDLWrapper;
import org.apache.axis2.jaxws.utility.ClassUtils;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.axis2.jaxws.wsdl.SchemaReader;
import org.apache.axis2.jaxws.wsdl.SchemaReaderException;
import org.apache.axis2.jaxws.wsdl.impl.SchemaReaderImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.jws.WebService;
import javax.wsdl.Definition;
import javax.wsdl.WSDLException;
import javax.xml.bind.JAXBElement;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 * In order to marshal or unmarshal the user data, we need to know the set of packages involved.
 * The set of packages is used to construct an appropriate JAXBContext object during the
 * marshalling/unmarshalling.
 * <p/>
 * There are two ways to get this data.
 * <p/>
 * Schema Walk (preferred):  Get the list of packages by walking the schemas that are referenced by
 * the wsdl (or generated wsdl).  Each schema represents a different package.  The package is
 * obtained using the jaxb customization or JAXB default ns<->package rule.
 * <p/>
 * Annotation Walk(secondary) : Walk the list of Endpoints, Operations, Parameters, etc. and build a
 * list of packages by looking at the classes involved.
 * <p/>
 * The Schema Walk is faster and more complete, but relies on the presence of the schema or wsdl.
 * <p/>
 * The Annotation Walk is slower and is not complete.  For example, the annotation walk may not
 * discover the packages for derived types that are defined in a different schema than the formal
 * parameter types.
 */
public class PackageSetBuilder {

    private static Log log = LogFactory.getLog(PackageSetBuilder.class);

    /** This is a static utility class.  The constructor is intentionally private */
    private PackageSetBuilder() {
    }

    /**
     * Walks the schemas of the serviceDesc's wsdl (or generated wsdl) to determine the list of
     * packages. This is the preferred algorithm for discovering the package set.
     *
     * @param serviceDesc ServiceDescription
     * @return Set of Packages
     */
    public static TreeSet<String> getPackagesFromSchema(ServiceDescription serviceDesc) {
    	
    	if (log.isDebugEnabled()) {
    		log.debug("start getPackagesFromSchema");
    		log.debug("ServiceDescription = " + serviceDesc.toString());
    	}

        TreeSet<String> set = new TreeSet<String>();
        //If we are on client side we will get wsdl definition from ServiceDescription. If we are on server side we will have to
        //read wsdlLocation from @WebService Annotation.
        ServiceDescriptionWSDL sdw = (ServiceDescriptionWSDL)serviceDesc;
        Definition wsdlDefinition = sdw.getWSDLDefinition();
        Collection<EndpointDescription> endpointDescs = serviceDesc.getEndpointDescriptions_AsCollection();
        if (endpointDescs != null) {
            for (EndpointDescription ed:endpointDescs) {

                if (wsdlDefinition == null) {
                    // TODO I don't think we should be trying to load the wsdlDefinition here.

                    //Let see if we can get wsdl definition from endpoint @WebService annotation.
                    if (ed instanceof EndpointDescriptionJava) {
                        String wsdlLocation =
                                ((EndpointDescriptionJava)ed).getAnnoWebServiceWSDLLocation();
                        wsdlDefinition = getWSDLDefinition(wsdlLocation);
                    }
                }
                //So at this point either we got wsdl definition from ServiceDescription (which means we are running this code
                //on client side) or we got it from the @WebService annotation (which means we are running this code on server side)
                if (wsdlDefinition != null) {
                    SchemaReader sr = new SchemaReaderImpl();
                    try {
                        Set<String> pkgSet = sr.readPackagesFromSchema(wsdlDefinition);
                        set.addAll(pkgSet);
                    } catch (SchemaReaderException e) {
                        throw ExceptionFactory.makeWebServiceException(e);
                    }
                }
            }
        }
        if (log.isDebugEnabled()) {
        	log.debug("end getPackagesFromSchema");
        }
        return set;
    }

    /**
     * @param serviceDescription ServiceDescription
     * @return Set of Packages
     */
    public static TreeSet<String> getPackagesFromAnnotations(ServiceDescription serviceDesc,
                                                             MarshalServiceRuntimeDescription msrd) {
    	if (log.isDebugEnabled()) {
    		log.debug("start getPackagesFromAnnotations");
    		log.debug("ServiceDescription = " + serviceDesc.toString());
    		log.debug("MarshalServiceRuntimeDescription = " + msrd.toString());
    	}
        TreeSet<String> set = new TreeSet<String>();
        Collection<EndpointDescription> endpointDescs = serviceDesc.getEndpointDescriptions_AsCollection();
        
        // Build a set of packages from all of the endpoints
        if (endpointDescs != null) {
            for (EndpointDescription endpointDesc: endpointDescs) {
                set.addAll(getPackagesFromAnnotations(endpointDesc, msrd));
            }
        }
        if (log.isDebugEnabled()) {
        	log.debug("end getPackagesFromAnnotations");
        }
        return set;
    }

    /**
     * @param endpointDesc EndpointDescription
     * @return Set of Packages
     */
    private static TreeSet<String> getPackagesFromAnnotations(EndpointDescription endpointDesc,
                                                              MarshalServiceRuntimeDescription msrd) {
    	if (log.isDebugEnabled()) {
    		log.debug("start getPackagesFromAnnotations for EndpointDescription " + endpointDesc.getName());
    	}
        TreeSet<String> set = new TreeSet<String>();
        String implClassName = getServiceImplClassName(endpointDesc);
        if (implClassName != null) {
        	if (log.isDebugEnabled()) {
        		log.debug("EndpointDescription implClassName = " + implClassName);
        	}
            Class clz = loadClass(implClassName);
            if(clz == null){
                clz = loadClass(implClassName, endpointDesc.getAxisService().getClassLoader());
            }
            if (clz != null) {
                addXmlSeeAlsoPackages(clz, msrd, set);
            }
        }
        EndpointInterfaceDescription endpointInterfaceDesc =
                endpointDesc.getEndpointInterfaceDescription();
        if (endpointInterfaceDesc != null) {
            getPackagesFromAnnotations(endpointDesc, endpointInterfaceDesc, set, msrd);
        }
        if (log.isDebugEnabled()) {
    		log.debug("end getPackagesFromAnnotations for EndpointDescription " + endpointDesc.getName());
    	}
        return set;
    }

    /**
     * @param endpointInterfaceDescription EndpointInterfaceDescription
     * @param Set of Packages
     * @param msrd
     */
    private static void getPackagesFromAnnotations(
            EndpointDescription ed,
            EndpointInterfaceDescription endpointInterfaceDesc,
            TreeSet<String> set,
            MarshalServiceRuntimeDescription msrd) {
        
    	if (log.isDebugEnabled()) {
    		log.debug("start getPackagesFromAnnotations for EndpointInterfaceDescription " + 
    					endpointInterfaceDesc.getPortType());
    	}
        OperationDescription[] opDescs = endpointInterfaceDesc.getDispatchableOperations();

        // Inspect the @XmlSeeAlso classes on the interface.
        // A) The SEI class is accessible via the getSEIClass method -OR-
        // B) The endpoint directly implements the sei class
        //    (The @XmlSeeAlso annotations were picked up when the endpoint is examined) -OR-
        // C) Find the SEI class using the @WebService annotation
        Class seicls = endpointInterfaceDesc.getSEIClass();
        if (log.isDebugEnabled()) {
        	log.debug("SEI Class is " + seicls);
        }
        if (seicls == null) {
        	String implClassName = getServiceImplClassName(ed);
            if (implClassName != null) {
            	if (log.isDebugEnabled()) {
            		log.debug("EndpointDescription implClassName = " + implClassName);
            	}
                Class clz = loadClass(implClassName);
                if(clz == null){
                    clz = loadClass(implClassName, ed.getAxisService().getClassLoader());
                }
                if (clz != null) {
                	WebService ws = (WebService) getAnnotation(clz, WebService.class);
                	if (ws != null) {
                		String intClassName = ws.endpointInterface();
                		if (log.isDebugEnabled()) {
                    		log.debug("WebService endpointinterface = " + intClassName);
                    	}
                        seicls = loadClass(intClassName);
                        if (seicls== null){
                            seicls = loadClass(intClassName, ed.getAxisService().getClassLoader());
                        }
                	}
                }
            }
        }
        addXmlSeeAlsoPackages(seicls, msrd, set);
        
        
        // Build a set of packages from all of the operations
        if (opDescs != null) {
            for (int i = 0; i < opDescs.length; i++) {
                getPackagesFromAnnotations(ed, opDescs[i], set, msrd);
            }
        }
        if (log.isDebugEnabled()) {
    		log.debug("end getPackagesFromAnnotations for EndpointInterfaceDescription " + 
    					endpointInterfaceDesc.getPortType());
    	}
        return;
    }

    /**
     * Update the package set with the packages referenced by this OperationDesc
     *
     * @param opDesc OperationDescription
     * @param set    Set<Package> that is updated
     */
    private static void getPackagesFromAnnotations(EndpointDescription ed, OperationDescription opDesc, TreeSet<String> set,
                                                   MarshalServiceRuntimeDescription msrd) {

        // Walk the parameter information
        ParameterDescription[] parameterDescs = opDesc.getParameterDescriptions();
        if (parameterDescs != null) {
            for (int i = 0; i < parameterDescs.length; i++) {
                getPackagesFromAnnotations(parameterDescs[i], set, msrd);
            }
        }

        // Walk the fault information
        FaultDescription[] faultDescs = opDesc.getFaultDescriptions();
        if (faultDescs != null) {
            for (int i = 0; i < faultDescs.length; i++) {
                getPackagesFromAnnotations(ed, faultDescs[i], set, msrd);
            }
        }

        // Also consider the request and response wrappers
        String requestWrapperPkg = getPackageFromClassName(msrd.getRequestWrapperClassName(opDesc));
        if (log.isDebugEnabled()) {
            log.debug("Package from Request Wrapper annotation = " + requestWrapperPkg);
        }
        if (requestWrapperPkg != null) {
            set.add(requestWrapperPkg);
        }
        String responseWrapperPkg = getPackageFromClassName(msrd.getResponseWrapperClassName(opDesc));
        if (log.isDebugEnabled()) {
            log.debug("Package from Response Wrapper annotation = " + responseWrapperPkg);
        }
        if (responseWrapperPkg != null) {
            set.add(responseWrapperPkg);
        }
        
        // In most doc/literal cases, the @RequestWrapper or @ResponseWrapper classes are successfully found.
        // The wrapper classes contain the representation of the parameters, thus the parameters don't need
        // to be separately processed.
        // However if the @RequestWrapper or @ResponseWrappers are not found, then the parameters must be examined.
        if (requestWrapperPkg == null || responseWrapperPkg == null) {
            if (log.isDebugEnabled()) {
                log.debug("Collect the packages of the parameters");
            }
            addPackagesFromParameters(set, opDesc) ;          
        }

        // Finally consider the result type
        Class cls = opDesc.getResultActualType();
        if (cls != null && cls != void.class && cls != Void.class) {
            String pkg = getPackageFromClass(cls);
            if (log.isDebugEnabled()) {
                log.debug("Package from Return Type = " + pkg);
            }
            if (pkg != null) {
                set.add(pkg);
            }
        }
    }
    
    
    /**
     * addPackagesFromParameters
     * @param set  Set of packages
     * @param opDesc OperationDesc containing ParameterDescs
     */
    private static void addPackagesFromParameters(TreeSet<String> set, OperationDescription opDesc) {
        ParameterDescription[] pDescs = opDesc.getParameterDescriptions();
        if (pDescs != null) {
            for (int i=0; i<pDescs.length; i++) {
                ParameterDescription pDesc = pDescs[i];
                if (pDesc != null) {
                    // Get the actual type of the parameter.
                    // For example if the parameter is Holder<A>, the A.class is
                    // returned.
                    // TODO Unfortunately the ParameterDescriptor only provides
                    // the class, not the full generic.  So if the parameter
                    // is List<A>, only List is returned.  The ParameterDescriptor
                    // and this logic will need to be improved to handle that case.
                    Class paramClass = pDesc.getParameterActualType();
                    String pkg = getPackageFromClass(paramClass);
                    if (log.isDebugEnabled()) {
                        log.debug("Package from Parameter (" + paramClass + ") = " + pkg);
                    }
                    if (pkg != null) {
                        set.add(pkg);
                    }
                }
            }
        }    
    }

    /**
     * Update the package set with the packages referenced by this ParameterDescription
     *
     * @param paramDesc ParameterDesc
     * @param set       Set<Package> that is updated
     */
    private static void getPackagesFromAnnotations(ParameterDescription paramDesc,
                                                   TreeSet<String> set,
                                                   MarshalServiceRuntimeDescription msrd) {

        // Get the type that defines the actual data.  (this is never a holder )
        Class paramClass = paramDesc.getParameterActualType();

        if (paramClass != null) {
            setTypeAndElementPackages(paramClass, paramDesc.getTargetNamespace(),
                                      paramDesc.getPartName(), set, msrd);
        }

    }

    /**
     * Update the package set with the packages referenced by this FaultDescription
     *
     * @param faultDesc FaultDescription
     * @param set       Set<Package> that is updated
     */
    private static void getPackagesFromAnnotations(EndpointDescription ed, 
                                                   FaultDescription faultDesc, TreeSet<String> set,
                                                   MarshalServiceRuntimeDescription msrd) {

        FaultBeanDesc faultBeanDesc = msrd.getFaultBeanDesc(faultDesc);
        if(faultBeanDesc == null){
            if(log.isDebugEnabled()){
                log.debug("faultBeanDesc from MarshallServiceRuntimeDescription is null");
            }
            //NO FaultBeanDesc found nothing we can do.
            return;
        }
        String faultBeanName = faultBeanDesc.getFaultBeanClassName();
        if(faultBeanName == null){
            if(log.isDebugEnabled()){
                log.debug("FaultBeanName is null");
            }
            //We cannot load the faultBeanName
            return;
        }
        Class faultBean = loadClass(faultBeanName);
        if(faultBean == null){
            faultBean = loadClass(faultBeanName, ed.getAxisService().getClassLoader());
        }
        if (faultBean != null) {
            setTypeAndElementPackages(faultBean, faultBeanDesc.getFaultBeanNamespace(),
                                      faultBeanDesc.getFaultBeanLocalName(), set, msrd);
        }
    }

    /**
     * For each data element, we need the package for both the element and its type.
     *
     * @param cls       Class representing element, type or both
     * @param namespace of the element
     * @param localPart of the element
     * @param set       with both type and element packages set
     */
    private static void setTypeAndElementPackages(Class cls, String namespace, String localPart,
                                                  TreeSet<String> set,
                                                  MarshalServiceRuntimeDescription msrd) {

        // Get the element and type classes
        Class eClass = getElement(cls, msrd);
        Class tClass = getType(cls);

        // Set the package for the type
        if (tClass != null) {
            Package typePkg = tClass.getPackage();
            //For primitive types there is no package
            String pkg = (typePkg != null) ? typePkg.getName() : null;

            if (pkg != null) {
                set.add(pkg);
            }
            addXmlSeeAlsoPackages(tClass, msrd, set);
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
                String pkg = (elementPkg != null) ? elementPkg.getName() : null;
                if (pkg != null) {
                    set.add(pkg);
                }
                addXmlSeeAlsoPackages(tClass, msrd, set);
            }
        }
    }

    /**
     * If cls represents an xml element then cls is returned. Otherwise null is returned
     *
     * @param cls Class
     * @return Class or null
     */
    private static Class getElement(Class cls, MarshalServiceRuntimeDescription msrd) {
        AnnotationDesc annotationDesc = msrd.getAnnotationDesc(cls);
        if (annotationDesc == null) {
            // This shouldn't happen
            annotationDesc = AnnotationDescImpl.create(cls);
        }
        if (annotationDesc.hasXmlRootElement()) {
            return cls;
        }
        return null;
    }

    private final static Class[] noClass = new Class[] { };

    /**
     * Returns the class that defines the type.
     *
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
     *
     * @param ns
     * @return
     */
    private static String makePackage(String ns) {
        String pkgName = JavaUtils.getPackageFromNamespace(ns);
        return pkgName;
    }

    /**
     * Return the package associated with the class name.  The className may not be specified (in
     * which case a null Package is returned). 
     * If class has anunnamed package return ""
     *
     * @param className String (may be null or empty)
     * @return String or null if problems occur
     */
    public static String getPackageFromClassName(String className) {
        Class clz = loadClass(className);
        String pkg = getPackageFromClass(clz);
        return pkg;
    }
    
    /**
     * Return the package associated with the class name.  The className may not be specified (in
     * which case a null Package is returned). if class has unnamed package return ""
     *
     * @param cls Class
     * @return String or null 
     */
    public static String getPackageFromClass(Class cls) {
        String pkgName = null;
        if (cls == null) {
            pkgName = null;
        } else if (cls.isArray()) {
            pkgName = getPackageFromClass(cls.getComponentType());
        } else if (cls.isPrimitive()) {
            pkgName = null;
        } else {
            pkgName =
                (cls.getPackage() == null) ? "" : cls.getPackage().getName(); 
        }
        return pkgName;
    }

    private static void addXmlSeeAlsoPackages(Class clz, 
                                              MarshalServiceRuntimeDescription msrd, 
                                              TreeSet<String> set) {
    	if (log.isDebugEnabled()) {
    		log.debug("start addXmlSeeAlsoPackages for " + clz);
    	}
        if (clz != null) {
            AnnotationDesc aDesc = msrd.getAnnotationDesc(clz);
            if (aDesc != null) {
                Class[] seeAlso = aDesc.getXmlSeeAlsoClasses();
                if (seeAlso != null) {
                    for (int i=0; i<seeAlso.length; i++) {
                        String pkg =
                            (seeAlso[i] == null) ? null : 
                                (seeAlso[i].getPackage() == null) ? "" : 
                                    seeAlso[i].getPackage().getName();
                        if (pkg != null) {
                        	if (log.isDebugEnabled()) {
                        		log.debug(" adding package = " + pkg);
                        	}
                            set.add(pkg);
                        }
                    }
                }
            }
            
            Class[] interfaces = clz.getInterfaces();
            if (interfaces != null) {
                for (int i=0; i<interfaces.length; i++) {
                    addXmlSeeAlsoPackages(interfaces[i], msrd, set);
                }
            }
        }
        if (log.isDebugEnabled()) {
    		log.debug("end addXmlSeeAlsoPackages for " + clz);
    	}
    }
    /**
     * Loads the class
     *
     * @param className
     * @return Class (or null if the class cannot be loaded)
     */
    private static Class loadClass(String className) {
        // Don't make this public, its a security exposure
        if (className == null || className.length() == 0) {
            return null;
        }
        try {
            // Class.forName does not support primitives
            Class cls = ClassUtils.getPrimitiveClass(className);
            if (cls == null) {
                cls = Class.forName(className, true, getContextClassLoader());
            }
            return cls;
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
     * Loads the class
     *
     * @param className
     * @return Class (or null if the class cannot be loaded)
     */
    private static Class loadClass(String className, ClassLoader loader) {
        // Don't make this public, its a security exposure
        if (className == null || className.length() == 0) {
            return null;
        }
        try {
            // Class.forName does not support primitives
            Class cls = ClassUtils.getPrimitiveClass(className);
            if (cls == null) {
                cls = forName(className, true, loader);
            }
            return cls;
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

    private static Definition getWSDLDefinition(String wsdlLoc){
        Definition wsdlDefinition = null;
        final String wsdlLocation = wsdlLoc;
        if (wsdlLocation != null && wsdlLocation.trim().length() > 0) {
            try {
                wsdlDefinition = (Definition) AccessController.doPrivileged(
                        new PrivilegedExceptionAction() {
                            public Object run() throws MalformedURLException, IOException, WSDLException {
                                String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
                                String wsdlLocationPath = new File(baseDir +File.separator+ wsdlLocation).getAbsolutePath();
                                File file = new File(wsdlLocationPath);
                                URL url = file.toURL();
                                if(log.isDebugEnabled()){
                                    log.debug("Reading WSDL from URL:" +url.toString());
                                }
                                // This is a temporary wsdl and we use it to dig into the schemas,
                                // Thus the memory limit is set to false.  It will be discarded after it is used
                                // by the PackageSetBuilder implementation.
                                WSDLWrapper wsdlWrapper = new WSDL4JWrapper(url, false, 0);
                                return wsdlWrapper.getDefinition();
                            }
                        });
            } catch (PrivilegedActionException e) {
                // Swallow and continue
                if (log.isDebugEnabled()) {
                    log.debug("Exception getting wsdlLocation: " +e.getException());
                }
            }
        }

        return wsdlDefinition;
    }

    /**
     * Return the class for this name
     *
     * @return Class
     */
    static Class forName(final String className, final boolean initialize,
                         final ClassLoader classloader) throws ClassNotFoundException {
        // NOTE: This method must remain protected because it uses AccessController
        Class cl = null;
        try {
            cl = (Class)AccessController.doPrivileged(
                    new PrivilegedExceptionAction() {
                        public Object run() throws ClassNotFoundException {
                            // Class.forName does not support primitives
                            Class cls = ClassUtils.getPrimitiveClass(className);
                            if (cls == null) {
                                cls = forName(className, initialize, classloader);
                            }
                            return cls;
                        }
                    }
            );
        } catch (PrivilegedActionException e) {
            if (log.isDebugEnabled()) {
                log.debug("Exception thrown from AccessController: " + e);
            }
            throw (ClassNotFoundException)e.getException();
        }

        return cl;
    }

    /** @return ClassLoader */
    static ClassLoader getContextClassLoader() {
        // NOTE: This method must remain private because it uses AccessController
        ClassLoader cl = null;
        try {
            cl = (ClassLoader)AccessController.doPrivileged(
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
            throw (RuntimeException)e.getException();
        }

        return cl;
    }
    /**
     * Get the Serivce Impl Class by looking at the AxisService
     * @param endpointDescription
     * @return class name or null
     */
    static private String getServiceImplClassName(EndpointDescription endpointDescription) {
        String result = null;
        if (endpointDescription != null) {
            AxisService as = endpointDescription.getAxisService();
            if (as != null) {
                Parameter param = as.getParameter(org.apache.axis2.Constants.SERVICE_CLASS);

                // If there was no implementation class, we should not go any further
                if (param != null) {
                    result = ((String)param.getValue()).trim();
                }
            }
        }
        return result;
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
}
