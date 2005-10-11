package org.apache.axis2.databinding.schema;

import org.apache.ws.commons.schema.*;
import org.apache.axis2.util.URLProcessor;

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
    private HashMap processedElementmap;
    private JavaBeanWriter writer;

    private Map baseSchemaTypeMap = TypeMap.getTypeMap();


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

            this.writer = new JavaBeanWriter(this.options.getOutputLocation());

        } catch (IOException e) {
            throw new SchemaCompilationException(e);
        }
    }

    /**
     *
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
     *
     * @param schema
     * @throws SchemaCompilationException
     */
    public void compile(XmlSchema schema) throws SchemaCompilationException{

        //write the code here to do the schema compilation
        //select the types
        XmlSchemaObjectTable types =  schema.getSchemaTypes();
        Iterator  xmlSchemaTypeIterator = types.getValues();
        while (xmlSchemaTypeIterator.hasNext()) {
            processSchema((XmlSchemaType)xmlSchemaTypeIterator.next());
        }

        //select all the elements next
        XmlSchemaObjectTable elements = schema.getElements();
        Iterator  xmlSchemaElementIterator = elements.getValues();
        while (xmlSchemaElementIterator.hasNext()) {
            processElement((XmlSchemaElement)xmlSchemaElementIterator.next());
        }



    }

    /**
     *
     * @param xsElt
     */
    private void processElement(XmlSchemaElement xsElt) throws SchemaCompilationException{
        //The processing element logic seems to be quite simple. Look at the relevant schema type
        //for each and every element and process that accordingly.
        //this means that any unused type definitions would not be generated!
        XmlSchemaType schemaType = xsElt.getSchemaType();
        if (processedElementmap.containsKey(xsElt.getQName())){
            return;
        }
        if (schemaType==null){
            throw new SchemaCompilationException("Schema type not found!");
        }
        processSchema(schemaType);
        QName qName = schemaType.getQName();

        //write a class for this element
        BeanWriterMetaInfoHolder metainf = new BeanWriterMetaInfoHolder();
        //there can be only one schema type
        String className = "";
        if (processedTypemap.containsKey(qName)){
            className =  processedTypemap.get(qName).toString();
        }else if (baseSchemaTypeMap.containsKey(qName)){
            className =  baseSchemaTypeMap.get(qName).toString();
        }else{
            //throw an exception here
        }
        metainf.addElementInfo(xsElt.getQName(),qName,className);

        String fullyQualifiedClassName = writer.write(xsElt,processedTypemap,metainf);
        processedElementmap.put(xsElt.getQName(),fullyQualifiedClassName);


    }

    private void processSchema(XmlSchemaType schemaType) throws SchemaCompilationException {
        if (schemaType instanceof XmlSchemaComplexType){
            //write classes for complex types
            processComplexSchemaType((XmlSchemaComplexType)schemaType);
        }else if (schemaType instanceof XmlSchemaSimpleType){
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

        //to start with we need to write a class to represent this
        //

        XmlSchemaParticle particle =  complexType.getParticle();
        BeanWriterMetaInfoHolder metaInfHolder = new BeanWriterMetaInfoHolder();
        if (particle!=null){
            //Process the particle
            processParticle(particle, metaInfHolder);
        }else{
            // Process the other types - Say the complex content, extensions and so on
        }

        //write the class. This type mapping would have been populated right now
        String fullyQualifiedClassName = writer.write(complexType,processedTypemap,metaInfHolder);
        processedTypemap.put(complexType.getQName(),fullyQualifiedClassName);

        //populate the type mapping with the elements

    }

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
                processElement(xsElt);
                processedElements.add(xsElt);
            }else if (item instanceof XmlSchemaComplexContent){
                // process the extension
                XmlSchemaContent content = ((XmlSchemaComplexContent)item).getContent();
                if (content instanceof XmlSchemaComplexContentExtension){
                    // handle the complex extension
                }else if (content instanceof XmlSchemaComplexContentRestriction){
                    //handle complex restriction
                }
                //handle the other types here
            }


        }

        // loop through the processed items and add them to the matainf object
        int processedCount = processedElements.size();
        for (int i = 0; i < processedCount; i++) {
            XmlSchemaElement elt = (XmlSchemaElement)processedElements.get(i);
            String clazzName = (String)processedElementmap.get(elt.getQName());
            metainfHolder.addElementInfo(elt.getQName(),
                    elt.getSchemaTypeName()
                    ,clazzName);

        }
        //set the ordered flag in the metainf holder
        metainfHolder.setOrdered(order);
    }

    /**
     * Handle the simple content
     * @param simpleType
     */
    private void processSimpleSchemaType(XmlSchemaSimpleType simpleType){
        //nothing to here yet. just populate the processed type map with this
        //the class Name would be from the base type map!
        QName qName = simpleType.getQName();
        processedTypemap.put(qName,baseSchemaTypeMap.get(qName));
    }


    /*     Utility methods       */
    private String getJavaClassNameFromComplexTypeQName(QName name){
        String className = name.getLocalPart();
        String packageName = URLProcessor.getNameSpaceFromURL(name.getNamespaceURI());
        return packageName + "." +className;

    }
}
