package org.apache.axis2.scripting;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

import junit.framework.TestCase;

import org.apache.axis2.AxisFault;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.engine.AxisConfiguration;

public class ScriptModuleTest extends TestCase {

//    public void testGetScriptForWSDL() throws MalformedURLException, URISyntaxException {
//        ScriptModule module = new ScriptModule();
//        URL wsdlURL = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.wsdl");
//        URL scriptURL = module.getScriptForWSDL(wsdlURL);
//        assertTrue(scriptURL.toString().endsWith("test.js"));
//    }
//
//    public void testGetWSDLsInDir() throws MalformedURLException, URISyntaxException {
//        ScriptModule module = new ScriptModule();
//        URL wsdlURL = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.wsdl");
//        URL scriptsDir = new File(wsdlURL.toURI()).getParentFile().toURL();
//        List wsdls = module.getWSDLsInDir(scriptsDir);
//        assertEquals(2, wsdls.size());
//        assertTrue(wsdls.get(0).toString().endsWith("test.wsdl"));
//    }
//
//    public void testReadScriptSource() throws AxisFault {
//        ScriptModule module = new ScriptModule();
//        URL url = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.js");
//        String s = module.readScriptSource(url);
//        assertEquals("petra", s);
//    }

    public void testGetScriptServicesDirectory() throws AxisFault, MalformedURLException, URISyntaxException {
        ScriptModule module = new ScriptModule();
        AxisConfiguration axisConfig = new AxisConfiguration();
        URL url = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.js");
        File dir = new File(url.toURI()).getParentFile();
        axisConfig.setRepository(dir.getParentFile().toURL());
        axisConfig.addParameter(new Parameter("scriptServicesDir", dir.getName()));
        assertEquals(dir.toURL(), module.getScriptServicesDirectory(axisConfig).toURL());
    }

//    public void testCreateService() throws AxisFault {
//        URL wsdlURL = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.wsdl");
//        URL scriptURL = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.js");
//        ScriptModule module = new ScriptModule();
//        AxisService axisService = module.createService(wsdlURL, scriptURL);
//        assertEquals("petra", axisService.getParameterValue(ScriptReceiver.SCRIPT_SRC_PROP));
//    }

    public void testInit() throws AxisFault, MalformedURLException, URISyntaxException, InterruptedException {
        ScriptModule module = new ScriptModule();
        AxisConfiguration axisConfig = new AxisConfiguration();
        URL url = getClass().getClassLoader().getResource("org/apache/axis2/scripting/testrepo/test.js");
        File dir = new File(url.toURI()).getParentFile();
        axisConfig.setRepository(dir.getParentFile().toURL());
        axisConfig.addParameter(new Parameter("scriptServicesDir", dir.getName()));
        ConfigurationContext configContext = new ConfigurationContext(axisConfig);

        module.init(configContext, null);
        
        Thread.sleep(500);
        
        assertNotNull(axisConfig.getService("test"));
    }

}
