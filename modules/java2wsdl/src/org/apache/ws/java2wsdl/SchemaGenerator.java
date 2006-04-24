package org.apache.ws.java2wsdl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Locale;
import java.util.Map;

import javax.xml.namespace.QName;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.java2wsdl.bytecode.MethodTable;
import org.apache.ws.java2wsdl.utils.TypeTable;
import org.codehaus.jam.JClass;
import org.codehaus.jam.JMethod;
import org.codehaus.jam.JParameter;
import org.codehaus.jam.JProperty;
import org.codehaus.jam.JamClassIterator;
import org.codehaus.jam.JamService;
import org.codehaus.jam.JamServiceFactory;
import org.codehaus.jam.JamServiceParams;

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
* @author : Deepal Jayasinghe (deepal@apache.org)
*
*/

public class SchemaGenerator implements Java2WSDLConstants {

    public static final String NAME_SPACE_PREFIX = "stn_";

    private static int prefixCount = 1;

    protected Map targetNamespacePrefixMap = new Hashtable();

    protected Map schemaMap = new Hashtable();

    protected XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();

    protected Log log = LogFactory.getLog(getClass());

    private ClassLoader classLoader;

    private String className;

    private TypeTable typeTable = new TypeTable();

    // to keep loadded method using JAM
    private JMethod methods [];

    //to store byte code method using Axis 1.x codes
    private MethodTable methodTable;

    private String schemaTargetNameSpace;

    private String schema_namespace_prefix;

    private Class clazz;

