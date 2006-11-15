package org.apache.axis2.maven2.wsdl2code;

/*
 * Copyright 2006 Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.axis2.maven2.wsdl2code.WSDL2CodeMojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.stubs.MavenProjectStub;

import org.codehaus.plexus.util.FileUtils;

/**
 * Test class for running the wsdl2code mojo.
 */
public class WSDL2CodeMojoTest extends AbstractMojoTestCase {
    /**
     * Tests running the java generator.
     */
    public void testJava() throws Exception {
		runTest( "src/test/test1", "wsdl2code" );
	}

	protected WSDL2CodeMojo newMojo( String pDir, String pGoal ) throws Exception
	{
		File baseDir = new File(new File(getBasedir()), pDir);
		File testPom = new File( baseDir, "pom.xml" );
		WSDL2CodeMojo mojo = (WSDL2CodeMojo) lookupMojo( pGoal, testPom );
		MavenProjectStub project = new MavenProjectStub();
		project.setDependencyArtifacts( new HashSet() );
		setVariableValueToObject(mojo, "project", project);
		setVariableValueToObject(mojo, "pluginArtifacts", new ArrayList());
		setVariableValueToObject(mojo, "wsdlFile", new File(baseDir, "src/main/axis2/service.wsdl"));
		setVariableValueToObject(mojo, "outputDirectory", new File(baseDir, "target/generated-sources/axis2/wsdl2code"));
		setVariableValueToObject(mojo, "syncMode", "both");
		setVariableValueToObject(mojo, "databindingName", "adb");
		setVariableValueToObject(mojo, "language", "java");
		return mojo;
	}

	protected void runTest( String pDir, String pGoal )
    	throws Exception
	{
		newMojo( pDir, pGoal ).execute();
	}
}
