package org.apache.axis2.description.java2wsdl;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
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

public class DocLitBareSchemaGenerator extends DefaultSchemaGenerator {

    private static final Log log = LogFactory.getLog(DocLitBareSchemaGenerator.class);
    private HashMap processedParameters = new HashMap();

    public DocLitBareSchemaGenerator(ClassLoader loader,
                                     String className,
                                     String schematargetNamespace,
                                     String schematargetNamespacePrefix,
                                     AxisService service) throws Exception {
        super(loader, className, schematargetNamespace,
                schematargetNamespacePrefix, service);
    }

    protected JMethod[] processMethods(JMethod[] declaredMethods) throws Exception {
        ArrayList list = new ArrayList();
        //short the elements in the array
        Arrays.sort(declaredMethods);

        // since we do not support overload
        HashMap uniqueMethods = new HashMap();
        XmlSchemaComplexType methodSchemaType;
        XmlSchemaSequence sequence;

        for (int i = 0; i < declaredMethods.length; i++) {
            JMethod jMethod = declaredMethods[i];
            JAnnotation methodAnnon = jMethod.getAnnotation(AnnotationConstants.WEB_METHOD);
            if (methodAnnon != null) {
                if (methodAnnon.getValue(AnnotationConstants.EXCLUDE).asBoolean()) {
                    continue;
                }
            }
            String methodName = getSimpleName(jMethod);
            // no need to think abt this method , since that is system
            // config method
            if (excludeMethods.contains(getSimpleName(jMethod))) {
                continue;
            }

            if (uniqueMethods.get(getSimpleName(jMethod)) != null) {
                log.warn("We don't support method overloading. Ignoring [" +
                        jMethod.getQualifiedName() + "]");
                continue;
            }

            if (!jMethod.isPublic()) {
                // no need to generate Schema for non public methods
                continue;
            }

            boolean addToService = false;
            AxisOperation axisOperation = service.getOperation(new QName(methodName));
            if (axisOperation == null) {
                axisOperation = Utils.getAxisOperationForJmethod(jMethod);
                addToService = true;
            }

            // Maintain a list of methods we actually work with
            list.add(jMethod);

            if (jMethod.getExceptionTypes().length > 0) {
                JClass[] extypes = jMethod.getExceptionTypes();
                for (int j = 0; j < extypes.length; j++) {
                    JClass extype = extypes[j];
                    if (AxisFault.class.getName().equals(extype.getQualifiedName())) {
                        continue;
                    }
                    String partQname = extype.getSimpleName() + "Fault";
                    methodSchemaType = createSchemaTypeForMethodPart(partQname);
                    sequence = new XmlSchemaSequence();
                    generateSchemaForType(sequence, extype, extype.getSimpleName());
                    methodSchemaType.setParticle(sequence);
                    if (AxisFault.class.getName().equals(extype.getQualifiedName())) {
                        continue;
                    }
                    AxisMessage faultMessage = new AxisMessage();
                    if (extypes.length > 1) {
                        faultMessage.setName(methodName + "Fault" + j);
                    } else {
                        faultMessage.setName(methodName + "Fault");
                    }
                    faultMessage.setElementQName(typeTable.getQNamefortheType(partQname));
                    axisOperation.setFaultMessages(faultMessage);
                }
            }
            uniqueMethods.put(getSimpleName(jMethod), jMethod);
            //create the schema type for the method wrapper

            uniqueMethods.put(getSimpleName(jMethod), jMethod);
            JParameter[] paras = jMethod.getParameters();
            String parameterNames[] = methodTable.getParameterNames(methodName);
            AxisMessage inMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMessage != null) {
                inMessage.setName(methodName + "RequestMessage");
            }
            if (paras.length > 1) {
                sequence = new XmlSchemaSequence();
                methodSchemaType = createSchemaTypeForMethodPart(getSimpleName(jMethod));
                methodSchemaType.setParticle(sequence);
                inMessage.setElementQName(typeTable.getQNamefortheType(methodName));
                service.addMessageElementQNameToOperationMapping(methodSchemaType.getQName(),
                        axisOperation);
                inMessage.setPartName(methodName);
                for (int j = 0; j < paras.length; j++) {
                    JParameter methodParameter = paras[j];
                    if (generateRequestSchema(methodParameter, parameterNames, j, jMethod, sequence)) {
                        break;
                    }
                }
            } else if (paras.length == 1) {
                if (paras[0].getType().isArrayType()) {
                    sequence = new XmlSchemaSequence();

                    methodSchemaType = createSchemaTypeForMethodPart(methodName);
                    methodSchemaType.setParticle(sequence);
                    JParameter methodParameter = paras[0];
                    inMessage.setElementQName(typeTable.getQNamefortheType(methodName));
                    service.addMessageElementQNameToOperationMapping(methodSchemaType.getQName(),
                            axisOperation);
                    inMessage.setPartName(methodName);
                    if (generateRequestSchema(methodParameter, parameterNames, 0, jMethod, sequence)) {
                        break;
                    }
                } else {
                    String parameterName = null;
                    JParameter methodParameter = paras[0];
                    JAnnotation paramterAnnon =
                            methodParameter.getAnnotation(AnnotationConstants.WEB_PARAM);
                    if (paramterAnnon != null) {
                        parameterName =
                                paramterAnnon.getValue(AnnotationConstants.NAME).asString();
                    }
                    if (parameterName == null || "".equals(parameterName)) {
                        parameterName = (parameterNames != null && parameterNames[0] != null) ?
                                parameterNames[0] : getSimpleName(methodParameter);
                    }
                    JMethod processMethod = (JMethod) processedParameters.get(parameterName);
                    if (processMethod != null) {
                        throw new AxisFault("Inavalid Java class," +
                                " there are two methods [" + processMethod.getSimpleName() + " and " +
                                jMethod.getSimpleName() + " ]which have the same parameter names");
                    } else {
                        processedParameters.put(parameterName, jMethod);
                        generateSchemaForType(null, paras[0].getType(), parameterName);
                        inMessage.setElementQName(typeTable.getQNamefortheType(parameterName));
                        inMessage.setPartName(parameterName);
                        service.addMessageElementQNameToOperationMapping(typeTable.getQNamefortheType(parameterName),
                                axisOperation);
                    }
                }
            }

            // for its return type
            JClass returnType = jMethod.getReturnType();

            if (!returnType.isVoidType()) {
                AxisMessage outMessage = axisOperation.getMessage(
                        WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                if (returnType.isArrayType()) {
                    methodSchemaType =
                            createSchemaTypeForMethodPart(getSimpleName(jMethod) + RESULT);
                    sequence = new XmlSchemaSequence();
                    methodSchemaType.setParticle(sequence);
                    JAnnotation returnAnnon =
                            jMethod.getAnnotation(AnnotationConstants.WEB_RESULT);
                    String returnName = "return";
                    if (returnAnnon != null) {
                        returnName = returnAnnon.getValue(AnnotationConstants.NAME).asString();
                        if (returnName != null && !"".equals(returnName)) {
                            returnName = "return";
                        }
                    }
                    if (nonRpcMethods.contains(methodName)) {
                        generateSchemaForType(sequence, null, returnName);
                    } else {
                        generateSchemaForType(sequence, returnType, returnName);
                    }
                } else {
                    generateSchemaForType(null, returnType, methodName + RESULT);

                }
                outMessage.setElementQName(typeTable.getQNamefortheType(methodName + RESULT));
                outMessage.setName(methodName + "ResponseMessage");
                outMessage.setPartName(methodName + RESULT);
                service.addMessageElementQNameToOperationMapping(
                        typeTable.getQNamefortheType(methodName + RESULT),
                        axisOperation);
            }
            if (addToService) {
                service.addOperation(axisOperation);
            }
        }
        return (JMethod[]) list.toArray(new JMethod[list.size()]);
    }

