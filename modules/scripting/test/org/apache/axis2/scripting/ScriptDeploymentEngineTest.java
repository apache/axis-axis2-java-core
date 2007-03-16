package org.apache.axis2.scripting;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.engine.AxisConfiguration;

public class ScriptDeploymentEngineTest extends TestCase {
    
    public void test1() throws URISyntaxException, InterruptedException, MalformedURLException, AxisFault {
        AxisConfiguration axisConfig = new AxisConfiguration();
        ScriptDeploymentEngine sde = new ScriptDeploymentEngine(axisConfig);
        URL testScript = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.js");
        File scriptsDir = new File(testScript.toURI()).getParentFile();
        sde.loadRepository(scriptsDir);
        sde.loadServices();
        assertNotNull(axisConfig.getService("test"));
    }

}
