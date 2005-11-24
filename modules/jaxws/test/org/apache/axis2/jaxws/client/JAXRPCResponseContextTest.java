package org.apache.axis2.jaxws.client;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.client.JAXRPCResponseContext;

public class JAXRPCResponseContextTest extends TestCase
{
  org.apache.axis2.jaxws.client.JAXRPCResponseContext jaxrpcresponsecontext = null;
  
  public JAXRPCResponseContextTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.client.JAXRPCResponseContext createInstance() throws Exception {
    return new org.apache.axis2.jaxws.client.JAXRPCResponseContext(null);
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    jaxrpcresponsecontext = createInstance();
  }
  
  protected void tearDown() throws Exception {
    jaxrpcresponsecontext = null;
    super.tearDown();
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(JAXRPCResponseContextTest.class);
  }
}
