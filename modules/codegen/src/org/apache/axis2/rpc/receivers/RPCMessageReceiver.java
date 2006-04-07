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

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.impl.builder.StAXOMBuilder;
import org.apache.axiom.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.util.StreamWrapper;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.databinding.typemapping.SimpleTypeMapper;
import org.apache.axis2.databinding.utils.BeanUtil;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.DependencyManager;
import org.apache.axis2.receivers.AbstractInOutSyncMessageReceiver;

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
            Object obj = getTheImplementationObject(inMessage);

            Class ImplClass = obj.getClass();
            DependencyManager.configureBusinessLogicProvider(obj,
                    inMessage.getOperationContext());

            AxisOperation op = inMessage.getOperationContext().getAxisOperation();
            AxisService service = inMessage.getAxisService();
            OMElement methodElement = inMessage.getEnvelope().getBody()
                    .getFirstElement();

            OMNamespace namespace = methodElement.getNamespace();
            if (namespace == null || !service.getSchematargetNamespace().equals(namespace.getName())) {
                throw new AxisFault("namespace mismatch require " +
                        service.getSchematargetNamespace() +
                        " found " + methodElement.getNamespace().getName());
            }
            String methodName = op.getName().getLocalPart();
            Method[] methods = ImplClass.getMethods();
            for (int i = 0; i < methods.length; i++) {
                if (methods[i].getName().equals(methodName)) {
                    this.method = methods[i];
                    break;
                }
            }


            Object[] objectArray = processRequest(methodElement);
            Object resObject;
            try {
                resObject = method.invoke(obj, objectArray);
            } catch (Exception e) {
                throw new AxisFault(e.getMessage());
            }
            SOAPFactory fac = getSOAPFactory(inMessage);

            // Handling the response
            OMNamespace ns = fac.createOMNamespace(service.getSchematargetNamespace(),
                    service.getSchematargetNamespacePrefix());
            SOAPEnvelope envelope = fac.getDefaultEnvelope();
            OMElement bodyContent = null;

            if (resObject instanceof Object[]) {
                QName resName = new QName(service.getSchematargetNamespace(),
                        method.getName() + "Response",
                        service.getSchematargetNamespacePrefix());
                OMElement bodyChild = getResponseElement(resName, (Object[]) resObject);
                envelope.getBody().addChild(bodyChild);
            } else {
                processResponse(fac, resObject, bodyContent, ns, envelope);
            }

            outMessage.setEnvelope(envelope);

        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }

    private Object[] processRequest(OMElement methodElement) throws AxisFault {
        Class[] parameters = method.getParameterTypes();
        return BeanUtil.deserialize(methodElement, parameters);
    }

    private OMElement getResponseElement(QName resname, Object [] objs) {
        return BeanUtil.getOMElement(resname, objs, RETURN_WRAPPER);
    }

    private void processResponse(SOAPFactory fac, Object resObject,
                                 OMElement bodyContent,
                                 OMNamespace ns,
                                 SOAPEnvelope envelope) {
        if (resObject != null) {
            //simple type
            if (resObject instanceof OMElement) {
                bodyContent = (OMElement) resObject;
            } else if (SimpleTypeMapper.isSimpleType(resObject)) {
                bodyContent = fac.createOMElement(
                        method.getName() + "Response", ns);
                OMElement child = fac.createOMElement(RETURN_WRAPPER, null);
                child.addChild(fac.createOMText(child, SimpleTypeMapper.getStringValue(resObject)));
                bodyContent.addChild(child);
            } else {
                bodyContent = fac.createOMElement(
                        method.getName() + "Response", ns);
                // Java Beans
                XMLStreamReader xr = BeanUtil.getPullParser(resObject,
                        new QName(RETURN_WRAPPER));
                StAXOMBuilder stAXOMBuilder =
                        OMXMLBuilderFactory.createStAXOMBuilder(
                                OMAbstractFactory.getOMFactory(), new StreamWrapper(xr));
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
