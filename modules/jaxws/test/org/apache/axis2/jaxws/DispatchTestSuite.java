package org.apache.axis2.jaxws;

import junit.framework.TestSuite;

public class DispatchTestSuite {

    public static TestSuite suite() {
        TestSuite suite = new TestSuite();
        suite = addTestSuites(suite);
        return suite;
    }
	
    public static TestSuite addTestSuites(TestSuite suite) {
        suite.addTestSuite(StringDispatch.class);
        suite.addTestSuite(SourceDispatch.class);
        suite.addTestSuite(DOMSourceDispatch.class);
        suite.addTestSuite(SAXSourceDispatch.class);
        // FIXME: Add this test in
        
        suite.addTestSuite(JAXBDispatch.class);
        
        return suite;
    }

}
