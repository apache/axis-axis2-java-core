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

package org.apache.ws.java2wsdl.jaxws;

import java.io.File;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.java2wsdl.utils.Java2WSDLCommandLineOptionParser;

public class JAXWS2WSDLCodegenEngineTest extends TestCase {
    
    private static final Log log = LogFactory
    .getLog(JAXWS2WSDLCodegenEngineTest.class);

    final String filePath = "target/out";

    public void setUp() throws Exception {
        super.setUp();
        File dir = new File(
                filePath.concat("org/apache/ws/java2wsdl/jaxws/jaxws"));
        File wsdl = new File(filePath.concat("ServerInfoService.wsdl"));
        assertEquals("Generated directory still exists ", false, dir.exists());
        assertEquals("Generated WSDL file still exists ", false, wsdl.exists());
        dir = new File(filePath);
        dir.mkdir();
    }

    public void tearDown() throws Exception {
        super.tearDown();
        String path = "org/apache/ws/java2wsdl/jaxws/jaxws";
        File file = new File(filePath.concat(path));
        if (file.exists() && file.isDirectory()) {
            for (File child : file.listFiles()) {
                child.delete();
            }
        }
        file.delete();

        int idx = path.lastIndexOf("/");
        while (idx > 0) {
            path = path.substring(0, idx);
            idx = path.lastIndexOf("/");
            file = new File(filePath.concat(path));
            file.delete();
        }
    }

    public void testGenerateWithMixOptions() throws Exception {
        String[] args = { "-jws", "-verbose", "-cp", "target/test-classes",
                "-cn", "org.apache.ws.java2wsdl.jaxws.ServerInfo", "-o",
                "./target/out" };
        Java2WSDLCommandLineOptionParser commandLineOptionParser = new Java2WSDLCommandLineOptionParser(
                args);
        JAXWS2WSDLCodegenEngine engine = new JAXWS2WSDLCodegenEngine(
                commandLineOptionParser.getAllOptions(), args);
        engine.generate();     
        verifyGeneration();
    }

    public void testGenerateWithAxisOptions() throws Exception {
        String[] args = { "-jws", "-cp", "target/test-classes", "-cn",
                "org.apache.ws.java2wsdl.jaxws.ServerInfo", "-o", "./target/out" };
        Java2WSDLCommandLineOptionParser commandLineOptionParser = new Java2WSDLCommandLineOptionParser(
                args);
        JAXWS2WSDLCodegenEngine engine = new JAXWS2WSDLCodegenEngine(
                commandLineOptionParser.getAllOptions(), args);
        engine.generate();
        verifyGeneration();
    }

    public void testGenerateWithJAXWSOptions() throws Exception {
        String[] args = { "-jws", "-cp", "target/test-classes", "-cn",
                "org.apache.ws.java2wsdl.jaxws.ServerInfo", "-d", "./target/out" };
        Java2WSDLCommandLineOptionParser commandLineOptionParser = new Java2WSDLCommandLineOptionParser(
                args);
        JAXWS2WSDLCodegenEngine engine = new JAXWS2WSDLCodegenEngine(
                commandLineOptionParser.getAllOptions(), args);
        engine.generate();
        verifyGeneration();
    }

    private void verifyGeneration() {
        File dir = new File(
                filePath.concat("/org/apache/ws/java2wsdl/jaxws/jaxws"));
        assertEquals("Generated directory does not exists ", true, dir.exists());
        assertEquals("Generated directory does not exists ", true,
                dir.isDirectory());
        /*
         * Since we use -Xnocompile there should be only 2 files but due to a a
         * exception current version doesn't support for this. Refer
         * http://java.net/jira/browse/JAX_WS-360
         */
        assertEquals("Incorrect number of generated files", true,
                (dir.listFiles().length >= 2));
        File wsdl = new File(filePath.concat("/ServerInfoService.wsdl"));       
        assertEquals("Generated WSDL file does not exists ", true,
                wsdl.exists());
        wsdl.delete();

    }

}
