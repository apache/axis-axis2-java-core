package org.apache.axis2.jaxws.server;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.server.JAXRPCServlet;

public class JAXRPCServletTest extends TestCase
{
  org.apache.axis2.jaxws.server.JAXRPCServlet jaxrpcservlet = null;
  
  public JAXRPCServletTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.server.JAXRPCServlet createInstance() throws Exception {
    return new org.apache.axis2.jaxws.server.JAXRPCServlet();
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    jaxrpcservlet = createInstance();
  }
  
  protected void tearDown() throws Exception {
    jaxrpcservlet = null;
    super.tearDown();
  }
  
  public void testService() throws Exception {
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(JAXRPCServletTest.class);
  }
}
