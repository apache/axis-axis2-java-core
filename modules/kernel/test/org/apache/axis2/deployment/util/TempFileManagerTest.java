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

package org.apache.axis2.deployment.util;

import junit.framework.TestCase;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;

public class TempFileManagerTest extends TestCase {

    /**
     * The stale-directory sweep that runs when TempFileManager is loaded must
     * not follow a symbolic link planted in the shared temp directory.
     */
    public void testStaleDirectoryCleanupDoesNotFollowSymlinks() throws Exception {
        File base = Files.createTempDirectory("axis2-tfm").toFile();
        File tmp = new File(base, "tmp");
        File victim = new File(base, "victim");
        assertTrue(tmp.mkdir());
        assertTrue(new File(victim, "sub").mkdirs());
        File kept = new File(victim, "sub/kept.txt");
        Files.write(kept.toPath(), new byte[] { 1 });

        File topLink = new File(tmp, "axis2-tmp-link");
        File stale = new File(tmp, "axis2-tmp-stale");
        assertTrue(stale.mkdir());
        File innerLink = new File(stale, "inner");
        try {
            Files.createSymbolicLink(topLink.toPath(), victim.toPath());
            Files.createSymbolicLink(innerLink.toPath(), victim.toPath());
        } catch (UnsupportedOperationException | java.io.IOException e) {
            return; // no symlink support on this platform
        }

        // Load a fresh copy of the class so its static cleanup runs against tmp.
        URL classes = TempFileManager.class.getProtectionDomain().getCodeSource().getLocation();
        String previous = System.getProperty("java.io.tmpdir");
        System.setProperty("java.io.tmpdir", tmp.getAbsolutePath());
        try (URLClassLoader loader = new URLClassLoader(new URL[] { classes },
                ClassLoader.getPlatformClassLoader())) {
            Class.forName(TempFileManager.class.getName(), true, loader);
        } finally {
            System.setProperty("java.io.tmpdir", previous);
        }

        assertTrue("symlink target was emptied", kept.exists());
        assertFalse("stale directory was not removed", stale.exists());
        assertTrue("top-level symlink was treated as a temp dir", Files.isSymbolicLink(topLink.toPath()));
    }
}
