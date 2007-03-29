package org.apache.axis2.wsdl;

import junit.framework.TestCase;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.WSDL11ToAxisServiceBuilder;
import org.apache.axis2.engine.ListenerManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
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
*
*
*/

public class WSDLServiceBuilderTest extends TestCase {

    private ConfigurationContext configContext;
    ListenerManager lm;

    protected void setUp() throws Exception {
        configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(null, null);
        lm = new ListenerManager();
        lm.init(configContext);
        lm.start();
    }

    protected void tearDown() throws AxisFault {
        configContext.terminate();
    }

    public void testWSDLClient() throws Exception {
        File testResourceFile = new File("target/test-classes");
        File outLocation = new File("target/test-resources");
        outLocation.mkdirs();
        if (testResourceFile.exists()) {
            File files [] = testResourceFile.listFiles();
            for (int i = 0; i < files.length; i++) {
                File file1 = files[i];
                if (file1.isFile() && file1.getName().endsWith(".wsdl")) {
                    if (file1.getName().equals("ping-modified.wsdl") ||
                            file1.getName().equals("ping-unbound.wsdl") ||
                            file1.getName().equals("wsat.wsdl") ||
                            file1.getName().equals("no-service.wsdl")) {
                        continue;
                    }
                    try {
                        WSDL11ToAxisServiceBuilder builder = new WSDL11ToAxisServiceBuilder(new FileInputStream(file1), null, null);
                        AxisService service = builder.populateService();
                        System.out.println("Testinf file: " + file1.getName());
                        configContext.getAxisConfiguration().addService(service);
                        OutputStream out = new FileOutputStream(new File(outLocation, file1.getName()));
                        service.printWSDL(out, "http://google.com/axis2/services" ,"services");
                        out.flush();
                        out.close();
//                        URL wsdlURL = new URL("http://localhost:" + 6060 +
//                                "/axis2/services/" + service.getName() + "?wsdl");
//                        builder = new WSDL11ToAxisServiceBuilder(wsdlURL.openStream(), null, null);
//                        service = builder.populateService();
                        configContext.getAxisConfiguration().removeService(service.getName());
                    } catch (Exception e) {
                        System.out.println("Error in WSDL : " + file1.getName());
                        throw e;
                    }

                }
            }
        }
    }
}
