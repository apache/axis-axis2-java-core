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
package org.apache.axis2.wsdl.codegen.writer;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.util.FileWriter;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.wsdl.Definition;
import javax.wsdl.Import;
import javax.wsdl.Types;
import javax.wsdl.Service;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.ArrayList;

public class WSDL11Writer {

    public static final String IMPORT_TAG = "import";
    public static final String INCLUDE_TAG = "include";
    public static final String SCHEMA_LOCATION = "schemaLocation";

    private File baseFolder = null;
    private int count;


    public WSDL11Writer(File baseFolder) {
        this.baseFolder = baseFolder;
        this.count = 0;
    }

    public void writeWSDL(AxisService axisService) {
        try {
            if (axisService != null) {
                //create a output file
                File outputFile = FileWriter.createClassFile(baseFolder,
                                                             null,
                                                             axisService.getName(),
                                                             ".wsdl");
                FileOutputStream fos = new FileOutputStream(outputFile);
                axisService.printWSDL(fos);
                fos.flush();
                fos.close();
            }
        } catch (Exception e) {
            throw new RuntimeException("WSDL writing failed!", e);
        }
    }

    public void writeWSDL(AxisService axisService, Definition definition, Map changedMap) {
        try {
            if (axisService != null) {
                writeWSDL(definition, axisService.getName(), changedMap);
            }
        } catch (Exception e) {
            throw new RuntimeException("WSDL writing failed!", e);
        }
    }

    private void writeWSDL(Definition definition,
                           String serviceName,
                           Map changedMap) throws Exception {
        // first process the imports and save them.
        Map imports = definition.getImports();
        if (imports != null && (imports.size() > 0)) {
            Vector importsVector = null;
            Import wsdlImport = null;
            String wsdlName = null;
            for (Iterator improtsVectorIter = imports.values().iterator();
                 improtsVectorIter.hasNext();) {
                importsVector = (Vector)improtsVectorIter.next();
                for (Iterator importsIter = importsVector.iterator(); importsIter.hasNext();) {
                    wsdlImport = (Import)importsIter.next();
                    wsdlName = "wsdl_" + count++ + ".wsdl";
                    writeWSDL(wsdlImport.getDefinition(), wsdlName, changedMap);
                    wsdlImport.setLocationURI(wsdlName);
                }
            }
        }
        // change the locations on the imported schemas
        adjustWSDLSchemaLocatins(definition, changedMap);
        // finally save the file
        WSDLWriter wsdlWriter = WSDLFactory.newInstance().newWSDLWriter();
        File outputFile = FileWriter.createClassFile(baseFolder,
                                                     null, serviceName, ".wsdl");
        FileOutputStream out = new FileOutputStream(outputFile);

        // we have a catch here
        // if there are multimple services in the definition object
        // we have to write only the relavent service.


        if (definition.getServices().size() > 1){
           List removedServices = new ArrayList();
           List servicesList = new ArrayList();

           Map services = definition.getServices();
           // populate the services list
           for (Iterator iter = services.values().iterator();iter.hasNext();){
               servicesList.add(iter.next());
           }
           Service service;
           for (Iterator iter = servicesList.iterator();iter.hasNext();){
               service = (Service) iter.next();
               if (!service.getQName().getLocalPart().equals(serviceName)){
                   definition.removeService(service.getQName());
                   removedServices.add(service);
               }
           }

           //now we have only the required service so write it
           wsdlWriter.writeWSDL(definition, out);

           // again add the removed services
           for (Iterator iter = removedServices.iterator(); iter.hasNext();){
               service = (Service) iter.next();
               definition.addService(service);
           }
        } else {
           // no problem proceed normaly
           wsdlWriter.writeWSDL(definition, out);
        }
        out.flush();
        out.close();
    }

    /**
     * adjust the schema locations in the original wsdl
     *
     * @param changedScheamLocations
     */
    public void adjustWSDLSchemaLocatins(Definition definition, Map changedScheamLocations) {
        Types wsdlTypes = definition.getTypes();
        if (wsdlTypes != null) {
            List extensibilityElements = wsdlTypes.getExtensibilityElements();
            Object currentObject;
            Schema schema;
            for (Iterator iter = extensibilityElements.iterator(); iter.hasNext();) {
                currentObject = iter.next();
                if (currentObject instanceof Schema) {
                    schema = (Schema)currentObject;
                    changeLocations(schema.getElement(), changedScheamLocations);
                }
            }
        }
    }

    private void changeLocations(Element element, Map changedScheamLocations) {
        NodeList nodeList = element.getChildNodes();
        String tagName;
        for (int i = 0; i < nodeList.getLength(); i++) {
            tagName = nodeList.item(i).getLocalName();
            if (IMPORT_TAG.equals(tagName) || INCLUDE_TAG.equals(tagName)) {
                processImport(nodeList.item(i), changedScheamLocations);
            }
        }
    }

    private void processImport(Node importNode, Map changedScheamLocations) {
        NamedNodeMap nodeMap = importNode.getAttributes();
        Node attribute;
        String attributeValue;
        for (int i = 0; i < nodeMap.getLength(); i++) {
            attribute = nodeMap.item(i);
            if (attribute.getNodeName().equals("schemaLocation")) {
                attributeValue = attribute.getNodeValue();
                if (changedScheamLocations.get(attributeValue) != null) {
                    attribute.setNodeValue(
                            (String)changedScheamLocations.get(attributeValue));
                }
            }
        }
    }


}