    public SchemaGenerator(ClassLoader loader, String className,
                           String schematargetNamespace, String schematargetNamespacePrefix)
            throws Exception {
        this.classLoader = loader;
        this.className = className;

        clazz = Class.forName(className, true, loader);
        methodTable = new MethodTable(clazz);

        if (schematargetNamespace != null
                && !schematargetNamespace.trim().equals("")) {
            this.schemaTargetNameSpace = schematargetNamespace;
        } else {
            this.schemaTargetNameSpace = Java2WSDLUtils
                    .schemaNamespaceFromClassName(className).toString();
        }
        if (schematargetNamespacePrefix != null
                && !schematargetNamespacePrefix.trim().equals("")) {
            this.schema_namespace_prefix = schematargetNamespacePrefix;
        } else {
            this.schema_namespace_prefix = SCHEMA_NAMESPACE_PRFIX;
        }

        XmlSchema xmlSchema = getXmlSchema(clazz.getPackage().getName(), schemaTargetNameSpace, schema_namespace_prefix);
        xmlSchema.getPrefixToNamespaceMap().put(DEFAULT_SCHEMA_NAMESPACE_PREFIX,
                URI_2001_SCHEMA_XSD);
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

            // since we do not support overload
            HashMap uniqueMethods = new HashMap();

            for (int i = 0; i < methods.length; i++) {
                JMethod jMethod = methods[i];
                // no need to think abt this method , since that is system
                // config method
                if (jMethod.getSimpleName().equals("init")
                        || "setOperationContext"
                        .equals(jMethod.getSimpleName()))
                    continue;
                if (uniqueMethods.get(jMethod.getSimpleName()) != null) {
                    throw new Exception(
                            " Sorry we don't support methods overloading !!!! ");
                }

                if (!jMethod.isPublic()) {
                    // no need to generate Schema for non public methods
                    continue;
                }
                uniqueMethods.put(jMethod.getSimpleName(), jMethod);

                //it can easily get the annotations
//                jMethod.getAnnotations();
                JParameter [] paras = jMethod.getParameters();
                for (int j = 0; j < paras.length; j++) {
                    JParameter methodParameter = paras[j];
                    JClass paraType = methodParameter.getType();
                    String classTypeName = paraType.getQualifiedName();
                    if (paraType.isArrayType()) {
                        classTypeName = paraType.getArrayComponentType()
                                .getQualifiedName();
                        if (!typeTable.isSimpleType(classTypeName)) {
                            generateSchema(paraType.getArrayComponentType());
                        }
                    } else {
                        if (!typeTable.isSimpleType(classTypeName)) {
                            generateSchema(methodParameter.getType());
                        }
                    }
                    /**
                     * 1. have to check whethet its a simple type 2. then to
                     * check whther its a simple type array 3. OM elemney 4.
                     * Bean
                     */

                }
                // for its return type
                JClass retuenType = jMethod.getReturnType();
                if (!retuenType.isVoidType()) {
                    if (retuenType.isArrayType()) {
                        String returnTypeName = retuenType
                                .getArrayComponentType().getQualifiedName();
                        if (!typeTable.isSimpleType(returnTypeName)) {
                            generateSchema(retuenType.getArrayComponentType());
                        }
                    } else {
                        if (!typeTable.isSimpleType(retuenType
                                .getQualifiedName())) {
                            generateSchema(retuenType);
                        }
                    }
                }

            }
            generateWrapperElements(methods);
        }
        return schemaMap.values();
    }

    /**
     * Generates wrapper element. If a method takes more than one parameter e.g.
     * foo(Type1 para1, Type2 para2){} creates a Wrapper element like <element
     * name="fooInParameter type="tns:fooInParameterElement""> <complexType
     * name="fooInParameterElement"> <sequnce> <element name="para1"
     * type="tns:Type1"> <element name="para2" type="tns:Type2"> </sequnce>
     * </complexType> </element>
     */
    private void generateWrapperElements(JMethod methods[]) throws Exception {
        for (int i = 0; i < methods.length; i++) {
            JMethod method = methods[i];
            if (method.getSimpleName().equals("init") ||
                    method.getSimpleName().equals("setOperationContext"))
                continue;
            if (!method.isPublic())
                continue;
            generateWrapperElementforMethod(method);
        }
    }

    private void generateWrapperElementforMethod(JMethod method) throws Exception {
        //since the wrapper elements are generated for the methods of the service interface
        //we get the schema that corresponds to the package of the service interface classname
        XmlSchema xmlSchema = getXmlSchema(clazz.getPackage().getName(),
                schemaTargetNameSpace,
                schema_namespace_prefix);
        String methodName = method.getSimpleName();
        XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
        XmlSchemaSequence sequence = new XmlSchemaSequence();

        XmlSchemaElement eltOuter = new XmlSchemaElement();
        eltOuter.setName(methodName);
        eltOuter.setSchemaType(complexType);
        // adding this type to the table
        QName elementName = new QName(this.schemaTargetNameSpace, eltOuter
                .getName(), this.schema_namespace_prefix);
        typeTable.addComplexSchema(methodName, elementName);
        xmlSchema.getItems().add(eltOuter);
        xmlSchema.getElements().add(elementName, eltOuter);

        JParameter [] paras = method.getParameters();
        if (paras.length > 0) {
            complexType.setParticle(sequence);
        }
        String parameterNames [] = null;
        if (paras.length > 0) {
            parameterNames = methodTable.getParameterNames(methodName);
        }
        for (int j = 0; j < paras.length; j++) {
            JParameter methodParameter = paras[j];
            String paraName = methodParameter.getSimpleName();
            String classTypeName = methodParameter.getType().getQualifiedName();
            boolean isArryType = methodParameter.getType().isArrayType();
            if (isArryType) {
                classTypeName = methodParameter.getType()
                        .getArrayComponentType().getQualifiedName();
            }

            if (parameterNames != null) {
                paraName = parameterNames[j];
            }

            if (typeTable.isSimpleType(classTypeName)) {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName(paraName);
                elt1.setSchemaTypeName(typeTable
                        .getSimpleSchemaTypeName(classTypeName));
                sequence.getItems().add(elt1);
                if (isArryType) {
                    elt1.setMaxOccurs(Long.MAX_VALUE);
                    elt1.setMinOccurs(0);
                }
            } else {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName(paraName);
                elt1.setSchemaTypeName(typeTable
                        .getComplexSchemaType(classTypeName));
                sequence.getItems().add(elt1);
                if (isArryType) {
                    elt1.setMaxOccurs(Long.MAX_VALUE);
                    elt1.setMinOccurs(0);
                }

                if (!xmlSchema.getPrefixToNamespaceMap().values().
                        contains(typeTable.getComplexSchemaType(classTypeName).getNamespaceURI())) {
                    XmlSchemaImport importElement = new XmlSchemaImport();
                    importElement.setNamespace(typeTable.getComplexSchemaType(classTypeName).getNamespaceURI());
                    xmlSchema.getItems().add(importElement);
                    xmlSchema.getPrefixToNamespaceMap().
                            put(generatePrefix(), typeTable.getComplexSchemaType(classTypeName).getNamespaceURI());
                }
            }
        }

        //generating wrapper element for retuen element
        JClass methodReturnType = method.getReturnType();
        generateWrapperforReturnType(methodReturnType, methodName);

    }

    private void generateWrapperforReturnType(JClass retuenType,
                                              String methodName) throws Exception {
        //since the wrapper elements are generated for the methods of the service interface
        //we get the schema that corresponds to the package of the service interface classname
        XmlSchema xmlSchema = getXmlSchema(clazz.getPackage().getName(),
                schemaTargetNameSpace,
                schema_namespace_prefix);
        if (!retuenType.isVoidType()) {
            XmlSchemaComplexType retuen_com_type = new XmlSchemaComplexType(
                    xmlSchema);
            XmlSchemaElement ret_eltOuter = new XmlSchemaElement();
            ret_eltOuter.setName(methodName + RESPONSE);

            ret_eltOuter.setSchemaType(retuen_com_type);
            QName ret_comTypeName = new QName(this.schemaTargetNameSpace,
                    ret_eltOuter.getName(), this.schema_namespace_prefix);
            xmlSchema.getItems().add(ret_eltOuter);
            xmlSchema.getElements().add(ret_comTypeName, ret_eltOuter);

            typeTable.addComplexSchema(
                    methodName + RESPONSE, ret_comTypeName);
            String classTypeName = retuenType.getQualifiedName();
            boolean isArryType = retuenType.isArrayType();
            XmlSchemaSequence sequence = new XmlSchemaSequence();
            retuen_com_type.setParticle(sequence);
            if (isArryType) {
                classTypeName = retuenType.getArrayComponentType()
                        .getQualifiedName();
            }
            if (typeTable.isSimpleType(classTypeName)) {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName("return");
                elt1.setSchemaTypeName(typeTable
                        .getSimpleSchemaTypeName(classTypeName));
                sequence.getItems().add(elt1);
                if (isArryType) {
                    elt1.setMaxOccurs(Long.MAX_VALUE);
                    elt1.setMinOccurs(0);
                }
            } else {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName("return");
                elt1.setSchemaTypeName(typeTable
                        .getComplexSchemaType(classTypeName));
                sequence.getItems().add(elt1);
                if (isArryType) {
                    elt1.setMaxOccurs(Long.MAX_VALUE);
                    elt1.setMinOccurs(0);
                }
                if (!xmlSchema.getPrefixToNamespaceMap().values().
                        contains(typeTable.getComplexSchemaType(classTypeName).getNamespaceURI())) {
                    XmlSchemaImport importElement = new XmlSchemaImport();
                    importElement.setNamespace(typeTable.getComplexSchemaType(classTypeName).getNamespaceURI());
                    xmlSchema.getItems().add(importElement);
                    xmlSchema.getPrefixToNamespaceMap().
                            put(generatePrefix(), typeTable.getComplexSchemaType(classTypeName).getNamespaceURI());
                }
            }
        }
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
    private void generateSchema(JClass javaType) {
        String name = javaType.getQualifiedName();
        if (typeTable.getComplexSchemaType(name) == null) {
            String simpleName = javaType.getSimpleName();

            String packageName = javaType.getContainingPackage().getQualifiedName();
            String targetNameSpace = Java2WSDLUtils.schemaNamespaceFromPackageName(packageName).toString();

            XmlSchema xmlSchema = getXmlSchema(packageName, targetNameSpace, generatePrefix());
            String targetNamespacePrefix = (String) targetNamespacePrefixMap.get(packageName);

            XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
            XmlSchemaSequence sequence = new XmlSchemaSequence();

            XmlSchemaElement eltOuter = new XmlSchemaElement();
            QName elemntName = new QName(targetNameSpace, simpleName, targetNamespacePrefix);
            eltOuter.setName(simpleName);
            eltOuter.setQName(elemntName);
            complexType.setParticle(sequence);
            complexType.setName(simpleName);

            xmlSchema.getItems().add(eltOuter);
            xmlSchema.getElements().add(elemntName, eltOuter);
            eltOuter.setSchemaTypeName(complexType.getQName());

           xmlSchema.getItems().add(complexType);
            xmlSchema.getSchemaTypes().add(elemntName, complexType);

            // adding this type to the table
            //  typeTable.addComplexScheam(name, complexType.getQName());
            typeTable.addComplexSchema(name, eltOuter.getQName());

            JProperty [] properties = javaType.getDeclaredProperties();
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
                        elt1.setMinOccurs(0);
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
                        elt1.setMinOccurs(0);
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
    }

    private XmlSchema getXmlSchema(String packageName, String targetNamespace, String targetNamespacePrefix) {
        XmlSchema xmlSchema ;
        // schema element that will hold this type i.e. schema element whose
        // targetnamespace
        // corresponds to this type's package
        if ((xmlSchema = (XmlSchema) schemaMap.get(packageName)) == null) {
            xmlSchema = new XmlSchema(targetNamespace, xmlSchemaCollection);
            targetNamespacePrefixMap.put(packageName, targetNamespacePrefix);
            schemaMap.put(packageName, xmlSchema);

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
}
