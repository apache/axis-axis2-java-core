package org.apache.axis2.deployment.util;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.repository.util.ArchiveReader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisOperationFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.Flow;
import org.apache.axis2.description.HandlerDescription;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.Handler;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.Loader;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.apache.ws.java2wsdl.AnnotationConstants;
import org.apache.ws.java2wsdl.Java2WSDLConstants;
import org.apache.ws.java2wsdl.SchemaGenerator;
import org.apache.ws.java2wsdl.utils.TypeTable;
import org.codehaus.jam.JAnnotation;
import org.codehaus.jam.JMethod;

import javax.xml.namespace.QName;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/*
* Copyright 2004,2005 The Apache Software Foundation.
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
*
*/

public class Utils {

    private static Log log = LogFactory.getLog(Utils.class);

    public static void addFlowHandlers(Flow flow, ClassLoader clsLoader) throws AxisFault {
        int count = flow.getHandlerCount();

        for (int j = 0; j < count; j++) {
            HandlerDescription handlermd = flow.getHandler(j);
            Class handlerClass;
            Handler handler;

            handlerClass = getHandlerClass(handlermd.getClassName(), clsLoader);

            try {
                handler = (Handler) handlerClass.newInstance();
                handler.init(handlermd);
                handlermd.setHandler(handler);
            } catch (InstantiationException e) {
                throw new AxisFault(e);
            } catch (IllegalAccessException e) {
                throw new AxisFault(e);
            }
        }
    }

