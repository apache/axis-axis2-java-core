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
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.Map;

import javax.xml.namespace.QName;

import junit.framework.TestCase;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.wsdl.codegen.CodeGenerationEngine;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;
import org.apache.axis2.wsdl.codegen.extension.JiBXExtension;
import org.apache.axis2.wsdl.util.CommandLineOption;
import org.apache.axis2.wsdl.util.CommandLineOptionConstants;
import org.apache.axis2.wsdl.util.CommandLineOptionParser;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.taskdefs.Javac;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Commandline.Argument;
import org.apache.axis2.util.Utils;
import org.jibx.binding.Compile;
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
    private static final String TEST_CLASSES_DIR = "target/test-classes";
    private static final String OUTPUT_LOCATION_BASE = "target/gen";
    private static final String OUTPUT_LOCATION_PREFIX = "/test";
    private static final String WSDL_BASE_DIR = "test-resources/wsdl/";
    private static final String BINDING_BASE_DIR = "test-resources/binding/";
    public static final String REPOSITORY_DIR = "test-resources/repo/";
    private static final String CLASSES_DIR = "/target/classes/";
    private static final String[] moduleNames= {"common", "core"};
    private static final String MODULE_PATH_PREFIX = "../modules/";
    private static final String COMPILE_TARGET_NAME = "compile";
    private static final String BIND_TARGET_NAME = "bind";
    private static final String STUB_CLASS =
        "org.apache.axis2.EchoCustomerServiceStub";

    public static final QName serviceName = new QName("EchoCustomerService");
    public static final QName operationName = new QName("echo");
    
    private AxisService service;


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
        service = Utils.createSimpleService(serviceName,
            Echo.class.getName(), operationName);
        UtilServer.start(REPOSITORY_DIR);
        UtilServer.deployService(service);
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
        UtilServer.unDeployService(serviceName);
        UtilServer.stop();
/*        File outputFile = new File(OUTPUT_LOCATION_BASE);
        if (outputFile.exists() && outputFile.isDirectory()){
            deleteDir(outputFile);
        }   */
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
    private void compile(String outdir) throws Exception {
        
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
        classPath.add(new Path(codeGenProject, TEST_CLASSES_DIR));
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
        javaCompiler.setDebug(true);
        javaCompiler.setVerbose(true);
        javaCompiler.execute();
//        codeGenProject.executeTarget(COMPILE_TARGET_NAME);
    }

    /**
     * Bind the data classes.
     * 
     * @param datadir
     * @param binding
     */
    private void bind(String datadir, String binding) throws Exception {
        
        // define ant java task for binding compiler
        Java java = new Java();
        Project codeGenProject = new Project();
        Target compileTarget = new Target();
        compileTarget.setName(BIND_TARGET_NAME);
        compileTarget.addTask(java);
        codeGenProject.addTarget(compileTarget);
        codeGenProject.setSystemProperties();
        java.setProject(codeGenProject);
        java.setFork(true);
        java.setClassname("org.jibx.binding.Compile");

        // set classpath to include generated classes
        Path classPath = new Path(codeGenProject, datadir) ;
        classPath.addExisting(classPath.concatSystemClasspath(), false);
        java.setClasspath(classPath);
        
        // add binding definition as argument
        Argument arg = java.createArg();
        arg.setValue(binding);

        // execute the binding compiler
        java.execute();
    }
    
    public void testBuildAndRun() throws Exception {
        
        // start by generating and compiling the Axis2 interface code
        String outdir =
            OUTPUT_LOCATION_BASE + OUTPUT_LOCATION_PREFIX;
        codeGenerate(WSDL_BASE_DIR + "customer-echo.wsdl",
            BINDING_BASE_DIR + "customer-binding.xml", outdir);
        compile(outdir);
        
        // execute the JiBX binding compiler
        bind(outdir + "/classes", BINDING_BASE_DIR + "customer-binding.xml");
        
        // finish by testing a roundtrip call to the echo server
        File classesdir = new File(outdir + "/classes");
        URLClassLoader loader = new URLClassLoader(new URL[] {classesdir.toURL()});
        Class stub = loader.loadClass(STUB_CLASS);
        Object inst = stub.newInstance();
        Person person = new Person(42, "John", "Smith");
        Customer customer = new Customer("Redmond", person, "+14258858080",
            "WA", "14619 NE 80th Pl.", new Integer(98052));
        Method method = stub.getMethod("echo", new Class[] {Customer.class});
        Object result = method.invoke(inst, new Object[] {customer});
        assertEquals("Result object does not match request object",
            customer, result);
    }
}