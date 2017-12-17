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
package org.apache.axis2.deployment.repository.util;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.net.URL;

import org.apache.axis2.deployment.Deployer;
import org.junit.Test;

public class DeploymentFileDataTest {
    @Test
    public void testGetNameWithFile() {
        DeploymentFileData dfd = new DeploymentFileData(new File("somedir", "myservice.aar"));
        assertThat(dfd.getName()).isEqualTo("myservice.aar");
    }

    @Test
    public void testGetNameWithURL() throws Exception {
        DeploymentFileData dfd = new DeploymentFileData(
                new URL("http://myserver.local/myservice.aar"), mock(Deployer.class),
                DeploymentFileData.class.getClassLoader());
        assertThat(dfd.getName()).isEqualTo("myservice.aar");
    }
}
