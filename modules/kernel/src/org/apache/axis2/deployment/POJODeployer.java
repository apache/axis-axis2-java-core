package org.apache.axis2.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.util.Loader;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.axis2.Constants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.java2wsdl.AnnotationConstants;
import org.codehaus.jam.JAnnotation;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JamClassIterator;
import org.codehaus.jam.JamService;
import org.codehaus.jam.JamServiceFactory;
import org.codehaus.jam.JamServiceParams;

import java.io.File;
import java.io.FileInputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
*
*/

public class POJODeployer implements Deployer {

    private static Log log = LogFactory.getLog(POJODeployer.class);

    private ConfigurationContext configCtx;

    //To initialize the deployer
    public void init(ConfigurationContext configCtx) {
        this.configCtx = configCtx;
    }//Will process the file and add that to axisConfig

    public void deploy(DeploymentFileData deploymentFileData) {
        ClassLoader threadClassLoader = null;
        try {
            threadClassLoader = Thread.currentThread().getContextClassLoader();
            String extension = deploymentFileData.getType();
            if (".class".equals(extension)) {
                File file = deploymentFileData.getFile();
                if (file != null) {
                    File parentFile = file.getParentFile();
                   ClassLoader classLoader =
                           Utils.getClassLoader(configCtx.getAxisConfiguration().getSystemClassLoader(), parentFile);
                    Thread.currentThread().setContextClassLoader(classLoader);
                    String className = file.getName();
                    className = className.replaceAll(".class", "");
                    JamServiceFactory factory = JamServiceFactory.getInstance();
                    JamServiceParams jam_service_parms = factory.createServiceParams();
                    jam_service_parms.addClassLoader(classLoader);
                    jam_service_parms.includeClass(className);
                    JamService service = factory.createService(jam_service_parms);
                    JamClassIterator jClassIter = service.getClasses();
                    while (jClassIter.hasNext()) {
                        JClass jclass = (JClass) jClassIter.next();
                        if (jclass.getQualifiedName().equals(className)) {
                            /**
                             * Schema genertaion done in two stage 1. Load all the methods and
                             * create type for methods parameters (if the parameters are Bean
                             * then it will create Complex types for those , and if the
                             * parameters are simple type which decribe in SimpleTypeTable
                             * nothing will happen) 2. In the next stage for all the methods
                             * messages and port types will be creteated
                             */
                            JAnnotation annotation =
                                    jclass.getAnnotation(AnnotationConstants.WEB_SERVICE);
                            if (annotation != null) {
                                Class claxx = Class.forName(
                                        "org.apache.axis2.jaxws.description.DescriptionFactory");
                                Method mthod = claxx.getMethod(
                                        "createAxisService",
                                        new Class[]{Class.class});
                                Class pojoClass = Loader.loadClass(classLoader, className);
                                AxisService axisService =
                                        (AxisService) mthod.invoke(claxx, new Object[]{pojoClass});
                                Utils.fillAxisService(axisService,
                                                      configCtx.getAxisConfiguration(),
                                                      new ArrayList(),
                                                      new ArrayList());
                                setMessageReceivers(axisService);
                                configCtx.getAxisConfiguration().addService(axisService);
                            } else {
                                HashMap messageReciverMap = new HashMap();
                                Class inOnlyMessageReceiver = Loader.loadClass(
                                        "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver");
                                MessageReceiver messageReceiver =
                                        (MessageReceiver) inOnlyMessageReceiver.newInstance();
                                messageReciverMap.put(
                                        WSDL2Constants.MEP_URI_IN_ONLY,
                                        messageReceiver);
                                Class inoutMessageReceiver = Loader.loadClass(
                                        "org.apache.axis2.rpc.receivers.RPCMessageReceiver");
                                MessageReceiver inOutmessageReceiver =
                                        (MessageReceiver) inoutMessageReceiver.newInstance();
                                messageReciverMap.put(
                                        WSDL2Constants.MEP_URI_IN_OUT,
                                        inOutmessageReceiver);
                                AxisService axisService = AxisService.createService(className,
                                                                                    configCtx.getAxisConfiguration(),
                                                                                    messageReciverMap,
                                                                                    null, null,
                                                                                    classLoader);
                                configCtx.getAxisConfiguration().addService(axisService);
                            }
                        }
                    }
                }

            } else if (".jar".equals(extension)) {
                ArrayList classList;
                FileInputStream fin = null;
                ZipInputStream zin = null;
                try {
                    fin = new FileInputStream(deploymentFileData.getAbsolutePath());
                    zin = new ZipInputStream(fin);
                    ZipEntry entry;
                    classList = new ArrayList();
                    while ((entry = zin.getNextEntry()) != null) {
                        String name = entry.getName();
                        if (name.endsWith(".class")) {
                            classList.add(name);
                        }
                    }
                    zin.close();
                    fin.close();
                } catch (Exception e) {
                    throw new DeploymentException(e);
                } finally {
                    if (zin != null) {
                        zin.close();
                    }
                    if (fin != null) {
                        fin.close();
                    }
                }
                ArrayList axisServiceList = new ArrayList();
                for (int i = 0; i < classList.size(); i++) {
                    String className = (String) classList.get(i);
                    ClassLoader classLoader = Utils.createClassLoader(
                            new URL[]{deploymentFileData.getFile().toURL()},
                            configCtx.getAxisConfiguration().getSystemClassLoader(),
                            true,
                            (File)configCtx.getAxisConfiguration().getParameterValue(Constants.Configuration.ARTIFACTS_TEMP_DIR));
                    Thread.currentThread().setContextClassLoader(classLoader);
                    className = className.replaceAll(".class", "");
                    className = className.replaceAll("/", ".");
                    JamServiceFactory factory = JamServiceFactory.getInstance();
                    JamServiceParams jam_service_parms = factory.createServiceParams();
                    jam_service_parms.addClassLoader(classLoader);
                    jam_service_parms.includeClass(className);
                    JamService service = factory.createService(jam_service_parms);
                    JamClassIterator jClassIter = service.getClasses();
                    while (jClassIter.hasNext()) {
                        JClass jclass = (JClass) jClassIter.next();
                        if (jclass.getQualifiedName().equals(className)) {
                            /**
                             * Schema genertaion done in two stage 1. Load all the methods and
                             * create type for methods parameters (if the parameters are Bean
                             * then it will create Complex types for those , and if the
                             * parameters are simple type which decribe in SimpleTypeTable
                             * nothing will happen) 2. In the next stage for all the methods
                             * messages and port types will be creteated
                             */
                            JAnnotation annotation =
                                    jclass.getAnnotation(AnnotationConstants.WEB_SERVICE);
                            if (annotation != null) {
                                Class claxx = Class.forName(
                                        "org.apache.axis2.jaxws.description.DescriptionFactory");
                                Method mthod = claxx.getMethod(
                                        "createAxisService",
                                        new Class[]{Class.class});
                                Class pojoClass = Loader.loadClass(classLoader, className);
                                AxisService axisService =
                                        (AxisService) mthod.invoke(claxx, new Object[]{pojoClass});
                                Utils.fillAxisService(axisService,
                                                      configCtx.getAxisConfiguration(),
                                                      new ArrayList(),
                                                      new ArrayList());
                                setMessageReceivers(axisService);
                                axisServiceList.add(axisService);
                            }
                        }
                    }
                }
                AxisServiceGroup serviceGroup = new AxisServiceGroup();
                serviceGroup.setServiceGroupName(deploymentFileData.getServiceName());
                for (int i = 0; i < axisServiceList.size(); i++) {
                    AxisService axisService = (AxisService) axisServiceList.get(i);
                    serviceGroup.addService(axisService);
                }
                configCtx.getAxisConfiguration().addServiceGroup(serviceGroup);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (threadClassLoader != null) {
                Thread.currentThread().setContextClassLoader(threadClassLoader);
            }
        }
    }

    public void setMessageReceivers(AxisService service) {
        Iterator iterator = service.getOperations();
        while (iterator.hasNext()) {
            AxisOperation operation = (AxisOperation) iterator.next();
            String MEP = operation.getMessageExchangePattern();
            if (MEP != null) {
                try {
                    if (WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY.equals(MEP)
                            || WSDLConstants.WSDL20_2004_Constants.MEP_URI_IN_ONLY.equals(MEP)
                            || WSDL2Constants.MEP_URI_IN_ONLY.equals(MEP)) {
                        Class inOnlyMessageReceiver = Loader.loadClass(
                                "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver");
                        MessageReceiver messageReceiver =
                                (MessageReceiver) inOnlyMessageReceiver.newInstance();
                        operation.setMessageReceiver(messageReceiver);
                    } else {
                        Class inoutMessageReceiver = Loader.loadClass(
                                "org.apache.axis2.rpc.receivers.RPCMessageReceiver");
                        MessageReceiver inOutmessageReceiver =
                                (MessageReceiver) inoutMessageReceiver.newInstance();
                        operation.setMessageReceiver(inOutmessageReceiver);
                    }
                } catch (ClassNotFoundException e) {
                    log.error(e);
                } catch (InstantiationException e) {
                    log.error(e);
                } catch (IllegalAccessException e) {
                    log.error(e);
                }
            }
        }
    }

    public void setDirectory(String directory) {
    }

    public void setExtension(String extension) {
    }

    public void unDeploy(String fileName) {
    }
}

