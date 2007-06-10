package org.tempuri.complex;

import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.apache.ws.java2wsdl.Java2WSDLBuilder;
import org.tempuri.BaseDataTypes;

import java.io.ByteArrayOutputStream;
import java.io.FileReader;
import java.io.StringReader;

public class ComplexDataTypesTest extends XMLTestCase {

    private String wsdlLocation = System.getProperty("basedir", ".") + "/" + "test-resources/ComplexDataTypes/ComplexDataTypes.wsdl";

    public void test1() throws Exception {
        XMLUnit.setIgnoreWhitespace(true);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Java2WSDLBuilder builder = new Java2WSDLBuilder(out, ComplexDataTypes.class.getName(), ComplexDataTypes.class.getClassLoader());
            builder.generateWSDL();
            FileReader control = new FileReader(wsdlLocation);
            StringReader test = new StringReader(new String(out.toByteArray()));
            assertXMLEqual(control, test);
        } finally {
            XMLUnit.setIgnoreWhitespace(false);
        }
    }
}
