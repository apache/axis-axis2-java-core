package org.apache.idaeplugin.bean;

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
 * Date: Sep 22, 2005
 * Time: 11:11:48 PM
 */
public class ServiceObj {
    private String serviceName;
    private String serviceClass;
    private ArrayList opeartions;

    public ServiceObj(String serviceName, String serviceClass, ArrayList opeartions) {
        this.serviceName = serviceName;
        this.serviceClass = serviceClass;
        this.opeartions = opeartions;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public ArrayList getOpeartions() {
        return opeartions;
    }

    public String toString(){
        String serviceXML = "<service name=\""+ serviceName + "\">\n" +
                "<description>\n" +
                "Please Type your service description here\n" +
                "</description>\n"+
                "<parameter name=\"ServiceClass\" locked=\"false\">" + serviceClass + "</parameter>\n";
        for (int i = 0; i < opeartions.size(); i++) {
            String s = (String) opeartions.get(i);
            String op = "<operation name=\"" + s + "\">\n" +   "</operation>\n";
            serviceXML = serviceXML + op;
        }
        serviceXML = serviceXML + "</service>\n";
        return serviceXML;
    }

}
