package org.apache.axis.deployment.util;

import org.apache.axis.deployment.DeploymentException;

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
 *         3:16:17 PM
 *
 */
public class Handler {

    private String name;
    private String type;
    private Parameter [] parameters;

    private int count = 0;  // to keep the number of parameters

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void addParameter(Parameter parameter){
        parameters[count]= parameter;
        count ++;
    }

    public Parameter getParameter(int index){
        if(index < count){
            return  parameters[index] ;
        }
        else {
            return null;
        }
    }
}
