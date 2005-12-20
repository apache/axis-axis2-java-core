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

/*
* Reflection based RPCMessageReceiver , request will be processed by looking at the method signature
* of the invocation method
*/

package org.apache.axis2.rpc.receivers;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.utils.BeanSerializerUtil;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis2.soap.SOAPEnvelope;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Method;

public class RPCMessageReceiver extends AbstractInOutSyncMessageReceiver {


    private Method method;
    private String RETURN_WRAPPER = "return";

    /**
     * reflect and get the Java method
     * - for each i'th param in the java method
     * - get the first child's i'th child
     * -if the elem has an xsi:type attr then find the deserializer for it
     * - if not found,
     * lookup deser for th i'th param (java type)
     * - error if not found
     * - deserialize & save in an object array
     * - end for
     * <p/>
     * - invoke method and get the return value
     * <p/>
     * - look up serializer for return value based on the value and type
     * <p/>
     * - create response msg and add return value as grand child of <soap:body>
     *
     * @param inMessage
     * @param outMessage
     * @throws AxisFault
     */

    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {
        try {
            // get the implementation class for the Web Service
            //todo namespace   , checking
            Object obj = getTheImplementationObject(inMessage);

            Class ImplClass = obj.getClass();
            DependencyManager.configureBusinessLogicProvider(obj, inMessage, null);

            AxisOperation op = inMessage.getOperationContext().getAxisOperation();

            OMElement methodElement = inMessage.getEnvelope().getBody()
                    .getFirstElement();
            String methodName = op.getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();
            //todo method validation has to be done
            //Todo if we find the method it should be store , in AxisOperation
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    this.method = methods[i];
                    break;
                }
            }


            Object[] objectArray = processRequest(methodElement);
            Object resObject = method.invoke(obj, objectArray);

            // Handling the response
            //todo NameSpace has to be taken from the AxisService
            OMNamespace ns = getSOAPFactory().createOMNamespace(
                    "http://soapenc/", "res");
            SOAPEnvelope envelope = getSOAPFactory().getDefaultEnvelope();
            OMElement bodyContent = null;

            if(resObject instanceof Object[]){
                QName resName = new QName("http://soapenc/",  method.getName() + "Response", "res");
                OMElement bodyChild = getResponseElement(resName,(Object[])resObject);
                envelope.getBody().addChild(bodyChild);
            }   else {
                processResponse(resObject, bodyContent, ns, envelope);
            }

            outMessage.setEnvelope(envelope);

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    private Object[] processRequest(OMElement methodElement) throws AxisFault {
        Class[] parameters = method.getParameterTypes();
        return   BeanSerializerUtil.deserialize(methodElement,parameters);
    }

    private OMElement getResponseElement(QName resname, Object [] objs){
        return BeanSerializerUtil.getOMElement(resname,objs);
    }

    private void processResponse(Object resObject, OMElement bodyContent, OMNamespace ns, SOAPEnvelope envelope) {
        if (resObject != null) {
            //todo first check to see where the desrilizer for the return object
            //simple type
            if(resObject instanceof OMElement){
                bodyContent=(OMElement)resObject;
            } else if (SimpleTypeMapper.isSimpleType(resObject)) {
                bodyContent = getSOAPFactory().createOMElement(
                        method.getName() + "Response", ns);
                OMElement child = getSOAPFactory().createOMElement(RETURN_WRAPPER, null);
                child.addChild(fac.createText(child, SimpleTypeMapper.getStringValue(resObject)));
                bodyContent.addChild(child);
            } else {
                bodyContent = getSOAPFactory().createOMElement(
                        method.getName() + "Response", ns);
                // Java Beans
                XMLStreamReader xr = BeanSerializerUtil.getPullParser(resObject,
                        new QName(RETURN_WRAPPER));
                StAXOMBuilder stAXOMBuilder =
                        OMXMLBuilderFactory.createStAXOMBuilder(
                                OMAbstractFactory.getOMFactory(), xr);
                OMElement documentElement = stAXOMBuilder.getDocumentElement();
                if (documentElement != null) {
                    bodyContent.addChild(documentElement);
                }
            }
        }
        if (bodyContent != null) {
            envelope.getBody().addChild(bodyContent);
        }
    }


}
