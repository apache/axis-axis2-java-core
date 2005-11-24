package org.apache.axis2.jaxws.handler.soap;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.handler.soap.SOAPMessageContextImpl;

public class SOAPMessageContextImplTest extends TestCase
{
  org.apache.axis2.jaxws.handler.soap.SOAPMessageContextImpl soapmessagecontextimpl = null;
  
  public SOAPMessageContextImplTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.handler.soap.SOAPMessageContextImpl createInstance() throws Exception {
    return new org.apache.axis2.jaxws.handler.soap.SOAPMessageContextImpl(null);
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    soapmessagecontextimpl = createInstance();
  }
  
  protected void tearDown() throws Exception {
    soapmessagecontextimpl = null;
    super.tearDown();
  }
  
  public void testSetGetMessage() throws Exception {
//    SOAPMessage[] tests = {new SOAPMessage(), null};
//    
//    for (int i = 0; i < tests.length; i++) {
//      soapmessagecontextimpl.setMessage(tests[i]);
//      assertEquals(tests[i], soapmessagecontextimpl.getMessage());
//    }
  }
  
  public void testGetHeaders() throws Exception {
  }
  
  public void testSetGetRoles() throws Exception {
    java.lang.String[][] tests = {new java.lang.String[0] , null};
    
    for (int i = 0; i < tests.length; i++) {
      soapmessagecontextimpl.setRoles(tests[i]);
      assertEquals(tests[i], soapmessagecontextimpl.getRoles());
    }
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(SOAPMessageContextImplTest.class);
  }
}
