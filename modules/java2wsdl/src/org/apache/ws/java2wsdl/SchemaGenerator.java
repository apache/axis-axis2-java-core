package org.apache.ws.java2wsdl;

import org.apache.ws.commons.schema.*;
import org.apache.ws.commons.schema.utils.NamespaceMap;
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

    public static final String NAME_SPACE_PREFIX = "ax2";// axis2 name space

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

    private ArrayList extraClasses = null;

    private boolean useWSDLTypesNamespace = false;

    private Map pkg2nsmap = null;

    private NamespaceGenerator nsGen = null;

    private String targetNamespace = null;
    //to keep the list of operation which uses MR other than RPC MR
    private ArrayList nonRpcMethods = new ArrayList();


    public NamespaceGenerator getNsGen() throws Exception {
        if ( nsGen == null ) {
            nsGen = new DefaultNamespaceGenerator();
        }
        return nsGen;
    }

    public void setNsGen(NamespaceGenerator nsGen) {
        this.nsGen = nsGen;
    }

    public SchemaGenerator(ClassLoader loader, String className,
                           String schematargetNamespace,
                           String schematargetNamespacePrefix)
            throws Exception {
        this.classLoader = loader;
        this.className = className;

        Class clazz =  Class.forName(className, true, loader);
        methodTable = new MethodTable(clazz);

        this.targetNamespace = Java2WSDLUtils.targetNamespaceFromClassName(
                className, loader, getNsGen()).toString();
        
        if (schematargetNamespace != null
                && schematargetNamespace.trim().length() != 0) {
            this.schemaTargetNameSpace = schematargetNamespace;
        } else {
            this.schemaTargetNameSpace =
                    Java2WSDLUtils.schemaNamespaceFromClassName(className, loader, getNsGen()).toString();
        }
        
        if (schematargetNamespacePrefix != null
                && schematargetNamespacePrefix.trim().length() != 0) {
            this.schema_namespace_prefix = schematargetNamespacePrefix;
        } else {
            this.schema_namespace_prefix = SCHEMA_NAMESPACE_PRFIX;
        }
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
                if(annotation!=null){
                    String tns = annotation.getValue(AnnotationConstants.TARGETNAMESPACE).asString();
                    if(tns!=null&&!"".equals(tns)){
                        targetNamespace = tns;
                    }
                }
                methods = jclass.getDeclaredMethods();
                //short the elements in the array
                Arrays.sort(methods);

                // since we do not support overload
                HashMap uniqueMethods = new HashMap();
                XmlSchemaComplexType methodSchemaType;
                XmlSchemaSequence sequence = null;

                for (int i = 0; i < methods.length; i++) {
                    JMethod jMethod = methods[i];
                    JAnnotation methodAnnon= jMethod.getAnnotation(AnnotationConstants.WEB_METHOD);
                    if(methodAnnon!=null){
                        if(methodAnnon.getValue(AnnotationConstants.EXCLUDE).asBoolean()){
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
                        throw new Exception(
                                " Sorry we don't support methods overloading !!!! ");
                    }

                    if (!jMethod.isPublic()) {
                        // no need to generate Schema for non public methods
                        continue;
                    }
                    if (jMethod.getExceptionTypes().length > 0) {
                        methodSchemaType = createSchemaTypeForMethodPart(getSimpleName(jMethod) + "Fault");
                        sequence = new XmlSchemaSequence();
                        XmlSchemaElement elt1 = new XmlSchemaElement();
                        elt1.setName(getSimpleName(jMethod) + "Fault");
                        elt1.setSchemaTypeName(typeTable.getQNamefortheType(Object.class.getName()));
                        sequence.getItems().add(elt1);
                        methodSchemaType.setParticle(sequence);
                    }
                    uniqueMethods.put(getSimpleName(jMethod), jMethod);
                    //create the schema type for the method wrapper

                    uniqueMethods.put(getSimpleName(jMethod), jMethod);
                    JParameter [] paras = jMethod.getParameters();
                    String parameterNames [] = null;
                    if (paras.length > 0) {
                        parameterNames = methodTable.getParameterNames(methodName);
                        sequence = new XmlSchemaSequence();

                        methodSchemaType = createSchemaTypeForMethodPart(getSimpleName(jMethod));
                        methodSchemaType.setParticle(sequence);
                    }

                    for (int j = 0; j < paras.length; j++) {
                        JParameter methodParameter = paras[j];
                        String parameterName =null;
                        JAnnotation paramterAnnon= methodParameter.getAnnotation(AnnotationConstants.WEB_PARAM);
                        if(paramterAnnon!=null){
                            parameterName = paramterAnnon.getValue(AnnotationConstants.NAME).asString();
                        }
                        if(parameterName==null||"".equals(parameterName)){
                            parameterName = (parameterNames != null && parameterNames[j] != null) ? parameterNames[j] : getSimpleName(methodParameter);
                        }
                        JClass paraType = methodParameter.getType();
                        if(nonRpcMethods.contains(getSimpleName(jMethod))){
                            generateSchemaForType(sequence, null,getSimpleName(jMethod));
                            break;
                        } else {
                            generateSchemaForType(sequence, paraType,parameterName);
                        }
                    }
                    // for its return type
                    JClass returnType = jMethod.getReturnType();

                    if (!returnType.isVoidType()) {
                        methodSchemaType = createSchemaTypeForMethodPart(getSimpleName(jMethod) + RESPONSE);
                        sequence = new XmlSchemaSequence();
                        methodSchemaType.setParticle(sequence);
                        JAnnotation returnAnnon= jMethod.getAnnotation(AnnotationConstants.WEB_RESULT);
                        String returnName ="return";
                        if(returnAnnon!=null){
                            returnName= returnAnnon.getValue(AnnotationConstants.NAME).asString();
                            if(returnName!=null&&!"".equals(returnName)){
                                returnName ="return";
                            }
                        }
                        if(nonRpcMethods.contains(getSimpleName(jMethod))){
                            generateSchemaForType(sequence, null, returnName);
                        } else {
                            generateSchemaForType(sequence, returnType, returnName);
                        }

                    }
                }
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
    private QName generateSchema(JClass javaType) throws Exception {
        String name = getQualifiedName(javaType);
        QName schemaTypeName = typeTable.getComplexSchemaType(name);
        if (schemaTypeName == null) {
            String simpleName =  getSimpleName(javaType);

            String packageName = getQualifiedName(javaType.getContainingPackage());
            String targetNameSpace = resolveSchemaNamespace(packageName);

            XmlSchema xmlSchema = getXmlSchema(targetNameSpace);
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

	    JClass tempClass = javaType;
	    Set propertiesSet = new HashSet();
	    while (tempClass != null && !"java.lang.Object".equals(getQualifiedName(tempClass))) {
		JProperty[] tempProperties = tempClass.getDeclaredProperties();
		for (int i = 0; i < tempProperties.length; i++) {
		    propertiesSet.add(tempProperties[i]);
		}
		tempClass = tempClass.getSuperclass();
	    }
	    JProperty[] properties = (JProperty[]) propertiesSet.toArray(new JProperty[0]);
            Arrays.sort(properties);
            for (int i = 0; i < properties.length; i++) {
                JProperty property = properties[i];
                String propertyName = getQualifiedName(property.getType());
                boolean isArryType = property.getType().isArrayType();
                if (isArryType) {
                    propertyName = getQualifiedName(property.getType().getArrayComponentType());
                }
                if (typeTable.isSimpleType(propertyName)) {
                    XmlSchemaElement elt1 = new XmlSchemaElement();
                    elt1.setName(getCorrectName(getSimpleName(property)));
                    elt1.setSchemaTypeName(typeTable.getSimpleSchemaTypeName(propertyName));
                    sequence.getItems().add(elt1);
                    if (isArryType) {
                        elt1.setMaxOccurs(Long.MAX_VALUE);
                        elt1.setMinOccurs(1);
                    }
                    if (String.class.getName().equals(propertyName)) {
                        elt1.setNillable(true);
                    }
                } else {
                    if (isArryType) {
                        generateSchema(property.getType().getArrayComponentType());
                    } else {
                        generateSchema(property.getType());
                    }
                    XmlSchemaElement elt1 = new XmlSchemaElement();
                    elt1.setName(getCorrectName(getSimpleName(property)));
                    elt1.setSchemaTypeName(typeTable.getComplexSchemaType(propertyName));
                    sequence.getItems().add(elt1);
                    if (isArryType) {
                        elt1.setMaxOccurs(Long.MAX_VALUE);
                        elt1.setMinOccurs(1);
                    }
                    elt1.setNillable(true);

                    if (!((NamespaceMap) xmlSchema.getNamespaceContext()).values().
                            contains(typeTable.getComplexSchemaType(propertyName).getNamespaceURI())) {
                        XmlSchemaImport importElement = new XmlSchemaImport();
                        importElement.setNamespace(typeTable.getComplexSchemaType(propertyName).getNamespaceURI());
                        xmlSchema.getItems().add(importElement);
                        ((NamespaceMap) xmlSchema.getNamespaceContext()).
                                put(generatePrefix(), typeTable.getComplexSchemaType(propertyName).getNamespaceURI());
                    }
                }
            }
        }
        return schemaTypeName;
    }

    private QName generateSchemaForType(XmlSchemaSequence sequence, JClass type, String partName) throws Exception {

        boolean isArrayType = false;
        if(type!=null){
            isArrayType = type.isArrayType();
        }
        if (isArrayType) {
            type = type.getArrayComponentType();
        }
        String classTypeName;
        if(type==null){
            classTypeName = "java.lang.Object";
        } else {
            classTypeName = getQualifiedName(type);
        }
        if (isArrayType && "byte".equals(classTypeName)) {
            classTypeName = "base64Binary";
            isArrayType = false;
        }

        QName schemaTypeName = typeTable.getSimpleSchemaTypeName(classTypeName);
        if (schemaTypeName == null) {
            schemaTypeName = generateSchema(type);
            addContentToMethodSchemaType(sequence,
                    schemaTypeName,
                    partName,
                    isArrayType);
            //addImport((XmlSchema)schemaMap.get(schemaTargetNameSpace), schemaTypeName);
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
        elt1.setNillable(true);
    }

    private XmlSchemaComplexType createSchemaTypeForMethodPart(String localPartName) {
        //XmlSchema xmlSchema = (XmlSchema)schemaMap.get(schemaTargetNameSpace);
        XmlSchema xmlSchema = getXmlSchema(schemaTargetNameSpace);
        QName elementName = new QName(this.schemaTargetNameSpace, localPartName, this.schema_namespace_prefix);
        XmlSchemaComplexType complexType = new XmlSchemaComplexType(xmlSchema);

        XmlSchemaElement globalElement = new XmlSchemaElement();
        globalElement.setSchemaType(complexType);
//        globalElement.setName(formGlobalElementName(localPartName));
        globalElement.setName(localPartName);
        globalElement.setQName(elementName);
        xmlSchema.getItems().add(globalElement);
        xmlSchema.getElements().add(elementName, globalElement);

        typeTable.addComplexSchema(localPartName, elementName);

        return complexType;
    }

    private XmlSchema getXmlSchema(String targetNamespace) {
        XmlSchema xmlSchema;

        if ((xmlSchema = (XmlSchema) schemaMap.get(targetNamespace)) == null) {
            String targetNamespacePrefix = null;
            
            if ( targetNamespace.equals(schemaTargetNameSpace) && 
                    schema_namespace_prefix != null ) {
                targetNamespacePrefix = schema_namespace_prefix;
            }
            else {
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
        if (!((NamespaceMap) xmlSchema.getNamespaceContext()).values().
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

    public ArrayList getExtraClasses() {
        if (extraClasses == null) {
            extraClasses = new ArrayList();
        }
        return extraClasses;
    }

    public void setExtraClasses(ArrayList extraClasses) {
        this.extraClasses = extraClasses;
    }

    private String resolveSchemaNamespace(String packageName) throws Exception {
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

   protected String getSimpleName(JMethod method){
       return method.getSimpleName();
   }
    protected String getSimpleName(JClass type){
       return type.getSimpleName();
   }
    protected String getSimpleName(JProperty peroperty){
        return peroperty.getSimpleName();
    }
    protected String getSimpleName(JParameter parameter){
        return parameter.getSimpleName();
   }

    protected String getQualifiedName(JMethod method){
        return method.getQualifiedName();
    }
    protected String getQualifiedName(JClass type){
        return type.getQualifiedName();
    }
    protected String getQualifiedName(JProperty peroperty){
        return peroperty.getQualifiedName();
    }
    protected String getQualifiedName(JParameter parameter){
        return parameter.getQualifiedName();
    }
    protected String getQualifiedName(JPackage packagez){
        return packagez.getQualifiedName();
    }
    public void setNonRpcMethods(ArrayList nonRpcMethods) {
        if(nonRpcMethods!=null){
            this.nonRpcMethods = nonRpcMethods;
        }
    }
}
