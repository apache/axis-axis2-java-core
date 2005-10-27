package org.apache.axis2.databinding.schema;

import org.apache.ws.commons.schema.*;
import org.apache.axis2.om.OMElement;

import javax.xml.namespace.QName;
import java.util.*;
import java.io.IOException;
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

public class SchemaCompiler {

    private CompilerOptions options;
    private HashMap processedTypemap;
    //The processedElementMap and the processedElementList have a subtle difference
    //The writing to the processedElementList happens when an outer element is processed.
    //
    private HashMap processedElementMap;
    private HashMap processedAnonymousComplexTypesMap;
    private ArrayList processedElementList;


    private JavaBeanWriter writer;

    private Map baseSchemaTypeMap = TypeMap.getTypeMap();
    private static final String ANY_ELEMENT_FIELD_NAME = "extraElements";
    private static final String EXTRA_ATTRIBUTE_FIELD_NAME = "extraAttributes";


    public HashMap getProcessedElementMap() {
        return processedElementMap;
    }

    /**
     *
     * @param options
     */
    public SchemaCompiler(CompilerOptions options) throws SchemaCompilationException {
        try {
            if (options==null){
                //create an empty options object
                this.options = new CompilerOptions();
            }else{
                this.options = options;
            }

            this.processedTypemap = new HashMap();
            this.processedElementMap = new HashMap();
            this.processedElementList = new ArrayList();
            this.processedAnonymousComplexTypesMap = new HashMap();

            this.writer = new JavaBeanWriter(this.options.getOutputLocation());

        } catch (IOException e) {
            throw new SchemaCompilationException(e);
        }
    }

    /**
     * Compile a list of schemas
     * This actually calls the compile (XmlSchema s) method repeatedly
     * @see #compile(org.apache.ws.commons.schema.XmlSchema)
     * @param schemalist
     * @throws SchemaCompilationException
     */
    public void  compile(List schemalist) throws SchemaCompilationException{
        XmlSchema schema;
        try {
            for (int i = 0; i < schemalist.size(); i++) {
                schema =  (XmlSchema)schemalist.get(i);
                compile(schema);
            }
        }catch(SchemaCompilationException e) {
            throw e;
        } catch (Exception e) {
            throw new SchemaCompilationException(e);
        }
    }

    /**
     *
     * Compile (rather codegen) a single schema element
     * @param schema
     * @throws SchemaCompilationException
     */
    public void compile(XmlSchema schema) throws SchemaCompilationException{

        //select all the elements. We generate the code for types
        //only if the elements refer them!!!

        XmlSchemaObjectTable elements = schema.getElements();
        Iterator  xmlSchemaElement1Iterator = elements.getValues();
        while (xmlSchemaElement1Iterator.hasNext()) {
            //this is the set of outer elements so we need to generate classes
            //The outermost elements do not contain occurence counts (!) so we do not need
            //to check for arraytypes
            processElement((XmlSchemaElement)xmlSchemaElement1Iterator.next(),true);
        }

        //Now re iterate through the elements and write them
        Iterator xmlSchemaElement2Iterator = elements.getValues();
        while (xmlSchemaElement2Iterator.hasNext()) {
            //this is the set of outer elements so we need to generate classes
            writeElement((XmlSchemaElement)xmlSchemaElement2Iterator.next());
        }

    }

    /**
     * Writes the element
     * @param xsElt
     * @throws SchemaCompilationException
     */
    private void writeElement(XmlSchemaElement xsElt) throws SchemaCompilationException{
        if (this.processedElementMap.containsKey(xsElt.getQName())){
            return;
        }

        XmlSchemaType schemaType = xsElt.getSchemaType();

        if (schemaType!=null){
            BeanWriterMetaInfoHolder metainf = new BeanWriterMetaInfoHolder();
            if (schemaType.getName()!=null){
                //this is a named type
                QName qName = schemaType.getQName();
                //find the class name
                String className = findClassName(qName,isArray(xsElt));
                metainf.registerMapping(xsElt.getQName(),
                        qName,
                        className);

            }else{
                //we are going to special case the anonymous complex type. Our algorithm for dealing
                //with it is to generate a single object that has the complex content inside. Really the
                //intent of the user when he declares the complexType anonymously is to use it privately
                //First copy the schema types content into the metainf holder
                metainf = (BeanWriterMetaInfoHolder)this.processedAnonymousComplexTypesMap.get(xsElt);
                metainf.setAnonymous(true);
            }
            String writtenClassName = writer.write(xsElt,processedTypemap,metainf);
            processedElementMap.put(xsElt.getQName(),writtenClassName);
        }
    }


