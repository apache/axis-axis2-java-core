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

package sample.groovy;

import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.MessageReceiver;
import org.apache.axis2.i18n.Messages;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

public class GroovyReceiver
        extends AbstractInOutSyncMessageReceiver
        implements MessageReceiver {

    public void invokeBusinessLogic(
            MessageContext inMessage,
            MessageContext outMessage)
            throws AxisFault {
        try {
            AxisService service =
                    inMessage
                            .getOperationContext()
                            .getServiceContext()
                            .getAxisService();
            Parameter implInfoParam = service.getParameter("GroovyClass");
            if (implInfoParam == null) {
                throw new AxisFault(
                        Messages.getMessage("paramIsNotSpecified", "ServiceClass"));
            }
            InputStream groovyFileStream =
                    service.getClassLoader().getResourceAsStream(
                            implInfoParam.getValue().toString());

            if (groovyFileStream == null) {
                throw new AxisFault(
                        Messages.getMessage("groovyUnableToLoad", implInfoParam.getValue().toString()));
            }

            //look at the method name. if available this should be a groovy method
            AxisOperation op =
                    inMessage.getOperationContext().getAxisOperation();
            if (op == null) {
                throw new AxisFault(
                        Messages.getMessage("notFound", "Operation"));
            }
            String methodName = op.getName().getLocalPart();
            OMElement firstChild =
                    (OMElement) inMessage.getEnvelope().getBody().getFirstOMChild();
            inMessage.getEnvelope().build();
            StringWriter writer = new StringWriter();
            firstChild.build();
            firstChild.serialize(writer);
            writer.flush();
            String value = writer.toString();
            if (value != null) {
                GroovyClassLoader loader = new GroovyClassLoader();
                Class groovyClass = loader.parseClass(groovyFileStream);
                GroovyObject groovyObject =
                        (GroovyObject) groovyClass.newInstance();
                Object[] arg = {new StringReader(value)};
                Object obj = groovyObject.invokeMethod(methodName, arg);
                if (obj == null) {
                    throw new AxisFault(Messages.getMessage("groovyNoanswer"));
                }

                SOAPFactory fac ;
                if (inMessage.isSOAP11()) {
                    fac = OMAbstractFactory.getSOAP11Factory();
                } else {
                    fac = OMAbstractFactory.getSOAP12Factory();
                }
                SOAPEnvelope envelope = fac.getDefaultEnvelope();
                OMNamespace ns =
                        fac.createOMNamespace("http://soapenc/", "res");
                OMElement responseElement =
                        fac.createOMElement(methodName + "Response", ns);
                String outMessageString = obj.toString();
                // System.out.println("outMessageString = " + outMessageString);
                // responseElement.setText(outMessageString);
                responseElement.addChild(getpayLoad(outMessageString));
                envelope.getBody().addChild(responseElement);
                outMessage.setEnvelope(envelope);
            }
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    private OMElement getpayLoad(String str) throws XMLStreamException {
        XMLStreamReader xmlReader =
                StAXUtils.createXMLStreamReader(
                        new ByteArrayInputStream(str.getBytes()));
        OMFactory fac = OMAbstractFactory.getOMFactory();

        StAXOMBuilder staxOMBuilder =
                new StAXOMBuilder(fac, xmlReader);
        return staxOMBuilder.getDocumentElement();
    }

}
