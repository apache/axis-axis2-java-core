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

package test.interop.whitemesa.round2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.OperationClient;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ConfigurationContextFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.wsdl.WSDLConstants;
import test.interop.whitemesa.round2.util.SunRound2ClientUtil;

import java.net.URL;

public class SunRound2Client {

    public static SOAPEnvelope sendMsg(SunRound2ClientUtil util, String epUrl, String soapAction) throws AxisFault {

        SOAPEnvelope resEnv;
        URL url;
        try {
            url = new URL(epUrl);

            Options options = new Options();
            options.setTo(new EndpointReference(url.toString()));
            options.setTransportInProtocol(Constants.TRANSPORT_HTTP);
            options.setSoapAction(soapAction);

            AxisConfiguration axisConfig = new AxisConfiguration();
            ConfigurationContext configCtx = new ConfigurationContext(axisConfig);
            MessageContext msgCtx = new MessageContext();
            msgCtx.setConfigurationContext(configCtx);
            SOAPEnvelope requestEnvilope = util.getEchoSoapEnvelope();
            msgCtx.setEnvelope(requestEnvilope);

            ConfigurationContext configContext =
                    ConfigurationContextFactory.createConfigurationContextFromFileSystem(
                            "target/test-resources/integrationRepo",null);
            ServiceClient serviceClient = new ServiceClient(configContext, null);
            serviceClient.setOptions(options);
            OperationClient opClient = serviceClient.createClient(ServiceClient.ANON_OUT_IN_OP);
            opClient.addMessageContext(msgCtx);
            opClient.setOptions(options);
            MessageContext responseMCtx = opClient.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);

            resEnv = responseMCtx.getEnvelope();

        } catch (Exception e) {
            throw new AxisFault(e);
        }
        return resEnv;
    }
}
