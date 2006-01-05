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

import org.apache.axis2.deployment.AxisServiceBuilder;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentEngine;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.DescriptionBuilder;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.deployment.ServiceGroupBuilder;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.om.OMAttribute;
import org.apache.axis2.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.wsdl.WSDLException;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ArchiveReader implements DeploymentConstants {
    private Log log = LogFactory.getLog(getClass());

    private ArrayList buildServiceGroup(InputStream zin, DeploymentEngine engine,
                                        AxisServiceGroup axisServiceGroup, HashMap wsdlServices,
                                        AxisConfiguration axisConfig)
            throws XMLStreamException, DeploymentException {

        DescriptionBuilder builder = new DescriptionBuilder(zin, axisConfig);
        OMElement rootElement = builder.buildOM();
        String elementName = rootElement.getLocalName();

        if (TAG_SERVICE.equals(elementName)) {
            AxisService axisService = null;
            OMAttribute serviceNameatt = rootElement.getAttribute(new QName(ATTRIBUTE_NAME));
            String serviceName = serviceNameatt.getAttributeValue();
            if(serviceName != null) {
                axisService = (AxisService) wsdlServices.get(serviceName);
            }
            if (axisService == null) {
                axisService = (AxisService) wsdlServices.get(
                        DescriptionBuilder.getShortFileName(
                                engine.getCurrentFileItem().getName()));
            }
            if (axisService == null) {
                axisService = new AxisService(
                        DescriptionBuilder.getShortFileName(engine.getCurrentFileItem().getName()));
            } else {
                axisService.setWsdlfound(true);
            }

            axisService.setParent(axisServiceGroup);
            axisService.setClassLoader(engine.getCurrentFileItem().getClassLoader());

            ServiceBuilder serviceBuilder = new ServiceBuilder(axisConfig, axisService);
            AxisService service = serviceBuilder.populateService(rootElement);

            ArrayList serviceList = new ArrayList();

            if (!axisService.isWsdlfound()) {
                //trying to generate WSDL for the service using JAM  and Java refelection
                try {
                    Utils.fillAxisService(service);
                } catch (Exception e) {
                    log.info("Error in scheam generating :" + e.getMessage());
                }
            }
            serviceList.add(service);
            return serviceList;
        } else if (TAG_SERVICE_GROUP.equals(elementName)) {
            ServiceGroupBuilder groupBuilder = new ServiceGroupBuilder(rootElement, wsdlServices,
                    axisConfig);
            return groupBuilder.populateServiceGroup(axisServiceGroup);
        }
        return null;
    }

    /**
     * Creates the module file archive file. Checks whether the module exists in home 
     * directory. If yes, returns that else reads the given module from classpath (from resources).
     * If found, gets the module.mar file from the resource stream and writes into 
     * the userhome/axis2home/module directory.
     *      
     * @param moduleName
     * @return  Returns File.
     * @throws DeploymentException
     */
    public File creatModuleArchivefromResource(String moduleName, String axis2repository)
            throws DeploymentException {
        File modulearchiveFile;
        File modules;

        try {
            int BUFFER = 2048;

            if (axis2repository == null) {
                String userHome = System.getProperty(DeploymentConstants.PROPERTY_TEMP_DIR);
                File userHomedir = new File(userHome);
                File repository = new File(userHomedir, DIRECTORY_AXIS2_HOME);

                modules = new File(repository, DIRECTORY_MODULES);
            } else {
                modules = new File(axis2repository, DIRECTORY_MODULES);
            }

            String modulearchiveName = moduleName + SUFFIX_MAR;

            modulearchiveFile = new File(modules, modulearchiveName);

            if (modulearchiveFile.exists()) {
                return modulearchiveFile;
            }

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream(RESOURCE_MODULES + moduleName + SUFFIX_MAR);

            if (in == null) {
                in = cl.getResourceAsStream(RESOURCE_MODULES + moduleName + SUFFIX_JAR);
            }

            if (in == null) {
                throw new DeploymentException(
                        Messages.getMessage(
                                DeploymentErrorMsgs.MODULE_XML_MISSING, moduleName));
            } else {
                if (!modules.exists()) {
                    modules.mkdirs();
                }

                modulearchiveFile.createNewFile();

                FileOutputStream dest = new FileOutputStream(modulearchiveFile);
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
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

    /**
     * Extract Service XML files and builds the service groups
     * 
     * @param filename
     * @param engine
     * @param axisServiceGroup
     * @param extractService
     * @param wsdls
     * @param axisConfig
     * @return
     * @throws DeploymentException
     */
    public ArrayList processServiceGroup(String filename, DeploymentEngine engine,
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
                    if (entry.getName().equals(SERVICES_XML)) {
                        axisServiceGroup.setServiceGroupName(
                                DescriptionBuilder.getShortFileName(
                                        engine.getCurrentFileItem().getName()));

                        return buildServiceGroup(zin, engine, axisServiceGroup, wsdls, axisConfig);
                    }
                }
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND, filename));
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        } else {
            File file = new File(filename, SERVICES_XML);

            if (file.exists()) {
                InputStream in;

                try {
                    in = new FileInputStream(file);
                    axisServiceGroup.setServiceGroupName(engine.getCurrentFileItem().getName());

                    return buildServiceGroup(in, engine, axisServiceGroup, wsdls, axisConfig);
                } catch (FileNotFoundException e) {
                    throw new DeploymentException(
                            Messages.getMessage(DeploymentErrorMsgs.FILE_NOT_FOUND, e.getMessage()));
                } catch (XMLStreamException e) {
                    throw new DeploymentException(
                            Messages.getMessage(DeploymentErrorMsgs.XML_STREAM_EXCEPTION, e.getMessage()));
                }
            } else {
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND));
            }
        }
    }

    /**
     * Creats AxisService.
     *
     * @param in
     * @return Returns AxisService.
     * @throws DeploymentException
     */
    private AxisService processWSDLFile(InputStream in) throws DeploymentException {
        try {
            AxisServiceBuilder axisServiceBuilder = new AxisServiceBuilder();
            return axisServiceBuilder.getAxisService(in);
        } catch (WSDLException e) {
            throw new DeploymentException(e);
        }
    }

    /**
     * Creates service objects from wsdl file inside a service archive file.
     *
     * @param file      <code>ArchiveFileData</code>
     * @param depengine <code>DeploymentEngine</code>
     * @throws DeploymentException <code>DeploymentException</code>
     */
    public HashMap processWSDLs(ArchiveFileData file, DeploymentEngine depengine)
            throws DeploymentException {
        File serviceFile = file.getFile();

        // to store service come from wsdl files
        HashMap servicesMap = new HashMap();
        boolean isDirectory = serviceFile.isDirectory();

        if (isDirectory) {
            try {
                File meta_inf = new File(serviceFile, META_INF);

                if (!meta_inf.exists()) {
                    throw new DeploymentException(
                            Messages.getMessage(
                                    DeploymentErrorMsgs.META_INF_MISSING, serviceFile.getName()));
                }

                File files[] = meta_inf.listFiles();

                for (int i = 0; i < files.length; i++) {
                    File file1 = files[i];
                    if (file1.getName().toLowerCase().endsWith(SUFFIX_WSDL)) {
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
                byte[]                buf = new byte[1024];
                int read;
                ByteArrayOutputStream out;

                while ((entry = zin.getNextEntry()) != null) {
                    String entryName = entry.getName().toLowerCase();

                    if (entryName.startsWith(META_INF.toLowerCase())
                            && entryName.endsWith(SUFFIX_WSDL)) {
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

    public void readModuleArchive(String filename, DeploymentEngine engine,
                                  ModuleDescription module, boolean explodedDir,
                                  AxisConfiguration axisConfig)
            throws DeploymentException {

        // get attribute values
        boolean foundmoduleXML = false;

        if (!explodedDir) {
            ZipInputStream zin;

            try {
                zin = new ZipInputStream(new FileInputStream(filename));

                ZipEntry entry;

                while ((entry = zin.getNextEntry()) != null) {
                    if (entry.getName().equals(MODULE_XML)) {
                        foundmoduleXML = true;

                        ModuleBuilder builder = new ModuleBuilder(zin, module, axisConfig);

                        // setting module name
                        module.setName(
                                new QName(
                                        DescriptionBuilder.getShortFileName(
                                                engine.getCurrentFileItem().getServiceName())));
                        builder.populateModule();

                        break;
                    }
                }

                zin.close();

                if (!foundmoduleXML) {
                    throw new DeploymentException(
                            Messages.getMessage(
                                    DeploymentErrorMsgs.MODULE_XML_MISSING, filename));
                }
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        } else {
            File file = new File(filename, MODULE_XML);

            if (file.exists()) {
                InputStream in;

                try {
                    in = new FileInputStream(file);

                    ModuleBuilder builder = new ModuleBuilder(in, module, axisConfig);

                    // setting module name
                    module.setName(
                            new QName(
                                    DescriptionBuilder.getShortFileName(
                                            engine.getCurrentFileItem().getServiceName())));
                    builder.populateModule();
                } catch (FileNotFoundException e) {
                    throw new DeploymentException(
                            Messages.getMessage(DeploymentErrorMsgs.FILE_NOT_FOUND, e.getMessage()));
                }
            } else {
                throw new DeploymentException(
                        Messages.getMessage(
                                DeploymentErrorMsgs.MODULE_XML_MISSING, filename));
            }
        }
    }
}
