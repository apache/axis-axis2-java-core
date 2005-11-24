package org.apache.axis2.jaxws.handler;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.handler.HandlerChainImpl;

public class HandlerChainImplTest extends TestCase
{
  org.apache.axis2.jaxws.handler.HandlerChainImpl handlerchainimpl = null;
  
  public HandlerChainImplTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.handler.HandlerChainImpl createInstance() throws Exception {
    return new org.apache.axis2.jaxws.handler.HandlerChainImpl();
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    handlerchainimpl = createInstance();
  }
  
  protected void tearDown() throws Exception {
    handlerchainimpl = null;
    super.tearDown();
  }
  
  public void testAddNewHandler() throws Exception {
  }
  
  public void testGetMessageInfo() throws Exception {
  }
  
  public void testHandleRequest() throws Exception {
  }
  
  public void testHandleResponse() throws Exception {
  }
  
  public void testHandleFault() throws Exception {
  }
  
  public void testInit() throws Exception {
  }
  
  public void testDestroy() throws Exception {
  }
  
  public void testSetGetRoles() throws Exception {
    java.lang.String[][] tests = {new java.lang.String[0] , null};
    
    for (int i = 0; i < tests.length; i++) {
      handlerchainimpl.setRoles(tests[i]);
      assertEquals(tests[i], handlerchainimpl.getRoles());
    }
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(HandlerChainImplTest.class);
  }
}
