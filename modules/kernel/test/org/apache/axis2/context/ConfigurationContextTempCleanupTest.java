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

package org.apache.axis2.context;

import org.apache.axis2.Constants;

import junit.framework.TestCase;

import java.io.File;
import java.nio.file.Files;

public class ConfigurationContextTempCleanupTest extends TestCase {

    /**
     * Removing the artifacts temp directory on terminate must delete a symbolic
     * link inside it, not the contents of the link's target.
     */
    public void testTerminateDoesNotFollowSymlinksInTempDir() throws Exception {
        File base = Files.createTempDirectory("axis2-cc").toFile();
        File artifacts = new File(base, "_axis2");
        File victim = new File(base, "victim");
        assertTrue(artifacts.mkdir());
        assertTrue(victim.mkdir());
        File kept = new File(victim, "kept.txt");
        Files.write(kept.toPath(), new byte[] { 1 });
        try {
            Files.createSymbolicLink(new File(artifacts, "link").toPath(), victim.toPath());
        } catch (UnsupportedOperationException | java.io.IOException e) {
            return; // no symlink support on this platform
        }

        ConfigurationContext configContext =
                ConfigurationContextFactory.createEmptyConfigurationContext();
        configContext.getAxisConfiguration().addParameter(
                Constants.Configuration.ARTIFACTS_TEMP_DIR, artifacts);
        configContext.terminate();

        assertTrue("symlink target was emptied", kept.exists());
        assertFalse("artifacts temp dir was not removed", artifacts.exists());
    }
}
