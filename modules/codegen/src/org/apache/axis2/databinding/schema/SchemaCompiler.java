package org.apache.axis2.databinding.schema;

import org.apache.ws.commons.schema.*;

import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
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

    /**
     *
     * @param options
     */
    public SchemaCompiler(CompilerOptions options) {
        if (options==null){
            //create an empty options object
            this.options = new CompilerOptions();
        }else{
            this.options = options;
        }

        this.processedTypemap = new HashMap();

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
        XmlSchemaElement xsElt;
        Iterator  xmlSchemaElementIterator = elements.getValues();
        while (xmlSchemaElementIterator.hasNext()) {
            processElement((XmlSchemaElement)xmlSchemaElementIterator.next());
        }

        System.out.println("processedTypemap = " + processedTypemap);
    }

    /**
     *
     * @param xsElt
     */
    private void processElement(XmlSchemaElement xsElt){
        //The processing element logic seems to be quite simple. Look at the relevant schema type
        //for each and every element and process that accordingly.
        //this means that any unused type definitions would not be generated!
        XmlSchemaType schemaType = xsElt.getSchemaType();
        if (schemaType!=null){
            processSchema(schemaType);
        }

        //write a class for this element

    }

    private void processSchema(XmlSchemaType schemaType) {
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
    private void processComplexSchemaType(XmlSchemaComplexType complexType){

        if (processedTypemap.containsKey(complexType.getQName())){
            return;
        }

        //to start with we need to write a class to represent this
        //

        XmlSchemaParticle particle =  complexType.getParticle();
        if (particle!=null){
            //check the particle
            if (particle instanceof XmlSchemaSequence ){
                XmlSchemaObjectCollection items = ((XmlSchemaSequence)particle).getItems();
                int count = items.getCount();
                for (int i = 0; i < count; i++) {
                    XmlSchemaObject item = items.getItem(i);
                    if (item instanceof XmlSchemaElement){
                        //recursively process the element
                        processElement((XmlSchemaElement)item);
                    }else{
                        //handle the other types here
                    }
                }
            }else if (particle instanceof XmlSchemaAll){
                //handle the all !

            }else if (particle instanceof XmlSchemaChoice){
                //handle the choice!
            }


        }

        //write the class. This type mapping would have been populated right now
        
        processedTypemap.put(complexType.getQName(),"");




        //populate the type mapping with the elements

    }

    /**
     * Handle the simple content
     * @param simpleType
     */
    private void processSimpleSchemaType(XmlSchemaSimpleType simpleType){
        //nothing to here yet
    }
}
