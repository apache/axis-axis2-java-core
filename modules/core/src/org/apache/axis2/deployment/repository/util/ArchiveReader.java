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

import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.DescriptionBuilder;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.deployment.ServiceGroupBuilder;
import org.apache.axis2.description.AxisDescWSDLComponentFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.wsdl.WSDLConstants;
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
import java.util.Iterator;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ArchiveReader implements DeploymentConstants {

    private Log log = LogFactory.getLog(getClass());

    /**
     * To create a ServiceDescrption <code>AxisService</code>   using given wsdl , if the
     * service.wsdl there in the arcive file AxisService will be creted using that else
     * default AxisService will be crated
     *
     * @param file
     * @return
     * @throws DeploymentException
     */
    public AxisService createService(ArchiveFileData file) throws DeploymentException {
        AxisService service = null;
        InputStream in = file.getClassLoader().getResourceAsStream(SERVICE_WSDL_WITH_FOLDER);
        boolean foundservice = false;
        try {
            if (in != null) {
                WOMBuilder builder = WOMBuilderFactory.getBuilder(WSDLConstants.WSDL_1_1);
                WSDLVersionWrapper wsdlVersionWrapper = builder.build(in,
                        new AxisDescWSDLComponentFactory());
                WSDLDescription womDescription = wsdlVersionWrapper.getDescription();
                Iterator iterator = womDescription.getServices().keySet()
                        .iterator();
                if (iterator.hasNext()) {
                    foundservice = true;
                    // remove <wsdl:service> and <wsdl:binding> elements from the service
                    // description we read in as we will be replacing them anyway.

                    WSDLServiceImpl serviceimpl = (WSDLServiceImpl) womDescription.getServices().get(iterator.next());
                    service = new AxisService(serviceimpl);
                }
                if (!foundservice) {
                    service = new AxisService();
                }
                service.setWSDLDefinition(wsdlVersionWrapper.getDefinition());
                in.close();
            } else {
                service = new AxisService();
                log.info(Messages.getMessage(DeploymentErrorMsgs.WSDL_FILE_NOT_FOUND,
                        file.getName()));
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
        }

        return service;
    }


    private void processWSDLFile(InputStream in , DeploymentEngine depengine) throws DeploymentException {
        try {
            WOMBuilder builder = WOMBuilderFactory.getBuilder(WSDLConstants.WSDL_1_1);
            WSDLVersionWrapper wsdlVersionWrapper = builder.build(in,
                    new AxisDescWSDLComponentFactory());
            WSDLDescription womDescription = wsdlVersionWrapper.getDescription();

            //removing binding
            Map bindings = wsdlVersionWrapper.getDefinition().getBindings();
            Iterator binfingIterator = bindings.keySet().iterator();
            while (binfingIterator.hasNext()) {
                Object o = binfingIterator.next();
                bindings.remove(o) ;

            }
            Iterator iterator = womDescription.getServices().keySet()
                    .iterator();
            if (iterator.hasNext()) {
                // remove <wsdl:service> and <wsdl:binding> elements from the service
                // description we read in as we will be replacing them anyway.
                WSDLServiceImpl serviceimpl = (WSDLServiceImpl)
                        womDescription.getServices().get(iterator.next());
                AxisService service = new AxisService(serviceimpl);
                service.setName(serviceimpl.getName());
                service.setWSDLDefinition(wsdlVersionWrapper.getDefinition());
                depengine.getCurrentFileItem().addService(service);
            }
        } catch (WSDLException e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * To create service objects out form wsdls file inside a service archive file
     * @param file <code>ArchiveFileData</code>
     * @param depengine <code>DeploymentEngine</code>
     * @throws DeploymentException  <code>DeploymentException</code>
     */
    public void processWSDLs(ArchiveFileData file , DeploymentEngine depengine) throws DeploymentException {
        File serviceFile = file.getFile();
        boolean isDirectory = serviceFile.isDirectory();
        if(isDirectory){
            try {
                File meta_inf = new File(serviceFile,META_INF);
                if(meta_inf.exists()){
                    File files [] = meta_inf.listFiles();
                    for (int i = 0; i < files.length; i++) {
                        File file1 = files[i];
                        String fileName = file1.getName();
                        if(fileName.endsWith(".wsdl") || fileName.endsWith(".WSDL")){
                            InputStream in = new FileInputStream(file1);
                            processWSDLFile(in,depengine);
                            try {
                                in.close();
                            } catch (IOException e) {
                                log.info(e);
                            }
                        }

                    }
                } else {
                    throw new DeploymentException(Messages.getMessage(
                            DeploymentErrorMsgs.INVALID_SERVICE));
                }
            } catch (FileNotFoundException e) {
                throw new DeploymentException(e);
            } catch (IOException e) {
                throw new DeploymentException(e);
            }
        }   else {

            ZipInputStream zin;
            try {
                zin = new ZipInputStream(new FileInputStream(serviceFile));
                ZipEntry entry;
                byte[] buf = new byte[1024];
                int read;
                ByteArrayOutputStream out ;
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
                        processWSDLFile(in,depengine);
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

    }

    /**
     * This method will readServiceArchive the given jar or aar.
     * it take two arguments filename and refereance to DeployEngine
     *
     * @param filename
     * @param engine
     */
    public void processServiceGroup(String filename,
                                    DeploymentEngine engine,
                                    AxisServiceGroup axisServiceGroup, boolean extarctService)
            throws DeploymentException {
        // get attribute values
        boolean foundServiceXML = false;
        if (! extarctService) {
            ZipInputStream zin;
            try {
                zin = new ZipInputStream(new FileInputStream(filename));
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    if (entry.getName().equals(SERVICEXML)) {
                        foundServiceXML = true;
                        buildServiceGroup(zin, engine, axisServiceGroup);
                        axisServiceGroup.setServiceGroupName(DescriptionBuilder.getShortFileName(
                                engine.getCurrentFileItem().getName()));
                        break;
                    }
                }
                //    zin.close();
                if (!foundServiceXML) {
                    throw new DeploymentException(
                            Messages.getMessage(DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND));
                }
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        } else {
            File file = new File(filename, SERVICEXML);
            if (file.exists()) {
                InputStream in ;
                try {
                    in = new FileInputStream(file);
                    buildServiceGroup(in,engine,axisServiceGroup);
                    axisServiceGroup.setServiceGroupName(engine.getCurrentFileItem().getName());
                } catch (FileNotFoundException e) {
                    throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.FNF_WITH_E
                            ,e.getMessage()));
                } catch (XMLStreamException e) {
                    throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.XTZX_EXCEPTION
                            ,e.getMessage()));
                }
            } else {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND));
            }
        }
    }

    private void buildServiceGroup(InputStream zin, DeploymentEngine engine,
                                   AxisServiceGroup axisServiceGroup)
            throws XMLStreamException, DeploymentException {
        DescriptionBuilder builder;
        String rootelementName;
        builder = new DescriptionBuilder(zin, engine);
        OMElement services = builder.buildOM();
        rootelementName = services.getLocalName();
        if(SERVICE_ELEMENT.equals(rootelementName)){
            AxisService axisService = engine.getCurrentFileItem().
                    getService(DescriptionBuilder.getShortFileName(
                            engine.getCurrentFileItem().getName()));
            if(axisService == null){
                axisService = new AxisService(
                        new QName(DescriptionBuilder.getShortFileName(
                                engine.getCurrentFileItem().getName())));
                engine.getCurrentFileItem().addService(axisService);
            }
            axisService.setParent(axisServiceGroup);
            axisService.setClassLoader(engine.getCurrentFileItem().getClassLoader());
            ServiceBuilder serviceBuilder = new ServiceBuilder(engine,axisService);
            serviceBuilder.populateService(services);
            engine.getCurrentFileItem().getDeploybleServices().add(axisService);
        } else if(SERVICE_GROUP_ELEMENT.equals(rootelementName)){
            ServiceGroupBuilder groupBuilder = new ServiceGroupBuilder(services,engine);
            groupBuilder.populateServiceGroup(axisServiceGroup);
        }
    }

    public void readModuleArchive(String filename,
                                  DeploymentEngine engine,
                                  ModuleDescription module , boolean explodedDir)
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
                        ModuleBuilder builder = new ModuleBuilder(zin, engine, module);
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
                    ModuleBuilder builder = new ModuleBuilder(in, engine, module);
                    builder.populateModule();
                } catch (FileNotFoundException e) {
                    throw new DeploymentException(Messages.getMessage(DeploymentErrorMsgs.FNF_WITH_E
                            ,e.getMessage()));
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
                if(!modules.exists()) {
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








