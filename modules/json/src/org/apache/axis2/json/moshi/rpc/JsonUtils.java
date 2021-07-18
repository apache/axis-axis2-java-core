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

import org.apache.commons.logging.LogFactory;
import org.apache.commons.logging.Log;

import com.squareup.moshi.JsonAdapter;
import com.squareup.moshi.JsonReader;
import com.squareup.moshi.JsonWriter;
import com.squareup.moshi.Moshi;
import com.squareup.moshi.Types;
import com.squareup.moshi.adapters.Rfc3339DateJsonAdapter;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import javax.annotation.Nullable;


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
            // define custom Moshi adapter so Json numbers become Java Long and Double
            JsonAdapter.Factory objectFactory =
                new JsonAdapter.Factory() {
                  @Override
                  public @Nullable JsonAdapter<?> create(
                      Type type, Set<? extends Annotation> annotations, Moshi moshi) {
                    if (type != Object.class) return null;
        
                    final JsonAdapter<Object> delegate = moshi.nextAdapter(this, Object.class, annotations);
                    return new JsonAdapter<Object>() {
                      @Override
                      public @Nullable Object fromJson(JsonReader reader) throws IOException {
                          if (reader.peek() != JsonReader.Token.NUMBER) {
                            return delegate.fromJson(reader);
                          } else {
                              String n = reader.nextString();
                              if (n.indexOf('.') != -1) {
                                  return Double.parseDouble(n);
                              }
              
                              try{
                                  Long longValue = Long.parseLong(n);
                                  return longValue;
                              }catch(Exception e){
                              }
              
                              //if exception parsing long, try double again
                              return Double.parseDouble(n);
        
                          }
                      }
        
                      @Override
                      public void toJson(JsonWriter writer, @Nullable Object value) {
                          try{
                              delegate.toJson(writer, value);
                          }catch(Exception ex){
                             log.error(ex.getMessage(), ex);

                          }
                      }
                    };
                  }
                };
			
            Moshi moshiFrom = new Moshi.Builder().add(objectFactory).add(Date.class, new Rfc3339DateJsonAdapter()).build();
            String[] argNames = new String[paramCount];
    
            jsonReader.beginObject();
            String messageName=jsonReader.nextName();     // get message name from input json stream
            if (messageName == null || !messageName.equals(operation.getName())) {
                log.error("JsonUtils.invokeServiceClass() throwing IOException, messageName: " +messageName+ " is unknown, it does not match the axis2 operation, the method name: " + operation.getName());
                throw new IOException("Bad Request");
            }
            jsonReader.beginArray();
    
            int i = 0;
            for (Class paramType : paramClasses) {
            	JsonAdapter<Map> moshiFromJsonAdapter = null;
            	moshiFromJsonAdapter = moshiFrom.adapter(paramType);
                jsonReader.beginObject();
                argNames[i] = jsonReader.nextName();
                methodParam[i] = moshiFromJsonAdapter.fromJson(jsonReader);   // moshi handles all types well and returns an object from it
                log.trace("JsonUtils.invokeServiceClass() completed processing on messageName: " +messageName+ " , arg name: " +argNames[i]+ " , methodParam: " +methodParam[i].getClass().getName()+ " , from argNames.length: " + argNames.length);
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
                return method;
            }
        }
        log.error("JsonUtils.getOpMethod() returning null, cannot find methodName: " +methodName+ " , from methodSet.length: " + methodSet.length);
        return null;
    }

}
