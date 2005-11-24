package org.apache.axis2.jaxws.server;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.server.JAXRPCInOutMessageReceiver;

public class JAXRPCInOutMessageReceiverTest extends TestCase
{
  org.apache.axis2.jaxws.server.JAXRPCInOutMessageReceiver jaxrpcinoutmessagereceiver = null;
  
  public JAXRPCInOutMessageReceiverTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.server.JAXRPCInOutMessageReceiver createInstance() throws Exception {
    return new org.apache.axis2.jaxws.server.JAXRPCInOutMessageReceiver();
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    jaxrpcinoutmessagereceiver = createInstance();
  }
  
  protected void tearDown() throws Exception {
    jaxrpcinoutmessagereceiver = null;
    super.tearDown();
  }
  
  public void testInvokeBusinessLogic() throws Exception {
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(JAXRPCInOutMessageReceiverTest.class);
  }
}
