/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.axis2.wsdl.codegen.jaxws;

import java.io.File;

import org.apache.axis2.util.CommandLineOptionParser;
import org.apache.axis2.wsdl.codegen.CodeGenerationException;

import junit.framework.TestCase;

/**
 * The Class JAXWSCodeGenerationEngineTest.
 */
public class JAXWSCodeGenerationEngineTest extends TestCase {

    final String filePath = "./target/sample";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("javax.xml.accessExternalSchema", "all");
        File dir = new File(filePath);
        assertEquals("Generated directory dtill exists ", false, dir.exists());
    }

    @Override
    protected void tearDown() throws Exception {

        super.tearDown();
        File file = new File(filePath);
        if (file.exists() && file.isDirectory()) {
            for (File child : file.listFiles()) {
                child.delete();
            }
        }
        file.delete();
    }

    public void testGenerateWithMixOptions() throws CodeGenerationException {
        String[] args = { "-jws", "-uri",
                "test-resources/wsdls//SimpleService.wsdl", "-o", "./target" };
        CommandLineOptionParser commandLineOptionParser = new CommandLineOptionParser(
                args);
        JAXWSCodeGenerationEngine engine = new JAXWSCodeGenerationEngine(
                commandLineOptionParser, args);
        engine.generate();
        File dir = new File(filePath);
        assertEquals("Generated directory does not exists ", true, dir.exists());
        assertEquals("Generated directory does not exists ", true,
                dir.isDirectory());
        assertEquals("Incorrect number of generated files", 6,
                dir.listFiles().length);
    }

    public void testGenerateWithAxisOptions() throws CodeGenerationException {
        String[] args = { "-jws", "-uri",
                "test-resources/wsdls//SimpleService.wsdl", "-o", "./target" };
        CommandLineOptionParser commandLineOptionParser = new CommandLineOptionParser(
                args);
        JAXWSCodeGenerationEngine engine = new JAXWSCodeGenerationEngine(
                commandLineOptionParser, args);
        engine.generate();
        File dir = new File(filePath);
        assertEquals("Generated directory does not exists ", true, dir.exists());
        assertEquals("Generated directory does not exists ", true,
                dir.isDirectory());
        assertEquals("Incorrect number of generated files", 6,
                dir.listFiles().length);
    }

    public void testGenerateWithJAXWSOptions() throws CodeGenerationException {
        String[] originalArgs = { "-jws", "-Xdebug", "-verbose",
                "test-resources/wsdls/SimpleService.wsdl", "-d", "./target" };
        String[] args = { "-jws" };
        CommandLineOptionParser commandLineOptionParser = new CommandLineOptionParser(
                args);
        JAXWSCodeGenerationEngine engine = new JAXWSCodeGenerationEngine(
                commandLineOptionParser, originalArgs);
        engine.generate();
        File dir = new File(filePath);
        assertEquals("Generated directory does not exists ", true, dir.exists());
        assertEquals("Generated directory does not exists ", true,
                dir.isDirectory());
        assertEquals("Incorrect number of generated files", 6,
                dir.listFiles().length);
    }

}
