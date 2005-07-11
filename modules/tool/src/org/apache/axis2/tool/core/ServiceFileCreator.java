package org.apache.axis.tool.core;

import java.io.File;
import java.io.FileWriter;
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
*/

public class ServiceFileCreator {
    public File createServiceFile(String providerClassName, String serviceClass, ArrayList methodList) throws Exception {


        String content = this.getFileString(providerClassName, serviceClass, methodList);
        File serviceFile = new File("service.xml");

        FileWriter fileWriter = new FileWriter(serviceFile);
        fileWriter.write(content);
        fileWriter.flush();

        return serviceFile;


    }

    private String getFileString(String providerClassName, String serviceClass, ArrayList methodList) {
        String str = "<service provider=\"" +
                providerClassName + "\" >" +
                "    <java:implementation class=\"" +
                serviceClass + "\" " +
                "xmlns:java=\"http://ws.apache.org/axis2/deployment/java\"/>\n";
        for (int i = 0; i < methodList.size(); i++) {
            str = str + "    <operation name=\"" +
                    methodList.get(i).toString() +
                    "\" qname=\"" +
                    methodList.get(i).toString() +
                    "\" >\n";

        }
        str = str + "</service>";
        return str;
    }

}
