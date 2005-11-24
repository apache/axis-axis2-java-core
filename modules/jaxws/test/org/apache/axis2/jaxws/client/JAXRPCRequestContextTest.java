package org.apache.axis2.jaxws.client;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.client.JAXRPCRequestContext;

public class JAXRPCRequestContextTest extends TestCase
{
  org.apache.axis2.jaxws.client.JAXRPCRequestContext jaxrpcrequestcontext = null;
  
  public JAXRPCRequestContextTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.client.JAXRPCRequestContext createInstance() throws Exception {
    return new org.apache.axis2.jaxws.client.JAXRPCRequestContext(null);
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    jaxrpcrequestcontext = createInstance();
  }
  
  protected void tearDown() throws Exception {
    jaxrpcrequestcontext = null;
    super.tearDown();
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(JAXRPCRequestContextTest.class);
  }
}
