package org.apache.axis2.jaxws.utils;

import junit.framework.TestCase;
import org.apache.axis2.jaxws.utils.ClassUtils;

public class ClassUtilsTest extends TestCase
{
  org.apache.axis2.jaxws.utils.ClassUtils classutils = null;
  
  public ClassUtilsTest(String name) {
    super(name);
  }
  
  public org.apache.axis2.jaxws.utils.ClassUtils createInstance() throws Exception {
    return new org.apache.axis2.jaxws.utils.ClassUtils();
  }
  
  protected void setUp() throws Exception {
    super.setUp();
    classutils = createInstance();
  }
  
  protected void tearDown() throws Exception {
    classutils = null;
    super.tearDown();
  }
  
  public void testSetGetDefaultClassLoader() throws Exception {
//    ClassLoader[] tests = {new ClassLoader(), null};
//    
//    for (int i = 0; i < tests.length; i++) {
//      classutils.setDefaultClassLoader(tests[i]);
//      assertEquals(tests[i], classutils.getDefaultClassLoader());
//    }
  }
  
  public void testSetClassLoader() throws Exception {
  }
  
  public void testGetClassLoader() throws Exception {
  }
  
  public void testRemoveClassLoader() throws Exception {
  }
  
  public void testForName() throws Exception {
  }
  
  public void testGetResourceAsStream() throws Exception {
  }
  
  public void testCreateClassLoader() throws Exception {
  }
    
  public static void main(String[] args) {
    junit.textui.TestRunner.run(ClassUtilsTest.class);
  }
}
