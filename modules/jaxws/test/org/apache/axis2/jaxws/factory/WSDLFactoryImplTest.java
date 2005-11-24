package org.apache.axis2.jaxws.factory;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.factory.WSDLFactoryImpl;

public class WSDLFactoryImplTest extends TestCase
{
  org.apache.axis2.jaxws.factory.WSDLFactoryImpl wsdlfactoryimpl = null;
  
  public WSDLFactoryImplTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.factory.WSDLFactoryImpl createInstance() throws Exception {
    return new org.apache.axis2.jaxws.factory.WSDLFactoryImpl();
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    wsdlfactoryimpl = createInstance();
  }
  
  protected void tearDown() throws Exception {
    wsdlfactoryimpl = null;
    super.tearDown();
  }
  
  public void testGetParser() throws Exception {
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(WSDLFactoryImplTest.class);
  }
}
