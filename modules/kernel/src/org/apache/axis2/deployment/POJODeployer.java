package org.apache.axis2.deployment;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.Loader;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.ws.java2wsdl.AnnotationConstants;
import org.codehaus.jam.*;

import java.io.File;
import java.net.URL;
import java.lang.reflect.Method;
import java.util.HashMap;

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
                    DeploymentClassLoader classLoader = new DeploymentClassLoader(new URL[]{parentFile.toURL()},
                            configCtx.getAxisConfiguration().getSystemClassLoader(), true);
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
                            boolean callJaxWs = false;
                            JAnnotation annotation = jclass.getAnnotation(AnnotationConstants.WEB_SERVICE);
                            if (annotation != null) {
                                String wsdlLocation = annotation.getValue(AnnotationConstants.WSDL_LOCATION).asString();
                                if (wsdlLocation != null && !"".equals(wsdlLocation)) {
                                    callJaxWs = true;
                                }
                            }
                            if (callJaxWs) {
                                Class claxx = Class.forName(
                                        "org.apache.axis2.jaxws.description.DescriptionFactory");
                                Method mthod = claxx.getMethod(
                                        "createServiceDescriptionFromServiceImpl",
                                        new Class[]{Class.class, AxisService.class});
                                Class pojoClass = Loader.loadClass(classLoader, className);
                                AxisService axisService = new AxisService(className);
                                axisService.setName(className);
                                mthod.invoke(claxx,new Object[]{pojoClass, axisService});
                                configCtx.getAxisConfiguration().addService(axisService);
                            } else {
                                HashMap messageReciverMap = new HashMap();
                                Class inOnlyMessageReceiver = Loader.loadClass(
                                        "org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver");
                                MessageReceiver messageReceiver =
                                        (MessageReceiver) inOnlyMessageReceiver.newInstance();
                                messageReciverMap.put(
                                        WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_ONLY,
                                        messageReceiver);
                                Class inoutMessageReceiver = Loader.loadClass(
                                        "org.apache.axis2.rpc.receivers.RPCMessageReceiver");
                                MessageReceiver inOutmessageReceiver =
                                        (MessageReceiver) inoutMessageReceiver.newInstance();
                                messageReciverMap.put(
                                        WSDLConstants.WSDL20_2006Constants.MEP_URI_IN_OUT,
                                        inOutmessageReceiver);
                                AxisService axisService = AxisService.createService(className,
                                        configCtx.getAxisConfiguration(),
                                        messageReciverMap, null, null, classLoader);
                                configCtx.getAxisConfiguration().addService(axisService);
                            }
                        }
                    }
                }

            } else if ("jar".equals(extension)) {
                //TODO need to extart the zip file and find out the classes which has annotated
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (threadClassLoader != null) {
                Thread.currentThread().setContextClassLoader(threadClassLoader);
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

