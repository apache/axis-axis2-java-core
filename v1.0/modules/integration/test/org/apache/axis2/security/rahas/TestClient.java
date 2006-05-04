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

package org.apache.axis2.security.rahas;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.security.handler.WSSHandlerConstants;
import org.apache.axis2.security.handler.config.InflowConfiguration;
import org.apache.axis2.security.handler.config.OutflowConfiguration;
import org.apache.axis2.security.rahas.RahasConfiguration;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import java.io.StringWriter;

import junit.framework.TestCase;

public abstract class TestClient extends TestCase {
    
    private static final String AXIS2_ECHO_STRING = "Axis2 Echo String";
    protected int port = UtilServer.TESTING_PORT;
    private QName rahasModule = new QName("rahas");
    
    public TestClient(String name) {
        super(name);
    }

    protected void setUp() throws Exception {
        UtilServer.start(Constants.TESTING_PATH + getServiceRepo() ,null);
    }
    
    protected void tearDown() throws Exception {
        UtilServer.stop();
    }

    /**
     * @param args
     */
    public void testRahas() {
        try {

            // Get the repository location from the args
            String repo = Constants.TESTING_PATH + "rahas_client_repo";

            OMElement payload = getEchoElement();
            ConfigurationContext configContext = ConfigurationContextFactory.createConfigurationContextFromFileSystem(repo,
                    null);
            ServiceClient serviceClient = new ServiceClient(configContext, null);
            Options options = new Options();
            options.setTo(new EndpointReference("http://127.0.0.1:" + port + "/axis2/services/Service"));
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);

            
            OutflowConfiguration clientOutflowConfiguration = getClientOutflowConfiguration();
            if(clientOutflowConfiguration != null) {
                options.setProperty(WSSHandlerConstants.OUTFLOW_SECURITY, clientOutflowConfiguration.getProperty());
            }
            InflowConfiguration clientInflowConfiguration = getClientInflowConfiguration();
            if(clientInflowConfiguration != null) {
                options.setProperty(WSSHandlerConstants.INFLOW_SECURITY, clientInflowConfiguration.getProperty());
            }
            options.setProperty(RahasConfiguration.RAHAS_CONFIG, getClientRahasConfiguration());
            
            options.setAction("urn:echo");
            
            serviceClient.engageModule(rahasModule);

            serviceClient.setOptions(options);
            
            //Blocking invocation
            serviceClient.sendReceive(payload);
            
            serviceClient.sendReceive(getEchoElement());
            
            OMElement result = serviceClient.sendReceive(getEchoElement());
            
            StringWriter writer = new StringWriter();
            result.serialize(StAXUtils
                    .createXMLStreamWriter(writer));
            writer.flush();
            
            assertTrue(writer.toString().indexOf(AXIS2_ECHO_STRING) > 0);
            System.out.println("SecureService Invocation successful :-)");
        } catch (AxisFault axisFault) {
            axisFault.printStackTrace();
            fail(axisFault.getMessage());
        } catch (XMLStreamException e) {
            e.printStackTrace();
            fail(e.getMessage());
        }
    }

    private OMElement getEchoElement() {
        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace omNs = fac.createOMNamespace(
                "http://example1.org/example1", "example1");
        OMElement method = fac.createOMElement("echo", omNs);
        OMElement value = fac.createOMElement("Text", omNs);
        value.addChild(fac.createOMText(value, AXIS2_ECHO_STRING));
        method.addChild(value);

        return method;
    }

    public abstract Parameter getClientRahasConfiguration();
    
    public abstract OutflowConfiguration getClientOutflowConfiguration();

    public abstract InflowConfiguration getClientInflowConfiguration();
    
    public abstract String getServiceRepo();
    
}
