package org.apache.axis2.jaxws.client;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.client.JAXRPCContextImpl;

public class JAXRPCContextImplTest extends TestCase
{
  org.apache.axis2.jaxws.client.JAXRPCContextImpl jaxrpccontextimpl = null;
  
  public JAXRPCContextImplTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.client.JAXRPCContextImpl createInstance() throws Exception {
    return new org.apache.axis2.jaxws.client.JAXRPCContextImpl(null);
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    jaxrpccontextimpl = createInstance();
  }
  
  protected void tearDown() throws Exception {
    jaxrpccontextimpl = null;
    super.tearDown();
  }
  
  public void testSetProperty() throws Exception {
  }
  
  public void testRemoveProperty() throws Exception {
  }
  
  public void testGetProperty() throws Exception {
  }
  
  public void testGetPropertyNames() throws Exception {
  }
  
  public void testGetAxis2Engine() throws Exception {
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(JAXRPCContextImplTest.class);
  }
}
