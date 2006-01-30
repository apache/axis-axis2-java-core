package org.apache.axis2.wsdl.builder;

import org.apache.axis2.wsdl.writer.WOMWriter;
import org.apache.axis2.wsdl.writer.WOMWriterFactory;
import org.apache.axis2.wsdl.builder.Java2WOMBuilder;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.wsdl.WSDLDescription;

import java.io.OutputStream;
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
*/

/**
 * The WSDL builder to be used when WSDL's need to be generated from a Java class
 */
public class Java2WSDLBuilder {

    private OutputStream out;
    private String className;
    private ClassLoader classLoader;
    private String wsdlPrefix = "wsdl";

    private String serviceName  = null;

    //these apply for the WSDL
    private String targetNamespace = null;
    private String targetNamespacePrefix = null;

    private String  schemaTargetNamespace = null;
    private String  schemaTargetNamespacePrefix = null;

    public String getSchemaTargetNamespace() {
        return schemaTargetNamespace;
    }

    public void setSchemaTargetNamespace(String schemaTargetNamespace) {
        this.schemaTargetNamespace = schemaTargetNamespace;
    }

    public String getSchemaTargetNamespacePrefix() {
        return schemaTargetNamespacePrefix;
    }

    public void setSchemaTargetNamespacePrefix(String schemaTargetNamespacePrefix) {
        this.schemaTargetNamespacePrefix = schemaTargetNamespacePrefix;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    public void setTargetNamespace(String targetNamespace) {
        this.targetNamespace = targetNamespace;
    }

    public String getTargetNamespacePrefix() {
        return targetNamespacePrefix;
    }

    public void setTargetNamespacePrefix(String targetNamespacePrefix) {
        this.targetNamespacePrefix = targetNamespacePrefix;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }


    public String getWsdlPrefix() {
        return wsdlPrefix;
    }

    public void setWsdlPrefix(String wsdlPrefix) {
        this.wsdlPrefix = wsdlPrefix;
    }

    /**
     *
     * @param out
     * @param className
     * @param classLoader
     */
    public Java2WSDLBuilder(OutputStream out, String className, ClassLoader classLoader) {
        this.out = out;
        this.className = className;
        this.classLoader = classLoader;
    }


    /**
     * Externally visible generator method
     * @throws Exception
     */
    public void generateWSDL() throws Exception {
        SchemaGenerator sg = new SchemaGenerator(classLoader, className, schemaTargetNamespace, schemaTargetNamespacePrefix);
        XmlSchema schema = sg.generateSchema();
        WSDLDescription wommodel = new Java2WOMBuilder(
                sg.getTypeTable(),
                sg.getMethods(),
                schema,
                serviceName==null?simpleClassName(className):serviceName,
                targetNamespace,
                targetNamespacePrefix).generateWOM();
        
        WOMWriter womWriter = WOMWriterFactory.createWriter(org.apache.wsdl.WSDLConstants.WSDL_1_1);
        womWriter.setdefaultWSDLPrefix(wsdlPrefix);
        womWriter.writeWOM(wommodel, out);

    }

    /**
     * A method to strip the fully qualified className to a simple classname
     * @param qualifiedName
     * @return
     */
    private String simpleClassName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(".");
        if (index > 0) {
            return qualifiedName.substring(index + 1, qualifiedName.length());
        }
        return qualifiedName;
    }
}
