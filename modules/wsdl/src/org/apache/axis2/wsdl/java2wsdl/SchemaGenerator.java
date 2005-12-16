package org.apache.axis2.wsdl.java2wsdl;

import org.apache.ws.commons.schema.*;
import org.apache.xmlbeans.impl.jam.*;

import javax.xml.namespace.QName;
import java.util.Hashtable;
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

public class SchemaGenerator {
    private ClassLoader classLoader;
    private String className;
    Hashtable prefixmap;
    XmlSchemaCollection schemaCollection;
    XmlSchema schema;
    TypeTable typeTable;
    private JMethod methods [];

    public static String METHOD_REQUEST_WRAPPER = "Request";
    public static String METHOD_RESPONSE_WRAPPER = "Response";
    public static String TARGET_NAMESPACE = null;
    public static String SCHEMA_TARGET_NAMESPACE = "http://ws.apache.org/axis2/xsd";
    public static String SCHEMA_NAMESPACE_PREFIX = "ns1";
    public static String TARGET_NAMESPACE_PREFIX = "tns";

    public SchemaGenerator(ClassLoader loader, String className,
                           String scheamtargetNamespace,
                           String scheamtargetNamespacePrefix) {
        this.classLoader = loader;
        this.className = className;
        TARGET_NAMESPACE = "http://" + className;
        if (scheamtargetNamespace != null && !scheamtargetNamespace.trim().equals("")) {
            SCHEMA_TARGET_NAMESPACE = scheamtargetNamespace;
        }
        if (scheamtargetNamespacePrefix != null && !scheamtargetNamespacePrefix.trim().equals("")) {
            SCHEMA_NAMESPACE_PREFIX = scheamtargetNamespacePrefix;
        }

        prefixmap = new Hashtable();
        prefixmap.put(SCHEMA_NAMESPACE_PREFIX, SCHEMA_TARGET_NAMESPACE);

        schemaCollection = new XmlSchemaCollection();

        schema = new XmlSchema(SCHEMA_TARGET_NAMESPACE, schemaCollection);
        schema.setElementFormDefault(new XmlSchemaForm(XmlSchemaForm.QUALIFIED));
        schema.setPrefixToNamespaceMap(prefixmap);
        this.typeTable = new TypeTable();
    }

    /**
     * Generates schema for all the parameters in method. It first generates schema for all different
     * parameter type and later refers to them.
     *
     * @return Returns XmlSchema
     * @throws Exception
     */
    public XmlSchema generateSchema() throws Exception {

        JamServiceFactory factory = JamServiceFactory.getInstance();
        JamServiceParams jam_service_parms = factory.createServiceParams();

        //it can possible to add the classLoader as well
        jam_service_parms.addClassLoader(classLoader);
        jam_service_parms.includeClass(className);
        JamService service = factory.createService(jam_service_parms);

        JamClassIterator jClassIter = service.getClasses();
        //all most all the time the ittr will have only one class in it
        while (jClassIter.hasNext()) {
            JClass jclass = (JClass) jClassIter.next();

            //todo in the future , when we support annotation we can use this
            //JAnnotation[] annotations = jclass.getAnnotations();

            /**
             * Schema generation done in two stage
             *  1. Load all the methods and create type for methods parameters (if the parameters are
             *     Beans then it will create Complex types for those , and if the parameters are simple
             *     type which described in SimpleTypeTable nothing will happen)
             *  2. In the next stage for all the methods messages and port types will be
             *     created.
             */
            methods = jclass.getDeclaredMethods();

            for (int i = 0; i < methods.length; i++) {
                JMethod jMethod = methods[i];

                JParameter [] params = jMethod.getParameters();
                for (int j = 0; j < params.length; j++) {
                    JParameter methodParameter = params[j];
                    JClass paramType = methodParameter.getType();
                    String classTypeName = paramType.getQualifiedName();
                    if (paramType.isArrayType()) {
                        classTypeName = paramType.getArrayComponentType().getQualifiedName();
                        if (!typeTable.isSimpleType(classTypeName)) {
                            generateSchema(paramType.getArrayComponentType());
                        }
                    } else {
                        if (!typeTable.isSimpleType(classTypeName)) {
                            generateSchema(methodParameter.getType());
                        }
                    }
                    /**
                     * 1. have to check whethet its a simple type
                     * 2. then to check whther its a simple type array
                     * 3. OM elemney
                     * 4. Bean
                     */

                }
                // for its return type
                JClass returnType = jMethod.getReturnType();
                if (!returnType.isVoidType()) {
                    if (returnType.isArrayType()) {
                        String returnTypeName = returnType.getArrayComponentType().getQualifiedName();
                        if (!typeTable.isSimpleType(returnTypeName)) {
                            generateSchema(returnType.getArrayComponentType());
                        }
                    } else {
                        if (!typeTable.isSimpleType(returnType.getQualifiedName())) {
                            generateSchema(returnType);
                        }
                    }
                }

            }
            generateWrapperElements(methods);
        }
        return schema;
    }

