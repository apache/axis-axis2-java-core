package org.apache.axis2.jaxws.client;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.client.CallImpl;

public class CallImplTest extends TestCase
{
  org.apache.axis2.jaxws.client.CallImpl callimpl = null;
  
  public CallImplTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.client.CallImpl createInstance() throws Exception {
    return new org.apache.axis2.jaxws.client.CallImpl();
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    callimpl = createInstance();
  }
  
  protected void tearDown() throws Exception {
    callimpl = null;
    super.tearDown();
  }
  
  public void testIsParameterAndReturnSpecRequired() throws Exception {
  }
  
  public void testAddParameter() throws Exception {
  }
  
  public void testGetParameterTypeByName() throws Exception {
  }
  
  public void testSetReturnType() throws Exception {
  }
  
  public void testGetJAXBObjectClassForQName() throws Exception {
  }
  
  public void testGetTypeMappingClassForQName() throws Exception {
  }
  
  public void testGetReturnType() throws Exception {
  }
  
  public void testRemoveAllParameters() throws Exception {
  }
  
  public void testSetGetOperationName() throws Exception {
//    QName[] tests = {new QName(), null};
//    
//    for (int i = 0; i < tests.length; i++) {
//      callimpl.setOperationName(tests[i]);
//      assertEquals(tests[i], callimpl.getOperationName());
//    }
  }
  
  public void testSetGetPortTypeName() throws Exception {
//    QName[] tests = {new QName(), null};
//    
//    for (int i = 0; i < tests.length; i++) {
//      callimpl.setPortTypeName(tests[i]);
//      assertEquals(tests[i], callimpl.getPortTypeName());
//    }
  }
  
  public void testSetGetTargetEndpointAddress() throws Exception {
    String[] tests = {new String(), null};
    
    for (int i = 0; i < tests.length; i++) {
      callimpl.setTargetEndpointAddress(tests[i]);
      assertEquals(tests[i], callimpl.getTargetEndpointAddress());
    }
  }
  
  public void testSetProperty() throws Exception {
  }
  
  public void testGetProperty() throws Exception {
  }
  
  public void testRemoveProperty() throws Exception {
  }
  
  public void testGetPropertyNames() throws Exception {
  }
  
  public void testInvoke() throws Exception {
  }
  
  public void testGetReturnObject() throws Exception {
  }
  
  public void testInvokeOneWay() throws Exception {
  }
  
  public void testGetOutputParams() throws Exception {
  }
  
  public void testGetOutputValues() throws Exception {
  }
  
  public void testSetGetService() throws Exception {
    ServiceImpl[] tests = {new ServiceImpl(), null};
    
    for (int i = 0; i < tests.length; i++) {
      callimpl.setService(tests[i]);
      assertEquals(tests[i], callimpl.getService());
    }
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(CallImplTest.class);
  }
}