    private boolean generateRequestSchema(JParameter methodParameter,
                                          String[] parameterNames,
                                          int j,
                                          JMethod jMethod,
                                          XmlSchemaSequence sequence) throws Exception {
        String parameterName = null;
        JAnnotation paramterAnnon =
                methodParameter.getAnnotation(AnnotationConstants.WEB_PARAM);
        if (paramterAnnon != null) {
            parameterName =
                    paramterAnnon.getValue(AnnotationConstants.NAME).asString();
        }
        if (parameterName == null || "".equals(parameterName)) {
            parameterName = (parameterNames != null && parameterNames[j] != null) ?
                    parameterNames[j] : getSimpleName(methodParameter);
        }
        JClass paraType = methodParameter.getType();
        if (nonRpcMethods.contains(getSimpleName(jMethod))) {
            generateSchemaForType(sequence, null, getSimpleName(jMethod));
            return true;
        } else {
            generateSchemaForType(sequence, paraType, parameterName);
        }
        return false;
    }

    private QName generateSchemaForType(XmlSchemaSequence sequence, JClass type, String partName)
            throws Exception {

        boolean isArrayType = false;
        if (type != null) {
            isArrayType = type.isArrayType();
        }
        if (isArrayType) {
            type = type.getArrayComponentType();
        }
        if (AxisFault.class.getName().equals(type)) {
            return null;
        }
        String classTypeName;
        if (type == null) {
            classTypeName = "java.lang.Object";
        } else {
            classTypeName = getQualifiedName(type);
        }
        if (isArrayType && "byte".equals(classTypeName)) {
            classTypeName = "base64Binary";
            isArrayType = false;
        }
        if ("javax.activation.DataHandler".equals(classTypeName)) {
            classTypeName = "base64Binary";
        }
        QName schemaTypeName = typeTable.getSimpleSchemaTypeName(classTypeName);
        if (schemaTypeName == null && type != null) {
            schemaTypeName = generateSchema(type);
            addContentToMethodSchemaType(sequence,
                    schemaTypeName,
                    partName,
                    isArrayType);
            String schemaNamespace = resolveSchemaNamespace(getQualifiedName(
                    type.getContainingPackage()));
            addImport(getXmlSchema(schemaNamespace), schemaTypeName);

        } else {
            if (sequence == null) {
                generateSchemaForSingleElement(schemaTypeName, partName, isArrayType, type);
            } else {
                addContentToMethodSchemaType(sequence,
                        schemaTypeName,
                        partName,
                        isArrayType);
            }
        }

        return schemaTypeName;
    }