    /**
     * To generate wrapper element , if a method take more than one parameter
     * if the method look like foo(Type1 para1, Type2 para2){}
     * will creat e Wrapper element like
     * <element name="fooInParameter type="tns:fooInParameterElement"">
     * <complexType name="fooInParameterElement">
     * <sequnce>
     * <element name="para1" type="tns:Type1">
     * <element name="para2" type="tns:Type2">
     * </sequnce>
     * </complexType>
     * </element>
     */
    private void generateWrapperElements(JMethod methods[]) {
        for (int i = 0; i < methods.length; i++) {
            JMethod method = methods[i];
            generateWrapperElementforMethod(method);
        }
    }

    private void generateWrapperElementforMethod(JMethod method) {
        String methodName = method.getSimpleName();
        XmlSchemaComplexType complexType = new XmlSchemaComplexType(schema);
        XmlSchemaSequence sequence = new XmlSchemaSequence();

        XmlSchemaElement eltOuter = new XmlSchemaElement();
        eltOuter.setName(methodName + METHOD_REQUEST_WRAPPER);
        schema.getItems().add(eltOuter);
        eltOuter.setSchemaType(complexType);
        // adding this type to the table
        //todo pls ask this from Ajith
        QName elementName = new QName(SchemaGenerator.SCHEMA_TARGET_NAMESPACE,
                eltOuter.getName(), SCHEMA_NAMESPACE_PREFIX);
        typeTable.addComplexScheam(methodName + METHOD_REQUEST_WRAPPER, elementName);

        JParameter [] params = method.getParameters();
        if (params.length > 0) {
            complexType.setParticle(sequence);
        }
        for (int j = 0; j < params.length; j++) {
            JParameter methodParameter = params[j];
            String classTypeName = methodParameter.getType().getQualifiedName();
            boolean isArrayType = methodParameter.getType().isArrayType();
            if (isArrayType) {
                classTypeName = methodParameter.getType().getArrayComponentType().getQualifiedName();
            }
            if (typeTable.isSimpleType(classTypeName)) {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName(methodParameter.getSimpleName());
                elt1.setSchemaTypeName(typeTable.getSimpleSchemaTypeName(classTypeName));
                sequence.getItems().add(elt1);
                if (isArrayType) {
                    elt1.setMaxOccurs(Long.MAX_VALUE);
                }
            } else {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName(methodParameter.getSimpleName());
                elt1.setSchemaTypeName(typeTable.getComplexScheamType(classTypeName));
                sequence.getItems().add(elt1);
                if (isArrayType) {
                    elt1.setMaxOccurs(Long.MAX_VALUE);
                }
            }
        }

        //generating wrapper element for return element
        JClass methodReturnType = method.getReturnType();
        generateWrapperforReturnType(methodReturnType, methodName);

    }

