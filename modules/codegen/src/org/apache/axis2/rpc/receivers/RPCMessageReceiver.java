package org.apache.axis2.rpc.receivers;

import org.apache.axis2.context.MessageContext;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.BeanSerializerUtil;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.OMNamespace;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.description.OperationDescription;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.lang.reflect.Method;
import java.util.Iterator;
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

/**
 * Author: Deepal Jayasinghe
 * Date: Oct 11, 2005
 * Time: 3:43:38 PM
 */
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

            OperationDescription op = inMessage.getOperationContext()
                    .getAxisOperation();
            if (op == null) {
                throw new AxisFault(
                        "Operation is not located, if this is doclit style the SOAP-ACTION should " +
                                "specified via the SOAP Action to use the RawXMLProvider");
            }
            String methodName = op.getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();
            //todo method validation has to be done
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    this.method = methods[i];
                    break;
                }
            }


            Class[] parameters = method.getParameterTypes();
            int paracount = 0;
            int numberofparas = parameters.length;

            Object [] objarray = new Object[numberofparas];
            OMElement methodElement = inMessage.getEnvelope().getBody()
                    .getFirstElement();
            Iterator parts = methodElement.getChildren();
            /**
             * Take the number of paramters in the method and , only take that much of child elements
             * from the OMElement , other are ignore , as an example
             * if the method is , foo(String a , int b)
             * and if the OMElemet
             * <foo>
             *  <arg0>Val1</arg0>
             *  <arg1>Val2</arg1>
             *  <arg2>Val3</arg2>
             *
             * only the val1 and Val2 take into account
             */
            while (parts.hasNext() && paracount < numberofparas) {
                OMElement omElement = (OMElement) parts.next();
                Class parameter = parameters[paracount];
                Object simpleObj = SimpleTypeMapper.getSimpleTypeObject(parameter, omElement);
                if (simpleObj != null) {
                    objarray[paracount] = simpleObj;
                } else {
                    //Handle only the JavaBean
                    simpleObj = new BeanSerializer(parameter, omElement).deserilze();
                    objarray[paracount] = simpleObj;
                }
                paracount ++;
            }
            Object resObject = method.invoke(obj, objarray);

            // Handling the response
            //todo NameSpace has to be taken from the serviceDescription
            OMNamespace ns = getSOAPFactory().createOMNamespace(
                    "http://soapenc/", "res");
            SOAPEnvelope envelope = getSOAPFactory().getDefaultEnvelope();
            OMElement bodyContent = getSOAPFactory().createOMElement(
                    method.getName() + "Response", ns);
            if (resObject != null) {
                //simple type
                if (SimpleTypeMapper.isSimpleType(resObject)) {
                    OMElement child = getSOAPFactory().createOMElement(RETURN_WRAPPER, null);
                    child.addChild(fac.createText(child, resObject.toString()));
                    bodyContent.addChild(child);
                } else {
                    // Java Beans
                    XMLStreamReader xr = BeanSerializerUtil.getPullParser(resObject,
                            new QName(RETURN_WRAPPER));
                    StAXOMBuilder stAXOMBuilder =
                            OMXMLBuilderFactory.createStAXOMBuilder(
                                    OMAbstractFactory.getSOAP11Factory(), xr);
                    OMElement documentElement = stAXOMBuilder.getDocumentElement();

                    if (documentElement != null) {
                        bodyContent.addChild(documentElement);
                    }
                }
            }
            if (bodyContent != null) {
                envelope.getBody().addChild(bodyContent);
            }
            outMessage.setEnvelope(envelope);

        } catch (Exception e) {
            e.printStackTrace();
            throw AxisFault.makeFault(e);
        }
    }


}
