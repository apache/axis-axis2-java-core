package org.apache.axis2.jaxws.description;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.ParameterDesc;

public class ParameterDescTest extends TestCase
{
  org.apache.axis2.jaxws.description.ParameterDesc parameterdesc = null;
  
  public ParameterDescTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.description.ParameterDesc createInstance() throws Exception {
    return new org.apache.axis2.jaxws.description.ParameterDesc();
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    parameterdesc = createInstance();
  }
  
  protected void tearDown() throws Exception {
    parameterdesc = null;
    super.tearDown();
  }
  
  public void testSetGetJavaType() throws Exception {
//    java.lang.Class[] tests = {new java.lang.Class(), null};
//    
//    for (int i = 0; i < tests.length; i++) {
//      parameterdesc.setJavaType(tests[i]);
//      assertEquals(tests[i], parameterdesc.getJavaType());
//    }
  }
  
  public void testSetGetMode() throws Exception {
//    ParameterMode[] tests = {new ParameterMode(), null};
//    
//    for (int i = 0; i < tests.length; i++) {
//      parameterdesc.setMode(tests[i]);
//      assertEquals(tests[i], parameterdesc.getMode());
//    }
  }
  
  public void testSetGetXmlType() throws Exception {
//    QName[] tests = {new QName(), null};
//    
//    for (int i = 0; i < tests.length; i++) {
//      parameterdesc.setXmlType(tests[i]);
//      assertEquals(tests[i], parameterdesc.getXmlType());
//    }
  }
  
  public void testSetGetName() throws Exception {
//    QName[] tests = {new QName(), null};
//    
//    for (int i = 0; i < tests.length; i++) {
//      parameterdesc.setName(tests[i]);
//      assertEquals(tests[i], parameterdesc.getName());
//    }
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(ParameterDescTest.class);
  }
}
