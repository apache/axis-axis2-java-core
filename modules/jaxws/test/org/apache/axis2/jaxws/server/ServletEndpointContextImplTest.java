package org.apache.axis2.jaxws.server;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.server.ServletEndpointContextImpl;

public class ServletEndpointContextImplTest extends TestCase
{
  org.apache.axis2.jaxws.server.ServletEndpointContextImpl servletendpointcontextimpl = null;
  
  public ServletEndpointContextImplTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.server.ServletEndpointContextImpl createInstance() throws Exception {
    return new org.apache.axis2.jaxws.server.ServletEndpointContextImpl(null, null, null);
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    servletendpointcontextimpl = createInstance();
  }
  
  protected void tearDown() throws Exception {
    servletendpointcontextimpl = null;
    super.tearDown();
  }
  
  public void testGetMessageContext() throws Exception {
  }
  
  public void testGetUserPrincipal() throws Exception {
  }
  
  public void testGetHttpSession() throws Exception {
  }
  
  public void testGetServletContext() throws Exception {
  }
  
  public void testIsUserInRole() throws Exception {
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(ServletEndpointContextImplTest.class);
  }
}
