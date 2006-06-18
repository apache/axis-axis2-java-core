package org.apache.axis2.schema;

import org.apache.axis2.wsdl.codegen.CodeGenConfiguration;
import org.apache.axis2.wsdl.databinding.JavaTypeMapper;
import org.apache.axis2.wsdl.databinding.DefaultTypeMapper;
import org.apache.axis2.wsdl.databinding.TypeMapper;
import org.apache.axis2.wsdl.util.XSLTConstants;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;
import java.util.List;
import java.io.File;
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
 * This is the utility for the extension to call
 */
public class ExtensionUtility {

    public static void invoke(CodeGenConfiguration configuration) throws Exception{
        List schemaList = configuration.getAxisService().getSchema();
        if (schemaList == null || schemaList.isEmpty()) {
            //there are no types to be code generated
            //However if the type mapper is left empty it will be a problem for the other
            //processes. Hence the default type mapper is set to the configuration
            configuration.setTypeMapper(new DefaultTypeMapper());
            return;
        }
        //call the schema compiler
        CompilerOptions options = new CompilerOptions();

        //set the default options
        populateDefaultOptions(options,configuration);

        //set the user parameters. the user parameters get the preference over
        //the default ones. But the user better know what he's doing if he
        //used module specific parameters
        populateUserparameters(options,configuration);

        SchemaCompiler schemaCompiler = new SchemaCompiler(options);
        // run the schema compiler
        schemaCompiler.compile(schemaList);

        //create the type mapper
        //First try to take the one that is already there
        TypeMapper mapper = configuration.getTypeMapper();
        if (mapper==null){
            mapper =new JavaTypeMapper();
        }
        
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

    }

    /**
     *
     * @param options
     */
    private static void populateUserparameters(CompilerOptions options,CodeGenConfiguration configuration){
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
    private static void populateDefaultOptions(CompilerOptions options,CodeGenConfiguration configuration) {
        //create the output directory
        File outputDir =  configuration.isFlattenFiles()?
                          configuration.getOutputLocation():
                          new File(configuration.getOutputLocation(),"src");
        
        if(!outputDir.exists()) {
            outputDir.mkdirs();
        }

        /// these options need to be taken from the command line
        options.setOutputLocation(outputDir);
        options.setNs2PackageMap(configuration.getUri2PackageNameMap()==null?
                new HashMap():
                configuration.getUri2PackageNameMap());

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
            options.setWriteOutput(!configuration.isPackClasses());
        }
    }

}
