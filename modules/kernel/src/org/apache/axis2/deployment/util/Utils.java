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

package org.apache.axis2.deployment.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAP12Constants;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.jaxrs.JAXRSModel;
import org.apache.axis2.jaxrs.JAXRSUtils;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentClassLoader;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.*;
import org.apache.axis2.description.java2wsdl.DefaultSchemaGenerator;
import org.apache.axis2.description.java2wsdl.DocLitBareSchemaGenerator;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.description.java2wsdl.SchemaGenerator;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.util.JavaUtils;
import org.apache.axis2.util.Loader;
import org.apache.axis2.util.PolicyUtil;
import org.apache.axis2.util.FaultyServiceData;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.wsdl.WSDLUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.neethi.PolicyComponent;
import org.apache.ws.commons.schema.utils.NamespaceMap;

import javax.xml.namespace.QName;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Utils {

    public static String defaultEncoding = new OutputStreamWriter(System.out).getEncoding();

    private static Log log = LogFactory.getLog(Utils.class);

    public static void addFlowHandlers(Flow flow, ClassLoader clsLoader)
            throws AxisFault {
        int count = flow.getHandlerCount();

        for (int j = 0; j < count; j++) {
            HandlerDescription handlermd = flow.getHandler(j);
            Handler handler;

            final Class handlerClass = getHandlerClass(
                    handlermd.getClassName(), clsLoader);

            try {
                handler = (Handler)org.apache.axis2.java.security.AccessController
                        .doPrivileged(new PrivilegedExceptionAction() {
                            public Object run() throws InstantiationException,
                                    IllegalAccessException {
                                return handlerClass.newInstance();
                            }
                        });
                handler.init(handlermd);
                handlermd.setHandler(handler);
            } catch (PrivilegedActionException e) {
                throw AxisFault.makeFault(e);
            }
        }
    }

    public static boolean loadHandler(ClassLoader loader1,
                                      HandlerDescription desc) throws DeploymentException {
        String handlername = desc.getClassName();
        Handler handler;
        try {
            final Class handlerClass = Loader.loadClass(loader1, handlername);
            Package aPackage = (Package)org.apache.axis2.java.security.AccessController
                    .doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            return handlerClass.getPackage();
                        }
                    });
            if (aPackage != null
                && aPackage.getName().equals("org.apache.axis2.engine")) {
                String name = handlerClass.getName();
                log.warn("Dispatcher " + name + " is now deprecated.");
                if (name.indexOf("InstanceDispatcher") != -1) {
                    log.warn("Please remove the entry for "
                             + handlerClass.getName() + "from axis2.xml");
                } else {
                    log.warn(
                            "Please edit axis2.xml and replace with the same class in org.apache.axis2.dispatchers package");
                }
            }
            handler = (Handler)org.apache.axis2.java.security.AccessController
                    .doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws InstantiationException,
                                IllegalAccessException {
                            return handlerClass.newInstance();
                        }
                    });
            handler.init(desc);
            desc.setHandler(handler);
        } catch (ClassNotFoundException e) {
            if (handlername.indexOf("jaxws") > 0) {
                log.warn("[JAXWS] - unable to load " + handlername);
                return false;
            }
            throw new DeploymentException(e);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        return true;
    }

    public static URL[] getURLsForAllJars(URL url, File tmpDir) {
        InputStream in = null;
        ZipInputStream zin = null;
        try {
            ArrayList<URL> array = new ArrayList<URL>();
            in = url.openStream();
            if (url.getProtocol().equals("file")) {
                array.add(url);
            } else {
                String fileName = url.getFile();
                int index = fileName.lastIndexOf('/');
                if (index != -1) {
                    fileName = fileName.substring(index + 1);
                }
                final File f = createTempFile(fileName, in, tmpDir);
                in.close();

                in = org.apache.axis2.java.security.AccessController
                        .doPrivileged(new PrivilegedExceptionAction<InputStream>() {
                            public InputStream run() throws FileNotFoundException {
                                return new FileInputStream(f);
                            }
                        });
                array.add(f.toURI().toURL());
            }
            zin = new ZipInputStream(in);

            ZipEntry entry;
            String entryName;
            while ((entry = zin.getNextEntry()) != null) {
                entryName = entry.getName();
                /**
                 * id the entry name start with /lib and end with .jar then
                 * those entry name will be added to the arraylist
                 */
                if ((entryName != null)
                    && entryName.toLowerCase().startsWith("lib/")
                    && entryName.toLowerCase().endsWith(".jar")) {
                    String suffix = entryName.substring(4);
                    File f2 = createTempFile(suffix, zin, tmpDir);
                    array.add(f2.toURI().toURL());
                }
            }
            return array.toArray(new URL[array.size()]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    //
                }
            }
            if (zin != null) {
                try {
                    zin.close();
                } catch (IOException e) {
                    //
                }
            }
        }
    }

    public static File createTempFile(final String suffix, InputStream in,
                                      final File tmpDir) throws IOException {
        byte data[] = new byte[2048];
        int count;
        File f = TempFileManager.createTempFile("axis2", suffix);

//        if (tmpDir == null) {
//            String directory = (String)org.apache.axis2.java.security.AccessController
//                    .doPrivileged(new PrivilegedAction() {
//                        public Object run() {
//                            return System.getProperty("java.io.tmpdir");
//                        }
//                    });
//            final File tempFile = new File(directory, "_axis2");
//            Boolean exists = (Boolean)org.apache.axis2.java.security.AccessController
//                    .doPrivileged(new PrivilegedAction() {
//                        public Object run() {
//                            return tempFile.exists();
//                        }
//                    });
//            if (!exists) {
//                Boolean mkdirs = (Boolean)org.apache.axis2.java.security.AccessController
//                        .doPrivileged(new PrivilegedAction() {
//                            public Object run() {
//                                return tempFile.mkdirs();
//                            }
//                        });
//                if (!mkdirs) {
//                    throw new IOException("Unable to create the directory");
//                }
//            }
//            try {
//                f = (File)org.apache.axis2.java.security.AccessController
//                        .doPrivileged(new PrivilegedExceptionAction() {
//                            public Object run() throws IOException {
//                                return File.createTempFile("axis2", suffix,
//                                                           tempFile);
//                            }
//                        });
//                f.deleteOnExit();
//            } catch (PrivilegedActionException e) {
//                throw (IOException)e.getException();
//            }
//        } else {
//            try {
//                f = (File)org.apache.axis2.java.security.AccessController
//                        .doPrivileged(new PrivilegedExceptionAction() {
//                            public Object run() throws IOException {
//                                return File.createTempFile("axis2", suffix,
//                                                           tmpDir);
//                            }
//                        });
//                f.deleteOnExit();
//            } catch (PrivilegedActionException e) {
//                throw (IOException)e.getException();
//            }
//        }
//        if (log.isDebugEnabled()) {
//            log.debug("Created temporary file : " + f.getAbsolutePath());// $NON-SEC-4
//        }
//        final File f2 = f;
//        org.apache.axis2.java.security.AccessController
//                .doPrivileged(new PrivilegedAction() {
//                    public Object run() {
//                        f2.deleteOnExit();
//                        return null;
//                    }
//                });
        FileOutputStream out;
        final File f2 = f;
        try {
            out = (FileOutputStream)org.apache.axis2.java.security.AccessController
                    .doPrivileged(new PrivilegedExceptionAction() {
                        public Object run() throws FileNotFoundException {
                            return new FileOutputStream(f2);
                        }
                    });
        } catch (PrivilegedActionException e) {
            throw (FileNotFoundException)e.getException();
        }
        while ((count = in.read(data, 0, 2048)) != -1) {
            out.write(data, 0, count);
        }
        out.close();
        return f;
    }

    public static ClassLoader getClassLoader(ClassLoader parent, String path, boolean isChildFirstClassLoading)
            throws DeploymentException {
        return getClassLoader(parent, new File(path), isChildFirstClassLoading);
    }

    /**
     * Get a ClassLoader which contains a classpath of a) the passed directory and b) any jar files
     * inside the "lib/" or "Lib/" subdirectory of the passed directory.
     *
     * @param parent parent ClassLoader which will be the parent of the result of this method
     * @param file   a File which must be a directory for this to be useful
     * @return a new ClassLoader pointing to both the passed dir and jar files under lib/
     * @throws DeploymentException if problems occur
     */
    public static ClassLoader getClassLoader(final ClassLoader parent, File file, final boolean isChildFirstClassLoading)
            throws DeploymentException {
        if (file == null)
            return null; // Shouldn't this just return the parent?

        try {
            ArrayList<URL> urls = new ArrayList<URL>();
            urls.add(file.toURI().toURL());

            // lower case directory name
            File libfiles = new File(file, "lib");
            if (!addFiles(urls, libfiles)) {
                // upper case directory name
                libfiles = new File(file, "Lib");
                addFiles(urls, libfiles);
            }

            final URL urllist[] = new URL[urls.size()];
            for (int i = 0; i < urls.size(); i++) {
                urllist[i] = urls.get(i);
            }
            if (log.isDebugEnabled()) {
                log.debug("Creating class loader with the following libraries: " + Arrays.asList(urllist));
            }
            return createDeploymentClassLoader(urllist, parent, isChildFirstClassLoading);
        } catch (MalformedURLException e) {
            throw new DeploymentException(e);
        }
    }

    private static boolean addFiles(ArrayList<URL> urls, final File libfiles)
            throws MalformedURLException {
        Boolean exists = org.apache.axis2.java.security.AccessController
                .doPrivileged(new PrivilegedAction<Boolean>() {
                    public Boolean run() {
                        return libfiles.exists();
                    }
                });
        if (exists) {
            urls.add(libfiles.toURI().toURL());
            File jarfiles[] = org.apache.axis2.java.security.AccessController
                    .doPrivileged(new PrivilegedAction<File[]>() {
                        public File[] run() {
                            return libfiles.listFiles();
                        }
                    });
            int i = 0;
            while (i < jarfiles.length) {
                File jarfile = jarfiles[i];
                if (jarfile.getName().endsWith(".jar")) {
                    urls.add(jarfile.toURI().toURL());
                }
                i++;
            }
        }
        return exists;
    }

    private static Class getHandlerClass(String className, ClassLoader loader1)
            throws AxisFault {
        Class handlerClass;

        try {
            handlerClass = Loader.loadClass(loader1, className);
        } catch (ClassNotFoundException e) {
            throw AxisFault.makeFault(e);
        }

        return handlerClass;
    }

    /**
     * This guy will create a AxisService using java reflection
     *
     * @param axisService       the target AxisService
     * @param axisConfig        the in-scope AxisConfiguration
     * @param excludeOperations a List of Strings (or null), each containing a method to exclude
     * @param nonRpcMethods     a List of Strings (or null), each containing a non-rpc method name
     * @throws Exception if a problem occurs
     */
    public static void fillAxisService(final AxisService axisService,
                                       AxisConfiguration axisConfig, ArrayList<String> excludeOperations,
                                       ArrayList<String> nonRpcMethods) throws Exception {
        Class<?> serviceClass = org.apache.axis2.util.Utils.getServiceClass(axisService);
        if (serviceClass == null) {
            return;
        }
        String enableJSONOnly = (String) axisConfig.getParameterValue("enableJSONOnly");
	if (enableJSONOnly !=null && enableJSONOnly.equalsIgnoreCase("true")) {
            log.debug("on enableJSONOnly: " +enableJSONOnly+ " starting fillAxisService(), serviceClass.name: " + serviceClass.getName());
            List<Method> serviceMethods = new ArrayList<Method>();
            Map<String, Method> uniqueMethods = new LinkedHashMap<String, Method>();
            for (Method method : serviceClass.getMethods()) {
                if (method.getDeclaringClass() == Object.class) {
                    continue;
                }
                if (!Modifier.isPublic(method.getModifiers())) {
                    // skip non public methods
                    continue;
                }
                String methodName = method.getName();
                if (excludeOperations.contains(methodName)) {
                    continue;
                }
                boolean addToService = false;
                AxisOperation axisOperation = axisService.getOperation(new QName(methodName));
                if (axisOperation == null) {
                    axisOperation = getAxisOperationForJmethod(method);
                    axisService.addOperation(axisOperation);
                    log.debug("on methodName: " +methodName+ " , enableJSONOnly: " +enableJSONOnly+ " , axisOperation added to service: " +axisService.getName());
                }
                // by now axis operation should be assigned but we better recheck & add the paramether
                if (axisOperation != null) {
                    axisOperation.addParameter("JAXRSAnnotaion", JAXRSUtils.getMethodModel(JAXRSUtils.getClassModel(serviceClass), method));
                }
                if (method.getDeclaringClass() != Object.class) {
                    serviceMethods.add(method);
                }
            }
            // The order of the methods returned by getMethods is undefined, but the test cases assume that the
            // order is the same on all Java versions. Java 6 seems to use reverse lexical order, so we use that
            // here to make things deterministic.
            Collections.sort(serviceMethods, new Comparator<Method>() {
                public int compare(Method o1, Method o2) {
                    return -o1.getName().compareTo(o2.getName());
                }
            });
    
            log.debug("fillAxisService() on enableJSONOnly=true found serviceMethods: " +serviceMethods);

            PhasesInfo pinfo = axisConfig.getPhasesInfo();

            for (Method jmethod : serviceMethods) {
                String opName = jmethod.getName();
                AxisOperation operation = axisService
                        .getOperation(new QName(opName));
                // if the operation there in services.xml then try to set it schema
                // element name
                if (operation == null) {
                    operation = axisService.getOperation(new QName(
                            jmethod.getName()));
                }
                MessageReceiver mr =
                        axisService.getMessageReceiver(operation.getMessageExchangePattern());
                if (mr == null) {
                    mr = axisConfig.getMessageReceiver(operation.getMessageExchangePattern());
                }
                if (operation.getMessageReceiver() == null) {
                    operation.setMessageReceiver(mr);
                }
                pinfo.setOperationPhases(operation);
                axisService.addOperation(operation);
                axisService.addJSONMessageNameToOperationMapping(opName, operation);
            }
            log.debug("fillAxisService() completed on enableJSONOnly=true , axisService name: " + axisService.getName());
	    return;
	}

        ClassLoader serviceClassLoader = axisService.getClassLoader();
        // adding name spaces
        NamespaceMap map = new NamespaceMap();
        map.put(Java2WSDLConstants.AXIS2_NAMESPACE_PREFIX,
                Java2WSDLConstants.AXIS2_XSD);
        map.put(Java2WSDLConstants.DEFAULT_SCHEMA_NAMESPACE_PREFIX,
                Java2WSDLConstants.URI_2001_SCHEMA_XSD);
        axisService.setNamespaceMap(map);
        SchemaGenerator schemaGenerator;
        Parameter generateBare = axisService
                .getParameter(Java2WSDLConstants.DOC_LIT_BARE_PARAMETER);
        if (generateBare != null && "true".equals(generateBare.getValue())) {
            schemaGenerator = new DocLitBareSchemaGenerator(serviceClassLoader,
                                                            serviceClass.getName(),
                                                            axisService.getSchemaTargetNamespace(),
                                                            axisService
                                                                    .getSchemaTargetNamespacePrefix(),
                                                            axisService);
        } else {
            schemaGenerator = new DefaultSchemaGenerator(serviceClassLoader,
                                                         serviceClass.getName(),
                                                         axisService.getSchemaTargetNamespace(),
                                                         axisService
                                                                 .getSchemaTargetNamespacePrefix(),
                                                         axisService);
        }
        schemaGenerator.setExcludeMethods(excludeOperations);
        schemaGenerator.setNonRpcMethods(nonRpcMethods);
        if (!axisService.isElementFormDefault()) {
            schemaGenerator
                    .setElementFormDefault(Java2WSDLConstants.FORM_DEFAULT_UNQUALIFIED);
        }
        // package to namespace map
        schemaGenerator.setPkg2nsmap(axisService.getP2nMap());
        Collection schemas = schemaGenerator.generateSchema();
        axisService.addSchema(schemas);
        axisService.setSchemaTargetNamespace(schemaGenerator
                .getSchemaTargetNameSpace());
        axisService.setTypeTable(schemaGenerator.getTypeTable());
        if (Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE.equals(axisService
                .getTargetNamespace())) {
            axisService
                    .setTargetNamespace(schemaGenerator.getTargetNamespace());
        }

        Method[] method = schemaGenerator.getMethods();
        PhasesInfo pinfo = axisConfig.getPhasesInfo();

        for (Method jmethod : method) {
            String opName = jmethod.getName();
            AxisOperation operation = axisService
                    .getOperation(new QName(opName));
            // if the operation there in services.xml then try to set it schema
            // element name
            if (operation == null) {
                operation = axisService.getOperation(new QName(
                        jmethod.getName()));
            }
            MessageReceiver mr =
                    axisService.getMessageReceiver(operation.getMessageExchangePattern());
            if (mr == null) {
                mr = axisConfig.getMessageReceiver(operation.getMessageExchangePattern());
            }
            if (operation.getMessageReceiver() == null) {
                operation.setMessageReceiver(mr);
            }
            pinfo.setOperationPhases(operation);
            axisService.addOperation(operation);
            if (operation.getSoapAction() == null) {
                operation.setSoapAction("urn:" + opName);
            }
        }
    }

    public static AxisOperation getAxisOperationForJmethod(Method method)
            throws AxisFault {
        AxisOperation operation;
        if ("void".equals(method.getReturnType().getName())) {
            if (method.getExceptionTypes().length > 0) {
                operation = AxisOperationFactory
                        .getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_OUT);
            } else {
                operation = AxisOperationFactory
                        .getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_ONLY);
            }
        } else {
            operation = AxisOperationFactory
                    .getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_OUT);
        }
        String opName = method.getName();


        operation.setName(new QName(opName));
        return operation;
    }

    public static OMElement getParameter(String name, String value,
                                         boolean locked) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement parameter = fac.createOMElement("parameter", null);
        parameter.addAttribute("name", name, null);
        parameter.addAttribute("locked", Boolean.toString(locked), null);
        parameter.setText(value);
        return parameter;
    }

    /**
     * Modules can contain services in some cases.  This method will deploy all the services
     * for a given AxisModule into the current AxisConfiguration.
     * <p>
     * The code looks for an "aars/" directory inside the module (either .mar or exploded),
     * and an "aars.list" file inside that to figure out which services to deploy.  Note that all
     * services deployed this way will have access to the Module's classes.
     * </p>
     *
     * @param module the AxisModule to search for services
     * @param configCtx ConfigurationContext in which to deploy
     */

    public static void deployModuleServices(AxisModule module,
                                            ConfigurationContext configCtx) throws AxisFault {
        try {
            AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
            ArchiveReader archiveReader = new ArchiveReader();
            PhasesInfo phasesInfo = axisConfig.getPhasesInfo();
            final ClassLoader moduleClassLoader = module.getModuleClassLoader();
            ArrayList services = new ArrayList();
            final InputStream in = (InputStream)org.apache.axis2.java.security.AccessController
                    .doPrivileged(new PrivilegedAction() {
                        public Object run() {
                            return moduleClassLoader
                                    .getResourceAsStream("aars/aars.list");
                        }
                    });
            if (in != null) {
                BufferedReader input;
                try {
                    input = new BufferedReader(
                            (InputStreamReader)org.apache.axis2.java.security.AccessController
                                    .doPrivileged(new PrivilegedAction() {
                                        public Object run() {
                                            return new InputStreamReader(in);
                                        }
                                    }));
                    String line;
                    while ((line = input.readLine()) != null) {
                        line = line.trim();
                        if (line.length() > 0 && line.charAt(0) != '#') {
                            services.add(line);
                        }
                    }
                    input.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (services.size() > 0) {
                for (Object service1 : services) {
                    final String servicename = (String)service1;
                    if (servicename == null || "".equals(servicename)) {
                        continue;
                    }
                    InputStream fin = (InputStream)org.apache.axis2.java.security.AccessController
                            .doPrivileged(new PrivilegedAction() {
                                public Object run() {
                                    return moduleClassLoader
                                            .getResourceAsStream("aars/"
                                                                 + servicename);
                                }
                            });
                    if (fin == null) {
                        throw new AxisFault("No service archive found : "
                                            + servicename);
                    }
                    File inputFile = Utils
                            .createTempFile(
                                    servicename,
                                    fin,
                                    (File)axisConfig
                                            .getParameterValue(
                                                    Constants.Configuration.ARTIFACTS_TEMP_DIR));
                    DeploymentFileData filedata = new DeploymentFileData(
                            inputFile);

                    filedata.setClassLoader(false,
                                    moduleClassLoader,
                                    (File)axisConfig.getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR),
                                    axisConfig.isChildFirstClassLoading());
                    HashMap wsdlservice = archiveReader.processWSDLs(filedata);
                    if (wsdlservice != null && wsdlservice.size() > 0) {
                        Iterator servicesitr = wsdlservice.values().iterator();
                        while (servicesitr.hasNext()) {
                            AxisService service = (AxisService)servicesitr
                                    .next();
                            Iterator operations = service.getOperations();
                            while (operations.hasNext()) {
                                AxisOperation axisOperation = (AxisOperation)operations
                                        .next();
                                phasesInfo.setOperationPhases(axisOperation);
                            }
                        }
                    }
                    AxisServiceGroup serviceGroup = new AxisServiceGroup(
                            axisConfig);
                    serviceGroup.setServiceGroupClassLoader(filedata
                            .getClassLoader());
                    ArrayList serviceList = archiveReader.processServiceGroup(
                            filedata.getAbsolutePath(), filedata, serviceGroup,
                            false, wsdlservice, configCtx);
                    for (Object aServiceList : serviceList) {
                        AxisService axisService = (AxisService)aServiceList;
                        Parameter moduleService = new Parameter();
                        moduleService.setValue("true");
                        moduleService.setName(AxisModule.MODULE_SERVICE);
                        axisService.addParameter(moduleService);
                        serviceGroup.addService(axisService);
                    }
                    axisConfig.addServiceGroup(serviceGroup);
                    fin.close();
                }
            }
        } catch (IOException e) {
            throw AxisFault.makeFault(e);
        }
    }

    /**
     * Normalize a uri containing ../ and ./ paths.
     *
     * @param uri The uri path to normalize
     * @return The normalized uri
     */
    public static String normalize(String uri) {
        if ("".equals(uri)) {
            return uri;
        }
        int leadingSlashes;
        for (leadingSlashes = 0; leadingSlashes < uri.length()
                                 && uri.charAt(leadingSlashes) == '/'; ++leadingSlashes) {
            // FIXME: this block is empty!!
        }
        boolean isDir = (uri.charAt(uri.length() - 1) == '/');
        StringTokenizer st = new StringTokenizer(uri, "/");
        LinkedList clean = new LinkedList();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ("..".equals(token)) {
                if (!clean.isEmpty() && !"..".equals(clean.getLast())) {
                    clean.removeLast();
                    if (!st.hasMoreTokens()) {
                        isDir = true;
                    }
                } else {
                    clean.add("..");
                }
            } else if (!".".equals(token) && !"".equals(token)) {
                clean.add(token);
            }
        }
        StringBuffer sb = new StringBuffer();
        while (leadingSlashes-- > 0) {
            sb.append('/');
        }
        for (Iterator it = clean.iterator(); it.hasNext();) {
            sb.append(it.next());
            if (it.hasNext()) {
                sb.append('/');
            }
        }
        if (isDir && sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }
        return sb.toString();
    }

    public static String getPath(String parent, String childPath) {
        Stack parentStack = new Stack();
        Stack childStack = new Stack();
        if (parent != null) {
            String[] values = parent.split("/");
            if (values.length > 0) {
                for (String value : values) {
                    parentStack.push(value);
                }
            }
        }
        String[] values = childPath.split("/");
        if (values.length > 0) {
            for (String value : values) {
                childStack.push(value);
            }
        }
        String filepath = "";
        while (!childStack.isEmpty()) {
            String value = (String)childStack.pop();
            if ("..".equals(value)) {
                parentStack.pop();
            } else if (!"".equals(value)) {
                if ("".equals(filepath)) {
                    filepath = value;
                } else {
                    filepath = value + "/" + filepath;
                }
            }
        }
        while (!parentStack.isEmpty()) {
            String value = (String)parentStack.pop();
            if (!"".equals(value)) {
                filepath = value + "/" + filepath;
            }
        }
        return filepath;
    }

    /**
     * Add the Axis2 lifecycle / session methods to a pre-existing list of names that will be
     * excluded when generating schemas.
     *
     * @param excludeList an ArrayList containing method names - we'll add ours to this.
     */
    public static void addExcludeMethods(List<String> excludeList) {
        excludeList.add("init");
        excludeList.add("setOperationContext");
        excludeList.add("startUp");
        excludeList.add("destroy");
        excludeList.add("shutDown");
    }

    public static DeploymentClassLoader createClassLoader(File serviceFile, boolean isChildFirstClassLoading)
            throws MalformedURLException {
        ClassLoader contextClassLoader =
                (ClassLoader)org.apache.axis2.java.security.AccessController
                        .doPrivileged(new PrivilegedAction() {
                            public Object run() {
                                return Thread.currentThread().getContextClassLoader();
                            }
                        });
        return createDeploymentClassLoader(new URL[]{serviceFile.toURI().toURL()},
                                           contextClassLoader, isChildFirstClassLoading);
    }

    public static File toFile(URL url) throws UnsupportedEncodingException {
        String path = URLDecoder.decode(url.getPath(), defaultEncoding);
        return new File(path.replace('/', File.separatorChar).replace('|', ':'));
    }

    public static ClassLoader createClassLoader(URL archiveUrl, URL[] extraUrls,
                                                ClassLoader serviceClassLoader,
                                                File tmpDir,
                                                boolean isChildFirstClassLoading) {
        List<URL> urls = new ArrayList<>();
        urls.addAll(Arrays.asList(Utils.getURLsForAllJars(archiveUrl, tmpDir)));
        if (extraUrls != null) {
            urls.addAll(Arrays.asList(extraUrls));
        }
        return createDeploymentClassLoader(urls.toArray(new URL[urls.size()]), serviceClassLoader,
                                           isChildFirstClassLoading);
    }

    private static DeploymentClassLoader createDeploymentClassLoader(
            final URL[] urls, final ClassLoader serviceClassLoader,
            final boolean isChildFirstClassLoading) {
        return AccessController
                .doPrivileged(new PrivilegedAction<DeploymentClassLoader>() {
                    public DeploymentClassLoader run() {
                        return new DeploymentClassLoader(urls,
                                                         serviceClassLoader, isChildFirstClassLoading);
                    }
                });
    }

    /**
     * This method is to process bean exclude parameter and the XML format of that would be
     * <parameter name="beanPropertyRules"> <bean class="full qualified class name"
     * excludeProperties="name,age"/>+ </parameter>
     *
     * @param service , AxisService object
     */
    public static void processBeanPropertyExclude(AxisService service) {
        Parameter excludeBeanProperty = service
                .getParameter("beanPropertyRules");
        if (excludeBeanProperty != null) {
            OMElement parameterElement = excludeBeanProperty
                    .getParameterElement();
            Iterator<OMElement> bneasItr = parameterElement.getChildrenWithName(new QName(
                    "bean"));
            ExcludeInfo excludeInfo = new ExcludeInfo();
            while (bneasItr.hasNext()) {
                OMElement bean = bneasItr.next();
                String clazz = bean.getAttributeValue(new QName(
                        DeploymentConstants.TAG_CLASS_NAME));
                String excludePropertees = bean.getAttributeValue(new QName(
                        DeploymentConstants.TAG_EXCLUDE_PROPERTIES));
                String includeProperties = bean.getAttributeValue(new QName(
                        DeploymentConstants.TAG_INCLUDE_PROPERTIES));
                excludeInfo.putBeanInfo(clazz, new BeanExcludeInfo(
                        excludePropertees, includeProperties));
            }
            service.setExcludeInfo(excludeInfo);
        }
    }

    public static String getShortFileName(String filename) {
        File file = new File(filename);
        return file.getName();
    }

    
    public static void addEndpointsToService(AxisService axisService)
            throws AxisFault {

        String serviceName = axisService.getName();
        Iterator transportInValues = null;

        if (axisService.isEnableAllTransports()) {
            AxisConfiguration axisConfiguration = axisService
                    .getAxisConfiguration();
            if (axisConfiguration != null) {
                ArrayList transports = new ArrayList();
                for (Object o : axisConfiguration.getTransportsIn().values()) {
                    TransportInDescription transportInDescription = (TransportInDescription)o;
                    transports.add(transportInDescription.getName());
                }
                transportInValues = transports.iterator();
            }
        } else {
            transportInValues = axisService.getExposedTransports().iterator();
        }

        HashMap bindingCache = new HashMap();

        if (transportInValues != null) {
            for (; transportInValues.hasNext();) {
                String transportName = (String)transportInValues.next();
                String protocol = transportName.substring(0, 1).toUpperCase()
                                  + transportName.substring(1, transportName.length())
                        .toLowerCase();

                //TODO do we use this method , we need to disable Http, SOAP11,SOAP12
                // Bindings according to parameters if we are using this
                /*
                     * populates soap11 endpoint
                     */
                String soap11EndpointName = serviceName + protocol
                                            + "Soap11Endpoint";

                AxisEndpoint httpSoap11Endpoint = new AxisEndpoint();
                httpSoap11Endpoint.setName(soap11EndpointName);
                httpSoap11Endpoint.setParent(axisService);
                httpSoap11Endpoint.setTransportInDescription(transportName);
                populateSoap11Endpoint(axisService, httpSoap11Endpoint,
                                       bindingCache);
                axisService.addEndpoint(httpSoap11Endpoint.getName(),
                                        httpSoap11Endpoint);
                // setting soap11 endpoint as the default endpoint
                axisService.setEndpointName(soap11EndpointName);

                /*
                     * generating Soap12 endpoint
                     */
                String soap12EndpointName = serviceName + protocol
                                            + "Soap12Endpoint";
                AxisEndpoint httpSoap12Endpoint = new AxisEndpoint();
                httpSoap12Endpoint.setName(soap12EndpointName);
                httpSoap12Endpoint.setParent(axisService);
                httpSoap12Endpoint.setTransportInDescription(transportName);
                populateSoap12Endpoint(axisService, httpSoap12Endpoint,
                                       bindingCache);
                axisService.addEndpoint(httpSoap12Endpoint.getName(),
                                        httpSoap12Endpoint);

                /*
                     * generating Http endpoint
                     */
                if ("http".equals(transportName)) {
                    String httpEndpointName = serviceName + protocol
                                              + "Endpoint";
                    AxisEndpoint httpEndpoint = new AxisEndpoint();
                    httpEndpoint.setName(httpEndpointName);
                    httpEndpoint.setParent(axisService);
                    httpEndpoint.setTransportInDescription(transportName);
                    populateHttpEndpoint(axisService, httpEndpoint, bindingCache);
                    axisService.addEndpoint(httpEndpoint.getName(),
                                            httpEndpoint);
                }
            }
        }
    }

    public static void setEndpointsToAllUsedBindings(AxisService axisService) {

        Map<QName, AxisBinding> usedBindings = new HashMap<QName, AxisBinding>();
        AxisBinding axisBinding = null;
        for (AxisEndpoint axisEndpoint : axisService.getEndpoints().values()) {
            axisBinding = axisEndpoint.getBinding();
            if (!usedBindings.containsKey(axisBinding.getName())) {
                usedBindings.put(axisBinding.getName(), axisBinding);
            }
        }

        // now remove all existing end points
        axisService.getEndpoints().clear();

        // add new end points according to the available transports
        List<String> availableTransports = new ArrayList<String>();
        if (axisService.isEnableAllTransports()) {
            AxisConfiguration axisConfiguration = axisService.getAxisConfiguration();
            for (TransportInDescription transportIn : axisConfiguration.getTransportsIn().values()) {
                availableTransports.add(transportIn.getName());
            }
        } else {
            availableTransports = axisService.getExposedTransports();
        }

        // add an end point per service per transport description
        for (AxisBinding usedAxisBinding : usedBindings.values()) {

            for (String transportIn : availableTransports) {
                String bindingType = usedAxisBinding.getType();
                // for http bindings we can use only the http transport
                if (WSDL2Constants.URI_WSDL2_HTTP.equals(bindingType) && !transportIn.startsWith("http")){
                    continue;
                }
                AxisEndpoint axisEndpoint = new AxisEndpoint();
                axisEndpoint.setName(axisService.getName() + transportIn + usedAxisBinding.getName().getLocalPart() + "Endpoint");
                axisEndpoint.setBinding(usedAxisBinding);
                axisEndpoint.setTransportInDescription(transportIn);
                axisEndpoint.setParent(axisService);
                axisService.addEndpoint(axisEndpoint.getName(), axisEndpoint);
                //set a valid endpoint name as the default endpoint of the service
                axisService.setEndpointName(axisEndpoint.getName());
            }
        }
    }

    public static void addEndpointsToService(AxisService axisService,
                                             AxisConfiguration axisConfiguration) throws AxisFault {

        String serviceName = axisService.getName();
        Iterator transportInValues = null;

        if (axisConfiguration != null) {
            ArrayList transports = new ArrayList();
            for (Object o : axisConfiguration.getTransportsIn().values()) {
                TransportInDescription transportInDescription = (TransportInDescription)o;
                transports.add(transportInDescription.getName());
            }
            transportInValues = transports.iterator();
        }

        HashMap bindingCache = new HashMap();
        if (transportInValues != null) {
            for (; transportInValues.hasNext();) {
                String transportName = (String)transportInValues.next();
                String protocol = transportName.substring(0, 1).toUpperCase()
                                  + transportName.substring(1, transportName.length())
                        .toLowerCase();

                // axis2.xml indicated no HTTP binding?
                boolean disableREST = false;
                Parameter disableRESTParameter = axisService
                        .getParameter(org.apache.axis2.Constants.Configuration.DISABLE_REST);
                if (disableRESTParameter != null
                    && JavaUtils.isTrueExplicitly(disableRESTParameter.getValue())) {
                    disableREST = true;
                }

                boolean disableSOAP11 = false;
                Parameter disableSOAP11Parameter = axisService
                        .getParameter(org.apache.axis2.Constants.Configuration.DISABLE_SOAP11);
                if (disableSOAP11Parameter != null
                    && JavaUtils.isTrueExplicitly(disableSOAP11Parameter.getValue())) {
                    disableSOAP11 = true;
                }

                boolean disableSOAP12 = false;
                Parameter disableSOAP12Parameter = axisService
                        .getParameter(org.apache.axis2.Constants.Configuration.DISABLE_SOAP12);
                if (disableSOAP12Parameter != null
                    && JavaUtils
                        .isTrueExplicitly(disableSOAP12Parameter.getValue())) {
                    disableSOAP12 = true;
                }

                /*
                     * populates soap11 endpoint
                     */
                if (!disableSOAP11) {
                    String soap11EndpointName = serviceName + protocol
                                                + "Soap11Endpoint";

                    AxisEndpoint httpSoap11Endpoint = new AxisEndpoint();
                    httpSoap11Endpoint.setName(soap11EndpointName);
                    httpSoap11Endpoint.setParent(axisService);
                    httpSoap11Endpoint.setTransportInDescription(transportName);
                    populateSoap11Endpoint(axisService, httpSoap11Endpoint,
                                           bindingCache);
                    axisService.addEndpoint(httpSoap11Endpoint.getName(),
                                            httpSoap11Endpoint);
                    // setting soap11 endpoint as the default endpoint
                    axisService.setEndpointName(soap11EndpointName);
                }

                /*
                     * generating Soap12 endpoint
                     */
                if (!disableSOAP12) {
                    String soap12EndpointName = serviceName + protocol
                                                + "Soap12Endpoint";
                    AxisEndpoint httpSoap12Endpoint = new AxisEndpoint();
                    httpSoap12Endpoint.setName(soap12EndpointName);
                    httpSoap12Endpoint.setParent(axisService);
                    httpSoap12Endpoint.setTransportInDescription(transportName);
                    populateSoap12Endpoint(axisService, httpSoap12Endpoint,
                                           bindingCache);
                    axisService.addEndpoint(httpSoap12Endpoint.getName(),
                                            httpSoap12Endpoint);
                }

                /*
                     * generating Http endpoint
                     */
                if (("http".equals(transportName)
                     || "https".equals(transportName)) && !disableREST) {
                    String httpEndpointName = serviceName + protocol
                                              + "Endpoint";
                    AxisEndpoint httpEndpoint = new AxisEndpoint();
                    httpEndpoint.setName(httpEndpointName);
                    httpEndpoint.setParent(axisService);
                    httpEndpoint.setTransportInDescription(transportName);
                    populateHttpEndpoint(axisService, httpEndpoint, bindingCache);
                    axisService.addEndpoint(httpEndpoint.getName(),
                                            httpEndpoint);
                }
            }
        }
    }

    private static String getEndpointName(AxisService axisService, String protocol,
            String soapVersion) {
        
        StringBuilder buffer = new StringBuilder();
        buffer.append(axisService.getName());
        buffer.append(protocol.substring(0, 1).toUpperCase());
        buffer.append(protocol.substring(1, protocol.length()).toLowerCase());
        if (soapVersion != null) {
            buffer.append(soapVersion);
        }
        buffer.append("Endpoint");
        return buffer.toString();
    }

    public static void addSoap11Endpoint(AxisService axisService, String url)
            throws Exception {
        String protocol = org.apache.axis2.util.Utils.getURIScheme(url);
        String soap11EndpointName = getEndpointName(axisService, protocol, "Soap11");

        AxisEndpoint httpSoap11Endpoint = new AxisEndpoint();
        httpSoap11Endpoint.setName(soap11EndpointName);
        httpSoap11Endpoint.setParent(axisService);
        httpSoap11Endpoint.setEndpointURL(url.toString());
        httpSoap11Endpoint.setTransportInDescription(protocol);

        populateSoap11Endpoint(axisService, httpSoap11Endpoint, null);
        axisService.addEndpoint(httpSoap11Endpoint.getName(),
                                httpSoap11Endpoint);
        // setting soap11 endpoint as the default endpoint
        axisService.setEndpointName(soap11EndpointName);
    }

    public static void addSoap11Endpoint(AxisService axisService, String protocol,
                                         String endpointName) throws Exception {
        AxisEndpoint httpSoap11Endpoint = new AxisEndpoint();
        httpSoap11Endpoint.setName(endpointName);
        httpSoap11Endpoint.setParent(axisService);
        httpSoap11Endpoint.setTransportInDescription(protocol);

        populateSoap11Endpoint(axisService, httpSoap11Endpoint, null);
        axisService.addEndpoint(httpSoap11Endpoint.getName(), httpSoap11Endpoint);
        // setting soap11 endpoint as the default endpoint
        axisService.setEndpointName(endpointName);
    }

    public static void addSoap12Endpoint(AxisService axisService, String url)
            throws Exception {
        String protocol = org.apache.axis2.util.Utils.getURIScheme(url);
        String soap12EndpointName = getEndpointName(axisService, protocol, "Soap12");

        AxisEndpoint httpSoap12Endpoint = new AxisEndpoint();
        httpSoap12Endpoint.setName(soap12EndpointName);
        httpSoap12Endpoint.setParent(axisService);
        httpSoap12Endpoint.setEndpointURL(url.toString());
        httpSoap12Endpoint.setTransportInDescription(protocol);

        populateSoap12Endpoint(axisService, httpSoap12Endpoint, null);
        axisService.addEndpoint(httpSoap12Endpoint.getName(),
                                httpSoap12Endpoint);
    }

    public static void addSoap12Endpoint(AxisService axisService, String protocol,
                                         String endpointName) throws Exception {
        AxisEndpoint httpSoap12Endpoint = new AxisEndpoint();
        httpSoap12Endpoint.setName(endpointName);
        httpSoap12Endpoint.setParent(axisService);
        httpSoap12Endpoint.setTransportInDescription(protocol);

        populateSoap12Endpoint(axisService, httpSoap12Endpoint, null);
        axisService.addEndpoint(httpSoap12Endpoint.getName(), httpSoap12Endpoint);
    }

    public static void addHttpEndpoint(AxisService axisService, String url) {
        String protocol = org.apache.axis2.util.Utils.getURIScheme(url);
        String httpEndpointName = getEndpointName(axisService, protocol, null);
        
        AxisEndpoint httpEndpoint = new AxisEndpoint();
        httpEndpoint.setName(httpEndpointName);
        httpEndpoint.setParent(axisService);
        httpEndpoint.setEndpointURL(url.toString());
        httpEndpoint.setTransportInDescription(protocol);
        populateHttpEndpoint(axisService, httpEndpoint, null);
        axisService.addEndpoint(httpEndpoint.getName(), httpEndpoint);
    }

    public static void addHttpEndpoint(AxisService axisService,
                                       String protocol, String endpointName) {
        AxisEndpoint httpEndpoint = new AxisEndpoint();
        httpEndpoint.setName(endpointName);
        httpEndpoint.setParent(axisService);
        httpEndpoint.setTransportInDescription(protocol);
        populateHttpEndpoint(axisService, httpEndpoint, null);
        axisService.addEndpoint(httpEndpoint.getName(), httpEndpoint);
    }

    public static void processPolicyAttachments(Iterator attachmentElements,
                                                AxisService service) throws XMLStreamException,
            FactoryConfigurationError {
        OMElement attachmentElement;
        HashMap attachmentsMap = new HashMap();

        for (; attachmentElements.hasNext();) {
            attachmentElement = (OMElement)attachmentElements.next();
            OMElement appliesToElem = null;
            for (Iterator it = attachmentElement.getChildElements(); it.hasNext(); ) {
                OMElement elem = (OMElement)it.next();
                if (org.apache.neethi.Constants.isPolicyNS(elem.getNamespaceURI())
                        && elem.getLocalName().equals(DeploymentConstants.TAG_APPLIES_TO)) {
                    appliesToElem = elem;
                    break;
                }
            }
            ArrayList policyComponents = new ArrayList();

            // process <wsp:Policy> elements ..
            for (Iterator policyElements = PolicyUtil.getPolicyChildren(attachmentElement);
                    policyElements.hasNext();) {
                PolicyComponent policy = PolicyUtil
                        .getPolicyFromOMElement((OMElement)policyElements
                                .next());
                policyComponents.add(policy);
            }

            // process <wsp:PolicyReference> elements ..
            for (Iterator policyRefElements = PolicyUtil.getPolicyRefChildren(attachmentElement);
                    policyRefElements.hasNext();) {
                PolicyComponent policyRef = PolicyUtil
                        .getPolicyReferenceFromOMElement((OMElement)policyRefElements
                                .next());
                policyComponents.add(policyRef);
            }

            for (Iterator<OMElement> policySubjects = appliesToElem
                    .getChildrenWithName(new QName("policy-subject")); policySubjects
                    .hasNext();) {
                OMElement policySubject = policySubjects.next();
                String identifier = policySubject.getAttributeValue(new QName(
                        "identifier"));

                ArrayList values = (ArrayList)attachmentsMap.get(identifier);
                if (values == null) {
                    values = new ArrayList();
                    attachmentsMap.put(identifier, values);
                }
                values.addAll(policyComponents);
            }
        }

        for (Object o : attachmentsMap.keySet()) {
            String identifier = (String)o;
            if (identifier.startsWith("binding:soap")) {
                processSoapAttachments(identifier, (List)attachmentsMap
                        .get(identifier), service);
            }
        }
    }

    private static void populateSoap11Endpoint(AxisService axisService,
                                               AxisEndpoint axisEndpoint, HashMap bindingCache) {
        String serviceName = axisService.getName();
        String name = serviceName + "Soap11Binding";

        Parameter parameter =
                axisService.getParameter(Java2WSDLConstants.SOAP11_BINDING_NAME_OPTION_LONG);
        if (parameter != null) {
            name = (String) parameter.getValue();
        }

        QName bindingName = new QName(name);

        AxisBinding axisBinding = (bindingCache != null) ? (AxisBinding)bindingCache
                .get(name)
                : null;
        if (axisBinding == null) {
            axisBinding = new AxisBinding();
            axisBinding.setName(bindingName);

            axisBinding.setType(Java2WSDLConstants.TRANSPORT_URI);
            axisBinding.setProperty(WSDLConstants.WSDL_1_1_STYLE,
                                    WSDLConstants.STYLE_DOC);

            axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
                                    SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI);

            for (Iterator iterator = axisService.getChildren(); iterator
                    .hasNext();) {
                AxisOperation operation = (AxisOperation)iterator.next();
                AxisBindingOperation axisBindingOperation = new AxisBindingOperation();

                axisBindingOperation.setName(operation.getName());
                axisBindingOperation.setAxisOperation(operation);

                String soapAction = operation.getSoapAction();
                if (soapAction != null) {
                    axisBindingOperation.setProperty(
                            WSDL2Constants.ATTR_WSOAP_ACTION, soapAction);
                }
                axisBinding.addChild(axisBindingOperation.getName(),
                                     axisBindingOperation);
                populateBindingOperation(axisBinding,
                                         axisBindingOperation);
            }
            if (bindingCache != null) {
                bindingCache.put(name, axisBinding);
            }
        }
        axisBinding.setParent(axisEndpoint);
        axisEndpoint.setBinding(axisBinding);
    }

    private static void populateSoap12Endpoint(AxisService axisService,
                                               AxisEndpoint axisEndpoint, HashMap bindingCache) {
        String serviceName = axisService.getName();
        String name = serviceName + "Soap12Binding";

        Parameter parameter =
                axisService.getParameter(Java2WSDLConstants.SOAP12_BINDING_NAME_OPTION_LONG);
        if (parameter != null) {
            name = (String) parameter.getValue();
        }

        QName bindingName = new QName(name);

        AxisBinding axisBinding = (bindingCache != null) ? (AxisBinding)bindingCache
                .get(name)
                : null;
        if (axisBinding == null) {
            axisBinding = new AxisBinding();
            axisBinding.setName(bindingName);

            axisBinding.setType(Java2WSDLConstants.TRANSPORT_URI);
            axisBinding.setProperty(WSDLConstants.WSDL_1_1_STYLE,
                                    WSDLConstants.STYLE_DOC);

            axisBinding.setProperty(WSDL2Constants.ATTR_WSOAP_VERSION,
                                    SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI);

            for (Iterator iterator = axisService.getChildren(); iterator
                    .hasNext();) {
                AxisOperation operation = (AxisOperation)iterator.next();
                AxisBindingOperation axisBindingOperation = new AxisBindingOperation();

                axisBindingOperation.setName(operation.getName());
                axisBindingOperation.setAxisOperation(operation);

                String soapAction = operation.getSoapAction();
                if (soapAction != null) {
                    axisBindingOperation.setProperty(
                            WSDL2Constants.ATTR_WSOAP_ACTION, soapAction);
                }
                axisBinding.addChild(axisBindingOperation.getName(),
                                     axisBindingOperation);

                populateBindingOperation(axisBinding,
                                         axisBindingOperation);
            }
            if (bindingCache != null) {
                bindingCache.put(name, axisBinding);
            }
        }
        axisBinding.setParent(axisEndpoint);
        axisEndpoint.setBinding(axisBinding);
    }

    private static void populateHttpEndpoint(AxisService axisService,
                                             AxisEndpoint axisEndpoint, HashMap bindingCache) {
        String serviceName = axisService.getName();
        String name = serviceName + "HttpBinding";

        Parameter param =
                axisService.getParameter(Java2WSDLConstants.REST_BINDING_NAME_OPTION_LONG);
        if (param != null) {
            name = (String) param.getValue();
        }

        QName bindingName = new QName(name);

        AxisBinding axisBinding = (bindingCache != null) ? (AxisBinding)bindingCache
                .get(name)
                : null;

        if (axisBinding == null) {
            axisBinding = new AxisBinding();
            axisBinding.setName(bindingName);

            axisBinding.setType(WSDL2Constants.URI_WSDL2_HTTP);
            axisBinding.setProperty(WSDL2Constants.ATTR_WHTTP_METHOD, "POST");


            Map httpLocationMap = new TreeMap<String, AxisOperation>(new Comparator() {
                public int compare(Object o1, Object o2) {
                    return (-1 * ((Comparable) o1).compareTo(o2));
                }
            });

            for (Iterator iterator = axisService.getChildren(); iterator
                    .hasNext();) {
                AxisOperation operation = (AxisOperation)iterator.next();
                AxisBindingOperation axisBindingOperation = new AxisBindingOperation();

                QName operationQName = operation.getName();
                axisBindingOperation.setName(operationQName);
                axisBindingOperation.setAxisOperation(operation);
                String httpLocation = operationQName.getLocalPart();
                String tempParam=null;
                String tempHTTPMethodParam=null;
                String tempHTTPLocationParam=null;
                // dealing with the REST data specified in Service.xml @ service class itself(using annotations)
                Parameter parameter = operation.getParameter(Constants.JSR311_ANNOTATIONS);
                JAXRSModel methodModel = (parameter != null && (parameter.getValue() instanceof
                        JAXRSModel)) ? (JAXRSModel) parameter.getValue() : null;

                // Setting the Produces value in the operation
                if ( (tempParam=Utils.getHTTPOutputSerializationFromservicesXML(operation)) != null) { // first we get produce from services xml if available

                        axisBindingOperation.setProperty(
                                WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION,
                                tempParam);


                } else if (methodModel != null && methodModel.getProduces() !=
                        null) {  // then get it from the JAX-RS if available
                    axisBindingOperation.setProperty(
                            WSDL2Constants.ATTR_WHTTP_OUTPUT_SERIALIZATION,
                            methodModel.getProduces());

                }

                //Setting the Consumes value in the operation
                if ((tempParam=Utils.getHTTPInputSerializationFromServicesXML(operation)) != null) { // first we get Consumes from services xml if available
                     axisBindingOperation.setProperty(
                                WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION,
                                tempParam);

                } else if (methodModel != null && methodModel.getConsumes() !=
                        null) {  // then get it from the JAX-RS if available

                    axisBindingOperation.setProperty(
                            WSDL2Constants.ATTR_WHTTP_INPUT_SERIALIZATION,
                            methodModel.getConsumes());

                }


                
                //Setting the HttpMethod in the operation
                if ((tempHTTPMethodParam=Utils.getHTTPMethodFromServicesXML(operation))!=null) { // first we get Consumes from services xml if available

                        axisBindingOperation.setProperty(
                                WSDL2Constants.ATTR_WHTTP_METHOD,
                                tempHTTPMethodParam);

                } else if (methodModel != null && (tempHTTPMethodParam=methodModel.getHTTPMethod()) !=
                        null) {  // then get it from the JAX-RS if available

                     if (tempHTTPMethodParam.equals(Constants.Configuration.HTTP_METHOD_HEAD)) {
                                log.warn("[JAXRS] http method HEAD is not supported by AXIS2  " +
                                        operation.getName());
                                tempHTTPMethodParam=null;  // resetting the HTTP Method if it is head
                            } else {

                                axisBindingOperation.setProperty(WSDL2Constants.ATTR_WHTTP_METHOD,
                                                                 tempHTTPMethodParam);
                            }

                }



                //setting the Http Location in the operation
                if((tempHTTPLocationParam=Utils.getHTTPLoacationFromServicesXML(operation) )==null){
                    tempHTTPLocationParam=(methodModel!=null)?methodModel.getPath():null;
                }


                if (tempHTTPLocationParam!=null && tempHTTPMethodParam != null ){
                     axisBindingOperation
                                .setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, tempHTTPLocationParam);
                        httpLocationMap.put(WSDLUtil.getConstantFromHTTPLocation(tempHTTPLocationParam,
                                                                                 tempHTTPMethodParam),operation);

                }else if(tempHTTPLocationParam != null && tempHTTPMethodParam == null){
                     axisBindingOperation
                                .setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, tempHTTPLocationParam);
                        httpLocationMap.put(WSDLUtil.getConstantFromHTTPLocation(tempHTTPLocationParam,
                                                                                 Constants.Configuration.HTTP_METHOD_POST),operation);
                } else if(tempHTTPLocationParam == null && tempHTTPMethodParam != null){
                      axisBindingOperation
                                .setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocation);
                        httpLocationMap.put(WSDLUtil.getConstantFromHTTPLocation(httpLocation,
                                                                                 tempHTTPMethodParam),operation);
                } else{  // default scenario : No REST related params in services XML or source file
                    axisBindingOperation
                            .setProperty(WSDL2Constants.ATTR_WHTTP_LOCATION, httpLocation);
                }


                axisBinding.addChild(axisBindingOperation.getName(),
                                     axisBindingOperation);

                populateBindingOperation(axisBinding,
                                         axisBindingOperation);

                // resetting my temperory parameters
                tempParam=null;
                tempHTTPMethodParam=null;
                tempHTTPLocationParam=null;

            }

            if (!httpLocationMap.isEmpty()) {
                axisBinding.setProperty(WSDL2Constants.HTTP_LOCATION_TABLE, httpLocationMap);
            }

            if (bindingCache != null) {
                bindingCache.put(name, axisBinding);
            }
        }
        axisBinding.setParent(axisEndpoint);
        axisEndpoint.setBinding(axisBinding);
    }

    private static void populateBindingOperation(AxisBinding axisBinding,
                                                 AxisBindingOperation axisBindingOperation) {

        AxisOperation axisOperation = axisBindingOperation.getAxisOperation();

        if (WSDLUtil.isInputPresentForMEP(axisOperation
                .getMessageExchangePattern())) {
            AxisMessage axisInMessage = axisOperation
                    .getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            AxisBindingMessage axisBindingInMessage = new AxisBindingMessage();

            axisBindingInMessage.setName(axisInMessage.getName());
            axisBindingInMessage.setDirection(axisInMessage.getDirection());
            axisBindingInMessage.setAxisMessage(axisInMessage);

            axisBindingInMessage.setParent(axisBindingOperation);
            axisBindingOperation.addChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE,
                                          axisBindingInMessage);
        }

        if (WSDLUtil.isOutputPresentForMEP(axisOperation
                .getMessageExchangePattern())) {
            AxisMessage axisOutMessage = axisOperation
                    .getMessage(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            AxisBindingMessage axisBindingOutMessage = new AxisBindingMessage();

            axisBindingOutMessage.setName(axisOutMessage.getName());
            axisBindingOutMessage.setDirection(axisOutMessage.getDirection());
            axisBindingOutMessage.setAxisMessage(axisOutMessage);

            axisBindingOutMessage.setParent(axisBindingOperation);
            axisBindingOperation.addChild(
                    WSDLConstants.MESSAGE_LABEL_OUT_VALUE,
                    axisBindingOutMessage);
        }

        ArrayList faultMessagesList = axisOperation.getFaultMessages();
        for (Object aFaultMessagesList : faultMessagesList) {
            AxisMessage axisFaultMessage = (AxisMessage)aFaultMessagesList;
            AxisBindingMessage axisBindingFaultMessage = new AxisBindingMessage();
            axisBindingFaultMessage.setName(axisFaultMessage.getName());
            axisBindingFaultMessage.setFault(true);
            axisBindingFaultMessage.setAxisMessage(axisFaultMessage);
            axisBindingFaultMessage.setParent(axisBindingOperation);
            axisBindingOperation.addFault(axisBindingFaultMessage);
            axisBinding.addFault(axisBindingFaultMessage);
        }

        axisBindingOperation.setAxisOperation(axisOperation);
        axisBindingOperation.setParent(axisBinding);
    }

    private static void processSoapAttachments(String identifier,
                                               List policyComponents, AxisService service) {
        Map map = service.getEndpoints();
        String soapVersion =
                (identifier.indexOf("soap12") > -1) ? SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI
                        : SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI;

        for (Object o : map.values()) {
            AxisEndpoint axisEndpoint = (AxisEndpoint)o;
            AxisBinding axisBinding = axisEndpoint.getBinding();
            String wsoap = (String)axisBinding
                    .getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
            if (soapVersion.equals(wsoap)) {
                String[] identifiers = identifier.split("/");
                int key = identifiers.length;
                if (key == 1) {
                    axisBinding.getPolicySubject().attachPolicyComponents(
                            policyComponents);
                } else if (key == 2 || key == 3) {
                    String opName = identifiers[1];
                    opName = opName.substring(opName.indexOf(":") + 1, opName
                            .length());
                    AxisBindingOperation bindingOperation = null;
                    boolean found = false;
                    for (Iterator i = axisBinding.getChildren(); i.hasNext();) {
                        bindingOperation = (AxisBindingOperation)i.next();
                        if (opName.equals(bindingOperation.getName()
                                .getLocalPart())) {
                            found = true;
                            break;
                        }
                    }
                    if (!found) {
                        throw new IllegalArgumentException(
                                "Invalid binding operation " + opName);
                    }

                    if (key == 2) {
                        bindingOperation.getPolicySubject()
                                .attachPolicyComponents(policyComponents);
                    } else {
                        if ("in".equals(identifiers[2])) {
                            AxisBindingMessage bindingInMessage =
                                    (AxisBindingMessage)bindingOperation
                                            .getChild(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                            bindingInMessage.getPolicySubject()
                                    .attachPolicyComponents(policyComponents);

                        } else if ("out".equals(identifiers[2])) {
                            AxisBindingMessage bindingOutMessage =
                                    (AxisBindingMessage)bindingOperation
                                            .getChild(WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                            bindingOutMessage.getPolicySubject()
                                    .attachPolicyComponents(policyComponents);
                        } else {
                            // FIXME faults
                        }
                    }
                }
                break;
            }
        }
    }

    public static boolean isSoap11Binding(AxisBinding binding) {
        String type = binding.getType();
        if (Java2WSDLConstants.TRANSPORT_URI.equals(type)
            || WSDL2Constants.URI_WSDL2_SOAP.equals(type)) {
            String v = (String)binding
                    .getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
            if (SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(v)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isSoap12Binding(AxisBinding binding) {
        String type = binding.getType();
        if (Java2WSDLConstants.TRANSPORT_URI.equals(type)
            || WSDL2Constants.URI_WSDL2_SOAP.equals(type)) {
            String v = (String)binding
                    .getProperty(WSDL2Constants.ATTR_WSOAP_VERSION);
            if (SOAP12Constants.SOAP_ENVELOPE_NAMESPACE_URI.equals(v)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isHttpBinding(AxisBinding binding) {
        String type = binding.getType();
        return WSDL2Constants.URI_WSDL2_HTTP.equals(type);
    }

    public static AxisBinding getSoap11Binding(AxisService service) {
        for (Object o : service.getEndpoints().values()) {
            AxisEndpoint endpoint = (AxisEndpoint)o;
            AxisBinding binding = endpoint.getBinding();

            if (isSoap11Binding(binding)) {
                return binding;
            }
        }
        return null;
    }

    public static AxisBinding getSoap12Binding(AxisService service) {
        for (Object o : service.getEndpoints().values()) {
            AxisEndpoint endpoint = (AxisEndpoint)o;
            AxisBinding binding = endpoint.getBinding();

            if (isSoap12Binding(binding)) {
                return binding;
            }
        }
        return null;
    }

    public static AxisBinding getHttpBinding(AxisService service) {
        for (Object o : service.getEndpoints().values()) {
            AxisEndpoint endpoint = (AxisEndpoint)o;
            AxisBinding binding = endpoint.getBinding();

            if (isHttpBinding(binding)) {
                return binding;
            }
        }
        return null;
    }

    public static AxisBindingOperation getBindingOperation(AxisBinding binding,
                                                           AxisOperation operation) {
        QName opName = operation.getName();
        for (Iterator bindingOps = binding.getChildren(); bindingOps.hasNext();) {
            AxisBindingOperation bindingOp = (AxisBindingOperation)bindingOps.next();
            if (opName.equals(bindingOp.getName())) {
                return bindingOp;
            }
        }
        return null;
    }

    public static AxisBindingMessage getBindingMessage(AxisBindingOperation bindingOperation,
                                                       AxisMessage message) {
        String msgName = message.getName();
        for (Iterator bindingMessages = bindingOperation.getChildren();
             bindingMessages.hasNext();) {
            AxisBindingMessage bindingMessage = (AxisBindingMessage)bindingMessages.next();
            if (msgName.equals(bindingMessage.getName())) {
                return bindingMessage;
            }
        }
        return null;
    }


    /**
     * extract the http location from services xml related to given operation
     * @param operation
     * @return
     */
    public static String getHTTPLoacationFromServicesXML(AxisOperation operation) {

        Parameter locationParam = operation.getParameter(Constants.Configuration.REST_LOCATION_PARAM);
        if (locationParam != null && locationParam.getValue() != null &&
                locationParam.getValue() instanceof String) {
            String location = ((String) locationParam.getValue()).trim();
            if (location.equals("")) {
                return null;
            } else{
                if(location.startsWith("/")){
                   location= location.substring(1);
                }

                return location;

            }
        } else
            return null;

    }

    /**
     * extract the http method from services xml related to given operation
     * @param operation
     * @return
     */
    public static String getHTTPMethodFromServicesXML(AxisOperation operation) {
        Parameter methodParam = operation.getParameter(Constants.Configuration.REST_METHOD_PARAM);
        if (methodParam != null && methodParam.getValue() != null &&
                methodParam.getValue() instanceof String) {
            String method = ((String) methodParam.getValue()).trim();
            if (method.equals("")) {
                return null;
            } else if (method.equals(Constants.Configuration.HTTP_METHOD_GET) || method.equals(Constants.Configuration.HTTP_METHOD_POST) ||
                    method.equals(Constants.Configuration.HTTP_METHOD_PUT) || method.equals(Constants.Configuration.HTTP_METHOD_DELETE)) {
                return method;
            } else if (method.equals(Constants.Configuration.HTTP_METHOD_HEAD)) {
                log.warn("Axis2 doesn't support httpMethod HEAD ");
                return null;
            } else {
                log.warn("cannot identify the HTTP method");
                return null;
            }
        } else
            return null;
    }

    /**
     * get http input mime type from the services xml
     * @param operation
     * @return
     */
    public static String getHTTPInputSerializationFromServicesXML(AxisOperation operation) {
        Parameter inputSerializationParam = operation.getParameter(Constants.Configuration.REST_INPUTSERIALIZE_PARAM);
        if (inputSerializationParam != null && inputSerializationParam.getValue() != null
                && inputSerializationParam.getValue() instanceof String) {
            String inputSerialization = ((String) inputSerializationParam.getValue()).trim();

            if (inputSerialization.equals("")) {
                return null;
            } else {
                String[] array = inputSerialization.split(",");
                if (array.length > 1) {
                    log.warn("WSDL2 supports only one input-serialization");
                    return array[0];
                } else {
                    return array[0];
                }
            }

        } else
            return null;


    }

    /**
     * get http output mime type from the services xml
     * @param operation
     * @return
     */
    public static String getHTTPOutputSerializationFromservicesXML(AxisOperation operation) {
        Parameter outputSerializationParam = operation.getParameter(Constants.Configuration.REST_OUTPUTSERIALIZE_PARAM);
        if (outputSerializationParam != null && outputSerializationParam.getValue() != null
                && outputSerializationParam.getValue() instanceof String) {
            String outputSerialization = ((String) outputSerializationParam.getValue()).trim();

            if (outputSerialization.equals("")) {
                return null;
            } else {
                String[] array = outputSerialization.split(",");
                if (array.length > 1) {
                    log.warn("WSDL2 supports only one input-serialization");
                    return array[0];
                } else {
                    return array[0];
                }
            }
        } else
            return null;

    }

    /**
     * Log faulty services info due to unavailability of modules and transports
     * @param axisConfig
     */
    public static void logFaultyServiceInfo(AxisConfiguration axisConfig){
        Map<String, Map<String, FaultyServiceData>> faultyServices = axisConfig.getFaultyServicesDuetoModules();
        if (faultyServices != null && !faultyServices.isEmpty()) {
            for (String moduleName : faultyServices.keySet()) {
                Map<String, FaultyServiceData> faultyServicesDueToModule = faultyServices.get(moduleName);
                for (String s : faultyServicesDueToModule.keySet()) {
                    log.warn("Service :" + s + " is faulty, due to unavailability of the module :" + moduleName);        
                }
            }
        }
    }

    /**
     * Computes the hierarchical part of the service name if this is such a service path.
     * Ex:  filePath = .../repository/services/foo/1.0.0/version.aar -> "foo/1.0.0/"
     *      filePath = .../repository/services/version.aar -> ""
     * @param filePath - input file path of the deploying file
     * @param serviceDir - 'services', 'pojo', 'servicejars' etc..
     * @return hierarchical path. either "" or a '/' separated string (Ex: foo/1.0.0/)
     */
    public static String getServiceHierarchy(String filePath, String serviceDir) {
        if (filePath == null || serviceDir == null) {
            return "";
        }
        filePath = formatPath(filePath);
        String[] splited = filePath.split("/" + serviceDir + "/");
        String serviceHierarchy = null;
        if (splited.length > 1) {
            String temp = splited[splited.length - 1];
            //if this is a directory, there can be a '/' at the end. Remove it..
            if (temp.endsWith("/")) {
                temp = temp.substring(0, temp.length() - 1);
            }
            //if this is not a hierarchical path, return a 0 length string
            if (temp.lastIndexOf('/') == -1) {
                return "";
            }
            serviceHierarchy = temp.substring(0, temp.lastIndexOf('/') + 1);
        }
        return serviceHierarchy;
    }

    /**
     * Format the string paths to match any platform.. windows, linux etc..
     *
     * @param path - input file path
     * @return formatted file path
     */
    public static String formatPath(String path) {
        // removing white spaces
        path = path.replaceAll("\\b\\s+\\b", "%20");
        // replacing all "\" with "/"
        return path.replace('\\', '/');
    }

    /**
     * Get the class name from a resource name referring to a class file.
     * 
     * @param resourceName the resource name
     * @return the class name
     */
    public static String getClassNameFromResourceName(String resourceName) {
        if (!resourceName.endsWith(".class")) {
            throw new IllegalArgumentException("The resource name doesn't refer to a class file");
        }
        return resourceName.substring(0, resourceName.length()-6).replace('/', '.');
    }
    
    /**
     * Scan a JAR file and return the list of classes contained in the JAR.
     * 
     * @param deploymentFileData
     *            the JAR to scan
     * @return a list of Java class names
     * @throws DeploymentException
     *             if an error occurs while scanning the file
     */
    public static List<String> getListOfClasses(DeploymentFileData deploymentFileData) throws DeploymentException {
        try {           
            List<String> classList = null;
            if(DeploymentFileData.isServiceArchiveFile(deploymentFileData.getAbsolutePath())){
                FileInputStream fin = new FileInputStream(deploymentFileData.getAbsolutePath());
                try {
                    ZipInputStream zin = new ZipInputStream(fin);
                    try {
                        ZipEntry entry;
                        classList = new ArrayList<String>();
                        while ((entry = zin.getNextEntry()) != null) {
                            String name = entry.getName();
                            if (name.endsWith(".class")) {
                                classList.add(getClassNameFromResourceName(name));
                            }
                        }
                        return classList;
                    } finally {
                        zin.close();
                    }
                } finally {
                    fin.close();
                }                
            } else {
                File directory = deploymentFileData.getFile();
                classList = new ArrayList<String>();
                for(Iterator<File> fileItr= FileUtils.iterateFiles(directory, new String[]{"class"}, true); fileItr.hasNext();){
                    String fileName = fileItr.next().getPath();
                    String className = getClassNameFromResourceName(fileName.replace(directory.getPath(), "").substring(1));                   
                    classList.add(className);                    
                }
                return classList;             
            }
        } catch (IOException e) {
            log.debug(Messages.getMessage(DeploymentErrorMsgs.DEPLOYING_EXCEPTION, e.getMessage()), e);
            throw new DeploymentException(e);
        }
    }
}
