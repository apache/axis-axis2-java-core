package org.apache.axis2.description.java2wsdl;

import org.apache.axis2.AxisFault;
import org.apache.axis2.deployment.util.Utils;
import org.apache.axis2.description.AxisMessage;
import org.apache.axis2.description.AxisOperation;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.java2wsdl.bytecode.MethodTable;
import org.apache.axis2.wsdl.WSDLConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
import org.apache.ws.commons.schema.utils.NamespacePrefixList;
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

public class DefaultSchemaGenerator implements Java2WSDLConstants, SchemaGenerator {

    private static final Log log = LogFactory.getLog(DefaultSchemaGenerator.class);

    public static final String NAME_SPACE_PREFIX = "ax2";// axis2 name space

    private static int prefixCount = 1;

    protected Map targetNamespacePrefixMap = new Hashtable();

    protected Map schemaMap = new Hashtable();

    protected XmlSchemaCollection xmlSchemaCollection = new XmlSchemaCollection();


    protected ClassLoader classLoader;

    protected String className;

    protected TypeTable typeTable = new TypeTable();

    // to keep loadded method using JAM
    protected JMethod methods[];

    //to store byte code method using Axis 1.x codes
    protected MethodTable methodTable;

    protected String schemaTargetNameSpace;

    protected String schema_namespace_prefix;

    protected String attrFormDefault = null;

    protected String elementFormDefault = null;

    protected ArrayList excludeMethods = new ArrayList();

    protected ArrayList extraClasses = null;

    protected boolean useWSDLTypesNamespace = false;

    protected Map pkg2nsmap = null;

    protected NamespaceGenerator nsGen = null;

    protected String targetNamespace = null;
    //to keep the list of operation which uses MR other than RPC MR
    protected ArrayList nonRpcMethods = new ArrayList();

    protected Class serviceClass = null;
    protected AxisService service;

    //To check whether we need to generate Schema element for Exception
    protected boolean generateBaseException ;

    public NamespaceGenerator getNsGen() throws Exception {
        if (nsGen == null) {
            nsGen = new DefaultNamespaceGenerator();
        }
        return nsGen;
    }

    public void setNsGen(NamespaceGenerator nsGen) {
        this.nsGen = nsGen;
    }

    public DefaultSchemaGenerator(ClassLoader loader, String className,
                                  String schematargetNamespace,
                                  String schematargetNamespacePrefix,
                                  AxisService service)
            throws Exception {
        this.classLoader = loader;
        this.className = className;
        this.service = service;

        serviceClass = Class.forName(className, true, loader);
        methodTable = new MethodTable(serviceClass);

        this.targetNamespace = Java2WSDLUtils.targetNamespaceFromClassName(
                className, loader, getNsGen()).toString();

        if (schematargetNamespace != null
                && schematargetNamespace.trim().length() != 0) {
            this.schemaTargetNameSpace = schematargetNamespace;
        } else {
            this.schemaTargetNameSpace =
                    Java2WSDLUtils.schemaNamespaceFromClassName(className, loader, getNsGen())
                            .toString();
        }

        if (schematargetNamespacePrefix != null
                && schematargetNamespacePrefix.trim().length() != 0) {
            this.schema_namespace_prefix = schematargetNamespacePrefix;
        } else {
            this.schema_namespace_prefix = SCHEMA_NAMESPACE_PRFIX;
        }
    }

    /**
     * Generates schema for all the parameters in method. First generates schema for all different
     * parameter type and later refers to them.
     *
     * @return Returns XmlSchema.
     * @throws Exception
     */
    public Collection generateSchema() throws Exception {

        JamServiceFactory factory = JamServiceFactory.getInstance();
        JamServiceParams jam_service_parms = factory.createServiceParams();
        //setting the classLoder
        //it can posible to add the classLoader as well
        jam_service_parms.addClassLoader(classLoader);
        jam_service_parms.includeClass(className);

        for (int count = 0; count < getExtraClasses().size(); ++count) {
            jam_service_parms.includeClass((String) getExtraClasses().get(count));
        }
        JamService service = factory.createService(jam_service_parms);
        QName extraSchemaTypeName;
        JamClassIterator jClassIter = service.getClasses();
        //all most all the time the ittr will have only one class in it
        while (jClassIter.hasNext()) {
            JClass jclass = (JClass) jClassIter.next();
            if (getQualifiedName(jclass).equals(className)) {
                /**
                 * Schema genertaion done in two stage 1. Load all the methods and
                 * create type for methods parameters (if the parameters are Bean
                 * then it will create Complex types for those , and if the
                 * parameters are simple type which decribe in SimpleTypeTable
                 * nothing will happen) 2. In the next stage for all the methods
                 * messages and port types will be creteated
                 */
                JAnnotation annotation = jclass.getAnnotation(AnnotationConstants.WEB_SERVICE);
                if (annotation != null) {
                    String tns =
                            annotation.getValue(AnnotationConstants.TARGETNAMESPACE).asString();
                    if (tns != null && !"".equals(tns)) {
                        targetNamespace = tns;
                    }
                }
                methods = processMethods(jclass.getDeclaredMethods());

            } else {
                //generate the schema type for extra classes
                extraSchemaTypeName = typeTable.getSimpleSchemaTypeName(getQualifiedName(jclass));
                if (extraSchemaTypeName == null) {
                    generateSchema(jclass);
                }
            }
        }
        return schemaMap.values();
    }

