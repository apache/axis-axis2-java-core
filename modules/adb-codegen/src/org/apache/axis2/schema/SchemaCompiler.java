package org.apache.axis2.schema;

import org.apache.axis2.namespace.Constants;
import org.apache.axis2.schema.i18n.SchemaCompilerMessages;
import org.apache.axis2.schema.util.SchemaPropertyLoader;
import org.apache.axis2.schema.writer.BeanWriter;
import org.apache.axis2.util.URLProcessor;
import org.apache.axis2.util.SchemaUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.*;

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
*/

/**
 * Schema compiler for ADB. Based on WS-Commons schema object model.
 */
public class SchemaCompiler {

    private static final Log log = LogFactory.getLog(SchemaCompiler.class);

    private CompilerOptions options;
    private HashMap processedTypemap;

    //the list of processedElements for the outer elements
    private HashMap processedElementMap;

    private HashMap processedAnonymousComplexTypesMap;

    //we need this map to keep the referenced elements. these elements need to be kept seperate
    //to avoid conflicts
    private HashMap processedElementRefMap;
    private HashMap simpleTypesMap;
    private HashMap changedTypeMap;

    private HashSet changedSimpleTypeSet;
    private HashSet changedComplexTypeSet;
    private HashSet changedElementSet;

    // this map is necessary to retain the metainformation of types. The reason why these
    // meta info 'bags' would be useful later is to cater for the extensions and restrictions
    // of types
    private HashMap processedTypeMetaInfoMap;

    //
    private ArrayList processedElementList;

    //a list of nillable elements - used to generate code
    //for nillable elements
    private List nillableElementList;
    // writee reference
    private BeanWriter writer = null;
    private Map baseSchemaTypeMap = null;

    //a map for keeping the already loaded schemas
    //the key is the targetnamespace and the value is the schema object
    private Map loadedSchemaMap = new HashMap();

    // A map keeping the available schemas
    //the key is the targetnamespace and the value is the schema object
    //this map will be populated when multiple schemas
    //are fed to the schema compiler!
    private Map availableSchemaMap = new HashMap();

    private Map loadedSourceURI = new HashMap();

    // a list of externally identified QNames to be processed. This becomes
    // useful when  only a list of external elements need to be processed

    public static final String ANY_ELEMENT_FIELD_NAME = "extraElement";
    public static final String EXTRA_ATTRIBUTE_FIELD_NAME = "extraAttributes";

    public static final String USE_OPTIONAL = "optional";
    public static final String USE_REQUIRED = "required";
    public static final String USE_NONE = "none";

    /**
     * @return the processes element map
     *         includes the Qname of the element as the key and a
     *         String representing the fully qualified class name
     */
    public HashMap getProcessedElementMap() {
        return processedElementMap;
    }


    /**
     * @return a map of Qname vs models. A model can be anything,
     *         ranging from a DOM document to a stream. This is taken from the
     *         writer and the schema compiler has no control over it
     */
    public Map getProcessedModelMap() {
        return writer.getModelMap();
    }

    /**
     * Constructor - Accepts a options bean
     *
     * @param options
     */
    public SchemaCompiler(CompilerOptions options) throws SchemaCompilationException {

        if (options == null) {
            //create an empty options object
            this.options = new CompilerOptions();
        } else {
            this.options = options;
        }

        //instantiate the maps
        processedTypemap = new HashMap();
        processedElementMap = new HashMap();
        simpleTypesMap = new HashMap();
        processedElementList = new ArrayList();
        processedAnonymousComplexTypesMap = new HashMap();
        changedTypeMap = new HashMap();
        processedTypeMetaInfoMap = new HashMap();
        processedElementRefMap = new HashMap();
        nillableElementList = new ArrayList();

        changedComplexTypeSet = new HashSet();
        changedSimpleTypeSet = new HashSet();
        changedElementSet = new HashSet();

        //load the writer and initiliaze the base types
        writer = SchemaPropertyLoader.getBeanWriterInstance();
        writer.init(this.options);

        //load the base types
        baseSchemaTypeMap = SchemaPropertyLoader.getTypeMapperInstance().getTypeMap();


    }

    /**
     * Compile a list of schemas
     * This actually calls the compile (XmlSchema s) method repeatedly
     *
     * @param schemalist
     * @throws SchemaCompilationException
     * @see #compile(org.apache.ws.commons.schema.XmlSchema)
     */
    public void compile(List schemalist) throws SchemaCompilationException {
        try {

            if (schemalist.isEmpty()) {
                return;
            }

            //clear the loaded and available maps
            loadedSchemaMap.clear();
            availableSchemaMap.clear();

            XmlSchema schema;
            // first round - populate the avaialble map
            for (int i = 0; i < schemalist.size(); i++) {
                schema = (XmlSchema) schemalist.get(i);
                availableSchemaMap.put(
                        schema.getTargetNamespace(),
                        schema);
            }

            //set a mapper package if not avaialable
            if (writer.getExtensionMapperPackageName() == null) {
                String nsp = null;
                //get the first schema from the list and take that namespace as the
                //mapper namespace
                for (int i = 0; nsp == null && i < schemalist.size(); i++) {
                    nsp = ((XmlSchema) schemalist.get(i)).getTargetNamespace();
                    if (nsp != null)
                        break;
                    XmlSchema[] schemas = SchemaUtil.getAllSchemas((XmlSchema) schemalist.get(i));
                    for (int j = 0; schemas != null && j < schemas.length; j++) {
                        nsp = schemas[j].getTargetNamespace();
                        if (nsp != null)
                            break;
                    }
                }
                if (nsp == null) {
                    nsp = URLProcessor.DEFAULT_PACKAGE;
                }

                // if this name space exists in the ns2p list then we use it.
                if ((options.getNs2PackageMap() != null)
                        && (options.getNs2PackageMap().containsKey(nsp))) {
                    writer.registerExtensionMapperPackageName((String) options.getNs2PackageMap().get(nsp));
                } else {
                    writer.registerExtensionMapperPackageName(URLProcessor.makePackageName(nsp));
                }
            }
            // second round - call the schema compiler one by one
            for (int i = 0; i < schemalist.size(); i++) {
                compile((XmlSchema) schemalist.get(i), true);
            }

            //finish up
            finalizeSchemaCompilation();

        } catch (SchemaCompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }
    }

    /**
     * Compile (rather codegen) a single schema element
     *
     * @param schema
     * @throws SchemaCompilationException
     */
    public void compile(XmlSchema schema) throws SchemaCompilationException {
        compile(schema, false);
    }

    /**
     * Compile (rather codegen) a single schema element
     *
     * @param schema
     * @param isPartofGroup
     * @throws SchemaCompilationException
     */
    private void compile(XmlSchema schema, boolean isPartofGroup) throws SchemaCompilationException {

        // some documents explicitly imports the schema of built in types. We don't actually need to compile
        // the built-in types. So check the target namespace here and ignore it.
        if (Constants.URI_2001_SCHEMA_XSD.equals(schema.getTargetNamespace())) {
            return;
        }

        //register the package from this namespace as the mapper classes package
        if (!isPartofGroup) {
            //set a mapper package if not avaialable
            if (writer.getExtensionMapperPackageName() == null) {
                writer.registerExtensionMapperPackageName(
                        URLProcessor.makePackageName(schema.getTargetNamespace()));
            }
        }

        //First look for the schemas that are imported and process them
        //Note that these are processed recursively!

        //add the schema to the loaded schema list
        if (!loadedSchemaMap.containsKey(schema.getTargetNamespace())) {
            loadedSchemaMap.put(schema.getTargetNamespace(), schema);
        }

        // If we have/are loading a schema with a specific targetnamespace from a certain URI,
        // then just return back to the caller to avoid recursion.
        if (schema.getSourceURI() != null) {
            String key = schema.getTargetNamespace() + ":" + schema.getSourceURI();
            if (loadedSourceURI.containsKey(key)) {
                return;
            }
            loadedSourceURI.put(key, key);
        }

        XmlSchemaObjectCollection includes = schema.getIncludes();
        if (includes != null) {
            Iterator tempIterator = includes.getIterator();
            while (tempIterator.hasNext()) {
                Object o = tempIterator.next();
                if (o instanceof XmlSchemaImport) {
                    XmlSchema schema1 = ((XmlSchemaImport) o).getSchema();
                    if (schema1 != null) compile(schema1, isPartofGroup);
                }
                if (o instanceof XmlSchemaInclude) {
                    XmlSchema schema1 = ((XmlSchemaInclude) o).getSchema();
                    if (schema1 != null) compile(schema1, isPartofGroup);
                }
            }
        }

        //select all the elements. We generate the code for types
        //only if the elements refer them!!! regardless of the fact that
        //we have a list of elementnames, we'll need to process all the elements
        XmlSchemaObjectTable elements = schema.getElements();
        Iterator xmlSchemaElement1Iterator = elements.getValues();
        while (xmlSchemaElement1Iterator.hasNext()) {
            //this is the set of outer elements so we need to generate classes
            //The outermost elements do not contain occurence counts (!) so we do not need
            //to check for arraytypes
            processElement((XmlSchemaElement) xmlSchemaElement1Iterator.next(), schema);
        }

        Iterator xmlSchemaElement2Iterator = elements.getValues();

        // re-iterate through the elements and write them one by one
        // if the mode is unpack this process will not really write the
        // classes but will accumilate the models for a final single shot
        // write
        while (xmlSchemaElement2Iterator.hasNext()) {
            //this is the set of outer elements so we need to generate classes
            writeElement((XmlSchemaElement) xmlSchemaElement2Iterator.next());
        }

        if (options.isGenerateAll()) {
            Iterator xmlSchemaTypes2Iterator = schema.getSchemaTypes().getValues();
            while (xmlSchemaTypes2Iterator.hasNext()) {
                XmlSchemaType schemaType = (XmlSchemaType) xmlSchemaTypes2Iterator.next();
                if (this.isAlreadyProcessed(schemaType.getQName())) {
                    continue;
                }
                if (schemaType instanceof XmlSchemaComplexType) {
                    //write classes for complex types
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType) schemaType;
                    if (complexType.getName() != null) {
                        processNamedComplexSchemaType(complexType, schema);
                    }
                } else if (schemaType instanceof XmlSchemaSimpleType) {
                    //process simple type
                    processSimpleSchemaType((XmlSchemaSimpleType) schemaType,
                            null,
                            schema, null);
                }
            }
        }

