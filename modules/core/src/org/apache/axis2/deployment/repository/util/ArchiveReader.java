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
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.DeploymentParser;
import org.apache.axis2.description.AxisDescWSDLComponentFactory;
import org.apache.axis2.description.ModuleDescription;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.wsdl.WSDLVersionWrapper;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.axis2.wsdl.builder.WOMBuilder;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLDescription;

import java.io.*;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class ArchiveReader implements DeploymentConstants {

    private Log log = LogFactory.getLog(getClass());

//    public ServiceDescription createService(String filename) throws DeploymentException {
//        String strArchive = filename;
//        ZipInputStream zin;
//        boolean foundwsdl = false;
//        ServiceDescription service = null;
//        Definition difinition = null;
//        try {
//            zin = new ZipInputStream(new FileInputStream(strArchive));
//            ZipEntry entry;
//            while ((entry = zin.getNextEntry()) != null) {
//                if (entry.getName().equals(SERVICEWSDL)) {
//                    WSDLVersionWrapper wsdlVersionWrapper = WOMBuilderFactory.getBuilder(
//                            WOMBuilderFactory.WSDL11).build(zin, new AxisDescWSDLComponentFactory());
//                    WSDLDescription      womDescription = wsdlVersionWrapper.getDescription();
//                    Iterator iterator = womDescription.getServices().keySet().iterator();
//                    if(iterator.hasNext()){
//                        service = (ServiceDescription)iterator.next();
//                    }
//                    difinition = wsdlVersionWrapper.getDefinition();
//                    foundwsdl = true;
//                    break;
//                }
//            }
//            zin.close();
//            if (!foundwsdl) {
//                service = new ServiceDescription();
//                log.info("WSDL file not found for the service :  " + filename);
//            }
//            service.setWSDLDefinition(difinition);
//        } catch (Exception e) {
//            throw new DeploymentException(e);
//        }
//        return service;
//    }

    public ServiceDescription createService(ArchiveFileData file) throws DeploymentException {
        ServiceDescription service = null;
        InputStream in= file.getClassLoader().getResourceAsStream(SERVICEWSDL);
        boolean foundservice = false;
        try {
            if(in!= null){
                WOMBuilder builder = WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11);
                WSDLVersionWrapper wsdlVersionWrapper = builder.build(in, new AxisDescWSDLComponentFactory());
                WSDLDescription  womDescription = wsdlVersionWrapper.getDescription();
                Iterator iterator = womDescription.getServices().keySet().iterator();
                if(iterator.hasNext()){
                    foundservice = true;
                    service = (ServiceDescription)womDescription.getServices().get(iterator.next());
                }
                if(!foundservice){
                    service = new ServiceDescription();
                }
                service.setWSDLDefinition(wsdlVersionWrapper.getDefinition());
                in.close();
            } else {
                service = new ServiceDescription();
                log.info("WSDL file not found for the service :  " + file.getName());
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

    public void readServiceArchive(String filename, DeploymentEngine engine, ServiceDescription service) throws DeploymentException {
        // get attribute values
        boolean foundServiceXML = false;
        String strArchive = filename;
        ZipInputStream zin;
        try {
            zin = new ZipInputStream(new FileInputStream(strArchive));
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals(SERVICEXML)) {
                    foundServiceXML = true;
                    DeploymentParser schme = new DeploymentParser(zin, engine);
                    schme.parseServiceXML(service);
                    break;
                }
            }
            zin.close();
            if (!foundServiceXML) {
                throw new DeploymentException("service.xml not found");
            }
        } catch (Exception e) {
            throw new DeploymentException(e);
        }
    }

    public void readModuleArchive(String filename, DeploymentEngine engine, ModuleDescription module) throws DeploymentException {
        // get attribute values
        boolean foundmoduleXML = false;
        String strArchive = filename;
        ZipInputStream zin = null;
        try {
            zin = new ZipInputStream(new FileInputStream(strArchive));
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals(MODULEXML)) {
                    foundmoduleXML = true;
                    DeploymentParser schme = new DeploymentParser(zin, engine);
                    schme.procesModuleXML(module);
                    break;
                }
            }
            //  zin.closeEntry();
            zin.close();
            if (!foundmoduleXML) {
                throw new DeploymentException("module.xml not found  for the module :  " + strArchive);
            }
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
    }

    /**
     * This method first check whether the given module is there in the user home dirctory if so return
     * that , else try to read the given module form classpath (from resources ) if found first get the module.mar
     * file from the resourceStream and write that into user home/axis2home/nodule directory
     * @param moduleName
     * @return
     * @throws DeploymentException
     */
    public File creatModuleArchivefromResource(String moduleName , String axis2repository) throws DeploymentException {
        File modulearchiveFile = null;
        File modules = null;
        try {
            int BUFFER = 2048;
            if(axis2repository == null ){
                String userHome = System.getProperty("user.home");
                File userHomedir = new File(userHome);
                File repository = new File(userHomedir, ".axis2home");
                if (!repository.exists()) {
                    repository.mkdirs();
                    modules = new File(repository, "modules");
                    modules.mkdirs();
                }
            } else {
                modules = new File(axis2repository , "modules");
                if(!modules.exists()){
                    modules = new File(axis2repository, "modules");
                    modules.mkdirs();
                }
            }
            String modulearchiveName =moduleName + ".mar";
            modulearchiveFile = new File(modules,modulearchiveName);
            if (modulearchiveFile.exists()) {
                return modulearchiveFile;
            } else {
                modulearchiveFile.createNewFile();
            }
            FileOutputStream dest = new
                    FileOutputStream(modulearchiveFile);
            ZipOutputStream out = new ZipOutputStream(new
                    BufferedOutputStream(dest));
            byte data[] = new byte[BUFFER];

            ClassLoader cl = Thread.currentThread().getContextClassLoader();
            InputStream in = cl.getResourceAsStream("modules/" + moduleName + ".mar");
            if(in == null ){
                in = cl.getResourceAsStream("modules/" + moduleName + ".jar");
            }
            if(in == null){
                throw new DeploymentException( moduleName + " module is not found");
            }
            ZipInputStream zin = null;
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
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
        return  modulearchiveFile;
    }

}








