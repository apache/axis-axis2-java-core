/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.client;

import java.io.File;
import java.net.URL;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.junit.Assert;
import org.junit.Test;

public class ServiceClientTest extends Assert {
    /**
     * Tests that imported schemas are correctly resolved if the WSDL is loaded from a ZIP file.
     * This is a regression test for AXIS2-4353 and checks that WSDLs (with imports) can be loaded
     * from the class path (which usually means a JAR file).
     * 
     * @throws Exception
     */
    @Test
    public void testWSDLWithImportsFromZIP() throws Exception {
        ConfigurationContext configContext = ConfigurationContextFactory.createEmptyConfigurationContext();
        String basedir = System.getProperty("basedir");
        if (basedir == null) {
            basedir = ".";
        }
        URL zipUrl = new File(basedir, "target/test-zip.zip").toURL();
        URL wsdlUrl = new URL("jar:" + zipUrl + "!/test.wsdl");
        ServiceClient serviceClient = new ServiceClient(configContext, wsdlUrl, new QName("urn:test", "EchoService"), "EchoPort");
        List<XmlSchema> schemas = serviceClient.getAxisService().getSchema();
        assertEquals(1, schemas.size());
        XmlSchema schema = schemas.get(0);
        assertNotNull(schema.getTypeByName(new QName("urn:test", "echoResponse")));
    }
}