        if (!isPartofGroup) {
            //complete the compilation
            finalizeSchemaCompilation();
        }
    }

    /**
     * Completes the schema compilation process by writing the
     * mappers and the classes in a batch if needed
     *
     * @throws SchemaCompilationException
     */
    private void finalizeSchemaCompilation() throws SchemaCompilationException {
        //write the extension mapping class
        writer.writeExtensionMapper(
                (BeanWriterMetaInfoHolder[])
                        processedTypeMetaInfoMap.values().toArray(
                                new BeanWriterMetaInfoHolder[processedTypeMetaInfoMap.size()]));


        if (options.isWrapClasses()) {
            writer.writeBatch();
        }

        // resets the changed types
        XmlSchemaComplexType xmlSchemaComplexType = null;
        for (Iterator iter = changedComplexTypeSet.iterator();iter.hasNext();){
            xmlSchemaComplexType = (XmlSchemaComplexType) iter.next();
            xmlSchemaComplexType.setName(null);
        }

        XmlSchemaSimpleType xmlSchemaSimpleType = null;
        for (Iterator iter = changedSimpleTypeSet.iterator();iter.hasNext();){
            xmlSchemaSimpleType = (XmlSchemaSimpleType) iter.next();
            xmlSchemaSimpleType.setName(null);
        }

        XmlSchemaElement xmlSchemaElement = null;
        for (Iterator iter = changedElementSet.iterator();iter.hasNext();){
            xmlSchemaElement = (XmlSchemaElement) iter.next();
            xmlSchemaElement.setSchemaTypeName(null);
        }

    }

    /**
     * @return the property map of the schemacompiler.
     *         In this case it would be the property map loaded from
     *         the configuration file
     */
    public Properties getCompilerProperties() {
        return SchemaPropertyLoader.getPropertyMap();
    }


    /**
     * Writes the element
     *
     * @param xsElt
     * @throws SchemaCompilationException
     */
    private void writeElement(XmlSchemaElement xsElt) throws SchemaCompilationException {

        if (this.processedElementMap.containsKey(xsElt.getQName())) {
            return;
        }

        XmlSchemaType schemaType = xsElt.getSchemaType();


        BeanWriterMetaInfoHolder metainf = new BeanWriterMetaInfoHolder();
        if (schemaType != null && schemaType.getName() != null) {
            //this is a named type
            QName qName = schemaType.getQName();
            //find the class name
            String className = findClassName(qName, isArray(xsElt));

            //this means the schema type actually returns a different QName
            if (changedTypeMap.containsKey(qName)) {
                metainf.registerMapping(xsElt.getQName(),
                        (QName) changedTypeMap.get(qName),
                        className);
            } else {
                metainf.registerMapping(xsElt.getQName(),
                        qName,
                        className);
            }


        } else if (xsElt.getRefName() != null) {
            // Since top level elements would not have references
            // and we only write toplevel elements, this should
            // not be a problem , atleast should not occur in a legal schema
        } else if (xsElt.getSchemaTypeName() != null) {
            QName qName = xsElt.getSchemaTypeName();
            String className = findClassName(qName, isArray(xsElt));
            metainf.registerMapping(xsElt.getQName(),
                    qName,
                    className);


        } else if (schemaType != null) {  //the named type should have been handled already

            //we are going to special case the anonymous complex type. Our algorithm for dealing
            //with it is to generate a single object that has the complex content inside. Really the
            //intent of the user when he declares the complexType anonymously is to use it privately
            //First copy the schema types content into the metainf holder
            metainf = (BeanWriterMetaInfoHolder) this.processedAnonymousComplexTypesMap.get(xsElt);
            metainf.setAnonymous(true);
        } else {
            //this means we did not find any schema type associated with the particular element.
            log.warn(SchemaCompilerMessages.getMessage("schema.elementWithNoType", xsElt.getQName().toString()));
            metainf.registerMapping(xsElt.getQName(),
                    null,
                    writer.getDefaultClassName(),
                    SchemaConstants.ANY_TYPE);
        }

        if (nillableElementList.contains(xsElt.getQName())) {
            metainf.registerNillableQName(xsElt.getQName());
        }


        String writtenClassName = writer.write(xsElt, processedTypemap, metainf);
        //register the class name
        xsElt.addMetaInfo(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY, writtenClassName);
        processedElementMap.put(xsElt.getQName(), writtenClassName);
    }

    /**
     * For inner elements
     *
     * @param xsElt
     * @param innerElementMap
     * @param parentSchema
     * @throws SchemaCompilationException
     */
    private void processElement(XmlSchemaElement xsElt, Map innerElementMap, List localNillableList, XmlSchema parentSchema) throws SchemaCompilationException {
        processElement(xsElt, false, innerElementMap, localNillableList, parentSchema);
    }

    /**
     * For outer elements
     *
     * @param xsElt
     * @param parentSchema
     * @throws SchemaCompilationException
     */
    private void processElement(XmlSchemaElement xsElt, XmlSchema parentSchema) throws SchemaCompilationException {
        processElement(xsElt, true, null, null, parentSchema);
    }

    /**
     * Process and Element
     *
     * @param xsElt
     * @param isOuter We need to know this since the treatment of outer elements is different that
     *                inner elements
     * @throws SchemaCompilationException
     */
    private void processElement(XmlSchemaElement xsElt, boolean isOuter, Map innerElementMap, List localNillableList, XmlSchema parentSchema) throws SchemaCompilationException {

        //if the element is null, which usually happens when the qname is not
        //proper, throw an exceptions
        if (xsElt == null) {
            throw new SchemaCompilationException(
                    SchemaCompilerMessages.getMessage("schema.elementNull"));
        }

        //The processing element logic seems to be quite simple. Look at the relevant schema type
        //for each and every element and process that accordingly.
        //this means that any unused type definitions would not be generated!
        if (isOuter && processedElementList.contains(xsElt.getQName())) {
            return;
        }

        XmlSchemaType schemaType = xsElt.getSchemaType();
        if (schemaType != null) {
            processSchema(xsElt, schemaType, parentSchema);
            //at this time it is not wise to directly write the class for the element
            //so we push the complete element to an arraylist and let the process
            //pass through. We'll be iterating through the elements writing them
            //later

            if (!isOuter) {
                if (schemaType.getName() != null) {
                    // this element already has a name. Which means we can directly
                    // register it
                    String className = findClassName(schemaType.getQName(),
                            isArray(xsElt));

                    innerElementMap.put(xsElt.getQName(), className);

                    // always store the class name in the element meta Info itself
                    // this details only needed by the unwrappig to set the complex type
                    schemaType.addMetaInfo(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY, className);
                    xsElt.addMetaInfo(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY, className);

                    if (baseSchemaTypeMap.containsValue(className)) {
                        schemaType.addMetaInfo(
                                SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_PRIMITVE_KEY,
                                Boolean.TRUE);
                    }
                    //since this is a inner element we should add it to the inner element map
                } else {
                    //this is an anon type. This should have been already processed and registered at
                    //the anon map. we've to write it just like we treat a referenced type(giving due
                    //care that this is meant to be an attribute in some class)

                    QName generatedTypeName = generateTypeQName(xsElt.getQName(), parentSchema);

                    if (schemaType instanceof XmlSchemaComplexType) {
                        //set a name
                        schemaType.setName(generatedTypeName.getLocalPart());
                        changedComplexTypeSet.add(schemaType);
                        // Must do this up front to support recursive types
                        String fullyQualifiedClassName = writer.makeFullyQualifiedClassName(schemaType.getQName());
                        processedTypemap.put(schemaType.getQName(), fullyQualifiedClassName);

                        BeanWriterMetaInfoHolder metaInfHolder = (BeanWriterMetaInfoHolder) processedAnonymousComplexTypesMap.get(xsElt);
                        metaInfHolder.setOwnQname(schemaType.getQName());
                        metaInfHolder.setOwnClassName(fullyQualifiedClassName);

                        writeComplexType((XmlSchemaComplexType) schemaType,
                                metaInfHolder);
                        //remove the reference from the anon list since we named the type
                        processedAnonymousComplexTypesMap.remove(xsElt);
                        String className = findClassName(schemaType.getQName(), isArray(xsElt));
                        innerElementMap.put(
                                xsElt.getQName(),
                                className);

                        //store in the schema map to retrive in the unwrapping
                        xsElt.addMetaInfo(
                                SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY,
                                className);
                    } else if (schemaType instanceof XmlSchemaSimpleType) {
                        //set a name
                        schemaType.setName(generatedTypeName.getLocalPart());
                        changedSimpleTypeSet.add(schemaType);
                        // Must do this up front to support recursive types
                        String fullyQualifiedClassName = writer.makeFullyQualifiedClassName(schemaType.getQName());
                        processedTypemap.put(schemaType.getQName(), fullyQualifiedClassName);

                        BeanWriterMetaInfoHolder metaInfHolder = (BeanWriterMetaInfoHolder) processedAnonymousComplexTypesMap.get(xsElt);
                        metaInfHolder.setOwnQname(schemaType.getQName());
                        metaInfHolder.setOwnClassName(fullyQualifiedClassName);

                        writeSimpleType((XmlSchemaSimpleType) schemaType,
                                metaInfHolder);
                        //remove the reference from the anon list since we named the type
                        processedAnonymousComplexTypesMap.remove(xsElt);
                        String className = findClassName(schemaType.getQName(), isArray(xsElt));
                        innerElementMap.put(
                                xsElt.getQName(),
                                className);

                        //store in the schema map
                        xsElt.addMetaInfo(
                                SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY,
                                className);
                    }
                }
            } else {
                this.processedElementList.add(xsElt.getQName());
            }
            //referenced name
        } else if (xsElt.getRefName() != null) {

            if (xsElt.getRefName().equals(SchemaConstants.XSD_SCHEMA)) {
                innerElementMap.put(xsElt.getQName(), writer.getDefaultClassName());
                return;
            }
            //process the referenced type. It could be thought that the referenced element replaces this
            //element
            XmlSchemaElement referencedElement = getReferencedElement(parentSchema, xsElt.getRefName());
            if (referencedElement == null) {
                throw new SchemaCompilationException(
                        SchemaCompilerMessages.getMessage("schema.referencedElementNotFound", xsElt.getRefName().toString()));
            }

            //if the element is referenced, then it should be one of the outer (global) ones
            processElement(referencedElement, parentSchema);

            //no outer check required here. If the element is having a ref, then it is definitely
            //not an outer element since the top level elements are not supposed to have refs
            //Also we are sure that it should have a type reference
            QName referenceEltQName = referencedElement.getQName();
            if (referencedElement.getSchemaTypeName() != null) {
                // we have to only find the class name without arrary part
                String javaClassName = findClassName(referencedElement.getSchemaTypeName(), false);
                //if this element is referenced, there's no QName for this element
                this.processedElementRefMap.put(referenceEltQName, javaClassName);
                referencedElement.addMetaInfo(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY,
                        javaClassName);
                // set the element class name to be used in unwrapping
                xsElt.addMetaInfo(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY,
                                javaClassName);
            } else {
                //this referenced element has an anon type and that anon type has been already
                //processed. But in this case we need it to be a seperate class since this
                //complextype has to be added as an attribute in a class.
                //generate a name for this type
                QName generatedTypeName = generateTypeQName(referenceEltQName, parentSchema);
                XmlSchemaType referenceSchemaType = referencedElement.getSchemaType();


                if (referenceSchemaType instanceof XmlSchemaComplexType) {

                    if (referencedElement.getSchemaTypeName() == null) {
                        referencedElement.setSchemaTypeName(generatedTypeName);
                    }

                    //set a name
                    referenceSchemaType.setName(generatedTypeName.getLocalPart());

                    String javaclassName = writeComplexType((XmlSchemaComplexType) referenceSchemaType,
                            (BeanWriterMetaInfoHolder) processedAnonymousComplexTypesMap.get(referencedElement)
                    );
                    //remove the reference from the anon list since we named the type
                    // DEEPAL :- We can not remove the entry from the hashtable ,
                    // this will fail if there are two reference for the same type

                    //processedAnonymousComplexTypesMap.remove(referencedElement);

                    processedTypemap.put(generatedTypeName, javaclassName);
                    this.processedElementRefMap.put(referenceEltQName, javaclassName);
                    // set the class name to be used in unwrapping
                    xsElt.addMetaInfo(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY,
                                javaclassName);

                }
            }
            // schema type name is present but not the schema type object
        } else if (xsElt.getSchemaTypeName() != null) {
            //There can be instances where the SchemaType is null but the schemaTypeName is not!
            //this specifically happens with xsd:anyType.
            QName schemaTypeName = xsElt.getSchemaTypeName();

            XmlSchema currentParentSchema = resolveParentSchema(schemaTypeName, parentSchema);
            XmlSchemaType typeByName = getType(currentParentSchema, schemaTypeName);

            if (typeByName != null) {
                //this type is found in the schema so we can process it
                processSchema(xsElt, typeByName, currentParentSchema);
                if (!isOuter) {
                    String className = findClassName(schemaTypeName, isArray(xsElt));
                    //since this is a inner element we should add it to the inner element map
                    innerElementMap.put(xsElt.getQName(), className);
                    // set the class name to be used in unwrapping
                    xsElt.addMetaInfo(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY,
                                className);
                } else {
                    this.processedElementList.add(xsElt.getQName());
                }
            } else {
                //this type is not found at all. we'll just register it with whatever the class name we can comeup with
                if (!isOuter) {
                    String className = findClassName(schemaTypeName, isArray(xsElt));
                    innerElementMap.put(xsElt.getQName(), className);
                    // set the class name to be used in unwrapping
                    xsElt.addMetaInfo(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY,
                                className);
                } else {
                    this.processedElementList.add(xsElt.getQName());
                }
            }
        }

        //add this elements QName to the nillable group if it has the  nillable attribute
        if (xsElt.isNillable()) {
            if (isOuter) {
                this.nillableElementList.add(xsElt.getQName());
            } else {
                localNillableList.add(xsElt.getQName());
            }
        }

    }

    /**
     * resolve the parent schema for the given schema type name
     *
     * @param schemaTypeName
     * @param currentSchema
     */
    private XmlSchema resolveParentSchema(QName schemaTypeName, XmlSchema currentSchema)
            throws SchemaCompilationException {
        String targetNamespace = schemaTypeName.getNamespaceURI();
        Object loadedSchema = loadedSchemaMap.get(targetNamespace);
        if (loadedSchema != null) {
            return (XmlSchema) loadedSchema;
        } else if (availableSchemaMap.containsKey(targetNamespace)) {
            //compile the referenced Schema first and then pass it
            XmlSchema schema = (XmlSchema) availableSchemaMap.get(targetNamespace);
            compile(schema);
            return schema;
        } else {
            return currentSchema;
        }
    }

    /**
     * Generate a unique type Qname using an element name
     *
     * @param referenceEltQName
     * @param parentSchema
     */
    private QName generateTypeQName(QName referenceEltQName, XmlSchema parentSchema) {
        QName generatedTypeName = new QName(referenceEltQName.getNamespaceURI(),
                referenceEltQName.getLocalPart() + getNextTypeSuffix(referenceEltQName.getLocalPart()));
        while (parentSchema.getTypeByName(generatedTypeName) != null) {
            generatedTypeName = new QName(referenceEltQName.getNamespaceURI(),
                    referenceEltQName.getLocalPart() + getNextTypeSuffix(referenceEltQName.getLocalPart()));
        }
        return generatedTypeName;
    }

    /**
     * Finds whether a given class is already made
     *
     * @param qName
     */
    private boolean isAlreadyProcessed(QName qName) {
        return processedTypemap.containsKey(qName) ||
                simpleTypesMap.containsKey(qName) ||
                baseSchemaTypeMap.containsKey(qName);
    }


    /**
     * A method to pick the ref class name
     *
     * @param name
     * @param isArray
     */
    private String findRefClassName(QName name, boolean isArray) {
        String className = null;
        if (processedElementRefMap.get(name) != null) {
            className = (String) processedElementRefMap.get(name);

            if (isArray) {
                //append the square braces that say this is an array
                //hope this works for all cases!!!!!!!
                //todo this however is a thing that needs to be
                //todo fixed to get complete language support
                className = className + "[]";
            }
        }
        return className;

    }

    /**
     * Finds a class name from the given Qname
     *
     * @param qName
     * @param isArray
     * @return FQCN
     */
    private String findClassName(QName qName, boolean isArray) throws SchemaCompilationException {

        //find the class name
        String className;
        if (processedTypemap.containsKey(qName)) {
            className = (String) processedTypemap.get(qName);
        } else if (simpleTypesMap.containsKey(qName)) {
            className = (String) simpleTypesMap.get(qName);
        } else if (baseSchemaTypeMap.containsKey(qName)) {
            className = (String) baseSchemaTypeMap.get(qName);
        } else {
            if (isSOAP_ENC(qName.getNamespaceURI())) {
                throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("schema.soapencoding.error", qName.toString()));

            }
            // We seem to have failed in finding a class name for the
            //contained schema type. We better set the default then
            //however it's better if the default can be set through the
            //property file
            className = writer.getDefaultClassName();
            log.warn(SchemaCompilerMessages
                    .getMessage("schema.typeMissing", qName.toString()));
        }

        if (isArray) {
            //append the square braces that say this is an array
            //hope this works for all cases!!!!!!!
            //todo this however is a thing that needs to be
            //todo fixed to get complete language support
            className = className + "[]";
        }
        return className;
    }

    /**
     * Returns true if SOAP_ENC Namespace.
     *
     * @param s a string representing the URI to check
     * @return true if <code>s</code> matches a SOAP ENCODING namespace URI,
     *         false otherwise
     */
    public static boolean isSOAP_ENC(String s) {
        if (s.equals(Constants.URI_SOAP11_ENC))
            return true;
        return s.equals(Constants.URI_SOAP12_ENC);
    }

    /**
     * Process a schema element which has been refered to by an element
     *
     * @param schemaType
     * @throws SchemaCompilationException
     */
    private void processSchema(XmlSchemaElement xsElt, XmlSchemaType schemaType, XmlSchema parentSchema) throws SchemaCompilationException {
        if (schemaType instanceof XmlSchemaComplexType) {
            //write classes for complex types
            XmlSchemaComplexType complexType = (XmlSchemaComplexType) schemaType;
            if (complexType.getName() != null) {
                processNamedComplexSchemaType(complexType, parentSchema);
            } else {
                processAnonymousComplexSchemaType(xsElt, complexType, parentSchema);
            }
        } else if (schemaType instanceof XmlSchemaSimpleType) {
            //process simple type
            processSimpleSchemaType((XmlSchemaSimpleType) schemaType,
                    xsElt,
                    parentSchema, null);
        }
    }


    /**
     * @param complexType
     * @throws SchemaCompilationException
     */
    private void processAnonymousComplexSchemaType(XmlSchemaElement elt,
                                                   XmlSchemaComplexType complexType,
                                                   XmlSchema parentSchema)
            throws SchemaCompilationException {
        BeanWriterMetaInfoHolder metaInfHolder = processComplexType(elt.getQName(),complexType, parentSchema);

        //since this is a special case (an unnamed complex type) we'll put the already processed
        //metainf holder in a special map to be used later
        this.processedAnonymousComplexTypesMap.put(elt, metaInfHolder);
    }

    /**
     * handle the complex types which are named
     *
     * @param complexType
     */
    private void processNamedComplexSchemaType(XmlSchemaComplexType complexType,
                                               XmlSchema parentSchema) throws SchemaCompilationException {

        if (processedTypemap.containsKey(complexType.getQName())
                || baseSchemaTypeMap.containsKey(complexType.getQName())) {
            return;
        }

        // Must do this up front to support recursive types
        String fullyQualifiedClassName = writer.makeFullyQualifiedClassName(complexType.getQName());
        processedTypemap.put(complexType.getQName(), fullyQualifiedClassName);

        //register that in the schema metainfo bag
        complexType.addMetaInfo(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY,
                fullyQualifiedClassName);

        BeanWriterMetaInfoHolder metaInfHolder = processComplexType(complexType.getQName(),complexType, parentSchema);
        //add this information to the metainfo holder
        metaInfHolder.setOwnQname(complexType.getQName());
        metaInfHolder.setOwnClassName(fullyQualifiedClassName);
        //write the class. This type mapping would have been populated right now
        //Note - We always write classes for named complex types
        writeComplexType(complexType, metaInfHolder);


    }

    /**
     * Writes a complex type
     *
     * @param complexType
     * @param metaInfHolder
     * @throws SchemaCompilationException
     */
    private String writeComplexType(XmlSchemaComplexType complexType, BeanWriterMetaInfoHolder metaInfHolder)
            throws SchemaCompilationException {
        String javaClassName = writer.write(complexType.getQName(), processedTypemap, metaInfHolder, complexType.isAbstract());
        processedTypeMetaInfoMap.put(complexType.getQName(), metaInfHolder);
        return javaClassName;
    }


    /**
     * Writes complex Sequence,Choice, all elements
     * @param qname complex type qname
     * @param metaInfHolder
     * @return  written java class name
     * @throws SchemaCompilationException
     */


    private String writeComplexParticle(QName qname,BeanWriterMetaInfoHolder metaInfHolder)
            throws SchemaCompilationException {
       String javaClassName = writer.write(qname, processedTypemap, metaInfHolder,false);
        processedTypeMetaInfoMap.put(qname, metaInfHolder);
        return javaClassName;
    }

    /**
     * Writes a complex type
     *
     * @param simpleType
     * @param metaInfHolder
     * @throws SchemaCompilationException
     */
    private void writeSimpleType(XmlSchemaSimpleType simpleType, BeanWriterMetaInfoHolder metaInfHolder)
            throws SchemaCompilationException {
        writer.write(simpleType, processedTypemap, metaInfHolder);
        processedTypeMetaInfoMap.put(simpleType.getQName(), metaInfHolder);
    }

    private BeanWriterMetaInfoHolder processComplexType(
            QName parentElementQName,
            XmlSchemaComplexType complexType,
            XmlSchema parentSchema) throws SchemaCompilationException {
        XmlSchemaParticle particle = complexType.getParticle();
        BeanWriterMetaInfoHolder metaInfHolder = new BeanWriterMetaInfoHolder();
        if (particle != null) {
            //Process the particle
            processParticle(parentElementQName, particle, metaInfHolder, parentSchema);
        }

        //process attributes - first look for the explicit attributes
        processAttributes(complexType.getAttributes(),metaInfHolder,parentSchema);

        //process any attribute
        //somehow the xml schema parser does not seem to pickup the any attribute!!
        XmlSchemaAnyAttribute anyAtt = complexType.getAnyAttribute();
        if (anyAtt != null) {
            processAnyAttribute(metaInfHolder, anyAtt);
        }


        //process content ,either  complex or simple
        if (complexType.getContentModel() != null) {
            processContentModel(complexType.getContentModel(),
                    metaInfHolder,
                    parentSchema);
        }
        return metaInfHolder;
    }

    private void processAttributes(XmlSchemaObjectCollection attributes,
                                   BeanWriterMetaInfoHolder metaInfHolder,
                                   XmlSchema parentSchema) throws SchemaCompilationException {
        Iterator attribIterator = attributes.getIterator();
        while (attribIterator.hasNext()) {
            Object o = attribIterator.next();
            if (o instanceof XmlSchemaAttribute) {
                processAttribute((XmlSchemaAttribute) o, metaInfHolder, parentSchema);
            } else if (o instanceof XmlSchemaAttributeGroupRef){
                processAttributeGroupReference((XmlSchemaAttributeGroupRef)o,metaInfHolder,parentSchema);
            }
        }
    }

    private void processAttributeGroupReference(XmlSchemaAttributeGroupRef attributeGroupRef,
                                                BeanWriterMetaInfoHolder metaInfHolder,
                                                XmlSchema parentSchema) throws SchemaCompilationException {

        QName attributeGroupRefName = attributeGroupRef.getRefName();
        if (attributeGroupRefName != null){
           parentSchema = resolveParentSchema(attributeGroupRefName,parentSchema);
           XmlSchemaAttributeGroup xmlSchemaAttributeGroup = getXmlSchemaAttributeGroup(attributeGroupRefName,
                                                                                        parentSchema);
           if (xmlSchemaAttributeGroup != null){
               processAttributes(xmlSchemaAttributeGroup.getAttributes(),metaInfHolder,parentSchema);
           } else {
               throw new SchemaCompilationException("Can not find an attribute group for group reference"
                       + attributeGroupRefName.getLocalPart());
           }
        } else {
            throw new SchemaCompilationException("No group refernce has given");
        }

    }

    private XmlSchemaAttributeGroup getXmlSchemaAttributeGroup(QName attributeGroupQName,
                                                               XmlSchema parentSchema){
        XmlSchemaAttributeGroup xmlSchemaAttributeGroup =
                (XmlSchemaAttributeGroup) parentSchema.getAttributeGroups().getItem(attributeGroupQName);
        if (xmlSchemaAttributeGroup == null){
            // i.e this attribute can be in a included or imported schema
            xmlSchemaAttributeGroup = (XmlSchemaAttributeGroup) parentSchema.getAttributeGroups().getItem(attributeGroupQName);
            if (xmlSchemaAttributeGroup == null) {
                // try to find in an import or an include
                XmlSchemaObjectCollection includes = parentSchema.getIncludes();
                if (includes != null) {
                    Iterator includesIter = includes.getIterator();
                    Object object = null;
                    while (includesIter.hasNext()) {
                        object = includesIter.next();
                        if (object instanceof XmlSchemaImport) {
                            XmlSchema schema1 = ((XmlSchemaImport) object).getSchema();
                            xmlSchemaAttributeGroup = (XmlSchemaAttributeGroup) schema1.getAttributeGroups().getItem(attributeGroupQName);
                        }
                        if (object instanceof XmlSchemaInclude) {
                            XmlSchema schema1 = ((XmlSchemaInclude) object).getSchema();
                            xmlSchemaAttributeGroup = (XmlSchemaAttributeGroup) schema1.getAttributeGroups().getItem(attributeGroupQName);
                        }
                        if (xmlSchemaAttributeGroup != null){
                            break;
                        }
                    }
                }
            }
        }
        return xmlSchemaAttributeGroup;
    }

    /**
     * Process the content models. A content model is either simple type or a complex type
     * and included inside a complex content
     */
    private void processContentModel(XmlSchemaContentModel content,
                                     BeanWriterMetaInfoHolder metaInfHolder,
                                     XmlSchema parentSchema)
            throws SchemaCompilationException {
        if (content instanceof XmlSchemaComplexContent) {
            processComplexContent((XmlSchemaComplexContent) content, metaInfHolder, parentSchema);
        } else if (content instanceof XmlSchemaSimpleContent) {
            processSimpleContent((XmlSchemaSimpleContent) content, metaInfHolder, parentSchema);
        }
    }

    /**
     * Prcess the complex content
     */
    private void processComplexContent(XmlSchemaComplexContent complexContent,
                                       BeanWriterMetaInfoHolder metaInfHolder,
                                       XmlSchema parentSchema)
            throws SchemaCompilationException {
        XmlSchemaContent content = complexContent.getContent();

        if (content instanceof XmlSchemaComplexContentExtension) {

            // to handle extension we need to attach the extended items to the base type
            // and create a new type
            XmlSchemaComplexContentExtension extension = (XmlSchemaComplexContentExtension)
                    content;

            //process the base type if it has not been processed yet
            if (!isAlreadyProcessed(extension.getBaseTypeName())) {
                //pick the relevant basetype from the schema and process it
                XmlSchemaType type = getType(parentSchema, extension.getBaseTypeName());
                if (type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType) type;
                    if (complexType.getName() != null) {
                        processNamedComplexSchemaType(complexType, parentSchema);
                    } else {
                        //this is not possible. The extension should always
                        //have a name
                        throw new SchemaCompilationException("Unnamed complex type used in extension");//Internationlize this
                    }
                } else if (type instanceof XmlSchemaSimpleType) {
                    //process simple type
                    processSimpleSchemaType((XmlSchemaSimpleType) type, null, parentSchema, null);
                }
            }

            // before actually processing this node, we need to recurse through the base types and add their
            // children (sometimes even preserving the order) to the metainfo holder of this type
            // the reason is that for extensions, the prefered way is to have the sequences of the base class
            //* before * the sequence of the child element.
            copyMetaInfoHierarchy(metaInfHolder, extension.getBaseTypeName(), parentSchema);

            //process the particle of this node
            if (extension.getParticle() != null) {
                processParticle(extension.getBaseTypeName(),extension.getParticle(), metaInfHolder, parentSchema);
            }

            // process attributes
            //process attributes - first look for the explicit attributes
            XmlSchemaObjectCollection attribs = extension.getAttributes();
            Iterator attribIterator = attribs.getIterator();
            while (attribIterator.hasNext()) {
                Object o = attribIterator.next();
                if (o instanceof XmlSchemaAttribute) {
                    processAttribute((XmlSchemaAttribute) o, metaInfHolder, parentSchema);

                }
            }

            //process any attribute
            //somehow the xml schema parser does not seem to pickup the any attribute!!
            XmlSchemaAnyAttribute anyAtt = extension.getAnyAttribute();
            if (anyAtt != null) {
                processAnyAttribute(metaInfHolder, anyAtt);
            }
            String className = findClassName(extension.getBaseTypeName(), false);

            if (!writer.getDefaultClassName().equals(className)) {
                //the particle has been processed, However since this is an extension we need to
                //add the basetype as an extension to the complex type class.
                // The basetype has been processed already
                metaInfHolder.setExtension(true);
                metaInfHolder.setExtensionClassName(className);
                //Note  - this is no array! so the array boolean is false
            }
        } else if (content instanceof XmlSchemaComplexContentRestriction) {
            XmlSchemaComplexContentRestriction restriction = (XmlSchemaComplexContentRestriction) content;

            //process the base type if it has not been processed yet
            if (!isAlreadyProcessed(restriction.getBaseTypeName())) {
                //pick the relevant basetype from the schema and process it
                XmlSchemaType type = getType(parentSchema, restriction.getBaseTypeName());
                if (type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType) type;
                    if (complexType.getName() != null) {
                        processNamedComplexSchemaType(complexType, parentSchema);
                    } else {
                        //this is not possible. The restriction should always
                        //have a name
                        throw new SchemaCompilationException("Unnamed complex type used in restriction");//Internationlize this
                    }
                } else if (type instanceof XmlSchemaSimpleType) {

                    throw new SchemaCompilationException("Not a valid restriction, complex content restriction base type cannot be a simple type.");
                }
            }

            copyMetaInfoHierarchy(metaInfHolder, restriction.getBaseTypeName(), parentSchema);

            //process the particle of this node
            processParticle(null,restriction.getParticle(), metaInfHolder, parentSchema);

            //process attributes - first look for the explicit attributes
            XmlSchemaObjectCollection attribs = restriction.getAttributes();
            Iterator attribIterator = attribs.getIterator();
            while (attribIterator.hasNext()) {
                Object o = attribIterator.next();
                if (o instanceof XmlSchemaAttribute) {
                    processAttribute((XmlSchemaAttribute) o, metaInfHolder, parentSchema);

                }
            }

            //process any attribute
            //somehow the xml schema parser does not seem to pickup the any attribute!!
            XmlSchemaAnyAttribute anyAtt = restriction.getAnyAttribute();
            if (anyAtt != null) {
                processAnyAttribute(metaInfHolder, anyAtt);
            }
            String className = findClassName(restriction.getBaseTypeName(), false);

            if (!writer.getDefaultClassName().equals(className)) {
                metaInfHolder.setRestriction(true);
                metaInfHolder.setRestrictionClassName(findClassName(restriction.getBaseTypeName(), false));
                //Note  - this is no array! so the array boolean is false
            }
        }
    }

    /**
     * Recursive method to populate the metainfo holders with info from the base types
     *
     * @param metaInfHolder
     * @param baseTypeName
     * @param parentSchema
     */
    private void copyMetaInfoHierarchy(BeanWriterMetaInfoHolder metaInfHolder,
                                       QName baseTypeName,
                                       XmlSchema parentSchema)
            throws SchemaCompilationException {

        XmlSchemaType type;
        type = parentSchema.getTypeByName(baseTypeName);
        if (type == null) {
            type = getType(parentSchema, baseTypeName);
        }


        BeanWriterMetaInfoHolder baseMetaInfoHolder = (BeanWriterMetaInfoHolder)
                processedTypeMetaInfoMap.get(baseTypeName);


        if (baseMetaInfoHolder != null) {

            // see whether this type is also extended from some other type first
            // if so proceed to set their parents as well.
            if (type instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType complexType = (XmlSchemaComplexType) type;
                if (complexType.getContentModel() != null) {
                    XmlSchemaContentModel content = complexType.getContentModel();
                    if (content instanceof XmlSchemaComplexContent) {
                        XmlSchemaComplexContent complexContent =
                                (XmlSchemaComplexContent) content;
                        if (complexContent.getContent() instanceof XmlSchemaComplexContentExtension) {
                            XmlSchemaComplexContentExtension extension =
                                    (XmlSchemaComplexContentExtension) complexContent.getContent();
                            //recursively call the copyMetaInfoHierarchy method
                            copyMetaInfoHierarchy(baseMetaInfoHolder,
                                    extension.getBaseTypeName(),
                                    parentSchema);

                        } else if (complexContent.getContent() instanceof XmlSchemaComplexContentRestriction) {

                            XmlSchemaComplexContentRestriction restriction =
                                    (XmlSchemaComplexContentRestriction) complexContent.getContent();
                            //recursively call the copyMetaInfoHierarchy method
                            copyMetaInfoHierarchy(baseMetaInfoHolder,
                                    restriction.getBaseTypeName(),
                                    parentSchema);

                        } else {
                            throw new SchemaCompilationException(
                                    SchemaCompilerMessages.getMessage("schema.unknowncontenterror"));
                        }

                    } else if (content instanceof XmlSchemaSimpleContent) {
                        throw new SchemaCompilationException(
                                SchemaCompilerMessages.getMessage("schema.unsupportedcontenterror", "Simple Content"));
                    } else {
                        throw new SchemaCompilationException(
                                SchemaCompilerMessages.getMessage("schema.unknowncontenterror"));
                    }
                }
                //Do the actual parent setting
                metaInfHolder.setAsParent(baseMetaInfoHolder);

            } else if (type instanceof XmlSchemaSimpleType) {

                // we have to copy the uion data if the parent simple type restriction
                // is an union
                // this union attribute is copied from the child to parent to genrate the parent
                // code as union
                if (baseMetaInfoHolder.isUnion()) {
                    metaInfHolder.setUnion(true);
                    Map memberTypes = baseMetaInfoHolder.getMemberTypes();
                    Object qname;
                    for (Iterator iter = memberTypes.keySet().iterator(); iter.hasNext();) {
                        qname = iter.next();
                        metaInfHolder.addMemberType((QName) qname, (String) memberTypes.get(qname));
                    }
                }

                // we have to copy the list type data to parent if it is a list
                if (baseMetaInfoHolder.isList()) {
                    metaInfHolder.setList(true);
                    metaInfHolder.setItemTypeQName(baseMetaInfoHolder.getItemTypeQName());
                    metaInfHolder.setItemTypeClassName(baseMetaInfoHolder.getItemTypeClassName());
                }
                metaInfHolder.setAsParent(baseMetaInfoHolder);
            }

        }
    }

    /**
     * @param simpleContent
     * @param metaInfHolder
     * @throws SchemaCompilationException
     */
    private void processSimpleContent(XmlSchemaSimpleContent simpleContent, BeanWriterMetaInfoHolder metaInfHolder, XmlSchema parentSchema)
            throws SchemaCompilationException {
        XmlSchemaContent content;
        content = simpleContent.getContent();
        if (content instanceof XmlSchemaSimpleContentExtension) {
            XmlSchemaSimpleContentExtension extension = (XmlSchemaSimpleContentExtension) content;

            //process the base type if it has not been processed yet
            if (!isAlreadyProcessed(extension.getBaseTypeName())) {
                //pick the relevant basetype from the schema and process it
                XmlSchemaType type = getType(parentSchema, extension.getBaseTypeName());
                if (type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType) type;
                    if (complexType.getName() != null) {
                        processNamedComplexSchemaType(complexType, parentSchema);
                    } else {
                        //this is not possible. The extension should always
                        //have a name
                        throw new SchemaCompilationException("Unnamed complex type used in extension");//Internationlize this
                    }
                } else if (type instanceof XmlSchemaSimpleType) {
                    //process simple type
                    processSimpleSchemaType((XmlSchemaSimpleType) type, null, parentSchema, null);
                }
            }

            //process extension base type
            processSimpleExtensionBaseType(extension.getBaseTypeName(), metaInfHolder, parentSchema);

            //process attributes
            XmlSchemaObjectCollection attribs = extension.getAttributes();
            Iterator attribIterator = attribs.getIterator();
            while (attribIterator.hasNext()) {
                Object attr = attribIterator.next();
                if (attr instanceof XmlSchemaAttribute) {
                    processAttribute((XmlSchemaAttribute) attr, metaInfHolder, parentSchema);

                }
            }

            //process any attribute
            XmlSchemaAnyAttribute anyAtt = extension.getAnyAttribute();
            if (anyAtt != null) {
                processAnyAttribute(metaInfHolder, anyAtt);
            }

        } else if (content instanceof XmlSchemaSimpleContentRestriction) {
            XmlSchemaSimpleContentRestriction restriction = (XmlSchemaSimpleContentRestriction) content;

            //process the base type if it has not been processed yet
            if (!isAlreadyProcessed(restriction.getBaseTypeName())) {
                //pick the relevant basetype from the schema and process it
                XmlSchemaType type = getType(parentSchema, restriction.getBaseTypeName());
                if (type instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = (XmlSchemaComplexType) type;
                    if (complexType.getName() != null) {
                        processNamedComplexSchemaType(complexType, parentSchema);
                    } else {
                        //this is not possible. The extension should always
                        //have a name
                        throw new SchemaCompilationException("Unnamed complex type used in restriction");//Internationlize this
                    }
                } else if (type instanceof XmlSchemaSimpleType) {
                    //process simple type
                    processSimpleSchemaType((XmlSchemaSimpleType) type, null, parentSchema, null);
                }
            }
            //process restriction base type
            processSimpleRestrictionBaseType(restriction.getBaseTypeName(),
                    restriction.getBaseTypeName(),
                    metaInfHolder,
                    parentSchema);
            metaInfHolder.setSimple(true);
        }
    }

    /**
     * Process Simple Extension Base Type.
     *
     * @param extBaseType
     * @param metaInfHolder
     */
    public void processSimpleExtensionBaseType(QName extBaseType,
                                               BeanWriterMetaInfoHolder metaInfHolder,
                                               XmlSchema parentSchema) throws SchemaCompilationException {

        //find the class name
        String className = findClassName(extBaseType, false);

        // if the base type is an primitive then we do not have to extend them
        // and it is considered as a property
        // on the otherhand if the base type is an generated class then we have to
        // extend from it

        if (baseSchemaTypeMap.containsKey(extBaseType)) {
            //this means the schema type actually returns a different QName
            if (changedTypeMap.containsKey(extBaseType)) {
                metaInfHolder.registerMapping(extBaseType,
                        (QName) changedTypeMap.get(extBaseType),
                        className, SchemaConstants.ELEMENT_TYPE);
            } else {
                metaInfHolder.registerMapping(extBaseType,
                        extBaseType,
                        className, SchemaConstants.ELEMENT_TYPE);
            }
            metaInfHolder.setSimple(true);
            // we have already process when it comes to this place
        } else if (processedTypemap.containsKey(extBaseType)) {
            //set the extension base class name

            XmlSchemaType type = getType(parentSchema, extBaseType);
            if (type instanceof XmlSchemaSimpleType) {
                metaInfHolder.setSimple(true);
                metaInfHolder.setExtension(true);
                metaInfHolder.setExtensionClassName(className);

                copyMetaInfoHierarchy(metaInfHolder, extBaseType, parentSchema);
            } else if (type instanceof XmlSchemaComplexType) {
                XmlSchemaComplexType complexType = (XmlSchemaComplexType) type;
                if (complexType.getContentModel() == null) {
                    // do not set as a simple type since we want to
                    // print the element names
                    metaInfHolder.setExtension(true);
                    metaInfHolder.setExtensionClassName(className);
                    copyMetaInfoHierarchy(metaInfHolder, extBaseType, parentSchema);
                }

            }
        } else {
            metaInfHolder.setSimple(true);
        }

        //get the binary state and add that to the status map
        if (isBinary(extBaseType)) {
            metaInfHolder.addtStatus(extBaseType,
                    SchemaConstants.BINARY_TYPE);
        }
    }

    /**
     * Process Simple Restriction Base Type.
     *
     * @param resBaseType
     * @param metaInfHolder
     */
    public void processSimpleRestrictionBaseType(QName qName,
                                                 QName resBaseType,
                                                 BeanWriterMetaInfoHolder metaInfHolder,
                                                 XmlSchema parentSchema) throws SchemaCompilationException {

        //find the class name
        String className = findClassName(resBaseType, false);

        //this means the schema type actually returns a different QName
        if (baseSchemaTypeMap.containsKey(resBaseType)) {
            if (changedTypeMap.containsKey(resBaseType)) {
                metaInfHolder.registerMapping(qName,
                        (QName) changedTypeMap.get(resBaseType),
                        className, SchemaConstants.ELEMENT_TYPE);
            } else {
                metaInfHolder.registerMapping(qName,
                        resBaseType,
                        className, SchemaConstants.ELEMENT_TYPE);
            }
        } else if (processedTypemap.containsKey(resBaseType)) {
            //this is not a standared type
            // so the parent class must extend it
            metaInfHolder.setSimple(true);
            metaInfHolder.setRestriction(true);
            metaInfHolder.setRestrictionClassName(className);
            copyMetaInfoHierarchy(metaInfHolder, resBaseType, parentSchema);
        }

        metaInfHolder.setRestrictionBaseType(resBaseType);


    }

    /**
     * Process Facets.
     *
     * @param metaInfHolder
     */
    private void processFacets(XmlSchemaSimpleTypeRestriction restriction,
                               BeanWriterMetaInfoHolder metaInfHolder,
                               XmlSchema parentSchema) {

        XmlSchemaObjectCollection facets = restriction.getFacets();
        Iterator facetIterator = facets.getIterator();

        while (facetIterator.hasNext()) {
            Object obj = facetIterator.next();

            if (obj instanceof XmlSchemaPatternFacet) {
                XmlSchemaPatternFacet pattern = (XmlSchemaPatternFacet) obj;
                // some patterns contain \ so we have to replace them
                String patternString = pattern.getValue().toString();
                metaInfHolder.setPatternFacet(patternString.replaceAll("\\\\", "\\\\\\\\"));
            }

            else if (obj instanceof XmlSchemaEnumerationFacet) {
                XmlSchemaEnumerationFacet enumeration = (XmlSchemaEnumerationFacet) obj;
                if (restriction.getBaseTypeName().equals(SchemaConstants.XSD_QNAME)) {
                    // we have to process the qname here and shoud find the local part and namespace uri
                    String value = enumeration.getValue().toString();
                    String prefix = value.substring(0, value.indexOf(":"));
                    String localPart = value.substring(value.indexOf(":") + 1);

                    String namespaceUri = parentSchema.getNamespaceContext().getNamespaceURI(prefix);
                    // set the string to suite for the convertQname method
                    String qNameString = value + "\", \"" + namespaceUri;
                    metaInfHolder.addEnumFacet(qNameString);
                } else {
                    metaInfHolder.addEnumFacet(enumeration.getValue().toString());
                }

            }

            else if (obj instanceof XmlSchemaLengthFacet) {
                XmlSchemaLengthFacet length = (XmlSchemaLengthFacet) obj;
                metaInfHolder.setLengthFacet(Integer.parseInt(length.getValue().toString()));
            }

            else if (obj instanceof XmlSchemaMaxExclusiveFacet) {
                XmlSchemaMaxExclusiveFacet maxEx = (XmlSchemaMaxExclusiveFacet) obj;
                metaInfHolder.setMaxExclusiveFacet(maxEx.getValue().toString());
            }

            else if (obj instanceof XmlSchemaMinExclusiveFacet) {
                XmlSchemaMinExclusiveFacet minEx = (XmlSchemaMinExclusiveFacet) obj;
                metaInfHolder.setMinExclusiveFacet(minEx.getValue().toString());
            }

            else if (obj instanceof XmlSchemaMaxInclusiveFacet) {
                XmlSchemaMaxInclusiveFacet maxIn = (XmlSchemaMaxInclusiveFacet) obj;
                metaInfHolder.setMaxInclusiveFacet(maxIn.getValue().toString());
            }

            else if (obj instanceof XmlSchemaMinInclusiveFacet) {
                XmlSchemaMinInclusiveFacet minIn = (XmlSchemaMinInclusiveFacet) obj;
                metaInfHolder.setMinInclusiveFacet(minIn.getValue().toString());
            }

            else if (obj instanceof XmlSchemaMaxLengthFacet) {
                XmlSchemaMaxLengthFacet maxLen = (XmlSchemaMaxLengthFacet) obj;
                metaInfHolder.setMaxLengthFacet(Integer.parseInt(maxLen.getValue().toString()));
            }

            else if (obj instanceof XmlSchemaMinLengthFacet) {
                XmlSchemaMinLengthFacet minLen = (XmlSchemaMinLengthFacet) obj;
                metaInfHolder.setMinLengthFacet(Integer.parseInt(minLen.getValue().toString()));
            }
        }
    }

    /**
     * Handle any attribute
     *
     * @param metainf
     */
    private void processAnyAttribute(BeanWriterMetaInfoHolder metainf, XmlSchemaAnyAttribute anyAtt) {

        //The best thing we can do here is to add a set of OMAttributes
        //since attributes do not have the notion of minoccurs/maxoccurs the
        //safest option here is to have an OMAttribute array
        QName qName = new QName(EXTRA_ATTRIBUTE_FIELD_NAME);
        metainf.registerMapping(qName,
                null,
                writer.getDefaultAttribArrayClassName(),//always generate an array of
                //OMAttributes
                SchemaConstants.ANY_TYPE);
        metainf.addtStatus(qName, SchemaConstants.ATTRIBUTE_TYPE);
        metainf.addtStatus(qName, SchemaConstants.ARRAY_TYPE);

    }


    /**
     * Process the attribute
     *
     * @param att
     * @param metainf
     */
    public void processAttribute(XmlSchemaAttribute att, BeanWriterMetaInfoHolder metainf, XmlSchema parentSchema)
            throws SchemaCompilationException {

        QName schemaTypeName = att.getSchemaTypeName();
        if (schemaTypeName != null) {
            if (att.getQName() != null) {
                if (baseSchemaTypeMap.containsKey(schemaTypeName)) {

                    metainf.registerMapping(att.getQName(), schemaTypeName,
                            baseSchemaTypeMap.get(schemaTypeName).toString(), SchemaConstants.ATTRIBUTE_TYPE);

                    // add optional attribute status if set
                    String use = att.getUse().getValue();
                    if (USE_NONE.equals(use) || USE_OPTIONAL.equals(use)) {
                        metainf.addtStatus(att.getQName(), SchemaConstants.OPTIONAL_TYPE);
                    }

                    String className = findClassName(schemaTypeName, false);

                    att.addMetaInfo(
                            SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY,
                            className);
                    // after
                } else {
                    XmlSchemaType type = getType(parentSchema, schemaTypeName);
                    if (type instanceof XmlSchemaSimpleType) {
                        XmlSchemaSimpleType simpleType = (XmlSchemaSimpleType) type;

                        if ((simpleType != null) &&
                                (simpleType.getContent() instanceof XmlSchemaSimpleTypeRestriction)) {
                            // we only support simple type restriction
                            if (!isAlreadyProcessed(schemaTypeName)) {
                                //process simple type
                                processSimpleSchemaType(simpleType, null, parentSchema, null);
                            }
                            metainf.registerMapping(att.getQName(),
                                    schemaTypeName,
                                    processedTypemap.get(schemaTypeName).toString(),
                                    SchemaConstants.ATTRIBUTE_TYPE);
                            // add optional attribute status if set
                            String use = att.getUse().getValue();
                            if (USE_NONE.equals(use) || USE_OPTIONAL.equals(use)) {
                                metainf.addtStatus(att.getQName(), SchemaConstants.OPTIONAL_TYPE);
                            }
                        }

                    }
                }
            } else {
                // this attribute has a type but does not have a name, seems to be invalid
            }

        } else if (att.getRefName() != null) {
            XmlSchema currentParentSchema = resolveParentSchema(att.getRefName(), parentSchema);
            XmlSchemaAttribute xmlSchemaAttribute = getXmlSchemaAttribute(att.getRefName(),currentParentSchema);

            if (xmlSchemaAttribute != null) {
                // call recursively to process the schema
                processAttribute(xmlSchemaAttribute, metainf, currentParentSchema);
            } else {
                throw new SchemaCompilationException("Attribute QName reference refer to an invalid attribute " +
                        att.getRefName());
            }

        } else {
            // this attribute refers to a custom type, probably one of the extended simple types.
            // with the inline scheam definition
            QName attributeQName = att.getQName();
            if (attributeQName != null) {
                XmlSchemaSimpleType attributeSimpleType = att.getSchemaType();
                if (attributeSimpleType == null) {
                    // try to get the schema for using qname
                    QName attributeSchemaQname = att.getSchemaTypeName();
                    if (attributeSchemaQname != null) {
                        attributeSimpleType = (XmlSchemaSimpleType) getType(parentSchema, attributeSchemaQname);
                    }
                }

                if (attributeSimpleType != null) {
                    QName schemaTypeQName = att.getSchemaTypeName();
                    if (schemaTypeQName == null) {
                        // set the parent schema target name space since attribute Qname uri is ""
                        if (attributeSimpleType.getQName() != null) {
                            schemaTypeQName = attributeSimpleType.getQName();
                        } else {
                            schemaTypeQName = new QName(parentSchema.getTargetNamespace(),
                                    attributeQName.getLocalPart() + getNextTypeSuffix(attributeQName.getLocalPart()));

                        }
                    }
                    if (!isAlreadyProcessed(schemaTypeQName)){
                        // we have to process only if it has not processed
                        processSimpleSchemaType(attributeSimpleType, null, parentSchema, schemaTypeQName);
                    }
                    metainf.registerMapping(att.getQName(),
                            schemaTypeQName,
                            processedTypemap.get(schemaTypeQName).toString(),
                            SchemaConstants.ATTRIBUTE_TYPE);
                    // add optional attribute status if set
                    String use = att.getUse().getValue();
                    if (USE_NONE.equals(use) || USE_OPTIONAL.equals(use)) {
                        metainf.addtStatus(att.getQName(), SchemaConstants.OPTIONAL_TYPE);
                    }
                } else {
                    // TODO: handle the case when no attribute type specifed
                    log.warn("No attribute type has defined to the Attribute " + attributeQName);
                }

            } else {
                throw new SchemaCompilationException("Attribute QName reference refer to an invalid attribute " +
                        attributeQName);
            }

        }
    }

    private XmlSchemaAttribute getXmlSchemaAttribute(QName attributeQName,
                                                     XmlSchema parentSchema){
        XmlSchemaAttribute xmlSchemaAttribute =
                (XmlSchemaAttribute) parentSchema.getAttributes().getItem(attributeQName);
        if (xmlSchemaAttribute == null){
            // i.e this attribute can be in a included or imported schema
            xmlSchemaAttribute = (XmlSchemaAttribute) parentSchema.getAttributes().getItem(attributeQName);
            if (xmlSchemaAttribute == null) {
                // try to find in an import or an include
                XmlSchemaObjectCollection includes = parentSchema.getIncludes();
                if (includes != null) {
                    Iterator includesIter = includes.getIterator();
                    Object object = null;
                    while (includesIter.hasNext()) {
                        object = includesIter.next();
                        if (object instanceof XmlSchemaImport) {
                            XmlSchema schema1 = ((XmlSchemaImport) object).getSchema();
                            xmlSchemaAttribute = (XmlSchemaAttribute) schema1.getAttributes().getItem(attributeQName);
                        }
                        if (object instanceof XmlSchemaInclude) {
                            XmlSchema schema1 = ((XmlSchemaInclude) object).getSchema();
                            xmlSchemaAttribute = (XmlSchemaAttribute) schema1.getAttributes().getItem(attributeQName);
                        }
                        if (xmlSchemaAttribute != null){
                            break;
                        }
                    }
                }
            }
        }
        return xmlSchemaAttribute;
    }

    /**
     * Process a particle- A particle may be a sequence,all or a choice
     * @param parentElementQName - this can either be parent element QName or parent Complex type qname
     * @param particle - particle being processed
     * @param metainfHolder -
     * @param parentSchema
     * @throws SchemaCompilationException
     */
    private void processParticle(QName parentElementQName,
                                 XmlSchemaParticle particle,
                                 BeanWriterMetaInfoHolder metainfHolder
            , XmlSchema parentSchema) throws SchemaCompilationException {

        if (particle instanceof XmlSchemaSequence) {
            XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) particle;

            XmlSchemaObjectCollection items = xmlSchemaSequence.getItems();
            //TODO: support parentElementQName null instances. i.e for extensions
            if ((xmlSchemaSequence.getMaxOccurs() > 1) && (parentElementQName != null)) {
                // we have to process many sequence types
                BeanWriterMetaInfoHolder beanWriterMetaInfoHolder = new BeanWriterMetaInfoHolder();
                process(parentElementQName, items, beanWriterMetaInfoHolder, true, parentSchema);
                beanWriterMetaInfoHolder.setParticleClass(true);
                QName sequenceQName = new QName(parentElementQName.getNamespaceURI(),
                         parentElementQName.getLocalPart() + "Sequence");
                String javaClassName = writeComplexParticle(sequenceQName,beanWriterMetaInfoHolder);
                processedTypemap.put(sequenceQName, javaClassName);

                // add this as an array to the original class
                metainfHolder.registerMapping(sequenceQName,
                        sequenceQName,
                        findClassName(sequenceQName,true),
                        SchemaConstants.ARRAY_TYPE);
                metainfHolder.setOrdered(true);
                metainfHolder.registerQNameIndex(sequenceQName,metainfHolder.getOrderStartPoint() + 1);
                metainfHolder.setHasParticleType(true);
                metainfHolder.addtStatus(sequenceQName,SchemaConstants.PARTICLE_TYPE_ELEMENT);
                metainfHolder.addMaxOccurs(sequenceQName,xmlSchemaSequence.getMaxOccurs());
                metainfHolder.addMinOccurs(sequenceQName,xmlSchemaSequence.getMinOccurs());


            } else {
                if (options.isBackwordCompatibilityMode()) {
                    process(parentElementQName,items, metainfHolder, false, parentSchema);
                } else {
                    process(parentElementQName,items, metainfHolder, true, parentSchema);
                }
            }

        } else if (particle instanceof XmlSchemaAll) {
            XmlSchemaObjectCollection items = ((XmlSchemaAll) particle).getItems();
            process(parentElementQName,items, metainfHolder, false, parentSchema);
        } else if (particle instanceof XmlSchemaChoice) {
            XmlSchemaChoice xmlSchemaChoice = (XmlSchemaChoice) particle;
            XmlSchemaObjectCollection items = ((XmlSchemaChoice) particle).getItems();

            if ((xmlSchemaChoice.getMaxOccurs() > 1)) {
                // we have to process many sequence types
                BeanWriterMetaInfoHolder beanWriterMetaInfoHolder = new BeanWriterMetaInfoHolder();
                beanWriterMetaInfoHolder.setChoice(true);
                process(parentElementQName,items, beanWriterMetaInfoHolder, false, parentSchema);
                beanWriterMetaInfoHolder.setParticleClass(true);
                QName choiceQName = new QName(parentElementQName.getNamespaceURI(),
                         parentElementQName.getLocalPart() + "Choice");
                String javaClassName = writeComplexParticle(choiceQName,beanWriterMetaInfoHolder);
                processedTypemap.put(choiceQName, javaClassName);

                // add this as an array to the original class
                metainfHolder.registerMapping(choiceQName,
                        choiceQName,
                        findClassName(choiceQName,true),
                        SchemaConstants.ARRAY_TYPE);
                metainfHolder.setOrdered(true);
                metainfHolder.setHasParticleType(true);
                metainfHolder.registerQNameIndex(choiceQName,metainfHolder.getOrderStartPoint() + 1);
                metainfHolder.addtStatus(choiceQName,SchemaConstants.PARTICLE_TYPE_ELEMENT);
                metainfHolder.addMaxOccurs(choiceQName,xmlSchemaChoice.getMaxOccurs());
                metainfHolder.addMinOccurs(choiceQName,xmlSchemaChoice.getMinOccurs());

            } else {
                metainfHolder.setChoice(true);
                process(parentElementQName,items, metainfHolder, false, parentSchema);
            }


        } else if (particle instanceof XmlSchemaGroupRef){

            XmlSchemaGroupRef xmlSchemaGroupRef = (XmlSchemaGroupRef) particle;
            QName groupQName = xmlSchemaGroupRef.getRefName();
            if (groupQName != null) {
                if (!processedTypemap.containsKey(groupQName)) {
                    // processe the schema here
                    XmlSchema resolvedParentSchema = resolveParentSchema(groupQName, parentSchema);
                    XmlSchemaGroup xmlSchemaGroup = getGroup(groupQName, resolvedParentSchema);
                    if (xmlSchemaGroup != null) {
                        processGroup(xmlSchemaGroup, groupQName, parentSchema);
                    } else {
                        throw new SchemaCompilationException("Refered Group " + groupQName.getLocalPart() + " can not be found ");
                    }
                }
            } else {
                throw new SchemaCompilationException("Referenced name is null");
            }
            boolean isArray = xmlSchemaGroupRef.getMaxOccurs() > 1;

            // add this as an array to the original class
            metainfHolder.registerMapping(groupQName, groupQName, findClassName(groupQName, isArray));
            if (isArray) {
                metainfHolder.addtStatus(groupQName, SchemaConstants.ARRAY_TYPE);
            }
            metainfHolder.addtStatus(groupQName, SchemaConstants.PARTICLE_TYPE_ELEMENT);
            metainfHolder.addMaxOccurs(groupQName, xmlSchemaGroupRef.getMaxOccurs());
            metainfHolder.addMinOccurs(groupQName, xmlSchemaGroupRef.getMinOccurs());
            metainfHolder.setHasParticleType(true);
            metainfHolder.setOrdered(true);
            metainfHolder.registerQNameIndex(groupQName,metainfHolder.getOrderStartPoint() + 1);

        }
    }

    /**
     *
     * @param parentElementQName - this could either be the complex type parentElementQName or element parentElementQName
     * @param items
     * @param metainfHolder
     * @param order
     * @param parentSchema
     * @throws SchemaCompilationException
     */
    private void process(QName parentElementQName,
                         XmlSchemaObjectCollection items,
                         BeanWriterMetaInfoHolder metainfHolder,
                         boolean order,
                         XmlSchema parentSchema) throws SchemaCompilationException {
        int count = items.getCount();
        Map processedElementArrayStatusMap = new LinkedHashMap();
        Map processedElementTypeMap = new LinkedHashMap();
        List localNillableList = new ArrayList();

        Map particleQNameMap = new HashMap();

        // this list is used to keep the details of the
        // elements within a choice withing sequence
        List innerChoiceElementList = new ArrayList();

        Map elementOrderMap = new HashMap();

        int sequenceCounter = 0;
        for (int i = 0; i < count; i++) {
            XmlSchemaObject item = items.getItem(i);

            if (item instanceof XmlSchemaElement) {
                //recursively process the element
                XmlSchemaElement xsElt = (XmlSchemaElement) item;

                boolean isArray = isArray(xsElt);
                processElement(xsElt, processedElementTypeMap, localNillableList, parentSchema); //we know for sure this is not an outer type
                processedElementArrayStatusMap.put(xsElt, (isArray) ? Boolean.TRUE : Boolean.FALSE);
                if (order) {
                    //we need to keep the order of the elements. So push the elements to another
                    //hashmap with the order number
                    elementOrderMap.put(xsElt, new Integer(sequenceCounter));
                }

                //handle xsd:any ! We place an OMElement (or an array of OMElements) in the generated class
            } else if (item instanceof XmlSchemaAny) {
                XmlSchemaAny any = (XmlSchemaAny) item;
                processedElementTypeMap.put(new QName(ANY_ELEMENT_FIELD_NAME), any);
                //any can also be inside a sequence
                if (order) {
                    elementOrderMap.put(any, new Integer(sequenceCounter));
                }
                //we do not register the array status for the any type
                processedElementArrayStatusMap.put(any, isArray(any) ? Boolean.TRUE : Boolean.FALSE);
            } else if (item instanceof XmlSchemaSequence) {
                // we have to process many sequence types

                XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) item;
                if (xmlSchemaSequence.getItems().getCount() > 0) {
                    BeanWriterMetaInfoHolder beanWriterMetaInfoHolder = new BeanWriterMetaInfoHolder();
                    process(parentElementQName, xmlSchemaSequence.getItems(), beanWriterMetaInfoHolder, true, parentSchema);
                    beanWriterMetaInfoHolder.setParticleClass(true);
                    String localName = parentElementQName.getLocalPart() + "Sequence";
                    QName sequenceQName = new QName(parentElementQName.getNamespaceURI(),
                            localName + getNextTypeSuffix(localName));
                    String javaClassName = writeComplexParticle(sequenceQName, beanWriterMetaInfoHolder);
                    processedTypemap.put(sequenceQName, javaClassName);

                    //put the partical to array
                    Boolean isArray = xmlSchemaSequence.getMaxOccurs() > 1 ? Boolean.TRUE : Boolean.FALSE;
                    processedElementArrayStatusMap.put(item, isArray);
                    particleQNameMap.put(item, sequenceQName);

                    if (order) {
                        elementOrderMap.put(item, new Integer(sequenceCounter));
                    }
                }

            } else if (item instanceof XmlSchemaChoice) {
                // we have to process many sequence types

                XmlSchemaChoice xmlSchemaChoice = (XmlSchemaChoice) item;
                if (xmlSchemaChoice.getItems().getCount() > 0) {
                    BeanWriterMetaInfoHolder beanWriterMetaInfoHolder = new BeanWriterMetaInfoHolder();
                    beanWriterMetaInfoHolder.setChoice(true);
                    process(parentElementQName, xmlSchemaChoice.getItems(), beanWriterMetaInfoHolder, false, parentSchema);
                    beanWriterMetaInfoHolder.setParticleClass(true);
                    String localName = parentElementQName.getLocalPart() + "Choice";
                    QName choiceQName = new QName(parentElementQName.getNamespaceURI(),
                            localName + getNextTypeSuffix(localName));
                    String javaClassName = writeComplexParticle(choiceQName, beanWriterMetaInfoHolder);
                    processedTypemap.put(choiceQName, javaClassName);

                    //put the partical to array
                    Boolean isArray = xmlSchemaChoice.getMaxOccurs() > 1 ? Boolean.TRUE : Boolean.FALSE;
                    processedElementArrayStatusMap.put(item, isArray);
                    particleQNameMap.put(item, choiceQName);

                    if (order) {
                        elementOrderMap.put(item, new Integer(sequenceCounter));
                    }
                }

            } else if (item instanceof XmlSchemaGroupRef) {

                XmlSchemaGroupRef xmlSchemaGroupRef = (XmlSchemaGroupRef) item;
                QName groupQName = xmlSchemaGroupRef.getRefName();
                if (groupQName != null){
                    if (!processedTypemap.containsKey(groupQName)){
                        // processe the schema here
                        XmlSchema resolvedParentSchema = resolveParentSchema(groupQName,parentSchema);
                        XmlSchemaGroup xmlSchemaGroup = getGroup(groupQName,resolvedParentSchema);
                        if (xmlSchemaGroup != null){
                            processGroup(xmlSchemaGroup, groupQName, parentSchema);
                        } else {
                            throw new SchemaCompilationException("Refered Group "+ groupQName.getLocalPart() + " can not be found ");
                        }

                    }

                    Boolean isArray = xmlSchemaGroupRef.getMaxOccurs() > 1 ? Boolean.TRUE : Boolean.FALSE;
                    processedElementArrayStatusMap.put(item,isArray);
                    particleQNameMap.put(item,groupQName);

                    if (order){
                        elementOrderMap.put(item, new Integer(sequenceCounter));
                    }

                } else {
                    throw new SchemaCompilationException("Referenced name is null");
                }

            } else if (order && (item instanceof XmlSchemaChoice)) {

                // this is a tempory patch for process only inner sequence choices
                // but we have do this with a proper design
                XmlSchemaChoice choice = (XmlSchemaChoice) item;
                XmlSchemaObject choiceChild;
                XmlSchemaObjectCollection schemaItems = choice.getItems();
                for (int j = 0; j < schemaItems.getCount(); j++) {
                   choiceChild = schemaItems.getItem(j);
                   if (choiceChild instanceof XmlSchemaElement){
                       // i.e this is an inner choice element
                       //recursively process the element
                        XmlSchemaElement xsElt = (XmlSchemaElement) choiceChild;

                        boolean isArray = isArray(xsElt);
                        processElement(xsElt, processedElementTypeMap, localNillableList, parentSchema); //we know for sure this is not an outer type
                        processedElementArrayStatusMap.put(xsElt, (isArray) ? Boolean.TRUE : Boolean.FALSE);
                        if (order) {
                            //we need to keep the order of the elements. So push the elements to another
                            //hashmap with the order number
                            elementOrderMap.put(xsElt, new Integer(sequenceCounter));
                            sequenceCounter++;
                        }
                       innerChoiceElementList.add(xsElt.getQName());
                   }
                }

            } else {
                //there may be other types to be handled here. Add them
                //when we are ready
            }
          sequenceCounter++;
        }

        // loop through the processed items and add them to the matainf object
        Iterator processedElementsIterator = processedElementArrayStatusMap.keySet().iterator();
        int startingItemNumberOrder = metainfHolder.getOrderStartPoint();
        while (processedElementsIterator.hasNext()) {
            Object child = processedElementsIterator.next();

            // process the XmlSchemaElement
            if (child instanceof XmlSchemaElement) {
                XmlSchemaElement elt = (XmlSchemaElement) child;
                QName referencedQName = null;


                if (elt.getQName() != null) {
                    referencedQName = elt.getQName();
                    QName schemaTypeQName = elt.getSchemaType() != null ? elt.getSchemaType().getQName() : elt.getSchemaTypeName();
                    if (schemaTypeQName != null) {
                        String clazzName = (String) processedElementTypeMap.get(elt.getQName());
                        metainfHolder.registerMapping(referencedQName,
                                schemaTypeQName,
                                clazzName,
                                ((Boolean) processedElementArrayStatusMap.get(elt)).booleanValue() ?
                                        SchemaConstants.ARRAY_TYPE :
                                        SchemaConstants.ELEMENT_TYPE);
                        if (innerChoiceElementList.contains(referencedQName)){
                            metainfHolder.addtStatus(referencedQName,SchemaConstants.INNER_CHOICE_ELEMENT);
                        }
                    }
                }

                if (elt.getRefName() != null) { //probably this is referenced
                    referencedQName = elt.getRefName();
                    boolean arrayStatus = ((Boolean) processedElementArrayStatusMap.get(elt)).booleanValue();
                    String clazzName = findRefClassName(referencedQName, arrayStatus);
                    if (clazzName == null) {
                        clazzName = findClassName(referencedQName, arrayStatus);
                    }
                    XmlSchemaElement refElement = getReferencedElement(parentSchema, referencedQName);

                    // register the mapping if we found the referenced element
                    // else throw an exception
                    if (refElement != null) {
                        metainfHolder.registerMapping(referencedQName,
                                refElement.getSchemaTypeName()
                                , clazzName,
                                arrayStatus ?
                                        SchemaConstants.ARRAY_TYPE :
                                        SchemaConstants.ELEMENT_TYPE);
                    } else {
                        if (referencedQName.equals(SchemaConstants.XSD_SCHEMA)) {
                            metainfHolder.registerMapping(referencedQName,
                                    null,
                                    writer.getDefaultClassName(),
                                    SchemaConstants.ANY_TYPE);
                        } else {
                            throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("schema.referencedElementNotFound", referencedQName.toString()));
                        }
                    }
                }

                if (referencedQName == null) {
                    throw new SchemaCompilationException(SchemaCompilerMessages.getMessage("schema.emptyName"));
                }

                //register the occurence counts
                metainfHolder.addMaxOccurs(referencedQName, elt.getMaxOccurs());
                metainfHolder.addMinOccurs(referencedQName, elt.getMinOccurs());
                //we need the order to be preserved. So record the order also
                if (order) {
                    //record the order in the metainf holder
                    Integer integer = (Integer) elementOrderMap.get(elt);
                    metainfHolder.registerQNameIndex(referencedQName,
                            startingItemNumberOrder + integer.intValue());
                }

                //get the nillable state and register that on the metainf holder
                if (localNillableList.contains(elt.getQName())) {
                    metainfHolder.registerNillableQName(elt.getQName());
                }

                //get the binary state and add that to the status map
                if (isBinary(elt)) {
                    metainfHolder.addtStatus(elt.getQName(),
                            SchemaConstants.BINARY_TYPE);
                }
                // process the XMLSchemaAny
            } else if (child instanceof XmlSchemaAny) {
                XmlSchemaAny any = (XmlSchemaAny) child;

                //since there is only one element here it does not matter
                //for the constant. However the problem occurs if the users
                //uses the same name for an element decalration
                QName anyElementFieldName = new QName(ANY_ELEMENT_FIELD_NAME);

                //this can be an array or a single element
                boolean isArray = ((Boolean) processedElementArrayStatusMap.get(any)).booleanValue();
                metainfHolder.registerMapping(anyElementFieldName,
                        null,
                        isArray ? writer.getDefaultClassArrayName() : writer.getDefaultClassName(),
                        SchemaConstants.ANY_TYPE);
                //if it's an array register an extra status flag with the system
                if (isArray) {
                    metainfHolder.addtStatus(anyElementFieldName,
                            SchemaConstants.ARRAY_TYPE);
                }
                metainfHolder.addMaxOccurs(anyElementFieldName, any.getMaxOccurs());
                metainfHolder.addMinOccurs(anyElementFieldName, any.getMinOccurs());

                if (order) {
                    //record the order in the metainf holder for the any
                    Integer integer = (Integer) elementOrderMap.get(any);
                    metainfHolder.registerQNameIndex(anyElementFieldName,
                            startingItemNumberOrder + integer.intValue());
                }
            } else if (child instanceof XmlSchemaSequence) {
                XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) child;
                QName sequenceQName = (QName) particleQNameMap.get(child);
                boolean isArray = xmlSchemaSequence.getMaxOccurs() > 1;

                // add this as an array to the original class
                metainfHolder.registerMapping(sequenceQName,
                        sequenceQName,
                        findClassName(sequenceQName, isArray));
                if (isArray) {
                    metainfHolder.addtStatus(sequenceQName, SchemaConstants.ARRAY_TYPE);
                }
                metainfHolder.addtStatus(sequenceQName, SchemaConstants.PARTICLE_TYPE_ELEMENT);
                metainfHolder.addMaxOccurs(sequenceQName, xmlSchemaSequence.getMaxOccurs());
                metainfHolder.addMinOccurs(sequenceQName, xmlSchemaSequence.getMinOccurs());
                metainfHolder.setHasParticleType(true);

                if (order) {
                    //record the order in the metainf holder for the any
                    Integer integer = (Integer) elementOrderMap.get(child);
                    metainfHolder.registerQNameIndex(sequenceQName,
                            startingItemNumberOrder + integer.intValue());
                }
            } else if (child instanceof XmlSchemaChoice) {
                XmlSchemaChoice xmlSchemaChoice = (XmlSchemaChoice) child;
                QName choiceQName = (QName) particleQNameMap.get(child);
                boolean isArray = xmlSchemaChoice.getMaxOccurs() > 1;

                // add this as an array to the original class
                metainfHolder.registerMapping(choiceQName,
                        choiceQName,
                        findClassName(choiceQName, isArray));
                if (isArray) {
                    metainfHolder.addtStatus(choiceQName, SchemaConstants.ARRAY_TYPE);
                }
                metainfHolder.addtStatus(choiceQName, SchemaConstants.PARTICLE_TYPE_ELEMENT);
                metainfHolder.addMaxOccurs(choiceQName, xmlSchemaChoice.getMaxOccurs());
                metainfHolder.addMinOccurs(choiceQName, xmlSchemaChoice.getMinOccurs());
                metainfHolder.setHasParticleType(true);

                if (order) {
                    //record the order in the metainf holder for the any
                    Integer integer = (Integer) elementOrderMap.get(child);
                    metainfHolder.registerQNameIndex(choiceQName,
                            startingItemNumberOrder + integer.intValue());
                }
            } else if (child instanceof XmlSchemaGroupRef) {
                XmlSchemaGroupRef xmlSchemaGroupRef = (XmlSchemaGroupRef) child;
                QName groupQName = (QName) particleQNameMap.get(child);
                boolean isArray = xmlSchemaGroupRef.getMaxOccurs() > 1;

                // add this as an array to the original class
                metainfHolder.registerMapping(groupQName,
                        groupQName,
                        findClassName(groupQName, isArray));
                if (isArray) {
                    metainfHolder.addtStatus(groupQName, SchemaConstants.ARRAY_TYPE);
                }
                metainfHolder.addtStatus(groupQName, SchemaConstants.PARTICLE_TYPE_ELEMENT);
                metainfHolder.addMaxOccurs(groupQName, xmlSchemaGroupRef.getMaxOccurs());
                metainfHolder.addMinOccurs(groupQName, xmlSchemaGroupRef.getMinOccurs());
                metainfHolder.setHasParticleType(true);

                if (order) {
                    //record the order in the metainf holder for the any
                    Integer integer = (Integer) elementOrderMap.get(child);
                    metainfHolder.registerQNameIndex(groupQName,
                            startingItemNumberOrder + integer.intValue());
                }
            }
        }

        //set the ordered flag in the metainf holder
        metainfHolder.setOrdered(order);
    }

    private XmlSchemaGroup getGroup(QName groupQName,
                          XmlSchema parentSchema){
         XmlSchemaGroup xmlSchemaGroup =
                (XmlSchemaGroup) parentSchema.getGroups().getItem(groupQName);
        if (xmlSchemaGroup == null){
            // i.e this attribute can be in a included or imported schema
            xmlSchemaGroup = (XmlSchemaGroup) parentSchema.getGroups().getItem(groupQName);
            if (xmlSchemaGroup == null) {
                // try to find in an import or an include
                XmlSchemaObjectCollection includes = parentSchema.getIncludes();
                if (includes != null) {
                    Iterator includesIter = includes.getIterator();
                    Object object = null;
                    while (includesIter.hasNext()) {
                        object = includesIter.next();
                        if (object instanceof XmlSchemaImport) {
                            XmlSchema schema1 = ((XmlSchemaImport) object).getSchema();
                            xmlSchemaGroup = (XmlSchemaGroup) schema1.getGroups().getItem(groupQName);
                        }
                        if (object instanceof XmlSchemaInclude) {
                            XmlSchema schema1 = ((XmlSchemaInclude) object).getSchema();
                            xmlSchemaGroup = (XmlSchemaGroup) schema1.getGroups().getItem(groupQName);
                        }
                        if (xmlSchemaGroup != null){
                            break;
                        }
                    }
                }
            }
        }
        return xmlSchemaGroup;
    }

    /**
     *
     * @param xmlSchemaGroup
     * @param schemaGroupQName- we have to pass this since xml schema does not provide
     * this properly
     * @param parentSchema
     * @throws SchemaCompilationException
     */

    private void processGroup(XmlSchemaGroup xmlSchemaGroup,
                              QName schemaGroupQName,
                              XmlSchema parentSchema) throws SchemaCompilationException {

        // find the group base item
        XmlSchemaGroupBase xmlSchemaGroupBase = xmlSchemaGroup.getParticle();
        if (xmlSchemaGroupBase != null){
            if (xmlSchemaGroupBase instanceof XmlSchemaSequence){
                XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) xmlSchemaGroupBase;
                if (xmlSchemaSequence.getItems().getCount() > 0) {
                    BeanWriterMetaInfoHolder beanWriterMetaInfoHolder = new BeanWriterMetaInfoHolder();
                    process(schemaGroupQName, xmlSchemaSequence.getItems(), beanWriterMetaInfoHolder, true, parentSchema);
                    beanWriterMetaInfoHolder.setParticleClass(true);
                    String javaClassName = writeComplexParticle(schemaGroupQName, beanWriterMetaInfoHolder);
                    processedTypemap.put(schemaGroupQName, javaClassName);
                }

            } else if (xmlSchemaGroupBase instanceof XmlSchemaChoice){
                XmlSchemaChoice xmlSchemaChoice = (XmlSchemaChoice) xmlSchemaGroupBase;
                if (xmlSchemaChoice.getItems().getCount() > 0) {
                    BeanWriterMetaInfoHolder beanWriterMetaInfoHolder = new BeanWriterMetaInfoHolder();
                    beanWriterMetaInfoHolder.setChoice(true);
                    process(schemaGroupQName, xmlSchemaChoice.getItems(), beanWriterMetaInfoHolder, false, parentSchema);
                    beanWriterMetaInfoHolder.setParticleClass(true);
                    String javaClassName = writeComplexParticle(schemaGroupQName, beanWriterMetaInfoHolder);
                    processedTypemap.put(schemaGroupQName, javaClassName);
                }
            }
        }
    }

    private XmlSchemaType getType(XmlSchema schema, QName schemaTypeName) throws SchemaCompilationException {
        // first check with the current parent schema
        XmlSchemaType typeByName = schema.getTypeByName(schemaTypeName);
        if (typeByName == null) {
            // try to resolve schema using the target names space
            schema = resolveParentSchema(schemaTypeName, schema);
            typeByName = schema.getTypeByName(schemaTypeName);
            if (typeByName == null) {
                // The referenced element seems to come from an imported
                // schema.
                XmlSchemaObjectCollection includes = schema.getIncludes();
                if (includes != null) {
                    Iterator tempIterator = includes.getIterator();
                    while (tempIterator.hasNext()) {
                        Object o = tempIterator.next();
                        XmlSchema inclSchema = null;
                        if (o instanceof XmlSchemaImport) {
                            inclSchema = ((XmlSchemaImport) o).getSchema();
                            if (inclSchema == null) {
                                inclSchema = (XmlSchema) loadedSchemaMap.get(((XmlSchemaImport) o).getNamespace());
                            }
                        }
                        if (o instanceof XmlSchemaInclude) {
                            inclSchema = ((XmlSchemaInclude) o).getSchema();
                        }
                        // get the element from the included schema
                        if (inclSchema != null) {
                            typeByName = inclSchema.getTypeByName(schemaTypeName);
                        }
                        if (typeByName != null) {
                            // we found the referenced element an can break the loop
                            break;
                        }
                    }
                }
            }
        }
        return typeByName;
    }

    private XmlSchemaElement getReferencedElement(XmlSchema parentSchema, QName referencedQName)
            throws SchemaCompilationException {
        XmlSchemaElement refElement = parentSchema.getElementByName(referencedQName);
        if (refElement == null){
            XmlSchema schema = resolveParentSchema(referencedQName, parentSchema);
            refElement = schema.getElementByName(referencedQName);
            if (refElement == null) {
                // The referenced element seems to come from an imported
                // schema.
                refElement = getReferenceElementFromSchema(schema, referencedQName);
            }
        }

        return refElement;
    }

    private XmlSchemaElement getReferenceElementFromSchema(
            XmlSchema schema,
            QName referencedQName) {
        XmlSchemaElement refElement = null;
        XmlSchemaObjectCollection includes = schema.getIncludes();
        if (includes != null) {
            Iterator tempIterator = includes.getIterator();
            while (tempIterator.hasNext()) {
                Object o = tempIterator.next();
                XmlSchema inclSchema = null;

                if (o instanceof XmlSchemaInclude) {
                    inclSchema = ((XmlSchemaInclude) o).getSchema();
                    if (inclSchema != null) {
                        // first check in the scheam
                        refElement = inclSchema.getElementByName(referencedQName);
                        if (refElement == null) {
                            // try to find the element in an inner schema
                            refElement = getReferenceElementFromSchema(inclSchema, referencedQName);
                        }
                        if (refElement != null) {
                            // we have found the element so exit from while loop;
                            break;
                        }
                    }
                }

                if (o instanceof XmlSchemaImport) {
                    inclSchema = ((XmlSchemaImport) o).getSchema();
                    if (inclSchema == null) {
                        inclSchema = (XmlSchema) loadedSchemaMap.get(((XmlSchemaImport) o).getNamespace());
                    }
                    if (inclSchema != null) {
                        // first check in the scheam
                        refElement = inclSchema.getElementByName(referencedQName);
                        if (refElement == null) {
                            // try to find the element in an inner schema
                            refElement = getReferenceElementFromSchema(inclSchema, referencedQName);
                        }
                        if (refElement != null) {
                            // we have found the element so exit from while loop;
                            break;
                        }
                    }
                }

            }
        }
        return refElement;
    }

    /**
     * Checks whether a given element is a binary element
     *
     * @param elt
     */
    private boolean isBinary(XmlSchemaElement elt) {
        return elt.getSchemaType() != null &&
                SchemaConstants.XSD_BASE64.equals(elt.getSchemaType().getQName());
    }

    /**
     * Checks whether a given qname is a binary
     *
     * @param qName
     */
    private boolean isBinary(QName qName) {
        return qName != null &&
                SchemaConstants.XSD_BASE64.equals(qName);
    }

    /**
     * @param simpleType
     * @param xsElt
     * @param parentSchema
     * @param qname        - fake Qname to use if the xsElt is null.
     * @throws SchemaCompilationException
     */
    private void processSimpleSchemaType(XmlSchemaSimpleType simpleType,
                                         XmlSchemaElement xsElt,
                                         XmlSchema parentSchema,
                                         QName qname) throws SchemaCompilationException {

        String fullyQualifiedClassName = null;
        if (simpleType.getQName() != null) {
            if (processedTypemap.containsKey(simpleType.getQName())
                    || baseSchemaTypeMap.containsKey(simpleType.getQName())) {
                return;
            }

            // Must do this up front to support recursive types
            fullyQualifiedClassName = writer.makeFullyQualifiedClassName(simpleType.getQName());
            // we put the qname to processed type map it is only named type
            // otherwise we have to any way process that element.
            processedTypemap.put(simpleType.getQName(), fullyQualifiedClassName);
        } else {

            QName fakeQname;
            if (xsElt != null) {
                fakeQname = new QName(xsElt.getQName().getNamespaceURI(), xsElt.getQName().getLocalPart() + getNextTypeSuffix(xsElt.getQName().getLocalPart()));
                // we have to set this otherwise the ours attribute would not set properly if refered to this simple
                // type from any other element
                xsElt.setSchemaTypeName(fakeQname);
                changedElementSet.add(xsElt);

            } else {
                fakeQname = qname;
            }
            if (processedTypemap.containsKey(fakeQname)
                    || baseSchemaTypeMap.containsKey(fakeQname)) {
                return;
            }
            fullyQualifiedClassName = writer.makeFullyQualifiedClassName(fakeQname);
            simpleType.addMetaInfo(SchemaConstants.SchemaCompilerInfoHolder.FAKE_QNAME, fakeQname);

            // should put this to the processedTypemap to generate the code correctly
            processedTypemap.put(fakeQname, fullyQualifiedClassName);
        }

        //register that in the schema metainfo bag
        simpleType.addMetaInfo(SchemaConstants.SchemaCompilerInfoHolder.CLASSNAME_KEY,
                fullyQualifiedClassName);

        BeanWriterMetaInfoHolder metaInfHolder = processSimpleType(simpleType, parentSchema);
        metaInfHolder.setSimple(true);

        if (simpleType.getQName() == null) {
            this.processedAnonymousComplexTypesMap.put(xsElt, metaInfHolder);
            QName fakeQname;
            if (xsElt != null) {
                fakeQname = new QName(xsElt.getQName().getNamespaceURI(), xsElt.getQName().getLocalPart());
            } else {
                fakeQname = qname;
                simpleType.setName(fakeQname.getLocalPart());
                simpleType.setSourceURI(fakeQname.getNamespaceURI());
            }
            simpleTypesMap.put(fakeQname, fullyQualifiedClassName);
        }
        //add this information to the metainfo holder
        metaInfHolder.setOwnQname(simpleType.getQName());
        if (fullyQualifiedClassName != null) {
            metaInfHolder.setOwnClassName(fullyQualifiedClassName);
        }
        //write the class. This type mapping would have been populated right now
        //Note - We always write classes for named complex types
        writeSimpleType(simpleType, metaInfHolder);
    }

    private BeanWriterMetaInfoHolder processSimpleType(XmlSchemaSimpleType simpleType, XmlSchema parentSchema) throws SchemaCompilationException {
        BeanWriterMetaInfoHolder metaInfHolder = new BeanWriterMetaInfoHolder();

        // handle the restriction
        XmlSchemaSimpleTypeContent content = simpleType.getContent();
        QName parentSimpleTypeQname = simpleType.getQName();
        if (parentSimpleTypeQname == null) {
            parentSimpleTypeQname = (QName) simpleType.getMetaInfoMap().get(SchemaConstants.SchemaCompilerInfoHolder.FAKE_QNAME);
        }
        if (content != null) {
            if (content instanceof XmlSchemaSimpleTypeRestriction) {
                XmlSchemaSimpleTypeRestriction restriction = (XmlSchemaSimpleTypeRestriction) content;

                QName baseTypeName = restriction.getBaseTypeName();
                //check whether the base type is one of the base schema types

                if (baseSchemaTypeMap.containsKey(baseTypeName)) {
                    //process restriction base type

                    processSimpleRestrictionBaseType(parentSimpleTypeQname, restriction.getBaseTypeName(), metaInfHolder, parentSchema);
                    //process facets
                    processFacets(restriction, metaInfHolder, parentSchema);
                } else {
                    //recurse
                    // this must be a xmlschema bug
                    // it should return the schematype for restriction.getBaseType():
                    XmlSchemaType restrictionBaseType = getType(parentSchema, baseTypeName);
                    if (restrictionBaseType instanceof XmlSchemaSimpleType) {
                        if ((restrictionBaseType != null) && (!isAlreadyProcessed(baseTypeName))) {
                            processSimpleSchemaType((XmlSchemaSimpleType) restrictionBaseType, null, parentSchema, null);
                        }
                        // process restriction
                        processSimpleRestrictionBaseType(parentSimpleTypeQname, restriction.getBaseTypeName(), metaInfHolder, parentSchema);
                    }


                }
            } else if (content instanceof XmlSchemaSimpleTypeUnion) {
                XmlSchemaSimpleTypeUnion simpleTypeUnion = (XmlSchemaSimpleTypeUnion) content;
                QName[] qnames = simpleTypeUnion.getMemberTypesQNames();
                if (qnames != null) {
                    QName qname;
                    for (int i = 0; i < qnames.length; i++) {
                        qname = qnames[i];
                        if (baseSchemaTypeMap.containsKey(qname)) {
                            metaInfHolder.addMemberType(qname, (String) baseSchemaTypeMap.get(qname));
                        } else {
                            XmlSchemaType type = getType(parentSchema, qname);
                            if (type instanceof XmlSchemaSimpleType) {
                                XmlSchemaSimpleType memberSimpleType = (XmlSchemaSimpleType) type;
                                if (!isAlreadyProcessed(qname)) {
                                    processSimpleSchemaType(memberSimpleType, null, parentSchema, null);
                                }
                                metaInfHolder.addMemberType(qname, (String) processedTypemap.get(qname));
                            } else {
                                throw new SchemaCompilationException("Unions can not have complex types as a member type");
                            }
                        }
                    }
                } else {
                    XmlSchemaObjectCollection xmlSchemaObjectCollection = simpleTypeUnion.getBaseTypes();
                    XmlSchemaObject xmlSchemaObject;
                    QName childQname;
                    int i = 1;
                    for (Iterator iter = xmlSchemaObjectCollection.getIterator(); iter.hasNext();) {
                        xmlSchemaObject = (XmlSchemaObject) iter.next();
                        i++;
                        if (xmlSchemaObject instanceof XmlSchemaSimpleType) {
                            XmlSchemaSimpleType unionSimpleType = (XmlSchemaSimpleType) xmlSchemaObject;
                            childQname = unionSimpleType.getQName();
                            if (childQname == null) {
                                // we create a fake Qname for all these simple types since most propably they don't have one
                                childQname = new QName(parentSimpleTypeQname.getNamespaceURI(), parentSimpleTypeQname.getLocalPart() + getNextTypeSuffix(parentSimpleTypeQname.getLocalPart()));
                            }
                            // this is an inner simple type of the union so it shold not have
                            // processed
                            processSimpleSchemaType(unionSimpleType, null, parentSchema, childQname);
                            metaInfHolder.addMemberType(childQname, (String) processedTypemap.get(childQname));
                        }

                    }
                }

                metaInfHolder.setUnion(true);

            } else if (content instanceof XmlSchemaSimpleTypeList) {
                XmlSchemaSimpleTypeList simpleTypeList = (XmlSchemaSimpleTypeList) content;
                QName itemTypeQName = simpleTypeList.getItemTypeName();

                if (itemTypeQName != null) {
                    if (!isAlreadyProcessed(itemTypeQName)) {
                        XmlSchemaType simpleSchemaType = getType(parentSchema, itemTypeQName);
                        if (simpleSchemaType instanceof XmlSchemaSimpleType) {
                            processSimpleSchemaType((XmlSchemaSimpleType) simpleSchemaType, null, parentSchema, null);
                        }
                    }
                } else {
                    XmlSchemaSimpleType listSimpleType = simpleTypeList.getItemType();
                    itemTypeQName = listSimpleType.getQName();
                    if (itemTypeQName == null) {
                        // we create a fake Qname for all these simple types since most propably they don't have one
                        itemTypeQName = new QName(parentSimpleTypeQname.getNamespaceURI(), parentSimpleTypeQname.getLocalPart() + "_type0");
                    }
                    processSimpleSchemaType(listSimpleType, null, parentSchema, itemTypeQName);

                }

                String className = findClassName(itemTypeQName, false);
                metaInfHolder.setList(true);
                metaInfHolder.setItemTypeQName(itemTypeQName);
                metaInfHolder.setItemTypeClassName(className);

            }
        }
        return metaInfHolder;
    }


    /**
     * Find whether a given particle is an array. The logic for deciding
     * whether a given particle is an array is depending on their minOccurs
     * and maxOccurs counts. If Maxoccurs is greater than one (1) then the
     * content is an array.
     * Also no higher level element will have the maxOccurs greater than one
     *
     * @param particle
     * @throws SchemaCompilationException
     */
    private boolean isArray(XmlSchemaParticle particle) throws SchemaCompilationException {
        long minOccurs = particle.getMinOccurs();
        long maxOccurs = particle.getMaxOccurs();

        if (maxOccurs < minOccurs) {
            throw new SchemaCompilationException();
        } else {
            return (maxOccurs > 1);
        }

    }

    HashMap mapTypeCount = new HashMap();
    private String getNextTypeSuffix(String localName) {
        Integer typeCounter = (Integer) mapTypeCount.get(localName);
        int count = 0;
        if (typeCounter != null) {
            if(typeCounter.intValue() == Integer.MAX_VALUE) {
                count = 0;
            } else {
                count = typeCounter.intValue();
            }
        }
        mapTypeCount.put(localName, new Integer(count+1));
        return ("_type" + count);
    }
}
