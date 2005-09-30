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

package org.apache.axis2.interop.whitemesa.round2;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.engine.AxisConfigurationImpl;
import org.apache.axis2.interop.whitemesa.round2.util.SunRound2ClientUtil;
import org.apache.axis2.soap.SOAPEnvelope;

import javax.xml.namespace.QName;
import java.net.URL;

public class SunRound2Client {

    public static SOAPEnvelope sendMsg(SunRound2ClientUtil util, String epUrl, String soapAction) throws AxisFault {

        SOAPEnvelope resEnv = null;
        Call call = null;
        URL url = null;
        try {
            call = new Call("target/test-resources/intregrationRepo");
            url = new URL(epUrl);
            call.setTo(new EndpointReference(url.toString()));
            call.setTransportInfo(Constants.TRANSPORT_HTTP, Constants.TRANSPORT_HTTP, false);
            call.setSoapAction(soapAction);

            AxisConfiguration axisConfig = new AxisConfigurationImpl();
            ConfigurationContext configCtx = new ConfigurationContext(axisConfig);
            MessageContext msgCtx = new MessageContext(configCtx);
            OperationDescription opDesc = new OperationDescription(new QName(""));
            SOAPEnvelope requestEnvilope = util.getEchoSoapEnvelope();
            msgCtx.setEnvelope(requestEnvilope);
            MessageContext responseMCtx = call.invokeBlocking(opDesc, msgCtx);
            resEnv = responseMCtx.getEnvelope();

        } catch (Exception e) {
            throw new AxisFault(e);
        }
        return resEnv;
    }
}
