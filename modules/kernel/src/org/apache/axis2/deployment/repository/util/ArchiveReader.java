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

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.DeploymentConstants;
import org.apache.axis2.deployment.DeploymentErrorMsgs;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.DescriptionBuilder;
import org.apache.axis2.deployment.ModuleBuilder;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.deployment.ServiceGroupBuilder;
import org.apache.axis2.deployment.resolver.AARBasedWSDLLocator;
import org.apache.axis2.deployment.resolver.AARFileBasedURIResolver;
import org.apache.axis2.deployment.resolver.WarBasedWSDLLocator;
import org.apache.axis2.deployment.resolver.WarFileBasedURIResolver;
import org.apache.axis2.description.AxisModule;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.AxisServiceGroup;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.description.WSDL20ToAxisServiceBuilder;
import org.apache.axis2.description.WSDLToAxisServiceBuilder;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.namespace.Constants;
import org.apache.axis2.util.XMLUtils;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class ArchiveReader implements DeploymentConstants {
    private static final Log log = LogFactory.getLog(ArchiveReader.class);

    public ArrayList buildServiceGroup(InputStream zin, DeploymentFileData currentFile,
                                       AxisServiceGroup axisServiceGroup, HashMap wsdlServices,
                                       ConfigurationContext configCtx)
            throws XMLStreamException, AxisFault {

        DescriptionBuilder builder = new DescriptionBuilder(zin, configCtx);
        OMElement rootElement = builder.buildOM();
        String elementName = rootElement.getLocalName();

        if (TAG_SERVICE.equals(elementName)) {
            AxisService axisService = null;
            String serviceName = DescriptionBuilder.getShortFileName(currentFile.getName());
            if (serviceName != null) {
                axisService = (AxisService) wsdlServices.get(serviceName);
            }
            if (axisService == null) {
                axisService = (AxisService) wsdlServices.get(
                        DescriptionBuilder.getShortFileName(currentFile.getName()));
            }
            if (axisService == null) {
                axisService = new AxisService(serviceName);
            } else {
                axisService.setWsdlFound(true);
                axisService.setCustomWsdl(true);
            }

            axisService.setParent(axisServiceGroup);
            axisService.setClassLoader(currentFile.getClassLoader());

            ServiceBuilder serviceBuilder = new ServiceBuilder(configCtx, axisService);
            AxisService service = serviceBuilder.populateService(rootElement);

            ArrayList serviceList = new ArrayList();
            serviceList.add(service);
            return serviceList;
        } else if (TAG_SERVICE_GROUP.equals(elementName)) {
            ServiceGroupBuilder groupBuilder = new ServiceGroupBuilder(rootElement, wsdlServices,
                                                                       configCtx);
            return groupBuilder.populateServiceGroup(axisServiceGroup);
        }
        throw new AxisFault("Invalid services.xml found");
    }

    /**
     * Extracts Service XML files and builds the service groups.
     *
     * @param filename
     * @param axisServiceGroup
     * @param extractService
     * @param wsdls
     * @param configCtx
     * @return Returns ArrayList.
     * @throws DeploymentException
     */
    public ArrayList processServiceGroup(String filename, DeploymentFileData currentFile,
                                         AxisServiceGroup axisServiceGroup,
                                         boolean extractService,
                                         HashMap wsdls,
                                         ConfigurationContext configCtx)
            throws AxisFault {
        // get attribute values
        if (!extractService) {
            ZipInputStream zin = null;
            FileInputStream fin = null;
            try {
                fin = new FileInputStream(filename);
                zin = new ZipInputStream(fin);
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    if (entry.getName().equalsIgnoreCase(SERVICES_XML)) {
                        axisServiceGroup.setServiceGroupName(
                                DescriptionBuilder.getShortFileName(currentFile.getName()));
                        return buildServiceGroup(zin, currentFile, axisServiceGroup, wsdls,
                                                 configCtx);
                    }
                }
                throw new DeploymentException(
                        Messages.getMessage(DeploymentErrorMsgs.SERVICE_XML_NOT_FOUND, filename));
            } catch (Exception e) {
                throw new DeploymentException(e);
            } finally {
                if (zin != null) {
                    try {
                        zin.close();
                    } catch (IOException e) {
                        log.info(Messages.getMessage("errorininputstreamclose"));
                    }
                }
                if (fin != null) {
                    try {
                        fin.close();
                    } catch (IOException e) {
                        log.info(Messages.getMessage("errorininputstreamclose"));
                    }
                }
            }
        } else {
            File file = new File(filename, SERVICES_XML);
            if (!file.exists()) {
                // try for meta-inf
                file = new File(filename, SERVICES_XML.toLowerCase());
            }
            if (file.exists()) {
                InputStream in = null;
                try {
                    in = new FileInputStream(file);
                    axisServiceGroup.setServiceGroupName(currentFile.getName());
                    return buildServiceGroup(in, currentFile, axisServiceGroup, wsdls, configCtx);
                } catch (FileNotFoundException e) {
                    throw new DeploymentException(
                            Messages.getMessage(DeploymentErrorMsgs.FILE_NOT_FOUND,
                                                e.getMessage()));
                } catch (XMLStreamException e) {
                    throw new DeploymentException(
                            Messages.getMessage(DeploymentErrorMsgs.XML_STREAM_EXCEPTION,
                                                e.getMessage()));
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            log.info(Messages.getMessage("errorininputstreamclose"));
                        }
                    }
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
    private AxisService processWSDLFile(WSDLToAxisServiceBuilder axisServiceBuilder,
                                        File serviceArchiveFile,
                                        boolean isArchive, InputStream in, String baseURI)
            throws DeploymentException {
        try {

            if (serviceArchiveFile != null && isArchive) {
                axisServiceBuilder.setCustomResolver(
                        new AARFileBasedURIResolver(serviceArchiveFile));
                if (axisServiceBuilder instanceof WSDL11ToAxisServiceBuilder) {

                    ((WSDL11ToAxisServiceBuilder) axisServiceBuilder).setCustomWSLD4JResolver(
                            new AARBasedWSDLLocator(baseURI, serviceArchiveFile, in));
                } else if (axisServiceBuilder instanceof WSDL20ToAxisServiceBuilder) {
                    // trying to use the jar scheme as the base URI. I think this can be used to handle
                    // wsdl 1.1 as well without using a custome URI resolver. Need to look at it later.
                    axisServiceBuilder.setBaseUri(
                            "jar:file://" + serviceArchiveFile.getAbsolutePath() + "!/" + baseURI);
                }
            } else {
                if (serviceArchiveFile != null) {
                    axisServiceBuilder.setBaseUri(
                            serviceArchiveFile.getParentFile().getAbsolutePath());
                }
            }
            return axisServiceBuilder.populateService();
        } catch (AxisFault axisFault) {
            log.info("Trouble processing wsdl file :" + axisFault.getMessage());
            if (log.isDebugEnabled()) {
                log.debug(axisFault);
            }
            return null;
        }
    }

    /**
     * Creates service objects from wsdl file inside a service archive file.
     *
     * @param file <code>ArchiveFileData</code>
     * @throws DeploymentException <code>DeploymentException</code>
     */
    public HashMap processWSDLs(DeploymentFileData file)
            throws DeploymentException {
        File serviceFile = file.getFile();
        // to store service come from wsdl files
        HashMap servicesMap = new HashMap();
        boolean isDirectory = serviceFile.isDirectory();
        if (isDirectory) {
            try {
                File metaInfFolder = new File(serviceFile, META_INF);

                if (!metaInfFolder.exists()) {
                    metaInfFolder = new File(serviceFile, META_INF.toLowerCase());
                    if (!metaInfFolder.exists()) {
                        throw new DeploymentException(
                                Messages.getMessage(
                                        DeploymentErrorMsgs.META_INF_MISSING,
                                        serviceFile.getName()));
                    }
                }

                processFilesInFolder(metaInfFolder, servicesMap);

            } catch (FileNotFoundException e) {
                throw new DeploymentException(e);
            } catch (IOException e) {
                throw new DeploymentException(e);
            } catch (XMLStreamException e) {
                throw new DeploymentException(e);
            }
        } else {
            ZipInputStream zin;
            FileInputStream fin;
            try {
                fin = new FileInputStream(serviceFile);
                zin = new ZipInputStream(fin);

                //TODO Check whether this WSDL is empty

                ZipEntry entry;
                byte[] buf = new byte[1024];
                int read;
                ByteArrayOutputStream out;
                while ((entry = zin.getNextEntry()) != null) {
                    String entryName = entry.getName().toLowerCase();
                    if (entryName.startsWith(META_INF.toLowerCase())
                            && entryName.endsWith(SUFFIX_WSDL)) {
                        out = new ByteArrayOutputStream();

                        // we do not want to generate the services for the
                        // imported wsdl of one file.
                        if ((entryName.indexOf("/") != entryName.lastIndexOf("/"))
                                || (entryName.indexOf("wsdl_") != -1)) {
                            //only care abt the toplevel wsdl
                            continue;
                        }

                        while ((read = zin.read(buf)) > 0) {
                            out.write(buf, 0, read);
                        }

                        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

                        // now the question is which version of WSDL file this archive contains.
                        // lets check the namespace of the root element and decide. But since we are
                        // using axiom (dude, you are becoming handy here :)), we will not build the
                        // whole thing.
                        OMNamespace documentElementNS =
                                ((OMElement) XMLUtils.toOM(in)).getNamespace();
                        if (documentElementNS != null) {
                            WSDLToAxisServiceBuilder wsdlToAxisServiceBuilder = null;
                            if (WSDLConstants.WSDL20_2006Constants.DEFAULT_NAMESPACE_URI
                                    .equals(documentElementNS.getNamespaceURI())) {
                                // we have a WSDL 2.0 document here.
                                wsdlToAxisServiceBuilder = new WSDL20ToAxisServiceBuilder(
                                        new ByteArrayInputStream(out.toByteArray()), null, null);
                                wsdlToAxisServiceBuilder.setBaseUri(entryName);
                            } else if (Constants.NS_URI_WSDL11.
                                    equals(documentElementNS.getNamespaceURI())) {
                                wsdlToAxisServiceBuilder = new WSDL11ToAxisServiceBuilder(
                                        new ByteArrayInputStream(out.toByteArray()), null, null);
                            } else {
                                new DeploymentException(Messages.getMessage("invalidWSDLFound"));
                            }
                            AxisService service = processWSDLFile(wsdlToAxisServiceBuilder,
                                                                  serviceFile, true,
                                                                  new ByteArrayInputStream(
                                                                          out.toByteArray()),
                                                                  entry.getName());
                            if (service != null) {
                                servicesMap.put(service.getName(), service);
                            }
                        }
                    }
                }
                try {
                    zin.close();
                } catch (IOException e) {
                    log.info(e);
                }
                try {
                    fin.close();
                } catch (IOException e) {
                    log.info(e);
                }
            } catch (FileNotFoundException e) {
                throw new DeploymentException(e);
            } catch (IOException e) {
                throw new DeploymentException(e);
            } catch (XMLStreamException e) {
                throw new DeploymentException(e);
            }
        }
        return servicesMap;
    }

    public AxisService getAxisServiceFromWsdl(InputStream in,
                                              ClassLoader loader, String wsdlUrl) throws Exception {
//         ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());

        // now the question is which version of WSDL file this archive contains.
        // lets check the namespace of the root element and decide. But since we are
        // using axiom (dude, you are becoming handy here :)), we will not build the
        // whole thing.
        OMElement element = (OMElement) XMLUtils.toOM(in);
        OMNamespace documentElementNS = element.getNamespace();
        if (documentElementNS != null) {
            WSDL11ToAxisServiceBuilder wsdlToAxisServiceBuilder;
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            element.serialize(out);
            if (Constants.NS_URI_WSDL11.
                    equals(documentElementNS.getNamespaceURI())) {
                wsdlToAxisServiceBuilder = new WSDL11ToAxisServiceBuilder(
                        new ByteArrayInputStream(out.toByteArray()), null, null);
                wsdlToAxisServiceBuilder.setCustomWSLD4JResolver(new WarBasedWSDLLocator(wsdlUrl,
                                                                                         loader,
                                                                                         new ByteArrayInputStream(
                                                                                                 out.toByteArray())));
                wsdlToAxisServiceBuilder.setCustomResolver(
                        new WarFileBasedURIResolver(loader));
                return wsdlToAxisServiceBuilder.populateService();
            } else {
                new DeploymentException(Messages.getMessage("invalidWSDLFound"));
            }
        }
        return null;
    }

    public void processFilesInFolder(File folder, HashMap servicesMap)
            throws FileNotFoundException, XMLStreamException, DeploymentException {
        File files[] = folder.listFiles();
        for (int i = 0; i < files.length; i++) {
            File file1 = files[i];
            if (file1.getName().toLowerCase().endsWith(SUFFIX_WSDL)) {
                InputStream in = new FileInputStream(file1);
                FileInputStream in2 = null;

                // now the question is which version of WSDL file this archive contains.
                // lets check the namespace of the root element and decide. But since we are
                // using axiom (dude, you are becoming handy here :)), we will not build the
                // whole thing.
                OMNamespace documentElementNS = ((OMElement) XMLUtils.toOM(in)).getNamespace();
                if (documentElementNS != null) {
                    WSDLToAxisServiceBuilder wsdlToAxisServiceBuilder = null;
                    if (WSDLConstants.WSDL20_2006Constants.DEFAULT_NAMESPACE_URI
                            .equals(documentElementNS.getNamespaceURI())) {
                        // we have a WSDL 2.0 document here.
                        in2 = new FileInputStream(file1);
                        wsdlToAxisServiceBuilder = new WSDL20ToAxisServiceBuilder(in2, null, null);
                    } else if (Constants.NS_URI_WSDL11.
                            equals(documentElementNS.getNamespaceURI())) {
                        in2 = new FileInputStream(file1);
                        wsdlToAxisServiceBuilder = new WSDL11ToAxisServiceBuilder(in2, null, null);
                    } else {
                        new DeploymentException(Messages.getMessage("invalidWSDLFound"));
                    }

                    FileInputStream in3 = new FileInputStream(file1);
                    AxisService service = processWSDLFile(wsdlToAxisServiceBuilder, file1, false,
                                                          in2, file1.toURI().toString());
                    try {
                        if (in2 != null) {
                            in2.close();
                        }
                        in3.close();
                    } catch (IOException e) {
                        log.info(e);
                    }
                    if (service != null) {
                        servicesMap.put(service.getName(), service);
                    }
                }

                try {
                    in.close();
                } catch (IOException e) {
                    log.info(e);
                }
            }
        }
    }

    public void readModuleArchive(DeploymentFileData deploymentFile,
                                  AxisModule module, boolean explodedDir,
                                  AxisConfiguration axisConfig)
            throws DeploymentException {

        // get attribute values
        boolean moduleXMLFound = false;
        if (!explodedDir) {
            ZipInputStream zin;
            FileInputStream fin;
            try {
                fin = new FileInputStream(deploymentFile.getAbsolutePath());
                zin = new ZipInputStream(fin);
                ZipEntry entry;
                while ((entry = zin.getNextEntry()) != null) {
                    if (entry.getName().equalsIgnoreCase(MODULE_XML)) {
                        moduleXMLFound = true;
                        ModuleBuilder builder = new ModuleBuilder(zin, module, axisConfig);
                        // setting module name
                        module.setName(
                                new QName(
                                        DescriptionBuilder.getShortFileName(
                                                deploymentFile.getServiceName())));
                        builder.populateModule();
                        break;
                    }
                }
                zin.close();
                fin.close();
                if (!moduleXMLFound) {
                    throw new DeploymentException(
                            Messages.getMessage(
                                    DeploymentErrorMsgs.MODULE_XML_MISSING,
                                    deploymentFile.getAbsolutePath()));
                }
            } catch (Exception e) {
                throw new DeploymentException(e);
            }
        } else {
            File file = new File(deploymentFile.getAbsolutePath(), MODULE_XML);

            if (file.exists() ||
                    (file = new File(deploymentFile.getAbsolutePath(), MODULE_XML.toLowerCase()))
                            .exists()) {
                InputStream in = null;
                try {
                    in = new FileInputStream(file);
                    ModuleBuilder builder = new ModuleBuilder(in, module, axisConfig);
                    // setting module name
                    module.setName(
                            new QName(
                                    DescriptionBuilder.getShortFileName(
                                            deploymentFile.getServiceName())));
                    builder.populateModule();
                } catch (FileNotFoundException e) {
                    throw new DeploymentException(
                            Messages.getMessage(DeploymentErrorMsgs.FILE_NOT_FOUND,
                                                e.getMessage()));
                } finally {
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e) {
                            log.info(Messages.getMessage("errorininputstreamclose"));
                        }
                    }
                }
            } else {
                throw new DeploymentException(
                        Messages.getMessage(
                                DeploymentErrorMsgs.MODULE_XML_MISSING,
                                deploymentFile.getAbsolutePath()));
            }
        }
    }
}