    /**
     *
     * @param xsElt
     * @param isOuter
     * @throws SchemaCompilationException
     */
    private void processElement(XmlSchemaElement xsElt,boolean isOuter) throws SchemaCompilationException{
        processElement(xsElt,isOuter,false);
    }

    /**
     *
     * @param xsElt
     * @param isOuter
     * @param isArray
     * @throws SchemaCompilationException
     */
    private void processElement(XmlSchemaElement xsElt,boolean isOuter,boolean isArray) throws SchemaCompilationException{
        //The processing element logic seems to be quite simple. Look at the relevant schema type
        //for each and every element and process that accordingly.
        //this means that any unused type definitions would not be generated!
        if (processedElementList.contains(xsElt.getQName())){
            return;
        }

        XmlSchemaType schemaType = xsElt.getSchemaType();
        if (schemaType!=null){
            processSchema(xsElt,schemaType);
            //at this time it is not wise to directly write the class for the element
            //so we push the complete element to an arraylist and let the process
            //pass through. We'll be iterating through the elements writing them
            //later
        }
        
        //There can be instances where the SchemaType is null but the schemaTypeName is not
        //this specifically happens with xsd:anyType.
        if (!isOuter){
            String className = findClassName(xsElt.getSchemaTypeName(),isArray);
            this.processedElementMap.put(xsElt.getQName(),className);
        }
        this.processedElementList.add(xsElt.getQName());

    }

    /**
     *
     * @param schemaType
     * @return
     */
    private String findClassName(QName qName,boolean isArray) {
        //find the class name
        String className;
        if (processedTypemap.containsKey(qName)) {
            className = (String)processedTypemap.get(qName);
        }else if(baseSchemaTypeMap.containsKey(qName)){
            className =(String)baseSchemaTypeMap.get(qName);
        }else{
            // We seem to have failed in finding a class name for the
            //contained schema type. We better set the default then
            className = OMElement.class.getName();
        }
        if (isArray){
            //append the square braces that say this is an array
            //hope this works for all cases!!!!!!!
            className = className + "[]";
        }
        return className;
    }

    /**
     * Process a schema element
     * @param schemaType
     * @throws SchemaCompilationException
     */
    private void processSchema(XmlSchemaElement xsElt,XmlSchemaType schemaType) throws SchemaCompilationException {
        if (schemaType instanceof XmlSchemaComplexType){
            //write classes for complex types
            XmlSchemaComplexType complexType = (XmlSchemaComplexType) schemaType;
            if (complexType.getName()!=null){
                processNamedComplexSchemaType(complexType);
            }else{
                processAnonymousComplexSchemaType(xsElt,complexType);
            }
        }else if (schemaType instanceof XmlSchemaSimpleType){
            //process simple type
            processSimpleSchemaType((XmlSchemaSimpleType)schemaType);
        }
    }