    private void generateWrapperforReturnType(JClass returnType, String methodName) {
        if (!returnType.isVoidType()) {
            XmlSchemaComplexType return_com_type = new XmlSchemaComplexType(schema);
            XmlSchemaElement ret_eltOuter = new XmlSchemaElement();
            ret_eltOuter.setName(methodName + METHOD_RESPONSE_WRAPPER);
            schema.getItems().add(ret_eltOuter);
            ret_eltOuter.setSchemaType(return_com_type);
            QName ret_comTypeName = new QName(SchemaGenerator.SCHEMA_TARGET_NAMESPACE,
                    ret_eltOuter.getName(), SCHEMA_NAMESPACE_PREFIX);
            typeTable.addComplexScheam(methodName + METHOD_RESPONSE_WRAPPER, ret_comTypeName);
            String classTypeName = returnType.getQualifiedName();
            boolean isArrayType = returnType.isArrayType();
            XmlSchemaSequence sequence = new XmlSchemaSequence();
            return_com_type.setParticle(sequence);
            if (isArrayType) {
                classTypeName = returnType.getArrayComponentType().getQualifiedName();
            }
            if (typeTable.isSimpleType(classTypeName)) {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName("return");
                elt1.setSchemaTypeName(typeTable.getSimpleSchemaTypeName(classTypeName));
                sequence.getItems().add(elt1);
                if (isArrayType) {
                    elt1.setMaxOccurs(Long.MAX_VALUE);
                }
            } else {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName("return");
                elt1.setSchemaTypeName(typeTable.getComplexScheamType(classTypeName));
                sequence.getItems().add(elt1);
                if (isArrayType) {
                    elt1.setMaxOccurs(Long.MAX_VALUE);
                }
            }
        }
    }


    private void generateSchema(JClass javaType) {
        String name = javaType.getQualifiedName();
        if (typeTable.getComplexScheamType(name) == null) {
            String simpleName = javaType.getSimpleName();

            XmlSchemaComplexType complexType = new XmlSchemaComplexType(schema);
            XmlSchemaSequence sequence = new XmlSchemaSequence();

            XmlSchemaElement eltOuter = new XmlSchemaElement();
            QName elemntName = new QName(SCHEMA_TARGET_NAMESPACE, simpleName, SCHEMA_NAMESPACE_PREFIX);
            eltOuter.setName(simpleName);
            eltOuter.setQName(elemntName);
            complexType.setParticle(sequence);
            complexType.setName(simpleName);

            schema.getItems().add(eltOuter);
            schema.getItems().add(complexType);
            eltOuter.setSchemaTypeName(complexType.getQName());

            // adding this type to the table
            typeTable.addComplexScheam(name, eltOuter.getQName());

            JProperty [] properties = javaType.getDeclaredProperties();
            for (int i = 0; i < properties.length; i++) {
                JProperty property = properties[i];
                String propertyName = property.getType().getQualifiedName();
                boolean isArrayType = property.getType().isArrayType();
                if (isArrayType) {
                    propertyName = property.getType().getArrayComponentType().getQualifiedName();
                }
                if (typeTable.isSimpleType(propertyName)) {
                    XmlSchemaElement elt1 = new XmlSchemaElement();
                    elt1.setName(property.getSimpleName());
                    elt1.setSchemaTypeName(typeTable.getSimpleSchemaTypeName(propertyName));
                    sequence.getItems().add(elt1);
                    if (isArrayType) {
                        //todo pls check this with Ajith
                        elt1.setMaxOccurs(Long.MAX_VALUE);
                    }
                } else {
                    if (isArrayType) {
                        generateSchema(property.getType().getArrayComponentType());
                    } else {
                        generateSchema(property.getType());
                    }
                    XmlSchemaElement elt1 = new XmlSchemaElement();
                    elt1.setName(property.getSimpleName());
                    elt1.setSchemaTypeName(typeTable.getComplexScheamType(propertyName));
                    sequence.getItems().add(elt1);
                    if (isArrayType) {
                        elt1.setMaxOccurs(Long.MAX_VALUE);
                    }
                }
            }
        }
    }

    public TypeTable getTypeTable() {
        return typeTable;
    }

    public JMethod[] getMethods() {
        return methods;
    }

}
