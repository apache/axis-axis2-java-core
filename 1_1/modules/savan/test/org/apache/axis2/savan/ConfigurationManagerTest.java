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