    /**
     *
     * @param complexType
     * @throws SchemaCompilationException
     */
    private void processAnonymousComplexSchemaType(XmlSchemaElement elt,XmlSchemaComplexType complexType) throws SchemaCompilationException{
        XmlSchemaParticle particle =  complexType.getParticle();
        BeanWriterMetaInfoHolder metaInfHolder = new BeanWriterMetaInfoHolder();
        if (particle!=null){
            //Process the particle
            processParticle(particle, metaInfHolder);
        }

        //process attributes - first look for the explicit attributes
        XmlSchemaObjectCollection attribs = complexType.getAttributes();
        Iterator attribIterator = attribs.getIterator();
        while (attribIterator.hasNext()) {
            Object o =  attribIterator.next();
            if (o instanceof XmlSchemaAttribute){
                processAttribute((XmlSchemaAttribute)o,metaInfHolder);

            }
        }

        //process any attribute
        //somehow the xml schema parser does not seem to pickup the any attribute!!
        XmlSchemaAnyAttribute anyAtt = complexType.getAnyAttribute();
        if (anyAtt!=null){
            processAnyAttribute(metaInfHolder);
        }

        //since this is a special case (an unnamed complex type) we'll put the already processed
        //metainf holder in a special map to be used later
        this.processedAnonymousComplexTypesMap.put(elt,metaInfHolder);
    }

    /**
     * handle the complex types which are named
     * @param complexType
     */
    private void processNamedComplexSchemaType(XmlSchemaComplexType complexType) throws SchemaCompilationException{

        if (processedTypemap.containsKey(complexType.getQName())
                || baseSchemaTypeMap.containsKey(complexType.getQName())){
            return;
        }

        XmlSchemaParticle particle =  complexType.getParticle();
        BeanWriterMetaInfoHolder metaInfHolder = new BeanWriterMetaInfoHolder();
        if (particle!=null){
            //Process the particle
            processParticle(particle, metaInfHolder);
        }

        //process attributes - first look for the explicit attributes
        XmlSchemaObjectCollection attribs = complexType.getAttributes();
        Iterator attribIterator = attribs.getIterator();
        while (attribIterator.hasNext()) {
            Object o =  attribIterator.next();
            if (o instanceof XmlSchemaAttribute){
                processAttribute((XmlSchemaAttribute)o,metaInfHolder);

            }
        }

        //process any attribute
        //somehow the xml schema parser does not seem to pickup the any attribute!!
        XmlSchemaAnyAttribute anyAtt = complexType.getAnyAttribute();
        if (anyAtt!=null){
            processAnyAttribute(metaInfHolder);
        }

        // Process the other types - Say the complex content, extensions and so on

        //write the class. This type mapping would have been populated right now
        //Note - We always write classes for complex types
        String fullyQualifiedClassName = writer.write(complexType,processedTypemap,metaInfHolder);
        //populate the type mapping with the elements
        processedTypemap.put(complexType.getQName(),fullyQualifiedClassName);



    }

    private void processAnyAttribute(BeanWriterMetaInfoHolder metainf) {
        //The best thing we can do here is to add a set of OMAttributes
        metainf.registerMapping(new QName(EXTRA_ATTRIBUTE_FIELD_NAME),
                null,
                OMElement[].class.getName(),
                SchemaConstants.ANY_ATTRIBUTE_TYPE);

    }

    public void processAttribute(XmlSchemaAttribute att,BeanWriterMetaInfoHolder metainf){
        //for now we assume (!!!) that attributes refer to standard types only
        QName schemaTypeName = att.getSchemaTypeName();
        if (baseSchemaTypeMap.containsKey(schemaTypeName)){
            metainf.registerMapping(att.getQName(),
                    schemaTypeName,
                    baseSchemaTypeMap.get(schemaTypeName).toString(),SchemaConstants.ATTRIBUTE_TYPE);
        } else {
            //this attribute refers to a custom type, probably one of the extended simple types.
            //handle it here
        }
    }

    /**
     *
     * @param particle
     * @param metainfHolder
     * @throws SchemaCompilationException
     */
    private void processParticle(XmlSchemaParticle particle, //particle being processed
                                 BeanWriterMetaInfoHolder metainfHolder // metainf holder
    ) throws SchemaCompilationException {
        if (particle instanceof XmlSchemaSequence ){
            XmlSchemaObjectCollection items = ((XmlSchemaSequence)particle).getItems();
            process(items, metainfHolder,true);
        }else if (particle instanceof XmlSchemaAll){
            XmlSchemaObjectCollection items = ((XmlSchemaAll)particle).getItems();
            process(items, metainfHolder,false);
        }else if (particle instanceof XmlSchemaChoice){
            //handle the choice!
        }
    }

