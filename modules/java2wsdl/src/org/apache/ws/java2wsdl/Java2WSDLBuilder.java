package org.apache.ws.java2wsdl;

import org.apache.axiom.om.OMElement;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
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
    private String style = Java2WSDLConstants.DOCUMENT;
    private String use = Java2WSDLConstants.LITERAL;
    private String locationUri = Java2WSDLConstants.DEFAULT_LOCATION_URL;

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
        ArrayList excludeOpeartion = new ArrayList();
        excludeOpeartion.add("init");
        excludeOpeartion.add("setOperationContext");
        excludeOpeartion.add("destroy");
        sg.setExcludeMethods(excludeOpeartion);
        Collection schemaCollection = sg.generateSchema();
        Java2OMBuilder java2OMBuilder = new Java2OMBuilder(sg.getMethods(),
                schemaCollection,
                sg.getTypeTable(),
                serviceName == null ? Java2WSDLUtils.getSimpleClassName(className) : serviceName,
                targetNamespace == null ? Java2WSDLUtils.namespaceFromClassName(className,classLoader).toString() : targetNamespace,
                targetNamespacePrefix,
                style,
                use,
                locationUri);
        OMElement wsdlElement = java2OMBuilder.generateOM();
        wsdlElement.serialize(out);
        out.flush();
        out.close();
    }
}

