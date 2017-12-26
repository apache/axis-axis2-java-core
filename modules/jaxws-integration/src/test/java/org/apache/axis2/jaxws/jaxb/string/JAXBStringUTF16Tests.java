package org.apache.axis2.jaxws.jaxb.string;

import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.Assert;
import org.junit.ClassRule;
import org.junit.Test;

import javax.xml.ws.BindingProvider;

public class JAXBStringUTF16Tests {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo");

    private void runTest16(String value) throws Exception {
        runTestWithUTF16(value, value);
    }

    private void runTest16(String value, String value1) throws Exception {
        runTestWithUTF16(value, value1);
    }
    
    @Test
    public void testSimpleString16BOM() throws Exception {
        // Call the Axis2 StringMessageProvider which has a check to ensure
        // that the BOM for UTF-16 is not written inside the message.
        runTestWithEncoding("a simple string", "a simple string", "UTF-16",
                server.getEndpoint("StringMessageProviderService.StringMessageProviderPort"));
    }

    @Test
    public void testSimpleString16() throws Exception {
        runTest16("a simple string");
    }

    @Test
    public void testStringWithApostrophes16() throws Exception {
        runTest16("this isn't a simple string");
    }

    @Test
    public void testStringWithEntities16() throws Exception {
        runTest16("&amp;&lt;&gt;&apos;&quot;", "&amp;&lt;&gt;&apos;&quot;");
    }

    @Test
    public void testStringWithRawEntities16() throws Exception {
        runTest16("&<>'\"", "&<>'\"");
    }

    @Test
    public void testStringWithLeadingAndTrailingSpaces16() throws Exception {
        runTest16("          centered          ");
    }

    @Test
    public void testWhitespace16() throws Exception {
        runTest16(" \n \t "); // note: \r fails
    }

    @Test
    public void testFrenchAccents16() throws Exception {
        runTest16("\u00e0\u00e2\u00e4\u00e7\u00e8\u00e9\u00ea\u00eb\u00ee\u00ef\u00f4\u00f6\u00f9\u00fb\u00fc");
    }

    @Test
    public void testGermanUmlauts16() throws Exception {
        runTest16(" Some text \u00df with \u00fc special \u00f6 chars \u00e4.");
    }

    @Test
    public void testWelcomeUnicode1_16() throws Exception {
        // welcome in several languages
        runTest16(
                "Chinese (trad.) : \u6b61\u8fce  ");
    }

    @Test
    public void testWelcomeUnicode2_16() throws Exception {
        // welcome in several languages
        runTest16(
                "Greek : \u03ba\u03b1\u03bb\u03ce\u03c2 \u03bf\u03c1\u03af\u03c3\u03b1\u03c4\u03b5");
    }

    @Test
    public void testWelcomeUnicode3_16() throws Exception {
        // welcome in several languages
        runTest16(
                "Japanese : \u3088\u3046\u3053\u305d");
    }

    private void runTestWithUTF16(String input, String output) throws Exception {
        runTestWithEncoding(input, output, "UTF-16");
    }
    private void runTestWithEncoding(String input, String output, String encoding) throws Exception {
        runTestWithEncoding(input, output, encoding, server.getEndpoint("JAXBStringService.JAXBStringPortTypeImplPort"));
    }
    private void runTestWithEncoding(String input, String output, String encoding, String endpoint) {
        JAXBStringPortType myPort = (new JAXBStringService()).getJAXBStringPort();
        BindingProvider p = (BindingProvider) myPort;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, endpoint);

        if (encoding != null) {
            p.getRequestContext().put(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING, encoding);
        }

        Echo request = new Echo();
        request.setArg(input);
        EchoResponse response = myPort.echoString(request);
        TestLogger.logger.debug(response.getResponse());
        Assert.assertEquals(output, response.getResponse());
    }
}

