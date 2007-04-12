package org.apache.axis2.jaxws.wsdl.schemareader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import javax.wsdl.Definition;

import org.apache.axis2.jaxws.util.WSDL4JWrapper;
import org.apache.axis2.jaxws.wsdl.impl.SchemaReaderImpl;
import org.apache.axis2.jaxws.TestLogger;

import junit.framework.TestCase;

public class SchemaReaderTests extends TestCase {
	public void testSchemaReader(){
		SchemaReaderImpl sri = new SchemaReaderImpl();
		
	    String wsdlLocation="/test-resources/wsdl/shapes.wsdl";
	    URL url = null;
	    try {
	    	try{
	        	String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
	        	wsdlLocation = new File(baseDir + wsdlLocation).getAbsolutePath();
        	}catch(Exception e){
        		e.printStackTrace();
        		fail();
        	}
	       	File file = new File(wsdlLocation);
	       	url = file.toURL();
	    } catch (MalformedURLException e) {
	        e.printStackTrace();
	        fail();
	    }
	    try{
	    	WSDL4JWrapper w4j = new WSDL4JWrapper(url);
	    	Definition wsdlDef = w4j.getDefinition();
	    	assertNotNull(wsdlDef);
	    	Set<String> pkg = sri.readPackagesFromSchema(wsdlDef);
            TestLogger.logger.debug("Packages:");
	    	for(String pkgName:pkg){
                TestLogger.logger.debug(pkgName);
	    	}
	    }catch(Exception e){
	    	e.printStackTrace();
	    	fail();
	    }
	}
	
}