    public static void loadHandler(ClassLoader loader1, HandlerDescription desc)
            throws DeploymentException {
        String handlername = desc.getClassName();
        Handler handler;
        Class handlerClass;

        try {
            handlerClass = Loader.loadClass(loader1, handlername);
            handler = (Handler) handlerClass.newInstance();
            handler.init(desc);
            desc.setHandler(handler);
        } catch (ClassNotFoundException e) {
            throw new DeploymentException(e);
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    public static URL[] getURLsForAllJars(URL url, boolean antiJARLocking) {
        try {
            ArrayList array = new ArrayList();
            String urlString = url.toString();
            InputStream in = url.openStream();
            ZipInputStream zin;
            FileInputStream fin = null;
            if (antiJARLocking) {
                File inputFile = createTempFile(urlString.substring(urlString.length() - 4), in);
                in.close();
                array.add(inputFile.toURL());
                fin = new FileInputStream(inputFile);
                zin = new ZipInputStream(fin);
            } else {
                array.add(url);
                zin = new ZipInputStream(in);
            }
            ZipEntry entry;
            String entryName;
            while ((entry = zin.getNextEntry()) != null) {
                entryName = entry.getName();
                /**
                 * id the entry name start with /lib and end with .jar
                 * then those entry name will be added to the arraylist
                 */
                if ((entryName != null) && entryName.toLowerCase().startsWith("lib/")
                        && entryName.toLowerCase().endsWith(".jar")) {
                    String suffix = entryName.substring(4);
                    File f = createTempFile(suffix, zin);
                    array.add(f.toURL());
                }
            }
            zin.close();
            if (!antiJARLocking) {
                in.close();
            }
            if (fin != null) {
                fin.close();
            }
            return (URL[]) array.toArray(new URL[array.size()]);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static File createTempFile(String suffix, InputStream in) throws IOException {
        byte data[] = new byte[2048];
        int count;
        new File(System.getProperty("java.io.tmpdir")).mkdirs();
        File f = File.createTempFile("axis2", suffix);
        f.deleteOnExit();
        FileOutputStream out = new FileOutputStream(f);
        while ((count = in.read(data, 0, 2048)) != -1) {
            out.write(data, 0, count);
        }
        out.close();
        return f;
    }

    public static ClassLoader getClassLoader(ClassLoader parent, String path)
            throws DeploymentException {
        return getClassLoader(parent, new File(path));
    }

    public static ClassLoader getClassLoader(ClassLoader parent, File file)
            throws DeploymentException {
        URLClassLoader classLoader;

        if (file != null) {
            try {
                ArrayList urls = new ArrayList();
                urls.add(file.toURL());
                // lower case directory name
                File libfiles = new File(file, "lib");
                if (libfiles.exists()) {
                    urls.add(libfiles.toURL());
                    File jarfiles[] = libfiles.listFiles();
                    for (int i = 0; i < jarfiles.length; i++) {
                        File jarfile = jarfiles[i];
                        if (jarfile.getName().endsWith(".jar")) {
                            urls.add(jarfile.toURL());
                        }
                    }
                } else {
                    // upper case directory name
                    libfiles = new File(file, "Lib");
                    if (libfiles.exists()) {
                        urls.add(libfiles.toURL());
                        File jarfiles[] = libfiles.listFiles();
                        for (int i = 0; i < jarfiles.length; i++) {
                            File jarfile = jarfiles[i];
                            if (jarfile.getName().endsWith(".jar")) {
                                urls.add(jarfile.toURL());
                            }
                        }
                    }
                }

                URL urllist[] = new URL[urls.size()];
                for (int i = 0; i < urls.size(); i++) {
                    urllist[i] = (URL) urls.get(i);
                }
                classLoader = new URLClassLoader(urllist, parent);
                return classLoader;
            } catch (MalformedURLException e) {
                throw new DeploymentException(e);
            }
        }

        return null;
    }

    private static Class getHandlerClass(String className, ClassLoader loader1) throws AxisFault {
        Class handlerClass;

        try {
            handlerClass = Loader.loadClass(loader1, className);
        } catch (ClassNotFoundException e) {
            throw new AxisFault(e.getMessage());
        }

        return handlerClass;
    }

    /**
     * This guy will create a AxisService using java reflection
     */
    public static void fillAxisService(AxisService axisService,
                                       AxisConfiguration axisConfig,
                                       ArrayList excludeOperations,
                                       ArrayList nonRpcMethods) throws Exception {
        String serviceClass;
        Parameter implInfoParam = axisService.getParameter(Constants.SERVICE_CLASS);
        ClassLoader serviceClassLoader = axisService.getClassLoader();

        if (implInfoParam != null) {
            serviceClass = (String) implInfoParam.getValue();
        } else {
            // if Service_Class is null, every AbstractMR will look for
            // ServiceObjectSupplier. This is user specific and may contain
            // other looks.
            implInfoParam = axisService.getParameter(Constants.SERVICE_OBJECT_SUPPLIER);
            if (implInfoParam != null) {
                Class serviceObjectMaker = Loader.loadClass(serviceClassLoader, ((String)
                        implInfoParam.getValue()).trim());

                // Find static getServiceObject() method, call it if there
                Method method = serviceObjectMaker.
                        getMethod("getServiceObject",
                                  new Class[]{AxisService.class});
                Object obj = null;
                if (method != null) {
                    obj = method.invoke(serviceObjectMaker.newInstance(),
                                        new Object[]{axisService});
                }
                if (obj == null) {
                    log.warn("ServiceObjectSupplier implmentation Object could not be found");
                    throw new DeploymentException(
                            "ServiceClass or ServiceObjectSupplier implmentation Object could not be found");
                }
                serviceClass = obj.getClass().getName();
            } else {
                return;
            }
        }
        // adding name spaces
        NamespaceMap map = new NamespaceMap();
        map.put(Java2WSDLConstants.AXIS2_NAMESPACE_PREFIX,
                Java2WSDLConstants.AXIS2_XSD);
        map.put(Java2WSDLConstants.DEFAULT_SCHEMA_NAMESPACE_PREFIX,
                Java2WSDLConstants.URI_2001_SCHEMA_XSD);
        axisService.setNameSpacesMap(map);
        SchemaGenerator schemaGenerator = new SchemaGenerator(serviceClassLoader,
                                                              serviceClass.trim(),
                                                              axisService.getSchematargetNamespace(),
                                                              axisService.getSchematargetNamespacePrefix());
        schemaGenerator.setExcludeMethods(excludeOperations);
        schemaGenerator.setNonRpcMethods(nonRpcMethods);
        if (!axisService.isElementFormDefault()) {
            schemaGenerator.setElementFormDefault(Java2WSDLConstants.FORM_DEFAULT_UNQUALIFIED);
        }
        // package to namespace map
        schemaGenerator.setPkg2nsmap(axisService.getP2nMap());
        Collection schemas = schemaGenerator.generateSchema();
        axisService.addSchema(schemas);
        axisService.setSchematargetNamespace(schemaGenerator.getSchemaTargetNameSpace());
        axisService.setTypeTable(schemaGenerator.getTypeTable());
        if (Java2WSDLConstants.DEFAULT_TARGET_NAMESPACE.equals(
                axisService.getTargetNamespace())) {
            axisService.setTargetNamespace(schemaGenerator.getTargetNamespace());
        }

        JMethod [] method = schemaGenerator.getMethods();
        TypeTable table = schemaGenerator.getTypeTable();
        PhasesInfo pinfo = axisConfig.getPhasesInfo();


        for (int i = 0; i < method.length; i++) {
            JMethod jmethod = method[i];
            JAnnotation methodAnnon = jmethod.getAnnotation(AnnotationConstants.WEB_METHOD);
            if (methodAnnon != null) {
                if (methodAnnon.getValue(AnnotationConstants.EXCLUDE).asBoolean()) {
                    continue;
                }
            }
            if (!jmethod.isPublic()) {
                // no need to expose , private and protected methods
                continue;
            }
            if (excludeOperations.contains(jmethod.getSimpleName())) {
                continue;
            }
            String opName = jmethod.getSimpleName();
            AxisOperation operation = axisService.getOperation(new QName(opName));
            // if the operation there in services.xml then try to set it schema element name
            if (operation != null) {
                AxisMessage inMessage = operation.getMessage(
                        WSDLConstants.MESSAGE_LABEL_IN_VALUE);
                if (inMessage != null) {
                    inMessage.setName(opName + Java2WSDLConstants.MESSAGE_SUFFIX);
                    QName complexSchemaType = table.getComplexSchemaType(jmethod.getSimpleName());
                    inMessage.setElementQName(complexSchemaType);
                    if (complexSchemaType != null) {
                        axisService.addMessageElementQNameToOperationMapping(complexSchemaType,
                                                                             operation);
                    }
                }
                if (!jmethod.getReturnType().isVoidType()) {
                    AxisMessage outMessage = operation.getMessage(
                            WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                    QName qNamefortheType = table.getQNamefortheType(jmethod.getSimpleName() +
                            Java2WSDLConstants.RESPONSE);
                    outMessage.setElementQName(qNamefortheType);
                    if (qNamefortheType != null) {
                        axisService.addMessageElementQNameToOperationMapping(qNamefortheType,
                                                                             operation);
                    }
                    outMessage.setName(opName + Java2WSDLConstants.RESPONSE);
                }
                if (jmethod.getExceptionTypes().length > 0) {
                    AxisMessage faultMessage = new AxisMessage();
                    faultMessage.setName(jmethod.getSimpleName() + "Fault");
                    faultMessage.setElementQName(
                            table.getComplexSchemaType(jmethod.getSimpleName() + "Fault"));
                    operation.setFaultMessages(faultMessage);
                }
            } else {
                operation = getAxisOperationforJmethod(jmethod, table);
                MessageReceiver mr = axisService.getMessageReceiver(
                        operation.getMessageExchangePattern());
                if (mr != null) {
                    operation.setMessageReceiver(mr);
                } else {
                    mr = axisConfig.getMessageReceiver(operation.getMessageExchangePattern());
                    operation.setMessageReceiver(mr);
                }
                pinfo.setOperationPhases(operation);
                axisService.addOperation(operation);
            }
            if (operation.getInputAction() == null) {
                operation.setSoapAction("urn:" + opName);
            }
        }
    }

    public static AxisOperation getAxisOperationforJmethod(JMethod jmethod,
                                                           TypeTable table) throws AxisFault {
        AxisOperation operation;
        String opName = jmethod.getSimpleName();
        if (jmethod.getReturnType().isVoidType()) {
            operation = AxisOperationFactory.getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_ONLY);
        } else {
            operation = AxisOperationFactory.getAxisOperation(WSDLConstants.MEP_CONSTANT_IN_OUT);
            AxisMessage outMessage = operation.getMessage(
                    WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
            outMessage.setElementQName(table.getQNamefortheType(jmethod.getSimpleName() +
                    Java2WSDLConstants.RESPONSE));
            outMessage.setName(opName + Java2WSDLConstants.RESPONSE);
        }
        if (jmethod.getExceptionTypes().length > 0) {
            AxisMessage faultMessage = new AxisMessage();
            faultMessage.setName(jmethod.getSimpleName() + "Fault");
            faultMessage
                    .setElementQName(table.getComplexSchemaType(jmethod.getSimpleName() + "Fault"));
            operation.setFaultMessages(faultMessage);
        }
        operation.setName(new QName(opName));
        AxisMessage inMessage = operation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
        if (inMessage != null) {
            inMessage.setElementQName(table.getComplexSchemaType(jmethod.getSimpleName()));
            inMessage.setName(opName + Java2WSDLConstants.MESSAGE_SUFFIX);
        }
        JAnnotation methodAnnon = jmethod.getAnnotation(AnnotationConstants.WEB_METHOD);
        if (methodAnnon != null) {
            String action = methodAnnon.getValue(AnnotationConstants.ACTION).asString();
            if (action != null && !"".equals(action)) {
                operation.setSoapAction(action);
            }
        }
        return operation;
    }

    public static OMElement getParameter(String name, String value, boolean locked) {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMElement parameter = fac.createOMElement("parameter", null);
        parameter.addAttribute("name", name, null);
        parameter.addAttribute("locked", Boolean.toString(locked), null);
        parameter.setText(value);
        return parameter;
    }

    /**
     * This method is to get the list of services there in a module
     * if module want to add services then the way of doing that is
     * 1. Add a directory called services inside the module (both in mar case and expanded case)
     * 2. Then add a services.list file into that directory adding all the modules
     * you want to add
     * 3. Then put all the services into services directory in the module
     * 4. All the class is module can be access via a the module services.
     */

    public static void deployModuleServices(AxisModule module,
                                            ConfigurationContext configCtx) throws AxisFault {
        try {
            AxisConfiguration axisConfig = configCtx.getAxisConfiguration();
            ArchiveReader archiveReader = new ArchiveReader();
            PhasesInfo phasesInfo = axisConfig.getPhasesInfo();
            ClassLoader moduleClassLoader = module.getModuleClassLoader();
            ArrayList services = new ArrayList();
            InputStream in = moduleClassLoader.getResourceAsStream("aars/aars.list");
            if (in != null) {
                BufferedReader input;
                try {
                    input = new BufferedReader(new InputStreamReader(in));
                    String line;
                    while ((line = input.readLine()) != null) {
                        services.add(line);
                    }
                    input.close();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
            if (services.size() > 0) {
                for (int i = 0; i < services.size(); i++) {
                    String servicename = (String) services.get(i);
                    if (servicename == null || "".equals(servicename)) {
                        continue;
                    }
                    InputStream fin = moduleClassLoader.getResourceAsStream("aars/" + servicename);
                    if (fin == null) {
                        throw new AxisFault("No service archiev found : " + servicename);
                    }
                    File inputFile = Utils.createTempFile(servicename, fin);
                    DeploymentFileData filedata = new DeploymentFileData(inputFile,
                                                                         DeploymentConstants.TYPE_SERVICE,
                                                                         false);

                    filedata.setClassLoader(false,
                                            moduleClassLoader);
                    HashMap wsdlservice = archiveReader.processWSDLs(filedata);
                    if (wsdlservice != null && wsdlservice.size() > 0) {
                        Iterator servicesitr = wsdlservice.values().iterator();
                        while (servicesitr.hasNext()) {
                            AxisService service = (AxisService) servicesitr.next();
                            Iterator operations = service.getOperations();
                            while (operations.hasNext()) {
                                AxisOperation axisOperation = (AxisOperation) operations.next();
                                phasesInfo.setOperationPhases(axisOperation);
                            }
                        }
                    }
                    AxisServiceGroup serviceGroup = new AxisServiceGroup(axisConfig);
                    serviceGroup.setServiceGroupClassLoader(
                            filedata.getClassLoader());
                    ArrayList serviceList = archiveReader.processServiceGroup(
                            filedata.getAbsolutePath(), filedata,
                            serviceGroup, false, wsdlservice,
                            configCtx);
                    for (int j = 0; j < serviceList.size(); j++) {
                        AxisService axisService = (AxisService) serviceList.get(j);
                        serviceGroup.addService(axisService);
                    }
                    axisConfig.addServiceGroup(serviceGroup);
                    fin.close();
                }
            }
        } catch (IOException e) {
            throw new AxisFault(e);
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
        int leadingSlashes = 0;
        for (leadingSlashes = 0; leadingSlashes < uri.length()
                && uri.charAt(leadingSlashes) == '/'; ++leadingSlashes) {
        }
        boolean isDir = (uri.charAt(uri.length() - 1) == '/');
        StringTokenizer st = new StringTokenizer(uri, "/");
        LinkedList clean = new LinkedList();
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if ("..".equals(token)) {
                if (! clean.isEmpty() && ! "..".equals(clean.getLast())) {
                    clean.removeLast();
                    if (! st.hasMoreTokens()) {
                        isDir = true;
                    }
                } else {
                    clean.add("..");
                }
            } else if (! ".".equals(token) && ! "".equals(token)) {
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
            String [] values = parent.split("/");
            if (values.length > 0) {
                for (int i = 0; i < values.length; i++) {
                    String value = values[i];
                    parentStack.push(value);
                }
            }
        }
        String [] values = childPath.split("/");
        if (values.length > 0) {
            for (int i = 0; i < values.length; i++) {
                String value = values[i];
                childStack.push(value);
            }
        }
        String filepath = "";
        while (!childStack.isEmpty()) {
            String value = (String) childStack.pop();
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
            String value = (String) parentStack.pop();
            if (!"".equals(value)) {
                filepath = value + "/" + filepath;
            }
        }
        return filepath;
    }
}