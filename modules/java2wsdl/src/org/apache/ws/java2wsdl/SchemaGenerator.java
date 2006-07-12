package org.apache.ws.java2wsdl;

import org.apache.ws.commons.schema.*;
import org.apache.ws.java2wsdl.bytecode.MethodTable;
import org.apache.ws.java2wsdl.utils.TypeTable;
import org.codehaus.jam.*;

import javax.xml.namespace.QName;
import java.util.*;

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

public class SchemaGenerator implements Java2WSDLConstants {

    public static final String NAME_SPACE_PREFIX = "stn_";

    private static int prefixCount = 1;

    protected Map targetNamespacePrefixMap = new Hashtable();

    protected Map schemaMap = new Hashtable();

    protected XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();


    private ClassLoader classLoader;

    private String className;

    private TypeTable typeTable = new TypeTable();

    // to keep loadded method using JAM
    private JMethod methods [];

    //to store byte code method using Axis 1.x codes
    private MethodTable methodTable;

    private String schemaTargetNameSpace;

    private String schema_namespace_prefix;

    private String attrFormDefault = null;

    private String elementFormDefault = null;

    private ArrayList excludeMethods = new ArrayList();

    public SchemaGenerator(ClassLoader loader, String className,
                           String schematargetNamespace, String schematargetNamespacePrefix)
            throws Exception {
        this.classLoader = loader;
        this.className = className;

        Class clazz = Class.forName(className, true, loader);
        methodTable = new MethodTable(clazz);

        if (schematargetNamespace != null
                && !schematargetNamespace.trim().equals("")) {
            this.schemaTargetNameSpace = schematargetNamespace;
        } else {
            this.schemaTargetNameSpace = Java2WSDLUtils
                    .schemaNamespaceFromClassName(className, loader).toString();
        }
        if (schematargetNamespacePrefix != null
                && !schematargetNamespacePrefix.trim().equals("")) {
            this.schema_namespace_prefix = schematargetNamespacePrefix;
        } else {
            this.schema_namespace_prefix = SCHEMA_NAMESPACE_PRFIX;
        }
        //initializeSchemaMap(this.schemaTargetNameSpace, this.schema_namespace_prefix);
    }

    /**
     * Generates schema for all the parameters in method. First generates schema
     * for all different parameter type and later refers to them.
     *
     * @return Returns XmlSchema.
     * @throws Exception
     */
    public Collection generateSchema() throws Exception {

        JamServiceFactory factory = JamServiceFactory.getInstance();
        JamServiceParams jam_service_parms = factory.createServiceParams();
        //setting the classLoder
//        jam_service_parms.setParentClassLoader(factory.createJamClassLoader(classLoader));
        //it can posible to add the classLoader as well
        jam_service_parms.addClassLoader(classLoader);
        jam_service_parms.includeClass(className);
        JamService service = factory.createService(jam_service_parms);

        JamClassIterator jClassIter = service.getClasses();
        //all most all the time the ittr will have only one class in it
        while (jClassIter.hasNext()) {
            JClass jclass = (JClass) jClassIter.next();
            // serviceName = jclass.getSimpleName();
            //todo in the future , when we support annotation we can use this
            //JAnnotation[] annotations = jclass.getAnnotations();

            /**
             * Schema genertaion done in two stage 1. Load all the methods and
             * create type for methods parameters (if the parameters are Bean
             * then it will create Complex types for those , and if the
             * parameters are simple type which decribe in SimpleTypeTable
             * nothing will happen) 2. In the next stage for all the methods
             * messages and port types will be creteated
             */
            methods = jclass.getDeclaredMethods();
            //short the elements in the array
            Arrays.sort(methods);

            // since we do not support overload
            HashMap uniqueMethods = new HashMap();
            XmlSchemaComplexType methodSchemaType;
            XmlSchemaSequence sequence = null;

            for (int i = 0; i < methods.length; i++) {
                JMethod jMethod = methods[i];
                String methodName = methods[i].getSimpleName();
                // no need to think abt this method , since that is system
                // config method
                if (excludeMethods.contains(jMethod.getSimpleName())) {
                    continue;
                }

                if (uniqueMethods.get(jMethod.getSimpleName()) != null) {
                    throw new Exception(
                            " Sorry we don't support methods overloading !!!! ");
                }

                if (!jMethod.isPublic()) {
                    // no need to generate Schema for non public methods
                    continue;
                }
                uniqueMethods.put(jMethod.getSimpleName(), jMethod);
                //create the schema type for the method wrapper

                uniqueMethods.put(jMethod.getSimpleName(), jMethod);
                JParameter [] paras = jMethod.getParameters();
                String parameterNames [] = null;
                if (paras.length > 0) {
                    parameterNames = methodTable.getParameterNames(methodName);
                    sequence = new XmlSchemaSequence();

                    methodSchemaType = createSchemaTypeForMethodPart(jMethod.getSimpleName());
                    methodSchemaType.setParticle(sequence);
                }

                for (int j = 0; j < paras.length; j++) {
                    JParameter methodParameter = paras[j];
                    JClass paraType = methodParameter.getType();
                    generateSchemaForType(sequence, paraType,
                            (parameterNames != null && parameterNames[j] != null) ? parameterNames[j] : methodParameter.getSimpleName());
                }
                // for its return type
                JClass returnType = jMethod.getReturnType();

                if (!returnType.isVoidType()) {
                    methodSchemaType = createSchemaTypeForMethodPart(jMethod.getSimpleName() + RESPONSE);
                    sequence = new XmlSchemaSequence();
                    methodSchemaType.setParticle(sequence);
                    generateSchemaForType(sequence, returnType, "return");
                }
            }
        }
        return schemaMap.values();
    }

