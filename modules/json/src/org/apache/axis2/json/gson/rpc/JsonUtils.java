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

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public class JsonUtils {

    private static final Log log = LogFactory.getLog(JsonUtils.class);

    public static Object invokeServiceClass(JsonReader jsonReader,
                                            Object service,
                                            Method operation ,
                                            Class[] paramClasses ,
                                            int paramCount ) throws InvocationTargetException,
            IllegalAccessException, IOException  {

        Object[] methodParam = new Object[paramCount];
	try {
            Gson gson = new Gson();
            String[] argNames = new String[paramCount];
    
            if( ! jsonReader.isLenient()){
                jsonReader.setLenient(true);
            }
            jsonReader.beginObject();
            String messageName=jsonReader.nextName();     // get message name from input json stream
            if (messageName == null || !messageName.equals(operation.getName())) {
                log.error("JsonUtils.invokeServiceClass() throwing IOException, messageName: " +messageName+ " is unknown, it does not match the axis2 operation, the method name: " + operation.getName());
                throw new IOException("Bad Request");
            }
            jsonReader.beginArray();
    
            int i = 0;
            for (Class paramType : paramClasses) {
                jsonReader.beginObject();
                argNames[i] = jsonReader.nextName();
                log.debug("JsonUtils.invokeServiceClass() on messageName: " +messageName+ " , is currently processing argName: " + argNames[i]);
                methodParam[i] = gson.fromJson(jsonReader, paramType);   // gson handle all types well and return an object from it
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