    protected JMethod[] processMethods(JMethod[] declaredMethods) throws Exception {
        ArrayList list = new ArrayList();
        //short the elements in the array
        Arrays.sort(declaredMethods);

        // since we do not support overload
        HashMap uniqueMethods = new HashMap();
        XmlSchemaComplexType methodSchemaType;
        XmlSchemaSequence sequence = null;

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
            if (excludeMethods.contains(methodName)) {
                continue;
            }

            if (uniqueMethods.get(methodName) != null) {
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
                    if (!generateBaseException) {
                        methodSchemaType = createSchemaTypeForMethodPart("Exception");
                        sequence = new XmlSchemaSequence();
                        QName schemaTypeName = typeTable.getSimpleSchemaTypeName(Exception.class.getName());
                        addContentToMethodSchemaType(sequence,
                                schemaTypeName,
                                "Exception",
                                false);
                        methodSchemaType.setParticle(sequence);
                        generateBaseException = true;
                    }
                    String partQname = extype.getSimpleName();
                    methodSchemaType = createSchemaTypeForMethodPart(partQname);
                    sequence = new XmlSchemaSequence();
                    if (Exception.class.getName().equals(extype.getQualifiedName())) {
                        addContentToMethodSchemaType(sequence,
                                typeTable.getComplexSchemaType("Exception"),
                                partQname,
                                false);
                        methodSchemaType.setParticle(sequence);
                        typeTable.addComplexSchema(Exception.class.getPackage().getName(),
                                methodSchemaType.getQName());
                    } else {
                        generateSchemaForType(sequence, extype, extype.getSimpleName());
                        methodSchemaType.setParticle(sequence);
                    }
                    if (AxisFault.class.getName().equals(extype.getQualifiedName())) {
                        continue;
                    }
                    AxisMessage faultMessage = new AxisMessage();
                    faultMessage.setName(extype.getSimpleName());
                    faultMessage.setElementQName(typeTable.getQNamefortheType(partQname));
                    axisOperation.setFaultMessages(faultMessage);
                }
            }
            uniqueMethods.put(methodName, jMethod);
            JParameter[] paras = jMethod.getParameters();
            String parameterNames[] = null;
            AxisMessage inMessage = axisOperation.getMessage(WSDLConstants.MESSAGE_LABEL_IN_VALUE);
            if (inMessage != null) {
                inMessage.setName(methodName + Java2WSDLConstants.MESSAGE_SUFFIX);
            }
            if (paras.length > 0) {
                parameterNames = methodTable.getParameterNames(methodName);
                sequence = new XmlSchemaSequence();

                methodSchemaType = createSchemaTypeForMethodPart(methodName);
                methodSchemaType.setParticle(sequence);
                inMessage.setElementQName(typeTable.getQNamefortheType(methodName));
                service.addMessageElementQNameToOperationMapping(methodSchemaType.getQName(),
                        axisOperation);
            }

