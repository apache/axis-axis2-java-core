package org.apache.axis2.jaxws.client;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.client.ServiceImpl;

public class ServiceImplTest extends TestCase
{
  org.apache.axis2.jaxws.client.ServiceImpl serviceimpl = null;
  
  public ServiceImplTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.client.ServiceImpl createInstance() throws Exception {
    return new org.apache.axis2.jaxws.client.ServiceImpl();
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    serviceimpl = createInstance();
  }
  
  protected void tearDown() throws Exception {
    serviceimpl = null;
    super.tearDown();
  }
  
  public void testCreateCall() throws Exception {
  }
  
  public void testCreateDispatch() throws Exception {
  }
  
  public void testCreatePort() throws Exception {
  }
  
  public void testGetCalls() throws Exception {
  }
  
  public void testGetHandlerRegistry() throws Exception {
  }
  
  public void testGetPort() throws Exception {
  }
  
  public void testGetPorts() throws Exception {
  }
  
  public void testGetSecurityConfiguration() throws Exception {
  }
  
  public void testGetServiceName() throws Exception {
  }
  
  public void testGetTypeMappingRegistry() throws Exception {
  }
  
  public void testGetWSDLDocumentLocation() throws Exception {
  }
  
  public void testSetIsJAXB_USAGE() throws Exception {
    boolean[] tests = {true, false};
    
    for (int i = 0; i < tests.length; i++) {
      serviceimpl.setJAXB_USAGE(tests[i]);
      assertEquals(tests[i], serviceimpl.isJAXB_USAGE());
    }
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(ServiceImplTest.class);
  }
}
