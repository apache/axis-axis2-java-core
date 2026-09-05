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
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;

import junit.framework.TestCase;

import org.apache.axis2.description.AxisService;

/**
 * The ?xsd= and ?wsdl2= routes serve a packaged document by the name the request
 * asked for. Two rules hold: only schema and WSDL documents, and only from the
 * queried service's own archive -- the AXIS2-5846 rule, which the transport route
 * followed and AxisService.printXSD/printWSDL2 did not, since they used
 * getResourceAsStream and so searched the whole ancestor chain.
 */
public class MetaInfResourcesTest extends TestCase {

    private File root;
    private File parentRoot;
    private URLClassLoader parent;
    private URLClassLoader loader;

    @Override
    protected void setUp() throws Exception {
        root = makeArchive("own", "mine.xsd", "<own/>");
        parentRoot = makeArchive("parent", "theirs.xsd", "<theirs/>");
        parent = new URLClassLoader(new URL[] { parentRoot.toURI().toURL() }, null);
        loader = new URLClassLoader(new URL[] { root.toURI().toURL() }, parent);
    }

    @Override
    protected void tearDown() throws Exception {
        if (loader != null) {
            loader.close();
        }
        if (parent != null) {
            parent.close();
        }
        delete(root);
        delete(parentRoot);
    }

    private File makeArchive(String prefix, String name, String content) throws Exception {
        File dir = File.createTempFile("axis2-" + prefix, "");
        dir.delete();
        File metaInf = new File(dir, "META-INF");
        assertTrue(metaInf.mkdirs());
        FileOutputStream out = new FileOutputStream(new File(metaInf, name));
        try {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        } finally {
            out.close();
        }
        return dir;
    }

    private void delete(File file) {
        if (file == null) {
            return;
        }
        File[] children = file.listFiles();
        if (children != null) {
            for (int i = 0; i < children.length; i++) {
                delete(children[i]);
            }
        }
        file.delete();
    }

    private AxisService serviceOn(ClassLoader classLoader) {
        AxisService service = new AxisService("Test");
        service.setClassLoader(classLoader);
        return service;
    }

    private String read(InputStream in) throws Exception {
        assertNotNull(in);
        try {
            byte[] buffer = new byte[256];
            int n = in.read(buffer);
            return new String(buffer, 0, n, StandardCharsets.UTF_8);
        } finally {
            in.close();
        }
    }

    public void testTheServicesOwnDocumentIsServed() throws Exception {
        InputStream in = MetaInfResources.getResourceAsStream(serviceOn(loader), "mine.xsd");
        assertEquals("<own/>", read(in));
    }

    /** The finding: getResourceAsStream delegates parent-first and would find this. */
    public void testADocumentInAnAncestorClassloaderIsNotServed() throws Exception {
        assertNotNull("the ancestor really does carry it, so the test means something",
                loader.getResourceAsStream("META-INF/theirs.xsd"));
        assertNull("a resource from an unrelated jar must not be reachable by name",
                MetaInfResources.getResourceAsStream(serviceOn(loader), "theirs.xsd"));
    }

    public void testAMissingDocumentIsNotFound() {
        assertNull(MetaInfResources.getResourceAsStream(serviceOn(loader), "absent.xsd"));
    }

    /**
     * A classloader that is not a URLClassLoader cannot be searched without
     * delegating, so this route serves nothing rather than serving the whole chain.
     */
    public void testANonUrlClassloaderServesNothing() {
        ClassLoader plain = new ClassLoader(loader) {
        };
        assertNull(MetaInfResources.getResourceAsStream(serviceOn(plain), "mine.xsd"));
    }

    public void testANullServiceIsHandled() {
        assertNull(MetaInfResources.getResourceAsStream(null, "mine.xsd"));
    }

    public void testOnlySchemaAndWsdlDocumentsAreServable() {
        assertTrue(MetaInfResources.isServable("a.xsd"));
        assertTrue(MetaInfResources.isServable("a.wsdl"));
        assertTrue(MetaInfResources.isServable("sub/a.xsd"));
        assertFalse("services.xml names keystores and callback classes",
                MetaInfResources.isServable("services.xml"));
        assertFalse(MetaInfResources.isServable("MANIFEST.MF"));
        assertFalse(MetaInfResources.isServable(null));
        assertFalse(MetaInfResources.isServable(""));
    }

    public void testTraversalAbsoluteAndSchemeNamesAreRefused() {
        assertFalse(MetaInfResources.isServable("../../services.xml"));
        assertFalse(MetaInfResources.isServable("../a.xsd"));
        assertFalse(MetaInfResources.isServable("/etc/a.xsd"));
        assertFalse(MetaInfResources.isServable("http://elsewhere/a.xsd"));
        assertFalse("a bare extension is not a document", MetaInfResources.isServable(".xsd"));
        assertFalse(MetaInfResources.isServable("sub/.wsdl"));
    }

    /** Traversal is refused whatever the archive holds, not merely not found. */
    public void testTraversalIsRefusedBeforeAnyLookup() throws Exception {
        assertNull(MetaInfResources.getResourceAsStream(
                serviceOn(loader), "../META-INF/mine.xsd"));
    }
}
