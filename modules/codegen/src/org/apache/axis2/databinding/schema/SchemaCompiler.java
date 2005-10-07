package org.apache.axis2.databinding.schema;

import org.apache.ws.commons.schema.*;

import java.util.List;
import java.util.Iterator;
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

    CompilerOptions options;

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
        XmlSchemaObjectTable elements = schema.getElements();
        XmlSchemaElement xsElt;
        Iterator  xmlSchemaElementIterator = elements.getValues();
        while (xmlSchemaElementIterator.hasNext()) {
            xsElt =  (XmlSchemaElement)xmlSchemaElementIterator.next();
            processElement(xsElt);
        }

    }

    /**
     *
     * @param xsElt
     */
    private void processElement(XmlSchemaElement xsElt){

        XmlSchemaType schemaType = xsElt.getSchemaType();
        if (schemaType!=null){
            if (schemaType instanceof XmlSchemaComplexType){
                //write classes for complex types
                processComplexSchemaType((XmlSchemaComplexType)schemaType);
            }else if (schemaType instanceof XmlSchemaSimpleType){
                processSimpleSchemaType((XmlSchemaSimpleType)schemaType);
            }
        }
    }

    /**
     *
     * @param complexType
     */
    private void processComplexSchemaType(XmlSchemaComplexType complexType){
        //to start with we need to write a class to represent this
        //
        XmlSchemaParticle particle =  complexType.getParticle();
        if (particle!=null){
            //check the particle
            if (particle instanceof XmlSchemaSequence){
                XmlSchemaObjectCollection items = ((XmlSchemaSequence)particle).getItems();
                int count = items.getCount();
                for (int i = 0; i < count; i++) {
                    XmlSchemaObject item = items.getItem(i);
                    if (item instanceof XmlSchemaElement){
                        //recursively process the element
                        processElement((XmlSchemaElement)item);
                    }

                    //process the items here. Usually the complex type needs to be represented as a  bean class


                    //populate a type map

                }
            }


        }

    }

    /**
     * Handle the simple content
     * @param simpleType
     */
    private void processSimpleSchemaType(XmlSchemaSimpleType simpleType){
        //nothing to here yet
    }
}
