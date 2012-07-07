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

package org.apache.axis2.json.impl.rpc;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.json.impl.utils.JsonConstant;
import org.apache.axis2.json.impl.utils.JsonUtils;
import org.apache.axis2.rpc.receivers.RPCInOnlyMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class JsonInOnlyRPCMessageReceiver extends RPCInOnlyMessageReceiver {

    private static Log log = LogFactory.getLog(JsonInOnlyRPCMessageReceiver.class);
    @Override
    public void invokeBusinessLogic(MessageContext inMessage) throws AxisFault {
        InputStream inputStream = (InputStream)inMessage.getProperty(JsonConstant.INPUT_STREAM);
        if (inputStream != null) {
            Method method = null;
            String msg;

            Object serviceObj = getTheImplementationObject(inMessage);
            Class implClass = serviceObj.getClass();
            Method[] allmethods =  implClass.getDeclaredMethods();
            AxisOperation op = inMessage.getOperationContext().getAxisOperation();
            String operation = op.getName().getLocalPart();
            method = JsonUtils.getOpMethod(operation, allmethods);
            Class [] paramClasses = method.getParameterTypes();
            String charSetEncoding = (String) inMessage.getProperty(Constants.Configuration.CHARACTER_SET_ENCODING);
            try {
                int paramCount=paramClasses.length;

                 JsonUtils.invokeServiceClass(inputStream,
                        serviceObj, method, paramClasses, paramCount, charSetEncoding);

            } catch (IllegalAccessException e) {
                msg = "Does not have access to " +
                        "the definition of the specified class, field, method or constructor";
                log.error(msg, e);
                throw AxisFault.makeFault(e);

            } catch (InvocationTargetException e) {
                msg = "Exception occurred while trying to invoke service method " +
                        (method != null ? method.getName() : "null");
                log.error(msg, e);
                throw AxisFault.makeFault(e);
            } catch (IOException e) {
                msg = "Exception occur while encording or " +
                        "access to the input string at the JsonRpcMessageReceiver";
                log.error(msg, e);
                throw AxisFault.makeFault(e);
            }
        } else{
            super.invokeBusinessLogic(inMessage);   // call RPCMessageReceiver if inputstream is null
        }
    }
}
