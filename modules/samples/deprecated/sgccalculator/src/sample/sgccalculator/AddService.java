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
package sample.sgccalculator;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.OperationContext;
import org.apache.axis2.context.ServiceGroupContext;
import org.apache.axis2.wsdl.WSDLConstants;

import java.util.Iterator;

public class AddService {

    MessageContext msgContext = null;

    public void setOperationContext(OperationContext opContext) throws AxisFault {
        this.msgContext = opContext.getMessageContext(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
    }

    public OMElement add(OMElement elem) {
        Iterator iter = elem.getChildElements();
        String param1Str = ((OMElement) iter.next()).getText();
        String param2Str = ((OMElement) iter.next()).getText();

        int param1 = Integer.parseInt(param1Str);
        int param2 = Integer.parseInt(param2Str);
        int result = param1 + param2;

        if (msgContext != null)
            System.out.println("ServiceGroupContextID:" + msgContext.getServiceGroupContextId());
        else
            System.out.println("Message Context is null");

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = fac.createOMNamespace("http://axis2/test/namespace1", "ns1");

        OMElement resultElem = fac.createOMElement("result", namespace);
        resultElem.setText(Integer.toString(result));

        msgContext.getServiceGroupContext().setProperty(Constants.CALCULATOR_PREVIOUS_KEY, Integer.toString(result));
        return resultElem;
    }

    public OMElement addPrevious(OMElement elem) {
        Iterator iter = elem.getChildElements();
        String paramStr = ((OMElement) iter.next()).getText();

        int param = Integer.parseInt(paramStr);

        if (msgContext == null) {
            System.out.println("message context is null");
            return null;
        }

        ServiceGroupContext sgc = msgContext.getServiceGroupContext();
        if (sgc == null) {
            System.out.println("message context is null");
            return null;
        }

        String previousStr = (String) sgc.getProperty(Constants.CALCULATOR_PREVIOUS_KEY);
        if (previousStr == null) {
            System.out.println("Previous is null");
            return null;
        }

        int previous = Integer.parseInt(previousStr);

        int result = previous + param;

        if (msgContext != null)
            System.out.println("ServiceGroupContextID:" + msgContext.getServiceGroupContextId());
        else
            System.out.println("Message Context is null");

        OMFactory fac = OMAbstractFactory.getOMFactory();
        OMNamespace namespace = fac.createOMNamespace("http://axis2/test/namespace1", "ns1");

        OMElement resultElem = fac.createOMElement("result", namespace);
        resultElem.setText(Integer.toString(result));

        msgContext.getServiceGroupContext().setProperty(Constants.CALCULATOR_PREVIOUS_KEY, Integer.toString(result));
        return resultElem;
    }
}