    /**
     *
     * @param items
     * @param metainfHolder
     * @param order
     * @throws SchemaCompilationException
     */
    private void process(XmlSchemaObjectCollection items,
                         BeanWriterMetaInfoHolder metainfHolder,
                         boolean order) throws SchemaCompilationException {
        int count = items.getCount();
        Map processedElements = new HashMap();
        Map elementOrderMap = new HashMap();

        for (int i = 0; i < count; i++) {
            XmlSchemaObject item = items.getItem(i);

            if (item instanceof XmlSchemaElement){
                //recursively process the element
                XmlSchemaElement xsElt = (XmlSchemaElement) item;

                boolean isArray = isArray(xsElt);
                processElement(xsElt,false,isArray); //we know for sure this is not an outer type
                processedElements.put(xsElt, (isArray) ? Boolean.TRUE : Boolean.FALSE);
                if (order){
                    //we need to keep the order of the elements. So push the elements to another
                    //hashmap with the order number
                    elementOrderMap.put(xsElt,new Integer(i));
                }
            }else if (item instanceof XmlSchemaComplexContent){
                // process the extension
                XmlSchemaContent content = ((XmlSchemaComplexContent)item).getContent();
                if (content instanceof XmlSchemaComplexContentExtension){
                    // handle the complex extension
                }else if (content instanceof XmlSchemaComplexContentRestriction){
                    //handle complex restriction
                }

                //handle xsd:any ! We place an OMElement in the generated class
            }else if (item instanceof XmlSchemaAny){
                processAny((XmlSchemaAny)item,metainfHolder);
            }


        }

        // loop through the processed items and add them to the matainf object
        Iterator processedElementsIterator= processedElements.keySet().iterator();
        while(processedElementsIterator.hasNext()){
            XmlSchemaElement elt = (XmlSchemaElement)processedElementsIterator.next();
            QName qName = elt.getQName();
            String clazzName = (String)processedElementMap.get(qName);
            metainfHolder.registerMapping(qName,
                    elt.getSchemaTypeName()
                    ,clazzName,
                    ((Boolean)processedElements.get(elt)).booleanValue()?
                            SchemaConstants.ANY_ARRAY_TYPE:
                            SchemaConstants.ELEMENT_TYPE);

            //register the occurence counts
            metainfHolder.addMaxOccurs(qName,elt.getMaxOccurs());
            metainfHolder.addMinOccurs(qName,elt.getMinOccurs());
            //we need the order to be preserved. So record the order also
            if (order){
                //record the order in the metainf holder
                Integer integer = (Integer) elementOrderMap.get(elt);
                metainfHolder.registerQNameIndex(qName,
                        integer.intValue());
            }

        }

        //set the ordered flag in the metainf holder
        metainfHolder.setOrdered(order);
    }

    /**
     *
     */
    private void processAny(XmlSchemaAny any,BeanWriterMetaInfoHolder metainf) {
        //handle the minoccurs/maxoccurs here.
        //However since the any element does not have a name
        //we need to put a name here
        metainf.registerMapping(new QName(ANY_ELEMENT_FIELD_NAME),
                null,
                OMElement.class.getName(),
                SchemaConstants.ANY_TYPE);

    }

    /**
     * Handle the simple content
     * @param simpleType
     */
    private void processSimpleSchemaType(XmlSchemaSimpleType simpleType){
        //nothing to here yet.

    }


    private boolean isArray(XmlSchemaParticle particle) throws SchemaCompilationException{
        long minOccurs = particle.getMinOccurs();
        long maxOccurs = particle.getMaxOccurs();
        if (maxOccurs < minOccurs){
            throw new SchemaCompilationException();
        }else{
            return (maxOccurs>1);
        }

    }

}
