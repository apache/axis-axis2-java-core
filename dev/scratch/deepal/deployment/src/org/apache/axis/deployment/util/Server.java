package org.apache.axis.deployment.util;

/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * @author Deepal Jayasinghe
 *         Oct 18, 2004
 *         4:18:09 PM
 *
 */
public class Server {

    private String name;
    private Parameter [] parameters;
    private Handler [] handlers;

    private int parameterCount = 0 ;
    private int handlerCount = 0 ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void appParameter(Parameter parameter){
        parameters[parameterCount]= parameter;
        parameterCount ++;
    }

    public Parameter getParameter(int index){
        if(index <= parameterCount ){
            return parameters[index];
        }else
            return null;
    }

    public void addHandlers(Handler handler){
        handlers[handlerCount] = handler;
        handlerCount ++;
    }

    public Handler getHandler(int index){
        if(index <=handlerCount){
            return handlers[index];
        }else
            return null;
    }
}
