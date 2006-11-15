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
package org.apache.axis2.tool.core;

import java.util.ArrayList;

public class ServiceXMLCreater {
    private String serviceName;
    private String serviceClass;
    private ArrayList operations;

    public ServiceXMLCreater(String serviceName, String serviceClass, ArrayList operations) {
        this.serviceName = serviceName;
        this.serviceClass = serviceClass;
        this.operations = operations;
    }

    public String getServiceName() {
        return serviceName;
    }

    public String getServiceClass() {
        return serviceClass;
    }

    public ArrayList getOperations() {
        return operations;
    }

    public String toString() {
        String serviceXML = "<service name=\"" + serviceName + "\" >\n" +
                "<description>\n" +
                "Please Type your service description here\n" +
                "</description>\n" +
                "<parameter name=\"ServiceClass\" locked=\"false\">" + serviceClass + "</parameter>\n";
        if (operations.size() > 0) {
            serviceXML = serviceXML + "<excludeOperations>\n";
            for (int i = 0; i < operations.size(); i++) {
                String s = (String) operations.get(i);
                String op = "<operation>" + s + "</operation>\n";
                serviceXML = serviceXML + op;
            }
            serviceXML = serviceXML + "</excludeOperations>\n";
        }
        serviceXML = serviceXML + "</service>\n";
        return serviceXML;
    }

}

