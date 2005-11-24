package org.apache.axis2.jaxws.handler;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.handler.Axis2Handler;

public class Axis2HandlerTest extends TestCase
{
  org.apache.axis2.jaxws.handler.Axis2Handler axis2handler = null;
  
  public Axis2HandlerTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.handler.Axis2Handler createInstance() throws Exception {
    return new org.apache.axis2.jaxws.handler.Axis2Handler();
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    axis2handler = createInstance();
  }
  
  protected void tearDown() throws Exception {
    axis2handler = null;
    super.tearDown();
  }
  
  public void testSetGetJaxRpcHandler() throws Exception {
//    javax.xml.rpc.handler.AbstractHandler[] tests = {new javax.xml.rpc.handler.AbstractHandler(), null};
//    
//    for (int i = 0; i < tests.length; i++) {
//      axis2handler.setJaxRpcHandler(tests[i]);
//      assertEquals(tests[i], axis2handler.getJaxRpcHandler());
//    }
  }
  
  public void testInvoke() throws Exception {
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(Axis2HandlerTest.class);
  }
}
