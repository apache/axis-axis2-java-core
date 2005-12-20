package org.apache.axis2.wsdl.java2wsdl;

import org.apache.axis2.wsdl.java2wsdl.bytecode.MethodTable;
import org.apache.ws.commons.schema.*;
import org.codehaus.jam.*;

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
    private XmlSchema schema;
    private TypeTable typeTable;
    // to keep loadded method using JAM
    private JMethod methods [];
    //to store byte code method using Axis 1.x codes
    private MethodTable methodTable;

    public static String METHOD_REQUEST_WRAPPER = "Request";
    public static String METHOD_RESPONSE_WRAPPER = "Response";
    public static String TARGET_NAMESPACE = null;
    public static String SCHEMA_TARGET_NAMESPASE = "http://org.apache.axis2/xsd";
    public static String SCHEMA_NAMESPASE_PRFIX = "ns1";
    public static String TARGET_NAMESPACE_PREFIX = "tns";

    public SchemaGenerator(ClassLoader loader, String className,
                           String scheamtargetNamespace,
                           String scheamtargetNamespacePrefix) {
        this.classLoader = loader;
        this.className = className;
        TARGET_NAMESPACE = "http://" + className;
        if (scheamtargetNamespace != null && !scheamtargetNamespace.trim().equals("")) {
            SCHEMA_TARGET_NAMESPASE = scheamtargetNamespace;
        }
        if (scheamtargetNamespacePrefix != null && !scheamtargetNamespacePrefix.trim().equals("")) {
            SCHEMA_NAMESPASE_PRFIX = scheamtargetNamespacePrefix;
        }
        Hashtable prefixmap = new Hashtable();
        prefixmap.put(SCHEMA_NAMESPASE_PRFIX, SCHEMA_TARGET_NAMESPASE);

        XmlSchemaCollection schemaCollection = new XmlSchemaCollection();

        schema = new XmlSchema(SCHEMA_TARGET_NAMESPASE, schemaCollection);
        schema.setElementFormDefault(new XmlSchemaForm(XmlSchemaForm.QUALIFIED));
        schema.setPrefixToNamespaceMap(prefixmap);
        this.typeTable = new TypeTable();

        try {
            Class clazz = Class.forName(className, true, loader);
            methodTable = new MethodTable(clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * To generate schema for all the parameters in method , first generate schema for all different
     * parameter type and later refer to them
     *
     * @return
     * @throws Exception
     */
    public XmlSchema generateSchema() throws Exception {

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
             * Schema genertaion done in two stage
             *  1. Load all the methods and create type for methods parameters (if the parameters are
             *     Bean then it will create Complex types for those , and if the parameters are simple
             *     type which decribe in SimpleTypeTable nothing will happen)
             *  2. In the next stage for all the methods messages and port types will be
             *     creteated
             */
            methods = jclass.getDeclaredMethods();

            for (int i = 0; i < methods.length; i++) {
                JMethod jMethod = methods[i];
                //no need to think abt this method , since that is system config method
                if (jMethod.getSimpleName().equals("init"))
                    continue;

                //it can easily get the annotations
//                jMethod.getAnnotations();
                JParameter [] paras = jMethod.getParameters();
                for (int j = 0; j < paras.length; j++) {
                    JParameter methodParameter = paras[j];
                    JClass paraType = methodParameter.getType();
                    String classTypeName = paraType.getQualifiedName();
                    if (paraType.isArrayType()) {
                        classTypeName = paraType.getArrayComponentType().getQualifiedName();
                        if (!typeTable.isSimpleType(classTypeName)) {
                            generateSchema(paraType.getArrayComponentType());
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
                JClass retuenType = jMethod.getReturnType();
                if (!retuenType.isVoidType()) {
                    if (retuenType.isArrayType()) {
                        String returnTypeName = retuenType.getArrayComponentType().getQualifiedName();
                        if (!typeTable.isSimpleType(returnTypeName)) {
                            generateSchema(retuenType.getArrayComponentType());
                        }
                    } else {
                        if (!typeTable.isSimpleType(retuenType.getQualifiedName())) {
                            generateSchema(retuenType);
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
            if (method.getSimpleName().equals("init"))
                continue;
            genereteWrapperElementforMethod(method);
        }
    }

    private void genereteWrapperElementforMethod(JMethod method) {
        String methodName = method.getSimpleName();
        XmlSchemaComplexType complexType = new XmlSchemaComplexType(schema);
        XmlSchemaSequence sequence = new XmlSchemaSequence();

        XmlSchemaElement eltOuter = new XmlSchemaElement();
        eltOuter.setName(methodName + METHOD_REQUEST_WRAPPER);
//        String complexTypeName = methodName + METHOD_REQUEST_WRAPPER;
//        complexType.setName(complexTypeName);
        schema.getItems().add(eltOuter);
//        schema.getItems().add(complexType);
//        eltOuter.setSchemaTypeName(complexType.getQName());
        eltOuter.setSchemaType(complexType);
        // adding this type to the table
        QName elementName = new QName(SchemaGenerator.SCHEMA_TARGET_NAMESPASE,
                eltOuter.getName(), SCHEMA_NAMESPASE_PRFIX);
        typeTable.addComplexScheam(methodName + METHOD_REQUEST_WRAPPER, elementName);

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
                classTypeName = methodParameter.getType().getArrayComponentType().getQualifiedName();
            }

            if (parameterNames != null) {
                paraName = parameterNames[j];
            }

            if (typeTable.isSimpleType(classTypeName)) {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName(paraName);
                elt1.setSchemaTypeName(typeTable.getSimpleSchemaTypeName(classTypeName));
                sequence.getItems().add(elt1);
                if (isArryType) {
                    elt1.setMaxOccurs(Long.MAX_VALUE);
                }
            } else {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName(paraName);
                elt1.setSchemaTypeName(typeTable.getComplexScheamType(classTypeName));
                sequence.getItems().add(elt1);
                if (isArryType) {
                    elt1.setMaxOccurs(Long.MAX_VALUE);
                }
            }
        }

        //generating wrapper element for retuen element
        JClass methodReturnType = method.getReturnType();
        generateWrapperforReturnType(methodReturnType, methodName);

    }

    private void generateWrapperforReturnType(JClass retuenType, String methodName) {
        if (!retuenType.isVoidType()) {
            XmlSchemaComplexType retuen_com_type = new XmlSchemaComplexType(schema);
            XmlSchemaElement ret_eltOuter = new XmlSchemaElement();
            ret_eltOuter.setName(methodName + METHOD_RESPONSE_WRAPPER);
            schema.getItems().add(ret_eltOuter);
            ret_eltOuter.setSchemaType(retuen_com_type);
            QName ret_comTypeName = new QName(SchemaGenerator.SCHEMA_TARGET_NAMESPASE,
                    ret_eltOuter.getName(), SCHEMA_NAMESPASE_PRFIX);
            typeTable.addComplexScheam(methodName + METHOD_RESPONSE_WRAPPER, ret_comTypeName);
            String classTypeName = retuenType.getQualifiedName();
            boolean isArryType = retuenType.isArrayType();
            XmlSchemaSequence sequence = new XmlSchemaSequence();
            retuen_com_type.setParticle(sequence);
            if (isArryType) {
                classTypeName = retuenType.getArrayComponentType().getQualifiedName();
            }
            if (typeTable.isSimpleType(classTypeName)) {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName("return");
                elt1.setSchemaTypeName(typeTable.getSimpleSchemaTypeName(classTypeName));
                sequence.getItems().add(elt1);
                if (isArryType) {
                    elt1.setMaxOccurs(Long.MAX_VALUE);
                }
            } else {
                XmlSchemaElement elt1 = new XmlSchemaElement();
                elt1.setName("return");
                elt1.setSchemaTypeName(typeTable.getComplexScheamType(classTypeName));
                sequence.getItems().add(elt1);
                if (isArryType) {
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
            QName elemntName = new QName(SCHEMA_TARGET_NAMESPASE, simpleName, SCHEMA_NAMESPASE_PRFIX);
            eltOuter.setName(simpleName);
            eltOuter.setQName(elemntName);
            complexType.setParticle(sequence);
            complexType.setName(simpleName);

            schema.getItems().add(eltOuter);
            schema.getItems().add(complexType);
            eltOuter.setSchemaTypeName(complexType.getQName());
//            System.out.println("QNAme: " + eltOuter.getQName().getPrefix());

            // adding this type to the table
            //  typeTable.addComplexScheam(name, complexType.getQName());
            typeTable.addComplexScheam(name, eltOuter.getQName());

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
                    elt1.setName(property.getSimpleName());
                    elt1.setSchemaTypeName(typeTable.getSimpleSchemaTypeName(propertyName));
                    sequence.getItems().add(elt1);
                    if (isArryType) {
                        elt1.setMaxOccurs(Long.MAX_VALUE);
//                        elt1.setMinOccurs(2);
                    }
                } else {
                    if (isArryType) {
                        generateSchema(property.getType().getArrayComponentType());
                    } else {
                        generateSchema(property.getType());
                    }
                    XmlSchemaElement elt1 = new XmlSchemaElement();
                    elt1.setName(property.getSimpleName());
                    elt1.setSchemaTypeName(typeTable.getComplexScheamType(propertyName));
                    sequence.getItems().add(elt1);
                    if (isArryType) {
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