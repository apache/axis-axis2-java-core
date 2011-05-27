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

import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.versioning.VersionRange;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.DefaultArtifactHandlerStub;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.util.ReaderFactory;

public abstract class AbstractAarTest extends AbstractMojoTestCase {

	public Mojo getAarMojoGoal(String goal, String testPom) throws Exception {

		File pom = new File(getBasedir(), testPom);
		MavenXpp3Reader pomReader = new MavenXpp3Reader();
		MavenProject project = new MavenProject();
		Model model = pomReader.read(ReaderFactory.newXmlReader(pom));
		// Set project properties.
		setVariableValueToObject(project, "model", model);
		setVariableValueToObject(project, "file", pom);
		Artifact artifact = new DefaultArtifact(model.getGroupId(),
				model.getArtifactId(),
				VersionRange.createFromVersionSpec("SNAPSHOT"), null, "aar",
				null, (new DefaultArtifactHandlerStub("aar", null)));
		artifact.setBaseVersion("SNAPSHOT");
		artifact.setVersion("SNAPSHOT");
		setVariableValueToObject(project, "artifact", artifact);
		// Create and set Mojo properties.
		Mojo mojo = lookupMojo(goal, pom);
		setVariableValueToObject(mojo, "aarDirectory", new File(getBasedir(),
				"target/aar"));
		setVariableValueToObject(mojo, "aarName", model.getArtifactId());
		setVariableValueToObject(mojo, "outputDirectory", "target");
		setVariableValueToObject(mojo, "project", project);
		// Use some classes only for testing.
		setVariableValueToObject(mojo, "classesDirectory", new File(
				getBasedir(), "target/classes"));
		assertNotNull(mojo);
		return mojo;

	}

}
