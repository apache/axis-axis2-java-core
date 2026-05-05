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
package org.apache.axis2.json.moshi.rpc;

import com.squareup.moshi.JsonReader;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.json.rpc.Axis2JsonErrorResponse;
import org.apache.axis2.json.rpc.JsonRpcFaultException;
import org.apache.axis2.json.moshi.MoshiXMLStreamReader;
import org.apache.axis2.json.factory.JsonConstant;
import org.apache.axis2.rpc.receivers.RPCMessageReceiver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class JsonRpcMessageReceiver extends RPCMessageReceiver {
    private static final Log log = LogFactory.getLog(RPCMessageReceiver.class);

    @Override
    public void invokeBusinessLogic(MessageContext inMessage, MessageContext outMessage) throws AxisFault {
        Object tempObj = inMessage.getProperty(JsonConstant.IS_JSON_STREAM);
        boolean isJsonStream;
        if (tempObj != null) {
            isJsonStream = Boolean.valueOf(tempObj.toString());
        } else {
            // if IS_JSON_STREAM property  is not set then it is not a JSON request
            isJsonStream = false;
        }
        if (isJsonStream) {
            Object o = inMessage.getProperty(JsonConstant.MOSHI_XML_STREAM_READER);
            if (o != null) {
                MoshiXMLStreamReader moshiXMLStreamReader = (MoshiXMLStreamReader)o;
                JsonReader jsonReader = moshiXMLStreamReader.getJsonReader();
                if (jsonReader == null) {
                    throw new AxisFault("JsonReader should not be null");
                }
                Object serviceObj = getTheImplementationObject(inMessage);
                AxisOperation op = inMessage.getOperationContext().getAxisOperation();
                String operation = op.getName().getLocalPart();
                String enableJSONOnly = (String)  inMessage.getAxisService().getParameterValue("enableJSONOnly");
                invokeService(jsonReader, serviceObj, operation , outMessage, enableJSONOnly);
            } else {
                throw new AxisFault("MoshiXMLStreamReader should be put as a property of messageContext " +
                        "to evaluate JSON message");
            }
        } else {
            super.invokeBusinessLogic(inMessage, outMessage);   // call RPCMessageReceiver if inputstream is null
        }
    }

    public void invokeService(JsonReader jsonReader, Object serviceObj, String operation_name, MessageContext outMes, String enableJSONOnly) throws AxisFault {
        Class implClass = serviceObj.getClass();
        Method[] allMethods = implClass.getDeclaredMethods();
        Method method = JsonUtils.getOpMethod(operation_name, allMethods);
        Class[] paramClasses = method.getParameterTypes();
        try {
            int paramCount = paramClasses.length;
            Object retObj = JsonUtils.invokeServiceClass(jsonReader, serviceObj, method, paramClasses, paramCount, enableJSONOnly);

            // handle response
            outMes.setProperty(JsonConstant.RETURN_OBJECT, retObj);
            outMes.setProperty(JsonConstant.RETURN_TYPE, method.getReturnType());

        } catch (InvocationTargetException e) {
            // Method.invoke() wraps any exception thrown by the service method
            // in InvocationTargetException.  Unwrap to inspect the real cause.
            Throwable cause = e.getCause();

            if (cause instanceof JsonRpcFaultException) {
                // ── Structured error path ────────────────────────────────────
                // Service explicitly signaled a typed error (e.g. validation 422).
                // Set the HTTP status code and put the error response as RETURN_OBJECT
                // so the Moshi formatter serializes it as a normal JSON body — NOT
                // through the SOAP fault path.
                JsonRpcFaultException fault = (JsonRpcFaultException) cause;
                Axis2JsonErrorResponse errorResponse = fault.getErrorResponse();
                log.warn("[errorRef=" + errorResponse.getErrorRef() + "] " +
                        errorResponse.getError() + " in operation '" + operation_name +
                        "': " + errorResponse.getMessage());
                outMes.setProperty(Constants.HTTP_RESPONSE_STATE,
                        String.valueOf(fault.getHttpStatusCode()));
                outMes.setProperty(JsonConstant.RETURN_OBJECT, errorResponse);
                outMes.setProperty(JsonConstant.RETURN_TYPE, Axis2JsonErrorResponse.class);
            } else {
                // ── Opaque error path ────────────────────────────────────────
                // Unexpected exception — create a CWE-209-safe AxisFault.
                Exception rootCause;
                if (cause instanceof Exception) {
                    rootCause = (Exception) cause;
                } else if (cause != null) {
                    rootCause = new RuntimeException("Service threw non-Exception Throwable", cause);
                } else {
                    rootCause = e;
                }
                throw JsonUtils.createSecureFault(rootCause, operation_name, false, outMes);
            }
        } catch (IllegalAccessException e) {
            throw JsonUtils.createSecureFault(e, operation_name, false, outMes);
        } catch (IOException e) {
            throw JsonUtils.createSecureFault(e, operation_name, true, outMes);
        }
    }
}
