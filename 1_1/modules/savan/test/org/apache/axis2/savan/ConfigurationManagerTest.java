/*
 * Copyright 2004,2005 The Apache Software Foundation.
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

package org.apache.axis2.savan;

import java.io.File;

import org.apache.savan.SavanException;
import org.apache.savan.configuration.ConfigurationManager;

import junit.framework.TestCase;

public class ConfigurationManagerTest extends TestCase {

	public void testFromXMLFile () throws SavanException {
		
        File baseDir = new File("");
        String testRource = baseDir.getAbsolutePath() + File.separator + "test-resources";
        String testConfigurationFile = testRource + File.separator + "savan-config-test.xml";
        
		File f = new File (testConfigurationFile);  //test-resources configuration file.
		if (!f.isFile())
			throw new SavanException ("Cant find the test configuration file");
		
		ConfigurationManager cm = new ConfigurationManager ();
		cm.configure(f);
		
		
	}
}
