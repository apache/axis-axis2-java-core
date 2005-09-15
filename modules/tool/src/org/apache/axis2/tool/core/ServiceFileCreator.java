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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.axis2.wsdl.codegen.writer.ClassWriter;
import org.apache.axis2.wsdl.codegen.writer.ServiceXMLWriter;
import org.apache.crimson.tree.XmlDocument;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class ServiceFileCreator {
    
    public File createServiceFile(String serviceName,String implementationClassName,ArrayList methodList) throws Exception {
        
        String currentUserDir = System.getProperty("user.dir");
        String fileName = "service.xml";
        
        ClassWriter serviceXmlWriter = new ServiceXMLWriter(currentUserDir);
        writeClass(getServiceModel(serviceName,implementationClassName,methodList),serviceXmlWriter,fileName);

        return new File(currentUserDir + File.separator + fileName);




    }

    private XmlDocument getServiceModel(String serviceName,String className,ArrayList methods){
        XmlDocument doc = new XmlDocument();
        Element rootElement = doc.createElement("interface");
        addAttribute(doc,"package","", rootElement);
        addAttribute(doc,"name",className,rootElement);
        addAttribute(doc,"servicename",serviceName,rootElement);
        Element methodElement = null;
        int size = methods.size();
        for(int i=0;i<size;i++){
            methodElement = doc.createElement("method");
            addAttribute(doc,"name",methods.get(i).toString(),methodElement);
            rootElement.appendChild(methodElement);
        }
        doc.appendChild(rootElement);
        return doc;
    }
    
    private void addAttribute(XmlDocument document,String AttribName, String attribValue, Element element){
        Attr attribute = document.createAttribute(AttribName);
        attribute.setValue(attribValue);
        element.setAttributeNode(attribute);
    }
    
    /**
     * A resusable method for the implementation of interface and implementation writing
     * @param model
     * @param writer
     * @throws IOException
     * @throws Exception
     */
    private void writeClass(XmlDocument model,ClassWriter writer,String fileName) throws IOException,Exception {
        ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
        model.write(memoryStream);
        writer.loadTemplate();
        writer.createOutFile(null,
                 fileName);
        writer.writeOutFile(new ByteArrayInputStream(memoryStream.toByteArray()));
    }
    
   

}
