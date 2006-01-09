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

package org.apache.axis2.wsdl;

import junit.framework.TestCase;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.util.CommandLineOption;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;
import org.apache.axis2.wsdl.util.CommandLineOptionParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class WSDL2JavaTest extends TestCase{

    public static final String OUTPUT_LOCATION_BASE = "./out_put_classes";
    public static final String OUTPUT_LOCATION_PREFIX = "/test";
    private static int folderCount = 0;
    // public static final String OUTPUT_LOCATION_BASE = "C:\\GeneratedCode\\test4\\src";
    public static final String WSDL_BASE_DIR = "test-resources/";
    public static final String CLASSES_DIR = "/target/classes/";
    private String[] moduleNames={"xml","common","core"};
    private static final String MODULE_PATH_PREFIX = "../modules/";
    private static final String COMPILE_TARGET_NAME = "compile";


    /**
     * Make the root output directory
     * @throws Exception
     */
    protected void setUp() throws Exception {
        File outputFile = new File(OUTPUT_LOCATION_BASE);
        if (outputFile.exists() && outputFile.isDirectory()){
            deleteDir(outputFile);
            outputFile.mkdir();
        }else{
            outputFile.mkdir();
        }
    }

    /**
     *  Remove the root output directory
     * @throws Exception
     */
    protected void tearDown() throws Exception {
        File outputFile = new File(OUTPUT_LOCATION_BASE);
        if (outputFile.exists() && outputFile.isDirectory()){
            deleteDir(outputFile);
        }
    }

    /**
     * Deletes all files and subdirectories under dir.
     * Returns true if all deletions were successful.
     * If a deletion fails, the method stops attempting to delete and returns false.
     */
    private boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i=0; i<children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

        // The directory is now empty so delete it
        return dir.delete();
    }

    /**
     * Test for the WSAT wsdl
     */
    public void testCodeGenerationWSAT(){

        try {
            generateAndCompile("wsat.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }

    /**
     * Test for the WSDL that's missing a service
     */
    public void testCodeGenerationNoService(){

        try {
            generateAndCompile("no-service.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }

     /**
     * Test for the Headers
     */
    public void testCodeGenerationHeaders(){

        try {
            generateAndCompile("headers.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }

    /**
     * Test for the ping WSDL
     */
    public void testCodeGenerationPing(){

        try {
            generateAndCompile("ping.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }


    /**
     * Test for the interoptestdoclitparameters
     */
    public void testCodeGenerationInteropTestDocLitParams(){

        try {
            generateAndCompile("interoptestdoclitparameters.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }

    /**
     * Test for the sales rank and price!
     */
    //Commented due to a failure in the tests with the WSDLPump
//    public void testCodeGenerationSalesRankNPrice(){
//
//        try {
//            generateAndCompile("SalesRankNPrice.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
//        } catch (CodeGenerationException e) {
//            fail("Exception while code generation test!"+ e.getMessage());
//        }
//    }

     /**
     * Test for the mime doc
     */
    public void testCodeGenerationMimeDoc(){

        try {
            generateAndCompile("mime-doc.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }

//     /**
//     * Test for the dime doc
//     */
//    public void testCodeGenerationDimeDoc(){
//
//        try {
//            generateAndCompile("dime-doc.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
//        } catch (CodeGenerationException e) {
//            fail("Exception while code generation test!"+ e.getMessage());
//        }
//    }
    /**
     * Test for the wscoor.wsdl
     */
    public void testCodeGenerationWSCOOR(){

        try {
            generateAndCompile("interoptestdoclit.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }

    /**
     * Test for the modified ping wsdl. it will be the test for the detached schema with only an import
     * statement
     */
    public void testCodeGenerationPingModified(){

        try {
            generateAndCompile("ping-modified.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }

    /**
     * Test for the modified ping-unboond wsdl. The binding is removed in this wsdl
     *
     */
    public void testCodeGenerationPingUnbound(){

        try {
            generateAndCompile("ping-unbound.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }

    /**
     * Test for the simple doc lit from Axis 1
     *
     */
    public void testCodeGenerationSimpleDocLiteral(){

        try {
            generateAndCompile("simple-doc-literal.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }

    /**
     * Test for the simple doc lit from Axis 1
     *
     */
    public void testCodeGenerationComplexDocLiteral(){

        try {
            generateAndCompile("complex-doc-literal.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }
    /**
     * Test for the mtom echo wsdl. This wsdl contains a restriction based on xmime and a
     * SOAP 1.2 binding
     *
     */
    public void testCodeGenerationMTOMEcho(){

        try {
            generateAndCompile("mtomecho.wsdl", OUTPUT_LOCATION_BASE+OUTPUT_LOCATION_PREFIX+folderCount++);
        } catch (CodeGenerationException e) {
            fail("Exception while code generation test!"+ e.getMessage());
        }
    }

    /**
     *
     * @param wsdlName
     * @param outputLocation
     * @throws CodeGenerationException
     */
    private void generateAndCompile(String wsdlName, String outputLocation) throws CodeGenerationException {
        codeGenerate(WSDL_BASE_DIR + wsdlName,outputLocation);
        //todo - Still the compilation fails (the original problem of the java.home was settled by setting fork
        //todo - to true). Now the compiler fails for some unknown reason (inside maven! works fine in the IDE though)

        //compile(outputLocation);
    }

    /**
     *
     * @param wsdlFile
     * @param outputLocation
     * @throws CodeGenerationException
     */
    private void codeGenerate(String wsdlFile,String outputLocation) throws CodeGenerationException {
        //create the option map
        Map optionMap = fillOptionMap(wsdlFile,outputLocation);
        CommandLineOptionParser parser =
                new CommandLineOptionParser(optionMap);
        new CodeGenerationEngine(parser).generate();
    }

    /**
     *
     * @param outputLocation
     */
    private void compile(String outputLocation){
        //using the ant javac task for compilation
        Javac javaCompiler = new Javac();
        Project codeGenProject = new Project();
        Target compileTarget = new Target();

        compileTarget.setName(COMPILE_TARGET_NAME);
        compileTarget.addTask(javaCompiler);
        codeGenProject.addTarget(compileTarget);
        codeGenProject.setSystemProperties();
        javaCompiler.setProject(codeGenProject);
        javaCompiler.setIncludejavaruntime(true);
        javaCompiler.setIncludeantruntime(true);

        /*
          This harmless looking setFork is actually very important. unless the compiler is
          forked it wont work!
        */
        javaCompiler.setFork(true);

        //Create classpath - The generated output directories also become part of the classpath
        //reason for this is that some codegenerators(XMLBeans) produce compiled classes as part of
        //generated artifacts
        File outputLocationFile = new File(outputLocation);
        Path classPath = new Path(codeGenProject,outputLocation) ;
        classPath.addExisting(classPath.concatSystemClasspath(),false);
        for (int i = 0; i < moduleNames.length; i++) {
            classPath.add(new Path(codeGenProject,MODULE_PATH_PREFIX +moduleNames[i]+CLASSES_DIR));
        }
        javaCompiler.setClasspath(classPath);

        //set sourcePath - The generated output directories also become part of the sourcepath
        Path sourcePath = new Path(codeGenProject,outputLocation) ;
        sourcePath.setLocation(outputLocationFile);
        javaCompiler.setSrcdir(sourcePath);

        //output the classes into the output dir as well
        javaCompiler.setDestdir(outputLocationFile);
        javaCompiler.setVerbose(true);
        try {
            codeGenProject.executeTarget(COMPILE_TARGET_NAME);
        } catch (BuildException e) {
            fail();
        }

    }

    /**
     *
     */
    private Map fillOptionMap(String wsdlFileName,String outputLocation) {
        Map optionMap = new HashMap();
        optionMap.put(
                CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.WSDL_LOCATION_URI_OPTION,
                        new String[]{wsdlFileName}));

        //use default sync option - No option is given
        //use default async option - No option is given
        //use default language option - No option is given
        //output location - code_gen_output

        optionMap.put(
                CommandLineOptionConstants.OUTPUT_LOCATION_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.OUTPUT_LOCATION_OPTION,
                        new String[]{outputLocation}));
        //server side option is on
        optionMap.put(
                CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION,
                new CommandLineOption(
                        CommandLineOptionConstants.SERVER_SIDE_CODE_OPTION,
                        new String[0]));
        // descriptor option is on
        optionMap.put(
                CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                new CommandLineOption(CommandLineOptionConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
                        new String[0]));
         // db is xmlbeans option is on
        optionMap.put(
                CommandLineOptionConstants.DATA_BINDING_TYPE_OPTION,
                new CommandLineOption(CommandLineOptionConstants.DATA_BINDING_TYPE_OPTION,
                        new String[]{TestConstants.Databinding.XML_BEANS}));

         optionMap.put(
                CommandLineOptionConstants.GENERATE_ALL_OPTION,
                new CommandLineOption(CommandLineOptionConstants.GENERATE_ALL_OPTION,
                        new String[0]));
        //todo Make this work
        //test case option is on
//        optionMap.put(
//                CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
//                new CommandLineOption(
//                        CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
//                        new String[0]));
        //databinding is default

        return optionMap;
    }


}
