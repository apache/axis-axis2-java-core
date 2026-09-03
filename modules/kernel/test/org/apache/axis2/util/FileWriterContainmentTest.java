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
package org.apache.axis2.util;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

/**
 * Code generation derives output file names from the document it was handed: a
 * {@code wsdl:service} name reaches the shared sink verbatim, and a QName localPart
 * is not required to be an NCName, so path separators and {@code ..} survive. A name
 * like {@code ../../../src/main/resources/api} would otherwise have the tool write
 * the re-serialized WSDL over a project's real contract file, which later builds
 * then consume.
 */
public class FileWriterContainmentTest extends TestCase {

    private File root;

    @Override
    protected void setUp() throws Exception {
        root = File.createTempFile("axis2-codegen-root", "");
        assertTrue(root.delete());
        assertTrue(root.mkdirs());
    }

    @Override
    protected void tearDown() throws Exception {
        if (root != null) {
            root.delete();
        }
    }

    public void testOrdinaryNamesResolveUnderTheRoot() throws Exception {
        File created = FileWriter.createClassFile(root, null, "Version", ".wsdl");
        assertEquals(new File(root, "Version.wsdl").getCanonicalPath(),
                created.getCanonicalPath());
    }

    public void testPackageDirectoriesStillWork() throws Exception {
        File created = FileWriter.createClassFile(root, "com.example.svc", "Stub", ".java");
        assertTrue("the package path must stay under the root",
                created.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator));
        assertTrue(created.getName().equals("Stub.java"));
    }

    /** The finding: a traversing service name must not select the write target. */
    public void testATraversingFileNameIsRefused() throws Exception {
        try {
            FileWriter.createClassFile(root, null,
                    ".." + File.separator + ".." + File.separator + "api", ".wsdl");
            fail("a name escaping the output directory must be refused");
        } catch (IOException expected) {
            assertTrue("the message should say what was refused, was: "
                            + expected.getMessage(),
                    expected.getMessage().contains("Refusing to write generated output"));
        }
    }

    /**
     * The package argument cannot traverse, and it is worth recording why rather
     * than assuming the guard is what stops it: the name is split on dots, so a
     * {@code ..} segment is consumed as two delimiters and never survives as a
     * directory name. The guard is still what contains a traversing file name.
     */
    public void testATraversingPackageCannotEscapeAnyway() throws Exception {
        File created = FileWriter.createClassFile(root,
                "a" + File.separator + ".." + File.separator + "..", "Stub", ".java");
        assertTrue("stays under the root: " + created.getCanonicalPath(),
                created.getCanonicalPath().startsWith(root.getCanonicalPath() + File.separator));
    }

    /**
     * An absolute-looking name is contained by File itself: {@code File(parent,
     * child)} resolves the child against the parent even when it starts with a
     * separator. Recorded so nobody removes the guard believing this test covered it.
     */
    public void testAnAbsoluteNameIsResolvedUnderTheRootAnyway() throws Exception {
        File elsewhere = File.createTempFile("axis2-elsewhere", ".wsdl");
        try {
            File created = FileWriter.createClassFile(root, null,
                    elsewhere.getAbsolutePath(), null);
            assertTrue("stays under the root: " + created.getCanonicalPath(),
                    created.getCanonicalPath()
                            .startsWith(root.getCanonicalPath() + File.separator));
        } finally {
            elsewhere.delete();
        }
    }
}
