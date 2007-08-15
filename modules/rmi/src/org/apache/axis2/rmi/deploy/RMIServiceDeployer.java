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
package org.apache.axis2.rmi.deploy;

import org.apache.axis2.deployment.Deployer;
import org.apache.axis2.deployment.DeploymentException;
import org.apache.axis2.deployment.DeploymentClassLoader;
import org.apache.axis2.deployment.repository.util.DeploymentFileData;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.rmi.metadata.Parameter;
import org.apache.axis2.rmi.deploy.config.Config;
import org.apache.axis2.rmi.deploy.config.Service;
import org.apache.axis2.rmi.deploy.config.PackageToNamespaceMap;
import org.apache.axis2.rmi.Configurator;
import org.apache.axis2.rmi.databind.XmlStreamParser;
import org.apache.axis2.rmi.databind.SimpleTypeHandler;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.XmlParsingException;
import org.apache.axis2.rmi.exception.ConfigFileReadingException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.util.Loader;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.Map;
import java.util.HashMap;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipEntry;


public class RMIServiceDeployer implements Deployer {

    private ConfigurationContext configurationContext;
    private AxisConfiguration axisConfiguration;

    public void init(ConfigurationContext configCtx) {
        this.configurationContext = configCtx;
        this.axisConfiguration = this.configurationContext.getAxisConfiguration();

    }

    public void deploy(DeploymentFileData deploymentFileData) throws DeploymentException {

        try {

            DeploymentClassLoader deploymentClassLoader =
                    new DeploymentClassLoader(new URL[]{deploymentFileData.getFile().toURL()}, null,
                            Thread.currentThread().getContextClassLoader());
            deploymentFileData.setClassLoader(deploymentClassLoader);
            String absolutePath = deploymentFileData.getFile().getAbsolutePath();

            // gettting the file reader for zipinput stream
            Config configObject = getConfig(absolutePath);

            Configurator configurator = new Configurator();

            if (configObject.getExtensionClasses() != null) {
                String[] extensionClasses = configObject.getExtensionClasses().getExtensionClass();
                if (extensionClasses != null) {
                    Class extensionClass;
                    for (int i = 0; i < extensionClasses.length; i++) {
                        extensionClass = Loader.loadClass(deploymentClassLoader, extensionClasses[i]);
                        configurator.addExtension(extensionClass);
                    }
                }
            }

            if (configObject.getPackageToNamespaceMapings() != null) {
                PackageToNamespaceMap[] packageToNamespaceMapings =
                        configObject.getPackageToNamespaceMapings().getPackageToNamespaceMap();
                if (packageToNamespaceMapings != null) {
                    for (int i = 0; i < packageToNamespaceMapings.length; i++) {
                        configurator.addPackageToNamespaceMaping(packageToNamespaceMapings[i].getPackageName(),
                                packageToNamespaceMapings[i].getNamespace());
                    }
                }
            }

            // set the simple type data handler if it is set
            if ((configObject.getSimpleDataHandlerClass() != null)
                    && (configObject.getSimpleDataHandlerClass().trim().length() > 0)){
                Class simpleTypeHandlerClass =
                        Loader.loadClass(deploymentClassLoader,configObject.getSimpleDataHandlerClass());
                try {
                    SimpleTypeHandler simpleTypeHandler = (SimpleTypeHandler) simpleTypeHandlerClass.newInstance();
                    configurator.setSimpleTypeHandler(simpleTypeHandler);
                } catch (InstantiationException e) {
                    throw new DeploymentException("Can not instantiate simple type handler",e);
                } catch (IllegalAccessException e) {
                    throw new DeploymentException("Can not instantiate simple type handler",e);
                }
            }

            Service[] services = configObject.getServices().getService();
            ClassDeployer classDeployer =
                    new ClassDeployer(configurationContext, deploymentClassLoader, configurator);
            Class serviceClass;

            for (int i = 0; i < services.length; i++) {
                serviceClass = Loader.loadClass(deploymentClassLoader, services[i].getServiceClass());
                classDeployer.deployClass(serviceClass);
            }

        } catch (ClassNotFoundException e) {
            throw new DeploymentException("Service class not found", e);
        } catch (AxisFault axisFault) {
            throw new DeploymentException("axis fault", axisFault);
        } catch (MalformedURLException e) {
            throw new DeploymentException("zip file url is not correct", e);
        } catch (IOException e) {
            throw new DeploymentException("zip file not found", e);
        } catch (ConfigFileReadingException e) {
            throw new DeploymentException("config file reading problem", e);
        }
    }

    private Config getConfig(String zipFilePath) throws ConfigFileReadingException {
        try {
            InputStream configFileInputStream = getConfigFileInputStream(zipFilePath);
            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(configFileInputStream);

            Configurator configurator = new Configurator();
            configurator.addPackageToNamespaceMaping("org.apache.axis2.rmi.deploy.config",
                    "http://ws.apache.org/axis2/rmi");
            Map processedTypeMap = new HashMap();
            Map processedSchemaMap = new HashMap();
            Parameter parameter = new Parameter(Config.class, "config");
            parameter.setNamespace("http://ws.apache.org/axis2/rmi");
            parameter.populateMetaData(configurator, processedTypeMap);
            parameter.generateSchema(configurator, processedSchemaMap);

            XmlStreamParser xmlStreamParser = new XmlStreamParser(processedTypeMap, configurator, processedSchemaMap);
            return (Config) xmlStreamParser.getObjectForParameter(xmlReader, parameter);
        } catch (IOException e) {
            throw new ConfigFileReadingException("Can not read configuration file", e);
        } catch (XMLStreamException e) {
            throw new ConfigFileReadingException("xml stream exception with configuration file", e);
        } catch (MetaDataPopulateException e) {
            throw new ConfigFileReadingException("metadata population problem with configuration file", e);
        } catch (XmlParsingException e) {
            throw new ConfigFileReadingException("xml stream reading problem with configuration file", e);
        } catch (SchemaGenerationException e) {
            throw new ConfigFileReadingException("problem in generating schema", e);
        }
    }

    private InputStream getConfigFileInputStream(String zipFilePath) throws IOException {

        ZipInputStream zipInputStream = new ZipInputStream(new FileInputStream(zipFilePath));
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ZipEntry zipEntry;
        byte[] buffer = new byte[1024];
        int read;
        while ((zipEntry = zipInputStream.getNextEntry()) != null) {
            if (zipEntry.getName().equals("META-INF" + File.separator + "config.xml")) {
                while ((read = zipInputStream.read(buffer)) > 0) {
                    byteArrayOutputStream.write(buffer, 0, read);
                }
            }

        }
        return new ByteArrayInputStream(byteArrayOutputStream.toByteArray());
    }

    public void setDirectory(String directory) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setExtension(String extension) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void unDeploy(String fileName) throws DeploymentException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
