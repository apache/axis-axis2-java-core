package org.apache.ws.java2wsdl;

import org.apache.axiom.om.OMElement;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
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

public class Java2WSDLBuilder implements Java2WSDLConstants {

    public static final String ALL = "all";
    private OutputStream out;
    private String className;
    private ClassLoader classLoader;
    private String wsdlPrefix = "wsdl";

    private String serviceName = null;

    //these apply for the WSDL
    private String targetNamespace = null;
    private String targetNamespacePrefix = null;

    private String attrFormDefault = null;
    private String elementFormDefault = null;
    private String schemaTargetNamespace = null;
    private String schemaTargetNamespacePrefix = null;
    private String style = Java2WSDLConstants.DOCUMENT;
    private String use = Java2WSDLConstants.LITERAL;
    private String locationUri = Java2WSDLConstants.DEFAULT_LOCATION_URL;
    private ArrayList extraClasses;
    
    private String nsGenClassName = null;
    private Map pkg2nsMap = null;
    private boolean pretty = true;

    public String getSchemaTargetNamespace() throws Exception {
        if ( schemaTargetNamespace == null ) {
            schemaTargetNamespace =
                Java2WSDLUtils.schemaNamespaceFromClassName(className, classLoader, resolveNSGen()).toString();
        }
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
        if ( schemaTargetNamespacePrefix == null ) {
            this.schemaTargetNamespacePrefix = SCHEMA_NAMESPACE_PRFIX;
        }
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
        SchemaGenerator schemaGenerator = new SchemaGenerator(classLoader, 
                                                    className,
                                                    getSchemaTargetNamespace(), 
                                                    getSchemaTargetNamespacePrefix());
        ArrayList excludedOperation = new ArrayList();
        excludedOperation.add("init");
        excludedOperation.add("setOperationContext");
        excludedOperation.add("destroy");
        schemaGenerator.setExcludeMethods(excludedOperation);
        schemaGenerator.setAttrFormDefault(getAttrFormDefault());
        schemaGenerator.setElementFormDefault(getElementFormDefault());
        schemaGenerator.setExtraClasses(getExtraClasses());
        schemaGenerator.setNsGen(resolveNSGen());
        schemaGenerator.setPkg2nsmap(getPkg2nsMap());
        if ( getPkg2nsMap() != null && !getPkg2nsMap().isEmpty() && 
                (getPkg2nsMap().containsKey(ALL) || getPkg2nsMap().containsKey(ALL.toUpperCase())) ) {
            schemaGenerator.setUseWSDLTypesNamespace(true);
        }

        Collection schemaCollection = schemaGenerator.generateSchema();
        
        Java2OMBuilder java2OMBuilder = new Java2OMBuilder(schemaGenerator.getMethods(),
                schemaCollection,
                schemaGenerator.getTypeTable(),
                serviceName == null ? Java2WSDLUtils.getSimpleClassName(className) : serviceName,
                targetNamespace == null ? Java2WSDLUtils.namespaceFromClassName(className,classLoader, resolveNSGen()).toString() : targetNamespace,
                targetNamespacePrefix,
                style,
                use,
                locationUri);
        java2OMBuilder.setSchemaTargetNamespace(getSchemaTargetNamespace());
        java2OMBuilder.setSchemaTargetNamespacePrefix(getSchemaTargetNamespacePrefix());
        OMElement wsdlElement = java2OMBuilder.generateOM();
        if(!isPretty()){
            wsdlElement.serialize(out);
        } else {
            Java2WSDLUtils.prettyPrint(wsdlElement, out);
        }
        out.flush();
        out.close();
    }

    public String getAttrFormDefault() {
        return attrFormDefault;
    }

    public void setAttrFormDefault(String attrFormDefault) {
        this.attrFormDefault = attrFormDefault;
    }

    public String getElementFormDefault() {
        return elementFormDefault;
    }

    public void setElementFormDefault(String elementFormDefault) {
        this.elementFormDefault = elementFormDefault;
    }

    public ArrayList getExtraClasses() {
        return extraClasses;
    }

    public void setExtraClasses(ArrayList extraClasses) {
        this.extraClasses = extraClasses;
    }

    public String getNsGenClassName() {
        return nsGenClassName;
    }

    public void setNsGenClassName(String nsGenClassName) {
        this.nsGenClassName = nsGenClassName;
    }

    public Map getPkg2nsMap() {
        return pkg2nsMap;
    }

    public void setPkg2nsMap(Map pkg2nsMap) {
        this.pkg2nsMap = pkg2nsMap;
    }
    
    private NamespaceGenerator resolveNSGen() {
        NamespaceGenerator nsGen = null;
        try {
            nsGen = (NamespaceGenerator)Class.forName(this.nsGenClassName).newInstance();
        } catch ( Exception e ) {
            nsGen = new DefaultNamespaceGenerator();
        }
        
        return nsGen;
    }

    public boolean isPretty() {
        return pretty;
    }

    public void setPretty(boolean pretty) {
        this.pretty = pretty;
    }
}

