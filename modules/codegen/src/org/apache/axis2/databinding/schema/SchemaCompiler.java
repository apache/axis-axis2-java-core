package org.apache.axis2.databinding.schema;

import org.apache.ws.commons.schema.*;
import org.apache.axis2.util.URLProcessor;

import javax.xml.namespace.QName;
import java.util.List;
import java.util.Iterator;
import java.util.HashMap;
import java.util.Map;
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
        if (schemaType!=null){
            processSchema(schemaType);
        }

        //write a class for this element

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

        if (processedTypemap.containsKey(complexType.getQName())){
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
            XmlSchemaContentModel contentModel = complexType.getContentModel();
            if (contentModel!=null){
                XmlSchemaContent content =  (contentModel).getContent();
                if (content instanceof XmlSchemaComplexContentExtension){
                    XmlSchemaComplexContentExtension xmlSchemaComplexContentExtension = (XmlSchemaComplexContentExtension) content;
                    processParticle(xmlSchemaComplexContentExtension.getParticle(),metaInfHolder);
                    metaInfHolder.setExtension(true);
                    metaInfHolder.setExtensionClassName(
                            getJavaClassNameFromComplexTypeQName(
                                    xmlSchemaComplexContentExtension.getBaseTypeName()));
                }


            }
        }

        //write the class. This type mapping would have been populated right now
        writer.write(complexType,processedTypemap,metaInfHolder);
        processedTypemap.put(complexType.getQName(),"");

        //populate the type mapping with the elements

    }

    private void processParticle(XmlSchemaParticle particle, //particle being processed
                                BeanWriterMetaInfoHolder metainfHolder // metainf holder
    ) throws SchemaCompilationException {
        if (particle instanceof XmlSchemaSequence ){
            XmlSchemaObjectCollection items = ((XmlSchemaSequence)particle).getItems();
            int count = items.getCount();
            for (int i = 0; i < count; i++) {
                XmlSchemaObject item = items.getItem(i);
                if (item instanceof XmlSchemaElement){
                    //recursively process the element
                    XmlSchemaElement xsElt = (XmlSchemaElement) item;
                    processElement(xsElt);
                    //add this to the processed element list
                    QName schemaTypeQName = xsElt.getSchemaType().getQName();
                    Class clazz = (Class)baseSchemaTypeMap.get(schemaTypeQName);
                    if (clazz!=null){
                        metainfHolder.addElementInfo(xsElt.getQName(),
                                                     xsElt.getSchemaTypeName()
                                                     ,clazz.getName());
                    }else{
                         metainfHolder.addElementInfo(xsElt.getQName(),
                                                     xsElt.getSchemaTypeName()
                                                     ,getJavaClassNameFromComplexTypeQName(schemaTypeQName));
                    }
                }else if (item instanceof XmlSchemaComplexContent){
                    // process the extension
                    XmlSchemaContent content = ((XmlSchemaComplexContent)item).getContent();
                    if (content instanceof XmlSchemaComplexContentExtension){
                        processParticle(((XmlSchemaComplexContentExtension)content).getParticle(),metainfHolder);
                    }else if (content instanceof XmlSchemaComplexContentRestriction){
                        //handle complex restriction
                    }
                    //handle the other types here
                }


            }
            //set the ordered flag in the metainf holder
            metainfHolder.setOrdered(true);
        }else if (particle instanceof XmlSchemaAll){
            //handle the all !

        }else if (particle instanceof XmlSchemaChoice){
            //handle the choice!
        }
    }

    /**
     * Handle the simple content
     * @param simpleType
     */
    private void processSimpleSchemaType(XmlSchemaSimpleType simpleType){
        //nothing to here yet
    }


    /*     Utility methods       */
    private String getJavaClassNameFromComplexTypeQName(QName name){
        String className = name.getLocalPart();
        String packageName = URLProcessor.getNameSpaceFromURL(name.getNamespaceURI());
        return packageName + "." +className;

    }
}
