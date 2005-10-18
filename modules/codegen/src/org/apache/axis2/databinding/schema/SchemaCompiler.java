package org.apache.axis2.databinding.schema;

import org.apache.ws.commons.schema.*;
import org.apache.axis2.util.URLProcessor;
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
    //The processedElementmap and the processedElementList have a subtle difference
    //The writing to the processedElementList happens when an outer element is processed.
    //
    private HashMap processedElementmap;
    private ArrayList processedElementList;

    private JavaBeanWriter writer;

    private Map baseSchemaTypeMap = TypeMap.getTypeMap();
    private static final String ANY_ELEMENT_FIELD_NAME = "extraElements";


    public HashMap getProcessedElementmap() {
        return processedElementmap;
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
            this.processedElementmap = new HashMap();
            this.processedElementList = new ArrayList();

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
            processElement((XmlSchemaElement)xmlSchemaElement1Iterator.next(),true);
        }

        //Now re iterate through the elements and write them
        Iterator xmlSchemaElement2Iterator = elements.getValues();
        while (xmlSchemaElement2Iterator.hasNext()) {
            //this is the set of outer elements so we need to generate classes
            writeElement((XmlSchemaElement)xmlSchemaElement2Iterator.next());
        }

    }

    private void writeElement(XmlSchemaElement schemaElement) throws SchemaCompilationException{
        if (this.processedElementmap.containsKey(schemaElement.getQName())){
            return;
        }

        XmlSchemaType schemaType = schemaElement.getSchemaType();

        if (schemaType!=null){
            BeanWriterMetaInfoHolder metainf = new BeanWriterMetaInfoHolder();
            QName qName = schemaType.getQName();
            //find the class name
            String className = findClassName(schemaType);
            metainf.registerMapping(schemaElement.getQName(),
                    qName,
                    className);
            String writtenClassName = writer.write(schemaElement,processedTypemap,metainf);
            processedElementmap.put(schemaElement.getQName(),writtenClassName);
        }


    }



    /**
     *
     * @param xsElt
     */
    private void processElement(XmlSchemaElement xsElt,boolean isOuter) throws SchemaCompilationException{
        //The processing element logic seems to be quite simple. Look at the relevant schema type
        //for each and every element and process that accordingly.
        //this means that any unused type definitions would not be generated!
        if (processedElementList.contains(xsElt.getQName())){
            return;
        }

        XmlSchemaType schemaType = xsElt.getSchemaType();

        if (schemaType!=null){
            processSchema(schemaType);

            //at this time it is not wise to directly write the class for the element
            //so we push the complete element to an arraylist and let the process
            //pass through. We'll be iterating through the elements writing them
            //later

            if (!isOuter){
                String className = findClassName(schemaType);
                this.processedElementmap.put(xsElt.getQName(),className);
            }
            this.processedElementList.add(xsElt.getQName());

        }else{
            //perhaps this has an anonymous complex type! Handle it here
            //BTW how do we handle an anonymous complex type
        }

    }

    /**
     *
     * @param schemaType
     * @return
     */
    private String findClassName(XmlSchemaType schemaType) {
        //find the class name
        QName qName = schemaType.getQName();
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
        return className;
    }

    /**
     * Process a schema element
     * @param schemaType
     * @throws SchemaCompilationException
     */
    private void processSchema(XmlSchemaType schemaType) throws SchemaCompilationException {
        if (schemaType instanceof XmlSchemaComplexType){
            //write classes for complex types
            processComplexSchemaType((XmlSchemaComplexType)schemaType);
        }else if (schemaType instanceof XmlSchemaSimpleType){
            //process simple type
            processSimpleSchemaType((XmlSchemaSimpleType)schemaType);
        }
    }

    /**
     * handle the complex type
     * @param complexType
     */
    private void processComplexSchemaType(XmlSchemaComplexType complexType) throws SchemaCompilationException{

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
        XmlSchemaAnyAttribute anyAtt = complexType.getAnyAttribute();
        if (anyAtt!=null){
            processAnyAttribute();
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
        metainf.registerMapping(new QName(""))

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
        List processedElements = new ArrayList();

        for (int i = 0; i < count; i++) {
            XmlSchemaObject item = items.getItem(i);
            if (item instanceof XmlSchemaElement){
                //recursively process the element
                XmlSchemaElement xsElt = (XmlSchemaElement) item;
                processElement(xsElt,false); //we know for sure this is not an outer type
                processedElements.add(xsElt);
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
        int processedCount = processedElements.size();
        for (int i = 0; i < processedCount; i++) {
            XmlSchemaElement elt = (XmlSchemaElement)processedElements.get(i);
            String clazzName = (String)processedElementmap.get(elt.getQName());
            metainfHolder.registerMapping(elt.getQName(),
                    elt.getSchemaTypeName()
                    ,clazzName);

        }
        //set the ordered flag in the metainf holder
        metainfHolder.setOrdered(order);
    }

    /**
     *
     */
    private void processAny(XmlSchemaAny any,BeanWriterMetaInfoHolder metainf) {
        //handle the minoccurs/maxoccurs here.
        // However since the any element does not have a name
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


/*     Utility methods       */
    private String getJavaClassNameFromComplexTypeQName(QName name){
        String className = name.getLocalPart();
        String packageName = URLProcessor.getNameSpaceFromURL(name.getNamespaceURI());
        return packageName + "." +className;

    }
}
