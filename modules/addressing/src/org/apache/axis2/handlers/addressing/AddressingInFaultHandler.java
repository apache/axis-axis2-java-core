/*
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

package org.apache.axis2.handlers.addressing;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAP11Constants;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axiom.soap.SOAPFault;
import org.apache.axiom.soap.SOAPFaultReason;
import org.apache.axiom.soap.SOAPFaultText;
import org.apache.axiom.soap.SOAPHeader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.addressing.AddressingConstants;
import org.apache.axis2.addressing.AddressingFaultsHelper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.handlers.AbstractHandler;

import javax.xml.namespace.QName;

/**
 * This class is used to extract WS-Addressing Spec defined Faults and FaultDetail and convert them
 * into understandable AxisFault objects.
 */
public class AddressingInFaultHandler extends AbstractHandler implements AddressingConstants {

    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        String action = msgContext.getWSAAction();

        if (Final.WSA_FAULT_ACTION.equals(action)
                || Final.WSA_SOAP_FAULT_ACTION.equals(action)
                || Submission.WSA_FAULT_ACTION.equals(action)) {
            String faultLocalName = getFaultLocalName(msgContext);
            String faultDetailString = getWSAFaultDetailString(msgContext);

            if (faultLocalName != null) {
                String newReason = AddressingFaultsHelper
                        .getMessageForAxisFault(faultLocalName, faultDetailString);

                if (newReason != null) {
                    SOAPEnvelope envelope = msgContext.getEnvelope();
                    SOAPFault fault = envelope.getBody().getFault();

                    SOAPFactory sf = ((SOAPFactory) envelope.getOMFactory());
                    SOAPFaultReason sfr = sf.createSOAPFaultReason();
                    if (envelope.getNamespace().getNamespaceURI()
                            .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                        sfr.setText(newReason);
                    } else {
                        SOAPFaultText sft = sf.createSOAPFaultText();
                        sft.setText(newReason);
                        sfr.addSOAPText(sft);
                    }
                    if (fault != null) {
                        // else call the on error method with the fault
                        AxisFault axisFault = new AxisFault(fault.getCode(), sfr,
                                                            fault.getNode(), fault.getRole(),
                                                            fault.getDetail());
                        msgContext.setProperty(Constants.INBOUND_FAULT_OVERRIDE, axisFault);
                    }
                }
            }
        }

        return InvocationResponse.CONTINUE;
    }

    private String getFaultLocalName(MessageContext msgContext) {
        SOAPEnvelope envelope = msgContext.getEnvelope();
        SOAPFault fault = envelope.getBody().getFault();
        QName faultCodeQName = null;
        String result = null;
        if (fault != null) {
            if (envelope.getNamespace().getNamespaceURI()
                    .equals(SOAP11Constants.SOAP_ENVELOPE_NAMESPACE_URI)) {
                faultCodeQName = fault.getCode().getTextAsQName();
            } else {
                faultCodeQName = fault.getCode().getValue().getTextAsQName();
            }
            if (fault.getCode().getSubCode() != null) {
                faultCodeQName = fault.getCode().getSubCode().getValue().getTextAsQName();
                if (fault.getCode().getSubCode().getSubCode() != null) {
                    faultCodeQName =
                            fault.getCode().getSubCode().getSubCode().getValue().getTextAsQName();
                }
            }
        }
        if (faultCodeQName != null) {
            result = faultCodeQName.getLocalPart();
        }
        return result;
    }

    private String getWSAFaultDetailString(MessageContext msgContext) {
        SOAPEnvelope envelope = msgContext.getEnvelope();
        OMElement faultDetailElement = null;
        String result = null;

        if (msgContext.isSOAP11()) {
            SOAPHeader header = envelope.getHeader();
            faultDetailElement = header.getFirstChildWithName(new QName(Final.FAULT_HEADER_DETAIL));
            if (faultDetailElement != null) {
                result = faultDetailElement.getFirstElement().getText();
            }
        } else {
            SOAPFault fault = envelope.getBody().getFault();
            if (fault != null) {
                if (fault.getDetail() != null) {
                    faultDetailElement = fault.getDetail()
                            .getFirstChildWithName(new QName(Final.FAULT_PROBLEM_ACTION_NAME));
                    if (faultDetailElement == null) {
                        faultDetailElement = fault.getDetail().getFirstChildWithName(
                                new QName(Final.FAULT_HEADER_PROB_HEADER_QNAME));
                    }
                }
            }
            if (faultDetailElement != null) {
                result = faultDetailElement.getText();
            }
        }
        return result;
    }
}
