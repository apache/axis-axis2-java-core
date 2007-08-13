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
package org.apache.axis2.rmi.databind;

import org.apache.axis2.rmi.deploy.config.*;
import org.apache.axis2.rmi.metadata.Parameter;
import org.apache.axis2.rmi.exception.MetaDataPopulateException;
import org.apache.axis2.rmi.exception.SchemaGenerationException;
import org.apache.axis2.rmi.exception.XmlSerializingException;
import org.apache.axis2.rmi.exception.XmlParsingException;
import org.apache.axis2.rmi.util.NamespacePrefix;
import org.apache.axiom.om.util.StAXUtils;

import javax.xml.stream.XMLStreamWriter;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;


public class ConfigObjectTest extends DataBindTest {

    public void testConfigObject() {

        Config config = new Config();

        Service[] services = new Service[2];
        services[0] = new Service();
        services[0].setServiceClass("Service1");

        services[1] = new Service();
        services[1].setServiceClass("Service2");

        Services testServices = new Services();
        testServices.setService(services);
        config.setServices(testServices);

        ExtensionClasses extensionClasses = new ExtensionClasses();
        extensionClasses.setExtensionClass(new String[]{"extension1", "extension2"});
        config.setExtensionClasses(extensionClasses);

        PackageToNamespaceMapings packageToNamespaceMapings = new PackageToNamespaceMapings();

        PackageToNamespaceMap[] packageToNamespaceMaps = new PackageToNamespaceMap[2];
        packageToNamespaceMaps[0] = new PackageToNamespaceMap();
        packageToNamespaceMaps[0].setNamespace("ns1");
        packageToNamespaceMaps[0].setPackageName("package1");

        packageToNamespaceMaps[1] = new PackageToNamespaceMap();
        packageToNamespaceMaps[1].setNamespace("ns2");
        packageToNamespaceMaps[1].setPackageName("package2");
        packageToNamespaceMapings.setPackageToNamespaceMap(packageToNamespaceMaps);
        config.setPackageToNamespaceMapings(packageToNamespaceMapings);


        Parameter parameter = new Parameter(Config.class, "config");
        parameter.setNamespace("http://ws.apache.org/axis2/rmi");


        try {
            this.configurator.addPackageToNamespaceMaping("org.apache.axis2.rmi.deploy.config",
                    "http://ws.apache.org/axis2/rmi");
            parameter.populateMetaData(configurator, processedMap);
            parameter.generateSchema(configurator, schemaMap);
            StringWriter configXmlWriter = new StringWriter();
            XMLStreamWriter writer = StAXUtils.createXMLStreamWriter(configXmlWriter);
            JavaObjectSerializer javaObjectSerializer = new JavaObjectSerializer(this.processedMap, this.configurator, this.schemaMap);
            javaObjectSerializer.serializeParameter(config, parameter, writer, new NamespacePrefix());
            writer.flush();

            String configXmlString = configXmlWriter.toString();

            XMLStreamReader xmlReader = StAXUtils.createXMLStreamReader(new ByteArrayInputStream(configXmlString.getBytes()));
            XmlStreamParser xmlStreamParser = new XmlStreamParser(this.processedMap, this.configurator, this.schemaMap);
            Config result = (Config) xmlStreamParser.getObjectForParameter(xmlReader, parameter);
            System.out.println("OK");
        } catch (XMLStreamException e) {
            fail();
        } catch (SchemaGenerationException e) {
            fail();
        } catch (MetaDataPopulateException e) {
            fail();
        } catch (XmlSerializingException e) {
            fail();
        } catch (XmlParsingException e) {
            fail();
        }

    }

}
