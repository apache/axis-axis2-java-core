package org.apache.axis2.description;

import junit.framework.TestCase;

import java.io.File;
import java.io.FileInputStream;
import java.io.StringWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.StringReader;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;

/**
 *
 */
public class WSDL11ToAxisServiceBuilderTest extends XMLTestCase {

    public void testVersion() {
        File testResourceFile = new File("test-resources/wsdl/Version.wsdl");
        System.out.println("testResourceFile: " + testResourceFile);
        try {
            WSDL11ToAllAxisServicesBuilder builder = new WSDL11ToAllAxisServicesBuilder(
                    new FileInputStream(testResourceFile));
            AxisService axisService = builder.populateService();
            System.out.println("WSDL file: " + testResourceFile.getName());
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            axisService.printWSDL(baos);
            XMLUnit.setIgnoreWhitespace(true);
            //TODO: FIXME
            //this.assertXMLEqual(new FileReader(testResourceFile), new StringReader(new String(baos.toByteArray())));
            XMLUnit.setIgnoreWhitespace(false);
        } catch (Exception e) {
            System.out.println("Error in WSDL : " + testResourceFile.getName());
            System.out.println("Exception: " + e.toString());
            e.printStackTrace();
            fail("Caught exception " + e.toString());
        }
        return;
    }

}
