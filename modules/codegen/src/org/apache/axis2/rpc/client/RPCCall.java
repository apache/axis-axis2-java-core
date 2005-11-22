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

package org.apache.axis2.rpc.client;

import org.apache.axis2.AxisFault;
import org.apache.axis2.client.Call;
import org.apache.axis2.client.Callback;
import org.apache.axis2.client.InOutMEPClient;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.om.OMElement;
import org.apache.axis2.soap.SOAPEnvelope;
import org.apache.axis2.util.BeanSerializerUtil;
import org.apache.wsdl.WSDLConstants;

import javax.xml.namespace.QName;

public class RPCCall extends Call {


    protected static AxisOperation axisOperationTemplate;

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
     * side too. Until it is finalized lets return OMElement as return value. And the retuen
     * value will be the body first element user has to deal with that and create
     * his own object out of that.
     *
     * @param opName Operation QName (to get the body wrapper element)
     * @param args   Arraylist of objects
     * @return Response OMElement
     */
    public OMElement invokeBlocking(QName opName, Object [] args) throws AxisFault {
        AxisOperation opDesc =
                serviceContext.getAxisService().getOperation(opName);
        opDesc = createOpDescAndFillInFlowInformation(opDesc, opName.getLocalPart(),
                WSDLConstants.MEP_CONSTANT_IN_OUT);
        opDesc.setParent(serviceContext.getAxisService());
        MessageContext msgctx = prepareTheSOAPEnvelope(BeanSerializerUtil.getOMElement(opName, args));

        this.lastResponseMsgCtx = super.invokeBlocking(opDesc, msgctx);
        SOAPEnvelope resEnvelope = lastResponseMsgCtx.getEnvelope();
        return resEnvelope.getBody().getFirstElement();
    }

    /**
     * @param opName      Operation QName (to get the body wrapper element)
     * @param args        Arraylist of objects
     * @param returnTypes , this array contains the JavaTypes for the return object , it could be one
     *                    or more depending on the return type , most of the type array will contain just one element
     *                    It should be noted that the array should only contains JavaTypes NOT real object , what this
     *                    methods does is , get the body first element , and if it contains more than one childern take
     *                    ith element and convert that to ith javatype and fill the return arrya
     *                    the array will look like as follows
     *                    [Integer, String, MyBean , etc]
     * @return Object array , whic will contains real object , but the object can either be simple type
     *         object or the JavaBeans, thats what this method can handle right now
     *         the return array will contains [10, "Axis2Echo", {"foo","baa","11"}]
     * @throws AxisFault
     */

    public Object[]  invokeBlocking(QName opName, Object [] args, Object [] returnTypes) throws AxisFault {
        AxisOperation opDesc =
                serviceContext.getAxisService().getOperation(opName);
        opDesc = createOpDescAndFillInFlowInformation(opDesc, opName.getLocalPart(),
                WSDLConstants.MEP_CONSTANT_IN_OUT);
        opDesc.setParent(serviceContext.getAxisService());
        MessageContext msgctx = prepareTheSOAPEnvelope(BeanSerializerUtil.getOMElement(opName, args));
        this.lastResponseMsgCtx = super.invokeBlocking(opDesc, msgctx);
        SOAPEnvelope resEnvelope = lastResponseMsgCtx.getEnvelope();
        return BeanSerializerUtil.deserialize(resEnvelope.getBody().getFirstElement(), returnTypes);
    }

    /**
     * Invoke the nonblocking/Asynchronous call
     *
     * @param opName
     * @param args     -  This should be OM Element (payload)
     *                 invocation behaves accordingly
     * @param callback
     * @throws org.apache.axis2.AxisFault
     */

    public void invokeNonBlocking(
            QName opName,
            Object [] args,
            Callback callback)
            throws AxisFault {
        AxisOperation opDesc =
                serviceContext.getAxisService().getOperation(opName);
        opDesc = createOpDescAndFillInFlowInformation(opDesc, opName.getLocalPart(), WSDLConstants.MEP_CONSTANT_IN_OUT);
        MessageContext msgctx = prepareTheSOAPEnvelope(BeanSerializerUtil.getOMElement(opName, args));
        //call the underline implementation
        super.invokeNonBlocking(opDesc, msgctx, callback);
    }

    /**
     * This method is used to get OMElement out from an Object array , the object array can contain
     * either RPCRequestParamter or Object (if it is object that should be either JavaBean or a Simple
     * TypeObject , other types of object can not handle yet)
     * @param opName
     * @param args
     */


}
