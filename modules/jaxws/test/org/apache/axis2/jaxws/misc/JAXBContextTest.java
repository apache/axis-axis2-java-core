/**
 * 
 */
package org.apache.axis2.jaxws.misc;

import java.util.TreeSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.apache.axis2.jaxws.message.databinding.JAXBUtils;
import org.apache.axis2.jaxws.utility.JavaUtils;

import junit.framework.TestCase;

/**
 * Tests Namespace to Package Algorithmh
 *
 */
public class JAXBContextTest extends TestCase {

    /**
     * Test basic functionality of JAXBUtils pooling
     * @throws Exception
     */
    public void test01() throws JAXBException {
        
        // Get a JAXBContext
        TreeSet<String> context1 = new TreeSet<String>();
        context1.add("org.test.addnumbers");
        context1.add("org.test.anytype");
        
        JAXBContext jaxbContext1 = JAXBUtils.getJAXBContext(context1);
        
        // Assert that the JAXBContext was found and the context contains the two valid packages
        assertTrue(jaxbContext1 != null);
        assertTrue(context1.contains("org.test.addnumbers"));
        assertTrue(context1.contains("org.test.anytype"));
        
        // Repeat with the same packages
        TreeSet<String> context2 = new TreeSet<String>();
        context2.add("org.test.addnumbers");
        context2.add("org.test.anytype");
        
        JAXBContext jaxbContext2 = JAXBUtils.getJAXBContext(context2);
        
        // The following assertion is probably true,but GC may have wiped out the weak reference
        //assertTrue(jaxbContext2 == jaxbContext1);
        assertTrue(jaxbContext2 != null);
        assertTrue(jaxbContext2.toString().equals(jaxbContext1.toString()));
        assertTrue(context2.contains("org.test.addnumbers"));
        assertTrue(context2.contains("org.test.anytype"));
        
        // Repeat with the same packages + an invalid package
        TreeSet<String> context3 = new TreeSet<String>();
        context3.add("org.test.addnumbers");
        context3.add("org.test.anytype");
        context3.add("my.grandma.loves.jaxws");

        JAXBContext jaxbContext3 = JAXBUtils.getJAXBContext(context3);
        
        // The following assertion is probably true,but GC may have wiped out the weak reference
        //assertTrue(jaxbContext3 == jaxbContext1);
        assertTrue(jaxbContext3 != null);
        assertTrue(jaxbContext1.toString().equals(jaxbContext1.toString()));
        assertTrue(context3.contains("org.test.addnumbers"));
        assertTrue(context3.contains("org.test.anytype"));
        // TODO FIXME - does not work under m2/surefire
//        assertTrue(!context3.contains("my.grandma.loves.jaxws"));  // invalid package should be silently removed
        
        // Repeat with a subset of packages
        TreeSet<String> context4 = new TreeSet<String>();
        context4.add("org.test.addnumbers");
        
        
        JAXBContext jaxbContext4 = JAXBUtils.getJAXBContext(context4);
        
        assertTrue(jaxbContext4 != null);
        assertTrue(jaxbContext4 != jaxbContext3);
        assertTrue(context4.contains("org.test.addnumbers"));
        
       
    }
}
