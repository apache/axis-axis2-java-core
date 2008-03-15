package org.apache.axis2.metadata.registry;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.wsdl.WSDLReaderConfigurator;

import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import java.io.File;


public class MetadataFactoryRegistryTests extends TestCase {
    
    public void testConfigurationFile() {
        String configLoc = null;
        try {
            String sep = "/";
            configLoc = sep + "test-resources" + sep + "META-INF" + sep + "services" +
            sep + "org.apache.axis2.metadata.registry.MetadataFactoryRegistry";
            String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
            configLoc = new File(baseDir + configLoc).getAbsolutePath();
        }
        catch(Exception e) {
            e.printStackTrace();
        }
        if(configLoc != null) {
            MetadataFactoryRegistry.setConfigurationFileLocation(configLoc);
            Object obj = MetadataFactoryRegistry.getFactory(TestInterface.class);
            assertNotNull(obj);
            assertEquals(obj.getClass().getName(), TestImplementation.class.getName()); 
        }
    }
    
    public void testRegisterWSDLReaderConfigurator() {
    	Exception e = null;
    	WSDLReader reader = null;
    	try {
    		WSDLFactory factory = WSDLFactory.newInstance();
        	reader = factory.newWSDLReader();
    	}
    	catch(Exception e2) {
    		e.printStackTrace();
    		e = e2;
    	}
    	assertNull(e);
    	assertNotNull(reader);
    	WSDLReaderConfigurator configurator = (WSDLReaderConfigurator) MetadataFactoryRegistry.
    		getFactory(WSDLReaderConfigurator.class);
    	assertNotNull(configurator);
    	try {
    		configurator.configureReaderInstance(reader);
    	}
    	catch(Exception e2) {
    		e = e2;
    	}
    	assertNull(e);
    	assertEquals(reader.getFeature(com.ibm.wsdl.Constants.FEATURE_VERBOSE), false);
    }

    // This interface class will be used to test the file based registration
    // of custom implementations with the MetadataFactoryRegistry
    public interface TestInterface {
        public void doSomething();
    }
    
}
