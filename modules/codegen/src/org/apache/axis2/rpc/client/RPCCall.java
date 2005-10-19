package org.apache.axis2.rpc.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.clientapi.Call;
import org.apache.axis2.clientapi.Callback;
import org.apache.axis2.clientapi.InOutMEPClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.databinding.utils.ADBPullParser;
import org.apache.axis2.description.OperationDescription;
import org.apache.axis2.om.OMAbstractFactory;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.om.impl.llom.builder.StAXOMBuilder;
import org.apache.axis2.om.impl.llom.factory.OMXMLBuilderFactory;
import org.apache.axis2.rpc.receivers.SimpleTypeMapper;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLStreamReader;
import java.util.ArrayList;
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
 * Date: Oct 13, 2005
 * Time: 10:37:52 AM
 */
public class RPCCall extends Call {


    protected static OperationDescription operationTemplate;

    /**
     * @throws org.apache.axis2.AxisFault
     */

    public RPCCall() throws AxisFault {
        super(assumeServiceContext(null));
    }

    /**
     * This is used to create call object with client home , using only this constructor it can
     * able to engage modules  , addning client side parameters
     *
     * @param clientHome
     * @throws org.apache.axis2.AxisFault
     */
    public RPCCall(String clientHome) throws AxisFault {
        super(assumeServiceContext(clientHome));
    }

    /**
     * @param service
     * @see InOutMEPClient constructer
     */
    public RPCCall(ServiceContext service) {
        super(service);
    }

    /**
     * Return value can be a single a object or an object array (itself an object) , but it is
     * difficulty to figure the return object correctly unless we have TyepMapping in the client
     *  side too. Until it is finalized lets return OMElement as return value. And the retuen
     * value will be the body first element user has to deal with that and create
     * his own object out of that.
     * @param opName  Operation QName (to get the body wrapper element)
     * @param args Arraylist of objects
     * @return  Response OMElement
     */
    public OMElement invokeBlocking(QName opName , Object [] args) throws AxisFault {
        OperationDescription opDesc =
                serviceContext.getServiceConfig().getOperation(opName);
        opDesc = createOpDescAndFillInFlowInformation(opDesc, opName.getLocalPart(),
                WSDLConstants.MEP_CONSTANT_IN_OUT);
        opDesc.setParent(serviceContext.getServiceConfig());
        MessageContext msgctx = prepareTheSOAPEnvelope(getOMElement(opName,args));

        this.lastResponseMessage = super.invokeBlocking(opDesc, msgctx);
        SOAPEnvelope resEnvelope = lastResponseMessage.getEnvelope();
        return resEnvelope.getBody().getFirstElement();
    }

    /**
     * Invoke the nonblocking/Asynchronous call
     *
     * @param opName
     * @param args   -  This should be OM Element (payload)
     *                 invocation behaves accordingly
     * @param callback
     * @throws org.apache.axis2.AxisFault
     */

    public void invokeNonBlocking(
            QName opName,
            Object [] args,
            Callback callback)
            throws AxisFault {
        OperationDescription opDesc =
                serviceContext.getServiceConfig().getOperation(opName);
        opDesc = createOpDescAndFillInFlowInformation(opDesc, opName.getLocalPart(), WSDLConstants.MEP_CONSTANT_IN_OUT);
        MessageContext msgctx = prepareTheSOAPEnvelope(getOMElement(opName,args));
        //call the underline implementation
        super.invokeNonBlocking(opDesc, msgctx, callback);
    }

    /**
     * This method is used to get OMElement out from an Object array , the object array can contain
     * either RPCRequestParamter or Object (if it is object that should be either JavaBean or a Simple
     * TypeObject , other types of object can not handle yet)
     * @param opName
     * @param args
     * @return
     * @throws AxisFault
     */
    private OMElement getOMElement(QName opName ,Object [] args) throws AxisFault {
        ArrayList objects ;
        try {
            objects = new ArrayList();
            int argCount =0;
            for (int i = 0; i < args.length; i++) {
                Object arg = args[i];
                if(arg instanceof RPCRequestParameter){
                    RPCRequestParameter para = (RPCRequestParameter)arg;
                    objects.add(para.getName());
                    objects.add(para.getValue());
                    if (para.isSimpleType()) {
                        objects.add(para.getName());
                        objects.add(para.getValue().toString());
                    } else {
                        objects.add(para.getName());
                        objects.add(para.getValue().toString());
                    }
                } else {
                    if(SimpleTypeMapper.isSimpleType(arg)){
                        objects.add("arg" + argCount);
                        objects.add(arg.toString());
                    }  else {
                        objects.add(new QName("arg" + argCount));
                        objects.add(arg);
                    }
                    argCount ++;
                }
            }
            XMLStreamReader xr = ADBPullParser.createPullParser(opName,objects.toArray(),null);
            StAXOMBuilder stAXOMBuilder =
                    OMXMLBuilderFactory.createStAXOMBuilder(
                            OMAbstractFactory.getOMFactory(), xr);
            return stAXOMBuilder.getDocumentElement();
        } catch (ClassCastException e) {
            throw new AxisFault("Object is not a RPCRequestParameter" + e, e);
        }

    }

}
