/**
 * 
 */
package org.apache.axis2.jaxws.misc;

import org.apache.axis2.jaxws.utility.JavaUtils;

import junit.framework.TestCase;

/**
 * Tests Namespace to Package Algorithmh
 *
 */
public class NS2PkgTest extends TestCase {

    public void test01() throws Exception {
        String ns1 = "http://example.org/NewBusiness/";
        String expectedPkg1 = "org.example.newbusiness";
        
        String pkg = JavaUtils.getPackageFromNamespace(ns1);
        assertTrue(expectedPkg1.equals(pkg));
    }
}
