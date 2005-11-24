package org.apache.axis2.jaxws.client;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.client.ServiceFactoryImpl;

public class ServiceFactoryImplTest extends TestCase
{
  org.apache.axis2.jaxws.client.ServiceFactoryImpl servicefactoryimpl = null;
  
  public ServiceFactoryImplTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.client.ServiceFactoryImpl createInstance() throws Exception {
    return new org.apache.axis2.jaxws.client.ServiceFactoryImpl();
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    servicefactoryimpl = createInstance();
  }
  
  protected void tearDown() throws Exception {
    servicefactoryimpl = null;
    super.tearDown();
  }
  
  public void testCreateService() throws Exception {
  }
  
  public void testLoadService() throws Exception {
  }
  
  public void testGetGeneratedClassPackageName() throws Exception {
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(ServiceFactoryImplTest.class);
  }
}
