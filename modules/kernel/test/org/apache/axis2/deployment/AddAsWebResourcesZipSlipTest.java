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

package org.apache.axis2.deployment;

import org.apache.axis2.description.AxisServiceGroup;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AddAsWebResourcesZipSlipTest extends TestCase {

    public void testWwwEntryCannotEscapeWebDirectory() throws Exception {
        File base = File.createTempFile("axis2-zipslip", "dir");
        assertTrue(base.delete());
        File webLocation = new File(base, "web");
        // out = webLocation/svc — the per-service web dir, created from the
        // archive's WWW/ directory entry just as a real deployment would.
        assertTrue(new File(webLocation, "svc").mkdirs());

        // A WWW/../../ entry resolves to base/, outside the web resource
        // directory entirely.
        File escapeTarget = new File(base, "pwned.txt");
        assertFalse(escapeTarget.exists());

        File archive = new File(base, "evil.aar");
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(archive))) {
            zos.putNextEntry(new ZipEntry("WWW/"));
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("WWW/../../pwned.txt"));
            zos.write("owned".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }

        String previous = DeploymentEngine.getWebLocationString();
        DeploymentEngine.setWebLocationString(webLocation.getAbsolutePath());
        try {
            Method m = DeploymentEngine.class.getDeclaredMethod(
                    "addAsWebResources", File.class, String.class, AxisServiceGroup.class);
            m.setAccessible(true);
            m.invoke(null, archive, "svc", new AxisServiceGroup());
        } finally {
            DeploymentEngine.setWebLocationString(previous);
        }

        assertFalse("zip slip: WWW/ entry was written outside the web resource directory",
                escapeTarget.exists());
    }
}
