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

package org.apache.axis2.maven2.aar;

import java.io.File;

public class AarMojoTest extends AbstractAarTest {

	public void testAarMojoGoal() throws Exception {

		AarMojo mojo = (AarMojo) getAarMojoGoal("aar",
				"target/test-classes/aar-plugin-config-1.xml");
		mojo.execute();
		String aarName = "target/axis2-aar-plugin-basic-test1.aar";
		assertTrue(" Can not find " + aarName, new File(aarName).exists());
	}

	public void testAarMojoGoalConfiguration() throws Exception {

		AarMojo mojo = (AarMojo) getAarMojoGoal("aar",
				"target/test-classes/aar-plugin-config-2.xml");
		mojo.execute();
		String aarName = "target/axis2-aar-plugin-configuration-test1.aar";
		assertTrue(" Can not find " + aarName, new File(aarName).exists());
	}

}
