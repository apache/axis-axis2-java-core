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
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilder;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLDescription;
import org.apache.wsdl.impl.WSDLServiceImpl;

import java.io.*;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ArchiveReader implements DeploymentConstants {

    private Log log = LogFactory.getLog(getClass());

    /**
     * To create a ServiceDescrption <code>ServiceDescription</code>   using given wsdl , if the
     * service.wsdl there in the arcive file ServiceDescription will be creted using that else
     * default ServiceDescription will be crated
     *
     * @param file
     * @return
     * @throws DeploymentException
     */
    public ServiceDescription createService(ArchiveFileData file) throws DeploymentException {
        ServiceDescription service = null;
        InputStream in = file.getClassLoader().getResourceAsStream(SERVICEWSDL);
        boolean foundservice = false;
        try {
            if (in != null) {
                WOMBuilder builder = WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11);
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
                    service = new ServiceDescription(serviceimpl);
//                    service =
//                            (ServiceDescription) womDescription.getServices()
//                            .get(iterator.next());
                }
                if (!foundservice) {
                    service = new ServiceDescription();
                }
                service.setWSDLDefinition(wsdlVersionWrapper.getDefinition());
                in.close();
            } else {
                service = new ServiceDescription();
                log.info(Messages.getMessage(DeploymentErrorMsgs.WSDL_FILE_NOT_FOUND,
                        file.getName()));
//                        "WSDL file not found for the service :  " +
//                        file.getName());
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
        }

        return service;
    }

    /**
     * This method will readServiceArchive the given jar or aar.
     * it take two arguments filename and refereance to DeployEngine
     *
     * @param filename
     * @param engine
     */
    public void processServiceDescriptor(String filename,
                                         DeploymentEngine engine,
                                         ServiceDescription service, boolean extarctService) throws DeploymentException {
        // get attribute values
        boolean foundServiceXML = false;
        ServiceBuilder builder;
        if (! extarctService) {
            ZipInputStream zin;
            try {
                zin = new ZipInputStream(new FileInputStream(filename));
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    if (entry.getName().equals(SERVICEXML)) {
                        foundServiceXML = true;
                        builder = new ServiceBuilder(zin, engine, service);
                        builder.populateService();
                        break;
                    }
                }
                zin.close();
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
                InputStream in = null;
                try {
                    in = new FileInputStream(file);
                    builder = new ServiceBuilder(in, engine, service);
                    builder.populateService();

                } catch (FileNotFoundException e) {
                    throw new DeploymentException("FileNotFound : " + e);
                } finally {
                    try {
                        if (in != null) {
                            in.close();
                        }
                    } catch (IOException e) {
                        throw new DeploymentException("IOException : " + e);
                    }
                }
            } else {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND));
            }
        }
    }

    public void readModuleArchive(String filename,
                                  DeploymentEngine engine,
                                  ModuleDescription module) throws DeploymentException {
        // get attribute values
        boolean foundmoduleXML = false;
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
                if (!repository.exists()) {
                    repository.mkdirs();
                    modules = new File(repository, "modules");
                    modules.mkdirs();
                } else {
                    modules = new File(repository, "modules");
                    if (!modules.exists()) {
                        modules.mkdirs();
                    }
                }
            } else {
                modules = new File(axis2repository, "modules");
                if (!modules.exists()) {
                    modules = new File(axis2repository, "modules");
                    modules.mkdirs();
                }
            }
            String modulearchiveName = moduleName + ".mar";
            modulearchiveFile = new File(modules, modulearchiveName);
            if (modulearchiveFile.exists()) {
                return modulearchiveFile;
            }
//
//            else {
//                modulearchiveFile.createNewFile();
//            }
//
            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream("modules/" + moduleName + ".mar");
            if (in == null) {
                in = cl.getResourceAsStream("modules/" + moduleName + ".jar");
            }
            if (in == null) {
                throw new DeploymentException(moduleName + " module is not found");
            } else {
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








