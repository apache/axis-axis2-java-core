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

package org.apache.axis2.maven2.repo;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.File;
import java.nio.file.Path;

import org.apache.maven.plugin.MojoFailureException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * The service name in a services.xml must not move the service directory out of
 * the repository's services directory.
 */
public class ServiceDirectoryContainmentTest {

    @TempDir
    Path temp;

    @Test
    public void ordinaryNameStaysInTheServicesDirectory() throws Exception {
        File services = new File(temp.toFile(), "repository/services");
        assertEquals(new File(services, "DemoService"),
                AbstractCreateRepositoryMojo.resolveServiceDirectory(services, "DemoService"));
    }

    @Test
    public void traversingNameIsRefused() {
        File services = new File(temp.toFile(), "repository/services");
        assertThrows(MojoFailureException.class, () ->
                AbstractCreateRepositoryMojo.resolveServiceDirectory(services, "../../../../escaped"));
        assertThrows(MojoFailureException.class, () ->
                AbstractCreateRepositoryMojo.resolveServiceDirectory(services, "a/../../escaped"));
        assertFalse(new File(temp.toFile().getParentFile(), "escaped").exists());
    }

    @Test
    public void nameResolvingToTheServicesDirectoryItselfIsRefused() {
        File services = new File(temp.toFile(), "repository/services");
        assertThrows(MojoFailureException.class, () ->
                AbstractCreateRepositoryMojo.resolveServiceDirectory(services, "."));
        assertThrows(MojoFailureException.class, () ->
                AbstractCreateRepositoryMojo.resolveServiceDirectory(services, ""));
    }

    @Test
    public void absoluteNameIsKeptUnderTheServicesDirectory() throws Exception {
        File services = new File(temp.toFile(), "repository/services");
        File resolved = AbstractCreateRepositoryMojo.resolveServiceDirectory(services, "/etc/demo");
        assertEquals(new File(services, "etc/demo").getCanonicalFile(), resolved.getCanonicalFile());
    }
}
