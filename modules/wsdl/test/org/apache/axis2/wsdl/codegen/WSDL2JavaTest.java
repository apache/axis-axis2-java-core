package org.apache.axis2.wsdl.codegen;

import junit.framework.TestCase;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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
*
*
*/
public class WSDL2JavaTest extends TestCase{

    public static final String OUTPUT_LOCATION = "./out_put_classes";
    // public static final String OUTPUT_LOCATION = "C:\\GeneratedCode\\test4\\src";
    public static final String WSDL_BASE_DIR = "./test-resources/";
    public static final String CLASSES_DIR = "/target/classes/";
    private String[] moduleNames={"xml","common","core"};


    protected void setUp() throws Exception {
        File outputFile = new File(OUTPUT_LOCATION);
        if (outputFile.exists() && outputFile.isDirectory()){
            deleteDir(outputFile);
            outputFile.mkdir();
        }else{
            outputFile.mkdir();
        }
    }

    protected void tearDown() throws Exception {
        File outputFile = new File(OUTPUT_LOCATION);
        if (outputFile.exists() && outputFile.isDirectory()){
            deleteDir(outputFile);
        }
    }

    // Deletes all files and subdirectories under dir.
    // Returns true if all deletions were successful.
    // If a deletion fails, the method stops attempting to delete and returns false.
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
            generateAndCompile("wsat.wsdl");
        } catch (CodeGenerationException e) {
            fail("Exception while codegeneration test!"+ e.getMessage());
        }
    }


    /**
     * Test for the interoptestdoclit.wsdl
     */
    public void testCodeGenerationWSCOOR(){

        try {
            generateAndCompile("interoptestdoclit.wsdl");
        } catch (CodeGenerationException e) {
            fail("Exception while codegeneration test!"+ e.getMessage());
        }
    }
    private void generateAndCompile(String wsdlName) throws CodeGenerationException {
        codeGenerate(WSDL_BASE_DIR + wsdlName);
        //todo - Strangely the java.home system variable does not point to the correct place
        //todo - Need to find the prob and uncomment this
        //compile();
    }
    private void codeGenerate(String wsdlFile) throws CodeGenerationException {
        //create the option map
        Map optionMap = fillOptionMap(wsdlFile);
        CommandLineOptionParser parser =
                new CommandLineOptionParser(optionMap);
        new CodeGenerationEngine(parser).generate();
    }

    private void compile(){
        //using the ant javac task for compilation
        Javac javaCompiler = new Javac();
        Project codeGenProject = new Project();
        Target compileTarget = new Target();

        compileTarget.setName("compile");
        compileTarget.addTask(javaCompiler);
        codeGenProject.addTarget(compileTarget);
        codeGenProject.setSystemProperties();
        javaCompiler.setProject(codeGenProject);
        javaCompiler.setIncludejavaruntime(true);
        javaCompiler.setIncludeantruntime(true);

        File outputLocationFile = new File(OUTPUT_LOCATION);

        Path classPath = new Path(codeGenProject,OUTPUT_LOCATION) ;
        classPath.addExisting(classPath.concatSystemClasspath(),false);
        for (int i = 0; i < moduleNames.length; i++) {
            classPath.add(new Path(codeGenProject,"../modules/"+moduleNames[i]+CLASSES_DIR));
        }
        javaCompiler.setClasspath(classPath);


        System.out.println("javaCompiler classpath = " + javaCompiler.getClasspath());
        System.out.println("System java home setting = " + System.getProperty("java.home"));
        System.out.println("Compiler name = " +javaCompiler.getExecutable());

        Path sourcePath = new Path(codeGenProject,OUTPUT_LOCATION) ;
        sourcePath.setLocation(outputLocationFile);
        javaCompiler.setSrcdir(sourcePath);

        javaCompiler.setDestdir(outputLocationFile);

        codeGenProject.executeTarget("compile");

    }

    /**
     *
     */
    private Map fillOptionMap(String wsdlFileName) {
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
                        new String[]{OUTPUT_LOCATION}));
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
