/*
 * Copyright 2006 The Apache Software Foundation.
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
package org.apache.axis2.jibx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.extension.JiBXExtension;
import org.apache.axis2.wsdl.util.CommandLineOption;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;
import org.apache.axis2.wsdl.util.CommandLineOptionParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * Full code generation and runtime test for JiBX data binding extension. This
 * is based on the XMLBeans test code.
 *
 */
public class Test extends TestCase
{
    public static final String OUTPUT_LOCATION_BASE = "target/gen";
    public static final String OUTPUT_LOCATION_PREFIX = "/test";
    private static int folderCount = 0;
    public static final String WSDL_BASE_DIR = "test-resources/wsdl/";
    public static final String BINDING_BASE_DIR = "test-resources/binding/";
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
     * Remove the root output directory
     * @throws Exception
     */
    protected void tearDown() throws Exception {
        File outputFile = new File(OUTPUT_LOCATION_BASE);
        if (outputFile.exists() && outputFile.isDirectory()){
            deleteDir(outputFile);
        }
    }

    /**
     * Handle linkage code generation.
     * 
     * @param wsdl
     * @param binding
     * @param outdir
     * @throws CodeGenerationException
     */
    private void codeGenerate(String wsdl, String binding, String outdir)
        throws CodeGenerationException {
        
        // create the option map
        Map optionMap = new HashMap();
        optionMap.put(CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION,
            new CommandLineOption(CommandLineOptionConstants.WSDL2JavaConstants.WSDL_LOCATION_URI_OPTION,
            new String[] {wsdl}));

        //use default sync option - No option is given
        //use default async option - No option is given
        //use default language option - No option is given
        
        // output location
        optionMap.put(CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
            new CommandLineOption(CommandLineOptionConstants.WSDL2JavaConstants.OUTPUT_LOCATION_OPTION,
            new String[] {outdir}));
        
        // server side option is on
        optionMap.put(CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION,
            new CommandLineOption(CommandLineOptionConstants.WSDL2JavaConstants.SERVER_SIDE_CODE_OPTION,
            new String[0]));
        
        // descriptor option is on
        optionMap.put(CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
            new CommandLineOption(CommandLineOptionConstants.WSDL2JavaConstants.GENERATE_SERVICE_DESCRIPTION_OPTION,
            new String[0]));
        
         // db is JiBX
         optionMap.put(CommandLineOptionConstants.WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION,
            new CommandLineOption(CommandLineOptionConstants.WSDL2JavaConstants.DATA_BINDING_TYPE_OPTION,
            new String[] {"jibx"}));
         
         // binding definition is supplied
         String option = CommandLineOptionConstants.WSDL2JavaConstants.EXTRA_OPTIONTYPE_PREFIX +
             JiBXExtension.BINDING_PATH_OPTION;
         optionMap.put(option, new CommandLineOption(option, new String[] {binding}));
         
        //TODO: Make this work
        //test case option is on
//        optionMap.put(
//                CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
//                new CommandLineOption(
//                        CommandLineOptionConstants.GENERATE_TEST_CASE_OPTION,
//                        new String[0]));
        CommandLineOptionParser parser = new CommandLineOptionParser(optionMap);
        new CodeGenerationEngine(parser).generate();
    }

    /**
     * Compile generated code.
     * 
     * @param outdir
     */
    private void compile(String outdir) {
        
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
        String classdir = outdir + "/classes";
        File outputLocationFile = new File(classdir);
        outputLocationFile.mkdir();
        Path classPath = new Path(codeGenProject, classdir) ;
        classPath.addExisting(classPath.concatSystemClasspath(), false);
        for (int i = 0; i < moduleNames.length; i++) {
            classPath.add(new Path(codeGenProject,
                MODULE_PATH_PREFIX + moduleNames[i] + CLASSES_DIR));
        }
        javaCompiler.setClasspath(classPath);

        //set sourcePath - The generated output directories also become part of the sourcepath
        Path sourcePath = new Path(codeGenProject, outdir) ;
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
    
    public void testRunCustomer() throws Exception {
        String outdir =
            OUTPUT_LOCATION_BASE + OUTPUT_LOCATION_PREFIX + folderCount++;
        codeGenerate(WSDL_BASE_DIR + "customer-echo.wsdl",
            BINDING_BASE_DIR + "customer-binding.xml", outdir);
        compile(outdir);
    }
    
    /**
     * Unmarshal the sample document from a file, then marshal it back out to
     * another file.
     */
/*	public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java -cp ... " +
                "org.jibx.starter.Test in-file out-file");
            System.exit(0);
        }
		try {
			
            // note that you can use multiple bindings with the same class, in
            //  which case you need to use the getFactory() call that takes the
            //  binding name as the first parameter
            IBindingFactory bfact = BindingDirectory.getFactory(Customer.class);
            
            // unmarshal customer information from file
            IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
            FileInputStream in = new FileInputStream(args[0]);
            Customer customer = (Customer)uctx.unmarshalDocument(in, null);
            
            // you can add code here to alter the unmarshalled customer
            
			// marshal object back out to file (with nice indentation, as UTF-8)
			IMarshallingContext mctx = bfact.createMarshallingContext();
			mctx.setIndent(2);
			FileOutputStream out = new FileOutputStream(args[1]);
			mctx.marshalDocument(customer, "UTF-8", null, out);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
            System.exit(1);
		} catch (JiBXException e) {
			e.printStackTrace();
            System.exit(1);
		}
	}    */
}