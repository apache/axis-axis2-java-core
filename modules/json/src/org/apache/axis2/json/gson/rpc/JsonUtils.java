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

package org.apache.axis2.json.gson.rpc;

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.json.factory.JsonConstant;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;


public class JsonUtils {

    private static final Log log = LogFactory.getLog(JsonUtils.class);

    public static Object invokeServiceClass(JsonReader jsonReader,
                                            Object service,
                                            Method operation ,
                                            Class[] paramClasses ,
                                            int paramCount,
		                            String enableJSONOnly ) throws InvocationTargetException, IllegalAccessException, IOException  {

        Object[] methodParam = new Object[paramCount];
	try {
            Gson gson = new Gson();
            String[] argNames = new String[paramCount];
    
            if( ! jsonReader.isLenient()){
                jsonReader.setLenient(true);
            }

            if (enableJSONOnly ==null || enableJSONOnly.equalsIgnoreCase("false")) {
                log.debug("JsonUtils.invokeServiceClass() detected enableJSONOnly=false, executing jsonReader.beginObject() and then jsonReader.beginArray() on method name: " + operation.getName());
                jsonReader.beginObject();
                String messageName=jsonReader.nextName();     // get message name from input json stream
                if (messageName == null || !messageName.equals(operation.getName())) {
                    log.error("JsonUtils.invokeServiceClass() throwing IOException, messageName: " +messageName+ " is unknown, it does not match the axis2 operation, the method name: " + operation.getName());
                    throw new IOException("Bad Request");
                }
            } else {
                log.debug("JsonUtils.invokeServiceClass() detected enableJSONOnly=true, executing jsonReader.beginArray()");
	    }	    

            jsonReader.beginArray();
    
            int i = 0;
            for (Class paramType : paramClasses) {
                jsonReader.beginObject();
                argNames[i] = jsonReader.nextName();
                methodParam[i] = gson.fromJson(jsonReader, paramType);   // gson handle all types well and return an object from it
                log.trace("JsonUtils.invokeServiceClass() completed processing on argNames: " +argNames[i]+ " , methodParam: " +methodParam[i].getClass().getName()+ " , from argNames.length: " + argNames.length);
                jsonReader.endObject();
                i++;
            }
    
            jsonReader.endArray();
            jsonReader.endObject();
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new IOException("Bad Request");
        }

        return  operation.invoke(service, methodParam);

    }

    /**
     * Build a secure {@link AxisFault} for any unexpected failure in the JSON-RPC
     * message receivers.  The full context is logged server-side under an opaque
     * correlation ID; only {@code "Bad Request [errorRef=<uuid>]"} or
     * {@code "Internal Server Error [errorRef=<uuid>]"} is returned to the caller.
     * This prevents information disclosure under penetration testing (CWE-209).
     *
     * @param e             the caught exception
     * @param operationName the Axis2 operation being dispatched (may be null)
     * @param isParsingError {@code true} for malformed-input IOExceptions,
     *                       {@code false} for internal reflection/invocation failures
     * @return an AxisFault safe to send to the client
     */
    static AxisFault createSecureFault(Exception e, String operationName, boolean isParsingError) {
        return createSecureFault(e, operationName, isParsingError, null);
    }

    /**
     * Build a secure {@link AxisFault} and, when an outgoing {@link MessageContext}
     * is available, also set the structured {@link Axis2JsonErrorResponse} as the
     * return object with the appropriate HTTP status code. This allows the JSON
     * formatter to serialize a clean error envelope instead of a SOAP fault XML fragment.
     *
     * @param e             the caught exception
     * @param operationName the Axis2 operation being dispatched (may be null)
     * @param isParsingError {@code true} for malformed-input IOExceptions,
     *                       {@code false} for internal reflection/invocation failures
     * @param outMessage    the outgoing message context (may be null for backward compat)
     * @return an AxisFault safe to send to the client
     */
    static AxisFault createSecureFault(Exception e, String operationName, boolean isParsingError,
                                        MessageContext outMessage) {
        String errorRef = UUID.randomUUID().toString();
        String opDisplay = operationName != null ? operationName : "<unknown>";
        Axis2JsonErrorResponse errorResponse;
        int httpStatus;
        if (isParsingError) {
            // Bad JSON from the client → 400.  Full stack trace logged server-side;
            // only the errorRef UUID reaches the client (CWE-209 safe).
            log.error("[errorRef=" + errorRef + "] Bad Request parsing JSON-RPC body " +
                    "for operation '" + opDisplay + "': " + e.getMessage(), e);
            errorResponse = Axis2JsonErrorResponse.badRequest(errorRef);
            httpStatus = 400;
        } else {
            // Unexpected internal failure (reflection, ClassCast, NPE, etc.) → 500.
            log.error("[errorRef=" + errorRef + "] Internal error invoking operation '" +
                    opDisplay + "': " + e.getMessage(), e);
            errorResponse = Axis2JsonErrorResponse.internalError(errorRef);
            httpStatus = 500;
        }
        if (outMessage != null) {
            // When we have the outgoing MessageContext, set the structured error
            // as RETURN_OBJECT so the JSON formatter serializes it as a clean
            // JSON envelope.  Also set HTTP_RESPONSE_STATE which AxisServlet /
            // AbstractHTTPTransportSender reads to set the actual HTTP status code.
            // Note: we still return an AxisFault below — the caller (invokeService)
            // throws it, but the transport will prefer the already-set RETURN_OBJECT
            // over the fault's XML representation for JSON content types.
            outMessage.setProperty(Constants.HTTP_RESPONSE_STATE, String.valueOf(httpStatus));
            outMessage.setProperty(JsonConstant.RETURN_OBJECT, errorResponse);
            outMessage.setProperty(JsonConstant.RETURN_TYPE, Axis2JsonErrorResponse.class);
        }
        return new AxisFault(errorResponse.getMessage());
    }

    public static Method getOpMethod(String methodName, Method[] methodSet) {
        for (Method method : methodSet) {
            String mName = method.getName();
            if (mName.equals(methodName)) {
                log.debug("JsonUtils.getOpMethod() returning methodName: " +methodName);
                return method;
            }
        }
        log.debug("JsonUtils.getOpMethod() returning null");
        return null;
    }

}
