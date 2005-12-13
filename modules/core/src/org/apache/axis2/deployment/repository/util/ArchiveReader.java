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
 */

package org.apache.axis2.deployment.repository.util;

import org.apache.axis2.deployment.*;
import org.apache.axis2.description.AxisDescWSDLComponentFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilder;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.impl.WSDLServiceImpl;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ArchiveReader implements DeploymentConstants {

    private Log log = LogFactory.getLog(getClass());

    private AxisService processWSDLFile(InputStream in) throws DeploymentException {
        try {
            WOMBuilder builder = WOMBuilderFactory.getBuilder(org.apache.wsdl.WSDLConstants.WSDL_1_1);
            WSDLVersionWrapper wsdlVersionWrapper = builder.build(in,
                    new AxisDescWSDLComponentFactory());
            WSDLDescription womDescription = wsdlVersionWrapper.getDescription();

            //removing binding
//            Map bindings = wsdlVersionWrapper.getDefinition().getBindings();
//            Iterator binfingIterator = bindings.keySet().iterator();
//            while (binfingIterator.hasNext()) {
//                Object o = binfingIterator.next();
//                bindings.remove(o) ;
//
//            }
            Iterator iterator = womDescription.getServices().keySet()
                    .iterator();
            if (iterator.hasNext()) {
                // remove <wsdl:service> and <wsdl:binding> elements from the service
                // description we read in as we will be replacing them anyway.
                WSDLServiceImpl serviceimpl = (WSDLServiceImpl)
                        womDescription.getServices().get(iterator.next());
                AxisService service = new AxisService();
                service.setName(serviceimpl.getName().getLocalPart());
                service.setWSDLDefinition(wsdlVersionWrapper.getDefinition());
                return service;
//                depengine.getCurrentFileItem().addService(service);
            }
        } catch (WSDLException e) {
            throw new DeploymentException(e);
        }
        return null;
    }

    /**
     * To create service objects out form wsdls file inside a service archive file
     *
     * @param file      <code>ArchiveFileData</code>
     * @param depengine <code>DeploymentEngine</code>
     * @throws DeploymentException <code>DeploymentException</code>
     */
    public HashMap processWSDLs(ArchiveFileData file, DeploymentEngine depengine) throws DeploymentException {
        File serviceFile = file.getFile();
        //to store service come from wsdl files
        HashMap servicesMap = new HashMap();
        boolean isDirectory = serviceFile.isDirectory();
        if (isDirectory) {
            try {
                File meta_inf = new File(serviceFile, META_INF);
                if (!meta_inf.exists()) {
                    throw new DeploymentException(Messages.getMessage(
                            DeploymentErrorMsgs.NO_META_INF,
                            serviceFile.getName()));
                }
                File files [] = meta_inf.listFiles();
                for (int i = 0; i < files.length; i++) {
                    File file1 = files[i];
                    String fileName = file1.getName();
                    if (fileName.endsWith(".wsdl") || fileName.endsWith(".WSDL")) {
                        InputStream in = new FileInputStream(file1);
                        AxisService service = processWSDLFile(in);
                        servicesMap.put(service.getName(), service);
                        try {
                            in.close();
                        } catch (IOException e) {
                            log.info(e);
                        }
                    }

                }
            } catch (FileNotFoundException e) {
                throw new DeploymentException(e);
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
        } else {

            ZipInputStream zin;
            try {
                zin = new ZipInputStream(new FileInputStream(serviceFile));
                ZipEntry entry;
                byte[] buf = new byte[1024];
                int read;
                ByteArrayOutputStream out;
                while ((entry = zin.getNextEntry()) != null) {
                    String entryName = entry.getName();
                    if ((entryName.startsWith(META_INF) ||
                            entryName.startsWith(META_INF.toLowerCase()))
                            && (entryName.endsWith(".wsdl")
                            || entryName.endsWith(".WSDL"))) {
                        out = new ByteArrayOutputStream();
                        while ((read = zin.read(buf)) > 0) {
                            out.write(buf, 0, read);
                        }
                        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
                        AxisService service = processWSDLFile(in);
                        servicesMap.put(service.getName(), service);
                    }
                }
                try {
                    zin.close();
                } catch (IOException e) {
                    log.info(e);
                }
            } catch (FileNotFoundException e) {
                throw new DeploymentException(e);
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
        }
        return servicesMap;
    }

    /**
     * This method will readServiceArchive the given jar or aar.
     * it take two arguments filename and refereance to DeployEngine
     *
     * @param filename
     * @param engine
     */
    public ArrayList processServiceGroup(String filename,
                                         DeploymentEngine engine,
                                         AxisServiceGroup axisServiceGroup,
                                         boolean extractService,
                                         HashMap wsdls,
                                         AxisConfiguration axisConfig)
            throws DeploymentException {
        // get attribute values
        if (!extractService) {
            ZipInputStream zin;
            try {
                zin = new ZipInputStream(new FileInputStream(filename));
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    if (entry.getName().equals(SERVICEXML)) {
                        axisServiceGroup.setServiceGroupName(DescriptionBuilder.getShortFileName(
                                engine.getCurrentFileItem().getName()));
                        return buildServiceGroup(zin, engine, axisServiceGroup, wsdls, axisConfig);
                    }
                }
                //    zin.close();
                throw new DeploymentException(
                        Messages.getMessage(
                                DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND,
                                filename));
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        } else {
            File file = new File(filename, SERVICEXML);
            if (file.exists()) {
                InputStream in;
                try {
                    in = new FileInputStream(file);
                    axisServiceGroup.setServiceGroupName(engine.getCurrentFileItem().getName());
                    return buildServiceGroup(in, engine, axisServiceGroup, wsdls, axisConfig);
                } catch (FileNotFoundException e) {
                    throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.FILE_NOT_FOUND
                            , e.getMessage()));
                } catch (XMLStreamException e) {
                    throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.XTZX_EXCEPTION
                            , e.getMessage()));
                }
            } else {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND));
            }
        }
    }

    private ArrayList buildServiceGroup(InputStream zin, DeploymentEngine engine,
                                        AxisServiceGroup axisServiceGroup, HashMap wsdlServices,
                                        AxisConfiguration axisConfig)
            throws XMLStreamException, DeploymentException {
        DescriptionBuilder builder;
        String rootelementName;
        builder = new DescriptionBuilder(zin, axisConfig);
        OMElement services = builder.buildOM();
        rootelementName = services.getLocalName();
        if (SERVICE_ELEMENT.equals(rootelementName)) {
            AxisService axisService = (AxisService) wsdlServices.get(
                    DescriptionBuilder.getShortFileName(engine.getCurrentFileItem().getName()));
            if (axisService == null) {
                axisService = new AxisService(
                        DescriptionBuilder.getShortFileName(engine.getCurrentFileItem().getName()));
            }
            axisService.setParent(axisServiceGroup);
            axisService.setClassLoader(engine.getCurrentFileItem().getClassLoader());
            ServiceBuilder serviceBuilder = new ServiceBuilder(axisConfig, axisService);
            AxisService service = serviceBuilder.populateService(services);
            ArrayList serviceList = new ArrayList();
            serviceList.add(service);
            return serviceList;
//            engine.getCurrentFileItem().getDeploybleServices().add(axisService);
        } else if (SERVICE_GROUP_ELEMENT.equals(rootelementName)) {
            ServiceGroupBuilder groupBuilder = new ServiceGroupBuilder(services, wsdlServices, axisConfig);
            return groupBuilder.populateServiceGroup(axisServiceGroup);
        }
        return null;
    }

    public void readModuleArchive(String filename,
                                  DeploymentEngine engine,
                                  ModuleDescription module, boolean explodedDir, AxisConfiguration axisConfig)
            throws DeploymentException {
        // get attribute values
        boolean foundmoduleXML = false;
        if (!explodedDir) {
            ZipInputStream zin;
            try {
                zin = new ZipInputStream(new FileInputStream(filename));
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    if (entry.getName().equals(MODULEXML)) {
                        foundmoduleXML = true;
                        ModuleBuilder builder = new ModuleBuilder(zin, module, axisConfig);
                        //setting module name
                        module.setName(new QName(DescriptionBuilder.getShortFileName(
                                engine.getCurrentFileItem().getServiceName())));
                        builder.populateModule();
                        break;
                    }
                }
                zin.close();
                if (!foundmoduleXML) {
                    throw new DeploymentException(Messages.getMessage(
                            DeploymentErrorMsgs.MODULEXML_NOT_FOUND_FOR_THE_MODULE, filename));
                }
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        } else {
            File file = new File(filename, MODULEXML);
            if (file.exists()) {
                InputStream in;
                try {
                    in = new FileInputStream(file);
                    ModuleBuilder builder = new ModuleBuilder(in, module, axisConfig);
                    // setting module name
                    module.setName(new QName(DescriptionBuilder.getShortFileName(
                            engine.getCurrentFileItem().getServiceName())));
                    builder.populateModule();
                } catch (FileNotFoundException e) {
                    throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.FILE_NOT_FOUND
                            , e.getMessage()));
                }
            } else {
                throw new DeploymentException(Messages.getMessage(
                        DeploymentErrorMsgs.MODULEXML_NOT_FOUND_FOR_THE_MODULE, filename));
            }

        }
    }


    /**
     * This method first check whether the given module is there in the user home dirctory if so return
     * that , else try to read the given module form classpath (from resources ) if found first get the module.mar
     * file from the resourceStream and write that into user home/axis2home/nodule directory
     *
     * @param moduleName
     * @return
     * @throws DeploymentException
     */
    public File creatModuleArchivefromResource(String moduleName,
                                               String axis2repository) throws DeploymentException {
        File modulearchiveFile;
        File modules;
        try {
            int BUFFER = 2048;
            if (axis2repository == null) {
                String userHome = System.getProperty("user.home");
                File userHomedir = new File(userHome);
                File repository = new File(userHomedir, ".axis2home");
                modules = new File(repository, "modules");
            } else {
                modules = new File(axis2repository, "modules");
            }
            String modulearchiveName = moduleName + ".mar";
            modulearchiveFile = new File(modules, modulearchiveName);
            if (modulearchiveFile.exists()) {
                return modulearchiveFile;
            }
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream("modules/" + moduleName + ".mar");
            if (in == null) {
                in = cl.getResourceAsStream("modules/" + moduleName + ".jar");
            }
            if (in == null) {
                throw new DeploymentException(Messages.getMessage(
                        DeploymentErrorMsgs.MODULEXML_NOT_FOUND_FOR_THE_MODULE, moduleName));
            } else {
                if (!modules.exists()) {
                    modules.mkdirs();
                }
                modulearchiveFile.createNewFile();
                FileOutputStream dest = new
                        FileOutputStream(modulearchiveFile);
                ZipOutputStream out = new ZipOutputStream(new
                        BufferedOutputStream(dest));
                byte data[] = new byte[BUFFER];
                ZipInputStream zin;
                zin = new ZipInputStream(in);
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    ZipEntry zip = new ZipEntry(entry);
                    out.putNextEntry(zip);
                    int count;
                    while ((count = zin.read(data, 0, BUFFER)) != -1) {
                        out.write(data, 0, count);
                    }
                }
                out.close();
                zin.close();
            }

        } catch (Exception e) {
            throw new DeploymentException(e);
        }
        return modulearchiveFile;
    }

}








