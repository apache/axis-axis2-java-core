package org.apache.ws.java2wsdl;

import org.apache.axiom.om.OMElement;
import org.apache.ws.commons.schema.XmlSchema;

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
*
*/

public class Java2WSDLBuilder {

    private OutputStream out;
    private String className;
    private ClassLoader classLoader;
    private String wsdlPrefix = "wsdl";

    private String serviceName = null;

    //these apply for the WSDL
    private String targetNamespace = null;
    private String targetNamespacePrefix = null;

    private String schemaTargetNamespace = null;
    private String schemaTargetNamespacePrefix = null;
    private String style = Constants.DOCUMNT;
    private String use = Constants.LITERAL;
    private String locationUri = Constants.DEFAULT_LOCATION_URL;

    public String getSchemaTargetNamespace() {
        return schemaTargetNamespace;
    }

    public String getStyle() {
        return style;
    }

    public String getLocationUri() {
        return locationUri;
    }

    public void setLocationUri(String locationUri) {
        this.locationUri = locationUri;
    }

    public void setStyle(String style) {
        this.style = style;
    }

    public String getUse() {
        return use;
    }

    public void setUse(String use) {
        this.use = use;
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
     *
     * @throws Exception
     */
    public void generateWSDL() throws Exception {
        SchemaGenerator sg = new SchemaGenerator(classLoader, className,
                schemaTargetNamespace, schemaTargetNamespacePrefix);
        XmlSchema schema = sg.generateSchema();
        Java2OMBuilder java2OMBuilder = new Java2OMBuilder(sg.getMethods(),
                schema,
                serviceName == null ? simpleClassName(className) : serviceName,
                targetNamespace,
                style,
                use,
                locationUri);
        OMElement wsdlElemnet = java2OMBuilder.generateOM();
        wsdlElemnet.serialize(out);
        out.flush();
        out.close();
    }

    /**
     * A method to strip the fully qualified className to a simple classname
     *
     * @param qualifiedName
     */
    private String simpleClassName(String qualifiedName) {
        int index = qualifiedName.lastIndexOf(".");
        if (index > 0) {
            return qualifiedName.substring(index + 1, qualifiedName.length());
        }
        return qualifiedName;
    }
}

