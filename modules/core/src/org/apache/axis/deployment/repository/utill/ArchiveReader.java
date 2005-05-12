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

package org.apache.axis.deployment.repository.utill;

import java.io.FileInputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.axis.deployment.DeploymentConstants;
import org.apache.axis.deployment.DeploymentEngine;
import org.apache.axis.deployment.DeploymentException;
import org.apache.axis.deployment.DeploymentParser;
import org.apache.axis.description.AxisDescWSDLComponentFactory;
import org.apache.axis.description.ModuleDescription;
import org.apache.axis.description.ServiceDescription;
import org.apache.axis.wsdl.builder.wsdl4j.WSDL1ToWOMBuilder;

public class ArchiveReader implements DeploymentConstants {

    public ServiceDescription createService(String filename) throws DeploymentException {
        WSDL1ToWOMBuilder builder = new WSDL1ToWOMBuilder();
        String strArchive = filename;
        ZipInputStream zin;
        boolean foundwsdl = false;
        ServiceDescription service = null;
        try {
            zin = new ZipInputStream(new FileInputStream(strArchive));
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.getName().equals(SERVICEWSDL)) {
                    service = (ServiceDescription) builder.build(zin, new AxisDescWSDLComponentFactory());
                    foundwsdl = true;
                    break;
                }
            }
            zin.close();
            if (!foundwsdl) {
                service = new ServiceDescription();
            }
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
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
            throw new DeploymentException(e.getMessage());
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
                throw new DeploymentException("module.xml not found");
            }
        } catch (Exception e) {
            throw new DeploymentException(e.getMessage());
        }
    }
}