    /**
     * JAM convert first name of an attribute into UpperCase as an example if
     * there is a instance variable called foo in a bean , then Jam give that as
     * Foo so this method is to correct that error
     *
     * @param wrongName
     * @return the right name, using english as the locale for case conversion
     */
    public static String getCorrectName(String wrongName) {
        if (wrongName.length() > 1) {
            return wrongName.substring(0, 1).toLowerCase(Locale.ENGLISH)
                    + wrongName.substring(1, wrongName.length());
        } else {
            return wrongName.substring(0, 1).toLowerCase(Locale.ENGLISH);
        }
    }

    /**
     * @param javaType
     */
    private QName generateSchema(JClass javaType) {
        String name = javaType.getQualifiedName();
        QName schemaTypeName = typeTable.getComplexSchemaType(name);
        if (schemaTypeName == null) {
            String simpleName = javaType.getSimpleName();

            String packageName = javaType.getContainingPackage().getQualifiedName();
            String targetNameSpace = Java2WSDLUtils.schemaNamespaceFromPackageName(packageName).toString();

            XmlSchema xmlSchema = getXmlSchema(Java2WSDLUtils.schemaNamespaceFromPackageName(packageName).toString());
            String targetNamespacePrefix = (String) targetNamespacePrefixMap.get(targetNameSpace);

            XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
            XmlSchemaSequence sequence = new XmlSchemaSequence();

            XmlSchemaElement eltOuter = new XmlSchemaElement();
            schemaTypeName = new QName(targetNameSpace, simpleName, targetNamespacePrefix);
            eltOuter.setName(simpleName);
            eltOuter.setQName(schemaTypeName);
            complexType.setParticle(sequence);
            complexType.setName(simpleName);

            xmlSchema.getItems().add(eltOuter);
            xmlSchema.getElements().add(schemaTypeName, eltOuter);
            eltOuter.setSchemaTypeName(complexType.getQName());

            xmlSchema.getItems().add(complexType);
            xmlSchema.getSchemaTypes().add(schemaTypeName, complexType);

            // adding this type to the table
            typeTable.addComplexSchema(name, eltOuter.getQName());

            JProperty [] properties = javaType.getDeclaredProperties();
            Arrays.sort(properties);
            for (int i = 0; i < properties.length; i++) {
                JProperty property = properties[i];
                String propertyName = property.getType().getQualifiedName();
                boolean isArryType = property.getType().isArrayType();
                if (isArryType) {
                    propertyName = property.getType().getArrayComponentType().getQualifiedName();
                }
                if (typeTable.isSimpleType(propertyName)) {
                    XmlSchemaElement elt1 = new XmlSchemaElement();
                    elt1.setName(getCorrectName(property.getSimpleName()));
                    elt1.setSchemaTypeName(typeTable.getSimpleSchemaTypeName(propertyName));
                    sequence.getItems().add(elt1);
                    if (isArryType) {
                        elt1.setMaxOccurs(Long.MAX_VALUE);
                        elt1.setMinOccurs(1);
                    }
                } else {
                    if (isArryType) {
                        generateSchema(property.getType().getArrayComponentType());
                    } else {
                        generateSchema(property.getType());
                    }
                    XmlSchemaElement elt1 = new XmlSchemaElement();
                    elt1.setName(getCorrectName(property.getSimpleName()));
                    elt1.setSchemaTypeName(typeTable.getComplexSchemaType(propertyName));
                    sequence.getItems().add(elt1);
                    if (isArryType) {
                        elt1.setMaxOccurs(Long.MAX_VALUE);
                        elt1.setMinOccurs(1);
                    }

                    if (!xmlSchema.getPrefixToNamespaceMap().values().
                            contains(typeTable.getComplexSchemaType(propertyName).getNamespaceURI())) {
                        XmlSchemaImport importElement = new XmlSchemaImport();
                        importElement.setNamespace(typeTable.getComplexSchemaType(propertyName).getNamespaceURI());
                        xmlSchema.getItems().add(importElement);
                        xmlSchema.getPrefixToNamespaceMap().
                                put(generatePrefix(), typeTable.getComplexSchemaType(propertyName).getNamespaceURI());
                    }
                }
            }
        }
        return schemaTypeName;
    }