    protected void generateSchemaForSingleElement(QName schemaTypeName,
                                                  String paraName,
                                                  boolean isArray,
                                                  JClass javaType) throws Exception {
        XmlSchemaElement elt1 = new XmlSchemaElement();
        elt1.setName(paraName);
        elt1.setSchemaTypeName(schemaTypeName);
        elt1.setNillable(true);
        QName elementName =
                new QName(schemaTargetNameSpace, paraName, schema_namespace_prefix);
        elt1.setQName(elementName);
        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        xmlSchema.getElements().add(elementName, elt1);
        xmlSchema.getItems().add(elt1);
        typeTable.addComplexSchema(paraName, elementName);
    }

    /**
     * Generate schema construct for given type
     *
     * @param javaType
     */
    private QName generateSchema(JClass javaType) throws Exception {
        String name = getQualifiedName(javaType);
        QName schemaTypeName = typeTable.getComplexSchemaType(name);
        if (schemaTypeName == null) {
            String simpleName = getSimpleName(javaType);

            String packageName = getQualifiedName(javaType.getContainingPackage());
            String targetNameSpace = resolveSchemaNamespace(packageName);

            XmlSchema xmlSchema = getXmlSchema(targetNameSpace);
            String targetNamespacePrefix = (String) targetNamespacePrefixMap.get(targetNameSpace);
            if (targetNamespacePrefix == null) {
                targetNamespacePrefix = generatePrefix();
                targetNamespacePrefixMap.put(targetNameSpace, targetNamespacePrefix);
            }

            XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);
            XmlSchemaSequence sequence = new XmlSchemaSequence();
            XmlSchemaComplexContentExtension complexExtension =
                    new XmlSchemaComplexContentExtension();

            XmlSchemaElement eltOuter = new XmlSchemaElement();
            schemaTypeName = new QName(targetNameSpace, simpleName, targetNamespacePrefix);
            eltOuter.setName(simpleName);
            eltOuter.setQName(schemaTypeName);

            JClass sup = javaType.getSuperclass();

            if ((sup != null) && !("java.lang.Object".compareTo(sup.getQualifiedName()) == 0) &&
                    !("org.apache.axis2".compareTo(sup.getContainingPackage().getQualifiedName()) == 0)) {
                String superClassName = sup.getQualifiedName();
                String superclassname = getSimpleName(sup);
                String tgtNamespace;
                String tgtNamespacepfx;
                QName qName = typeTable.getSimpleSchemaTypeName(superClassName);
                if (qName != null) {
                    tgtNamespace = qName.getNamespaceURI();
                    tgtNamespacepfx = qName.getPrefix();
                } else {
                    tgtNamespace =
                            resolveSchemaNamespace(sup.getContainingPackage().getQualifiedName());
                    tgtNamespacepfx = (String) targetNamespacePrefixMap.get(tgtNamespace);
                    generateSchema(sup);
                }

                if (tgtNamespacepfx == null) {
                    tgtNamespacepfx = generatePrefix();
                    targetNamespacePrefixMap.put(tgtNamespace, tgtNamespacepfx);
                }

                QName basetype = new QName(tgtNamespace, superclassname, tgtNamespacepfx);


                complexExtension.setBaseTypeName(basetype);
                complexExtension.setParticle(sequence);

                XmlSchemaComplexContent contentModel = new XmlSchemaComplexContent();

                contentModel.setContent(complexExtension);

                complexType.setContentModel(contentModel);

            } else {
                complexType.setParticle(sequence);
            }

            complexType.setName(simpleName);

            xmlSchema.getItems().add(eltOuter);
            xmlSchema.getElements().add(schemaTypeName, eltOuter);
            eltOuter.setSchemaTypeName(complexType.getQName());

            xmlSchema.getItems().add(complexType);
            xmlSchema.getSchemaTypes().add(schemaTypeName, complexType);

            // adding this type to the table
            typeTable.addComplexSchema(name, eltOuter.getQName());
            // adding this type's package to the table, to support inheritance.
            typeTable.addComplexSchema(javaType.getContainingPackage().getQualifiedName(),
                    eltOuter.getQName());


            Set propertiesSet = new HashSet();
            Set propertiesNames = new HashSet();

            JProperty[] tempProperties = javaType.getDeclaredProperties();
            for (int i = 0; i < tempProperties.length; i++) {
                propertiesSet.add(tempProperties[i]);
            }

            JProperty[] properties = (JProperty[]) propertiesSet.toArray(new JProperty[0]);
            Arrays.sort(properties);
            for (int i = 0; i < properties.length; i++) {
                JProperty property = properties[i];
                boolean isArryType = property.getType().isArrayType();

                String propname = getCorrectName(property.getSimpleName());

                propertiesNames.add(propname);

                this.generateSchemaforFieldsandProperties(xmlSchema, sequence, property.getType(),
                        propname, isArryType);

            }

            JField[] tempFields = javaType.getDeclaredFields();
            HashMap FieldMap = new HashMap();


            for (int i = 0; i < tempFields.length; i++) {
                // create a element for the field only if it is public
                // and there is no property with the same name

                if (tempFields[i].isPublic()) {

                    // skip field with same name as a property
                    if (!propertiesNames.contains(tempFields[i].getSimpleName())) {

                        FieldMap.put(tempFields[i].getSimpleName(), tempFields[i]);
                    }
                }

            }

            // remove fields from super classes patch for defect Annogen-21
            // getDeclaredFields is incorrectly returning fields of super classes as well
            // getDeclaredProperties used earlier works correctly
            JClass supr = javaType.getSuperclass();
            while (supr != null && supr.getQualifiedName().compareTo("java.lang.Object") != 0) {
                JField[] suprFields = supr.getFields();
                for (int i = 0; i < suprFields.length; i++) {
                    FieldMap.remove(suprFields[i].getSimpleName());
                }
                supr = supr.getSuperclass();
            }
            // end patch for Annogen -21

            JField[] froperties = (JField[]) FieldMap.values().toArray(new JField[0]);
            Arrays.sort(froperties);

            for (int i = 0; i < froperties.length; i++) {
                JField field = froperties[i];
                boolean isArryType = field.getType().isArrayType();

                this.generateSchemaforFieldsandProperties(xmlSchema, sequence, field.getType(),
                        field.getSimpleName(), isArryType);
            }


        }
        return schemaTypeName;
    }


    private XmlSchemaComplexType createSchemaTypeForMethodPart(String localPartName) {
        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        QName elementName =
                new QName(this.schemaTargetNameSpace, localPartName, this.schema_namespace_prefix);

        XmlSchemaComplexType complexType = getComplexTypeForElement(xmlSchema, elementName);
        if (complexType == null) {
            complexType = new XmlSchemaComplexType(xmlSchema);

            XmlSchemaElement globalElement = new XmlSchemaElement();
            globalElement.setSchemaType(complexType);
            globalElement.setName(localPartName);
            globalElement.setQName(elementName);
            xmlSchema.getItems().add(globalElement);
            xmlSchema.getElements().add(elementName, globalElement);
        }
        typeTable.addComplexSchema(localPartName, elementName);

        return complexType;
    }

    private XmlSchema getXmlSchema(String targetNamespace) {
        XmlSchema xmlSchema;

        if ((xmlSchema = (XmlSchema) schemaMap.get(targetNamespace)) == null) {
            String targetNamespacePrefix;

            if (targetNamespace.equals(schemaTargetNameSpace) &&
                    schema_namespace_prefix != null) {
                targetNamespacePrefix = schema_namespace_prefix;
            } else {
                targetNamespacePrefix = generatePrefix();
            }


            xmlSchema = new XmlSchema(targetNamespace, xmlSchemaCollection);
            xmlSchema.setAttributeFormDefault(getAttrFormDefaultSetting());
            xmlSchema.setElementFormDefault(getElementFormDefaultSetting());


            targetNamespacePrefixMap.put(targetNamespace, targetNamespacePrefix);
            schemaMap.put(targetNamespace, xmlSchema);

            NamespaceMap prefixmap = new NamespaceMap();
            prefixmap.put(DEFAULT_SCHEMA_NAMESPACE_PREFIX, URI_2001_SCHEMA_XSD);
            prefixmap.put(targetNamespacePrefix, targetNamespace);
            xmlSchema.setNamespaceContext(prefixmap);
        }
        return xmlSchema;
    }

}
