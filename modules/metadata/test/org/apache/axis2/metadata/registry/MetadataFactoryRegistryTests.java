package org.apache.axis2.metadata.registry;

import java.io.File;

import junit.framework.TestCase;


public class MetadataFactoryRegistryTests extends TestCase {
    
    public void testConfigurationFile() {
        String configLoc = null;
        try {
            configLoc = "\\test-resources\\META-INF\\services\\" +
            "org.apache.axis2.metadata.registry.MetadataFactoryRegistry";
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

    // This interface class pair will be used to test the file based registration
    // of custom implementations with the MetadataFactoryRegistry
    public interface TestInterface {
        public void doSomething();
    }
    
}