            for (int j = 0; j < paras.length; j++) {
                JParameter methodParameter = paras[j];
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
                    break;
                } else {
                    generateSchemaForType(sequence, paraType, parameterName);
                }
            }
            // for its return type
            JClass returnType = jMethod.getReturnType();

            if (!returnType.isVoidType()) {
                String partQname = methodName + RESPONSE;
                methodSchemaType =
                        createSchemaTypeForMethodPart(partQname);
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
                if (nonRpcMethods.contains(getSimpleName(jMethod))) {
                    generateSchemaForType(sequence, null, returnName);
                } else {
                    generateSchemaForType(sequence, returnType, returnName);
                }
                AxisMessage outMessage = axisOperation.getMessage(
                        WSDLConstants.MESSAGE_LABEL_OUT_VALUE);
                outMessage.setElementQName(typeTable.getQNamefortheType(partQname));
                outMessage.setName(partQname);
                service.addMessageElementQNameToOperationMapping(methodSchemaType.getQName(),
                        axisOperation);
            }
            if (addToService) {
                service.addOperation(axisOperation);
            }
        }
        return (JMethod[]) list.toArray(new JMethod[list.size()]);
    }

    /**
     * JAM convert first name of an attribute into UpperCase as an example if there is a instance
     * variable called foo in a bean , then Jam give that as Foo so this method is to correct that
     * error
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

//            xmlSchema.getItems().add(eltOuter);
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
                String propertyName = getQualifiedName(property.getType());
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


    // moved code common to Fields & properties out of above method 
    protected void generateSchemaforFieldsandProperties(XmlSchema xmlSchema,
                                                        XmlSchemaSequence sequence, JClass type,
                                                        String name, boolean isArryType)
            throws Exception {

        String propertyName;

        if (isArryType) {
            propertyName = getQualifiedName(type.getArrayComponentType());
        } else
            propertyName = getQualifiedName(type);

        if (isArryType && "byte".equals(propertyName)) {
            propertyName = "base64Binary";
        }

        if (typeTable.isSimpleType(propertyName)) {
            XmlSchemaElement elt1 = new XmlSchemaElement();
            elt1.setName(name);
            elt1.setSchemaTypeName(typeTable.getSimpleSchemaTypeName(propertyName));
            sequence.getItems().add(elt1);

            if (isArryType && (!propertyName.equals("base64Binary"))) {
                elt1.setMaxOccurs(Long.MAX_VALUE);
            }
            elt1.setMinOccurs(0);
            if (!type.isPrimitiveType()) {
                elt1.setNillable(true);
            }
        } else {
            if (isArryType) {
                generateSchema(type.getArrayComponentType());
            } else {
                generateSchema(type);
            }
            XmlSchemaElement elt1 = new XmlSchemaElement();
            elt1.setName(name);
            elt1.setSchemaTypeName(typeTable.getComplexSchemaType(propertyName));
            sequence.getItems().add(elt1);
            if (isArryType) {
                elt1.setMaxOccurs(Long.MAX_VALUE);
            }
            elt1.setMinOccurs(0);
            elt1.setNillable(true);

            if (!((NamespaceMap) xmlSchema.getNamespaceContext()).values().
                    contains(typeTable.getComplexSchemaType(propertyName).getNamespaceURI())) {
                XmlSchemaImport importElement = new XmlSchemaImport();
                importElement.setNamespace(
                        typeTable.getComplexSchemaType(propertyName).getNamespaceURI());
                xmlSchema.getItems().add(importElement);
                ((NamespaceMap) xmlSchema.getNamespaceContext()).
                        put(generatePrefix(),
                                typeTable.getComplexSchemaType(propertyName).getNamespaceURI());
            }
        }


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
        if (schemaTypeName == null) {
            schemaTypeName = generateSchema(type);
            addContentToMethodSchemaType(sequence,
                    schemaTypeName,
                    partName,
                    isArrayType);
            String schemaNamespace;
            schemaNamespace = resolveSchemaNamespace(getQualifiedName(type.getContainingPackage()));
            addImport(getXmlSchema(schemaNamespace), schemaTypeName);

        } else {
            addContentToMethodSchemaType(sequence,
                    schemaTypeName,
                    partName,
                    isArrayType);
        }

        return schemaTypeName;
    }

    protected void addContentToMethodSchemaType(XmlSchemaSequence sequence,
                                                QName schemaTypeName,
                                                String paraName,
                                                boolean isArray) {
        XmlSchemaElement elt1 = new XmlSchemaElement();
        elt1.setName(paraName);
        elt1.setSchemaTypeName(schemaTypeName);
        if (sequence != null) {
            sequence.getItems().add(elt1);
        }

        if (isArray) {
            elt1.setMaxOccurs(Long.MAX_VALUE);
        }
        elt1.setMinOccurs(0);
        elt1.setNillable(true);
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

    protected XmlSchemaComplexType getComplexTypeForElement(XmlSchema xmlSchema, QName name) {
        Iterator iterator = xmlSchema.getItems().getIterator();
        while (iterator.hasNext()) {
            XmlSchemaObject object = (XmlSchemaObject) iterator.next();
            if (object instanceof XmlSchemaElement && ((XmlSchemaElement) object).getQName().equals(name)) {
                return (XmlSchemaComplexType) ((XmlSchemaElement) object).getSchemaType();
            }
        }
        return null;
    }

    private XmlSchema getXmlSchema(String targetNamespace) {
        XmlSchema xmlSchema;

        if ((xmlSchema = (XmlSchema) schemaMap.get(targetNamespace)) == null) {
            String targetNamespacePrefix = null;

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


    public TypeTable getTypeTable() {
        return typeTable;
    }

    public JMethod[] getMethods() {
        return methods;
    }

    protected String generatePrefix() {
        return NAME_SPACE_PREFIX + prefixCount++;
    }

    public void setExcludeMethods(ArrayList excludeMethods) {
        if (excludeMethods == null) excludeMethods = new ArrayList();
        this.excludeMethods = excludeMethods;
    }

    public String getSchemaTargetNameSpace() {
        return schemaTargetNameSpace;
    }

    protected void addImport(XmlSchema xmlSchema, QName schemaTypeName) {
        NamespacePrefixList map = xmlSchema.getNamespaceContext();
        if (map instanceof NamespaceMap && !((NamespaceMap) map).values().
                contains(schemaTypeName.getNamespaceURI())) {
            XmlSchemaImport importElement = new XmlSchemaImport();
            importElement.setNamespace(schemaTypeName.getNamespaceURI());
            xmlSchema.getItems().add(importElement);
            ((NamespaceMap) xmlSchema.getNamespaceContext()).
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

    protected XmlSchemaForm getAttrFormDefaultSetting() {
        if (FORM_DEFAULT_UNQUALIFIED.equals(getAttrFormDefault())) {
            return new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        } else {
            return new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
        }
    }

    protected XmlSchemaForm getElementFormDefaultSetting() {
        if (FORM_DEFAULT_UNQUALIFIED.equals(getElementFormDefault())) {
            return new XmlSchemaForm(XmlSchemaForm.UNQUALIFIED);
        } else {
            return new XmlSchemaForm(XmlSchemaForm.QUALIFIED);
        }
    }

    public ArrayList getExtraClasses() {
        if (extraClasses == null) {
            extraClasses = new ArrayList();
        }
        return extraClasses;
    }

    public void setExtraClasses(ArrayList extraClasses) {
        this.extraClasses = extraClasses;
    }

    protected String resolveSchemaNamespace(String packageName) throws Exception {
        //if all types must go into the wsdl types schema namespace
        if (useWSDLTypesNamespace) {
            //return schemaTargetNameSpace;
            return (String) pkg2nsmap.get("all");
        } else {
            if (pkg2nsmap != null && !pkg2nsmap.isEmpty()) {
                //if types should go into namespaces that are mapped against the package name for the type
                if (pkg2nsmap.get(packageName) != null) {
                    //return that mapping
                    return (String) pkg2nsmap.get(packageName);
                } else {
                    return getNsGen().schemaNamespaceFromPackageName(packageName).toString();
                }
            } else {
                // if  pkg2nsmap is null and if not default schema ns found for the custom bean
                return getNsGen().schemaNamespaceFromPackageName(packageName).toString();
            }
        }
    }

    public boolean isUseWSDLTypesNamespace() {
        return useWSDLTypesNamespace;
    }

    public void setUseWSDLTypesNamespace(boolean useWSDLTypesNamespace) {
        this.useWSDLTypesNamespace = useWSDLTypesNamespace;
    }

    public Map getPkg2nsmap() {
        return pkg2nsmap;
    }

    public void setPkg2nsmap(Map pkg2nsmap) {
        this.pkg2nsmap = pkg2nsmap;
    }

    public String getTargetNamespace() {
        return targetNamespace;
    }

    protected String getSimpleName(JMethod method) {
        return method.getSimpleName();
    }

    protected String getSimpleName(JClass type) {
        return type.getSimpleName();
    }

    protected String getSimpleName(JProperty peroperty) {
        return peroperty.getSimpleName();
    }

    protected String getSimpleName(JParameter parameter) {
        return parameter.getSimpleName();
    }

    protected String getQualifiedName(JMethod method) {
        return method.getQualifiedName();
    }

    protected String getQualifiedName(JClass type) {
        return type.getQualifiedName();
    }

    protected String getQualifiedName(JProperty peroperty) {
        return peroperty.getQualifiedName();
    }

    protected String getQualifiedName(JParameter parameter) {
        return parameter.getQualifiedName();
    }

    protected String getQualifiedName(JPackage packagez) {
        return packagez.getQualifiedName();
    }

    public void setNonRpcMethods(ArrayList nonRpcMethods) {
        if (nonRpcMethods != null) {
            this.nonRpcMethods = nonRpcMethods;
        }
    }

    public void setAxisService(AxisService service) {
        this.service = service;
    }
}
