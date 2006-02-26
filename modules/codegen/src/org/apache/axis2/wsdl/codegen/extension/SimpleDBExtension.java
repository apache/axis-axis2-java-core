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

package org.apache.axis2.wsdl.codegen.extension;

import org.apache.axis2.schema.CompilerOptions;
import org.apache.axis2.schema.SchemaCompiler;
import org.apache.axis2.schema.SchemaConstants;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.util.XSLTConstants;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaCollection;
import org.apache.wsdl.WSDLExtensibilityElement;
import org.apache.wsdl.WSDLTypes;
import org.apache.wsdl.WSDLInterface;
import org.apache.wsdl.WSDLOperation;
import org.apache.wsdl.MessageReference;
import org.apache.wsdl.extensions.ExtensionConstants;
import org.apache.wsdl.extensions.Schema;
import org.w3c.dom.Element;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.Vector;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * Extension for simple data binding.
 */
public class SimpleDBExtension extends AbstractDBProcessingExtension {

    /**
     * 
     */
    public void engage() {
        //test the databinding type. If not just fall through
        if (testFallThrough(configuration.getDatabindingType())) {
            return;
        }
        try {

            WSDLTypes typesList = configuration.getWom().getTypes();
            if (typesList == null) {
                //there are no types to be code generated
                //However if the type mapper is left empty it will be a problem for the other
                //processes. Hence the default type mapper is set to the configuration
                this.configuration.setTypeMapper(new DefaultTypeMapper());
                return;
            }

            List typesArray = typesList.getExtensibilityElements();
            WSDLExtensibilityElement extensiblityElt;
            Vector xmlSchemaTypeVector = new Vector();
            XmlSchemaCollection schemaColl = new XmlSchemaCollection();
            //add the base uri
            if (configuration.getBaseURI()!=null){
                schemaColl.setBaseUri(configuration.getBaseURI());
            }



            for (int i = 0; i < typesArray.size(); i++) {
                extensiblityElt = (WSDLExtensibilityElement) typesArray.get(i);

                //add the namespace map here. it is absolutely needed
                Map nsMap = configuration.getWom().getNamespaces();
                Iterator keys = nsMap.keySet().iterator();
                String key;
                while (keys.hasNext()) {
                    key = (String) keys.next();
                    schemaColl.mapNamespace(key, (String) nsMap.get(key));
                }
                Schema schema;

                if (ExtensionConstants.SCHEMA.equals(extensiblityElt.getType())) {
                    schema = (Schema) extensiblityElt;
                    Stack importedSchemaStack = schema.getImportedSchemaStack();
                    //compile these schemas
                    while (!importedSchemaStack.isEmpty()) {
                        Element el = (Element) importedSchemaStack.pop();
                        if (el != null) {
                            XmlSchema thisSchema = schemaColl.read(el);
                            xmlSchemaTypeVector.add(thisSchema);
                        }
                    }
                }
            }
            //call the schema compiler
            CompilerOptions options = new CompilerOptions();

            //set the default options
            populateDefaultOptions(options);

            //set the user parameters. the user parameters get the preference over
            //the default ones. But the user better know what he's doing if he
            //used module specific parameters
            populateUserparameters(options);

            SchemaCompiler schemaCompiler = new SchemaCompiler(options);
            // run the schema compiler
            schemaCompiler.compile(xmlSchemaTypeVector);

            //create the type mapper
            JavaTypeMapper mapper = new JavaTypeMapper();

            if (options.isWriteOutput()){
                //get the processed element map and transfer it to the type mapper
                Map processedMap = schemaCompiler.getProcessedElementMap();
                Iterator processedkeys = processedMap.keySet().iterator();
                QName qNameKey;
                while (processedkeys.hasNext()) {
                    qNameKey = (QName) processedkeys.next();
                    mapper.addTypeMappingName(qNameKey, processedMap.get(qNameKey).toString());
                }

            }else{
                //get the processed model map and transfer it to the type mapper
                //since the options mentiond that its not writable, it should have
                //populated the model map
                Map processedModelMap = schemaCompiler.getProcessedModelMap();
                Iterator processedkeys = processedModelMap.keySet().iterator();
                QName qNameKey;
                while (processedkeys.hasNext()) {
                    qNameKey = (QName) processedkeys.next();
                    mapper.addTypeMappingObject(qNameKey, processedModelMap.get(qNameKey));
                }

                Map processedMap = schemaCompiler.getProcessedElementMap();
                processedkeys = processedMap.keySet().iterator();
                while (processedkeys.hasNext()) {
                    qNameKey = (QName) processedkeys.next();
                    mapper.addTypeMappingName(qNameKey, processedMap.get(qNameKey).toString());
                }

                //get the ADB template from the schema compilers property bag and set the
                //template
                configuration.putProperty(XSLTConstants.EXTERNAL_TEMPLATE_PROPERTY_KEY,
                        schemaCompiler.getCompilerProperties().getProperty(
                                SchemaConstants.SchemaPropertyNames.BEAN_WRITER_TEMPLATE_KEY));

            }

            //set the type mapper to the config
            configuration.setTypeMapper(mapper);


        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

//    /**
//     *  populates the unwrapped Qnames from the messagereference
//     */
//
//    private void populateUnwrappedElements(List unwrappedElementQNames) {
//        Map wsdlInterfaces = configuration.getWom().getWsdlInterfaces();
//        Iterator interaceIterator = wsdlInterfaces.values().iterator();
//        WSDLInterface wsdlInterface;
//        while (interaceIterator.hasNext()) {
//            wsdlInterface =  (WSDLInterface)interaceIterator.next();
//            HashMap allOperations = wsdlInterface.getAllOperations();
//            Iterator operationsIterator = allOperations.values().iterator();
//            while (operationsIterator.hasNext()) {
//                WSDLOperation operation =  (WSDLOperation)operationsIterator.next();
//                MessageReference inputMessage = operation.getInputMessage();
//                if (inputMessage!= null){
//                    Map metadataBag = inputMessage.getMetadataBag();
//                    Iterator qNameIterator = metadataBag.keySet().iterator();
//                    while (qNameIterator.hasNext()) {
//                        unwrappedElementQNames.add(qNameIterator.next());
//                    }
//                }
//
//                //at this point we should add the output messages as well
//                MessageReference outputMessage = operation.getOutputMessage();
//                if (outputMessage!=null){
//                    unwrappedElementQNames.add(outputMessage.getElementQName());
//                }
//            }
//        }
//    }

    /**
     *
     * @param options
     */
    private void populateUserparameters(CompilerOptions options){
        Map propertyMap = configuration.getProperties();
        if (propertyMap.containsKey(SchemaConstants.SchemaCompilerArguments.WRAP_SCHEMA_CLASSES)){
            if (Boolean.valueOf(
                    propertyMap.get(SchemaConstants.SchemaCompilerArguments.WRAP_SCHEMA_CLASSES).toString()).
                    booleanValue()) {
                options.setWrapClasses(true);
            }else{
                options.setWrapClasses(false);
            }
        }

        if (propertyMap.containsKey(SchemaConstants.SchemaCompilerArguments.WRITE_SCHEMA_CLASSES)){
            if (Boolean.valueOf(
                    propertyMap.get(SchemaConstants.SchemaCompilerArguments.WRITE_SCHEMA_CLASSES).toString()).
                    booleanValue()) {
                options.setWriteOutput(true);
            }else{
                options.setWriteOutput(false);
            }
        }

        // add the custom package name
        if (propertyMap.containsKey(SchemaConstants.SchemaCompilerArguments.PACKAGE)){
            String packageName = (String)propertyMap.get(SchemaConstants.SchemaCompilerArguments.PACKAGE);
            if (packageName!=null || !"".equals(packageName)){
               options.setPackageName(packageName);
            }

        }
    }


    /**
     *
     * @param options
     */
    private void populateDefaultOptions(CompilerOptions options) {
        //create the output directory
        File outputDir = new File(configuration.getOutputLocation(), "src");
        if(!outputDir.exists()) {
            outputDir.mkdirs();
        }

        /// these options need to be taken from the command line
        options.setOutputLocation(outputDir);

        //default setting is to set the wrap status depending on whether it's
        //the server side or the client side
        if (configuration.isServerSide()){
            //for the serverside we generate unwrapped  by default
            options.setWrapClasses(false);
            //for the serverside we write the output by default
            options.setWriteOutput(true);
        }else{
            // for the client let the users preference be the word here
            options.setWrapClasses(configuration.isPackClasses());
            //for the client side the default setting is not to write the
            //output
            options.setWriteOutput(false);
            //options.setWriteOutput(false);
        }
    }
}
