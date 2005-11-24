package org.apache.axis2.jaxws.client;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.client.DispatchImpl;
import javax.xml.namespace.QName;

public class DispatchImplTest extends TestCase
{
  org.apache.axis2.jaxws.client.DispatchImpl dispatchimpl = null;
  
  public DispatchImplTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.client.DispatchImpl createInstance() throws Exception {
	  String tgtEndptAddr = "TempValue";
	  QName opName = new QName("TempValue", "TempValue");
	 
	  return new org.apache.axis2.jaxws.client.DispatchImpl(tgtEndptAddr, opName);
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    dispatchimpl = createInstance();
  }
  
  protected void tearDown() throws Exception {
    dispatchimpl = null;
    super.tearDown();
  }
  
  public void testInvoke() throws Exception {
  }
  
  public void testInvokeAsync() throws Exception {
  }
  
  public void testInvokeOneWay() throws Exception {
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(DispatchImplTest.class);
  }
}