    private QName generateSchemaForType(XmlSchemaSequence sequence, JClass type, String partName) throws Exception {
        boolean isArrayType = type.isArrayType();
        if (isArrayType) {
            type = type.getArrayComponentType();
        }

        String classTypeName = type.getQualifiedName();

        QName schemaTypeName = typeTable.getSimpleSchemaTypeName(classTypeName);
        if (schemaTypeName == null) {
            schemaTypeName = generateSchema(type);
            addContentToMethodSchemaType(sequence,
                    schemaTypeName,
                    partName,
                    isArrayType);
            //addImport((XmlSchema)schemaMap.get(schemaTargetNameSpace), schemaTypeName);
            String schemaNamespace = Java2WSDLUtils.schemaNamespaceFromPackageName(type.getContainingPackage().
                    getQualifiedName()).toString();
            addImport(getXmlSchema(schemaNamespace), schemaTypeName);

        } else {
            addContentToMethodSchemaType(sequence,
                    schemaTypeName,
                    partName,
                    isArrayType);
        }

        return schemaTypeName;
    }

    private void addContentToMethodSchemaType(XmlSchemaSequence sequence,
                                              QName schemaTypeName,
                                              String paraName,
                                              boolean isArray) {
        XmlSchemaElement elt1 = new XmlSchemaElement();
        elt1.setName(paraName);
        elt1.setSchemaTypeName(schemaTypeName);
        sequence.getItems().add(elt1);

        if (isArray) {
            elt1.setMaxOccurs(Long.MAX_VALUE);
            elt1.setMinOccurs(1);
        }
    }

    private XmlSchemaComplexType createSchemaTypeForMethodPart(String localPartName) {
        //XmlSchema xmlSchema = (XmlSchema)schemaMap.get(schemaTargetNameSpace);
        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        QName elementName = new QName(this.schemaTargetNameSpace, localPartName, this.schema_namespace_prefix);
        XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);

        XmlSchemaElement globalElement = new XmlSchemaElement();
        globalElement.setSchemaType(complexType);
        globalElement.setName(formGlobalElementName(localPartName));
        globalElement.setQName(elementName);
        xmlSchema.getItems().add(globalElement);
        xmlSchema.getElements().add(elementName, globalElement);

        typeTable.addComplexSchema(localPartName, elementName);

        return complexType;
    }


    private String formGlobalElementName(String typeName) {
        String firstChar = typeName.substring(0, 1);
        return typeName.replaceFirst(firstChar, firstChar.toLowerCase());
    }

    private XmlSchema getXmlSchema(String targetNamespace) {
        XmlSchema xmlSchema;

        if ((xmlSchema = (XmlSchema) schemaMap.get(targetNamespace)) == null) {
            String targetNamespacePrefix = generatePrefix();

            xmlSchema = new XmlSchema(targetNamespace, xmlSchemaCollection);
            xmlSchema.setAttributeFormDefault(getAttrFormDefaultSetting());
            xmlSchema.setElementFormDefault(getElementFormDefaultSetting());


            targetNamespacePrefixMap.put(targetNamespace, targetNamespacePrefix);
            schemaMap.put(targetNamespace, xmlSchema);

            Hashtable prefixmap = new Hashtable();
            prefixmap.put(DEFAULT_SCHEMA_NAMESPACE_PREFIX, URI_2001_SCHEMA_XSD);
            prefixmap.put(targetNamespacePrefix, targetNamespace);
            xmlSchema.setPrefixToNamespaceMap(prefixmap);
        }
        return xmlSchema;
    }


    public TypeTable getTypeTable() {
        return typeTable;
    }

    public JMethod[] getMethods() {
        return methods;
    }

    private String generatePrefix() {
        return NAME_SPACE_PREFIX + prefixCount++;
    }

    public void setExcludeMethods(ArrayList excludeMethods) {
        this.excludeMethods = excludeMethods;
    }

    public String getSchemaTargetNameSpace() {
        return schemaTargetNameSpace;
    }

    private void addImport(XmlSchema xmlSchema, QName schemaTypeName) {
        if (!xmlSchema.getPrefixToNamespaceMap().values().
                contains(schemaTypeName.getNamespaceURI())) {
            XmlSchemaImport importElement = new XmlSchemaImport();
            importElement.setNamespace(schemaTypeName.getNamespaceURI());
            xmlSchema.getItems().add(importElement);
            xmlSchema.getPrefixToNamespaceMap().
                    put(generatePrefix(), schemaTypeName.getNamespaceURI());
        }
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

    private XmlSchemaForm getAttrFormDefaultSetting() {
        if (FORM_DEFAULT_UNQUALIFIED.equals(getAttrFormDefault())) {
            return new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        } else {
            return new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
        }
    }

    private XmlSchemaForm getElementFormDefaultSetting() {
        if (FORM_DEFAULT_UNQUALIFIED.equals(getElementFormDefault())) {
            return new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        } else {
            return new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
        }
    }

}
