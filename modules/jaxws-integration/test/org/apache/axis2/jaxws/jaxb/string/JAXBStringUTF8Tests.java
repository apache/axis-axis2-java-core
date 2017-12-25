package org.apache.axis2.jaxws.jaxb.string;

import org.junit.ClassRule;
import org.junit.Test;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.testutils.Axis2Server;

import static org.junit.Assert.assertEquals;

import javax.xml.ws.BindingProvider;

public class JAXBStringUTF8Tests {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo");

    private void runTest(String value) throws Exception{
        runTestWithUTF8(value, value);
    }

    private void runTest(String value, String value1) throws Exception {
        runTestWithUTF8(value, value1);
    }

    @Test
    public void testSimpleString() throws Exception {
        runTest("a simple string");
    }

    @Test
    public void testSimpleStringSwitchEncoding() throws Exception {
        String input = "a simple string";
        String output = "a simple string";
        
        // Run with different encodings to verify proper processing.
        runTestWithEncoding(input, output, null);  // no encoding means to use default, UTF-8
        runTestWithEncoding(input, output, "UTF-16");  // Make a call with UTF-16
        runTestWithEncoding(input, output, null);  // now try again...using default, UTF-8
    }
    
    @Test
    public void testStringWithApostrophes() throws Exception {
        runTest("this isn't a simple string");
    }

    @Test
    public void testStringWithEntities() throws Exception {
        runTest("&amp;&lt;&gt;&apos;&quot;", "&amp;&lt;&gt;&apos;&quot;");
    }

    @Test
    public void testStringWithRawEntities() throws Exception {
        runTest("&<>'\"", "&<>'\"");
    }

    @Test
    public void testStringWithLeadingAndTrailingSpaces() throws Exception {
        runTest("          centered          ");
    }

    @Test
    public void testWhitespace() throws Exception {
        runTest(" \n \t "); // note: \r fails
    }

    @Test
    public void testFrenchAccents() throws Exception {
        runTest("\u00e0\u00e2\u00e4\u00e7\u00e8\u00e9\u00ea\u00eb\u00ee\u00ef\u00f4\u00f6\u00f9\u00fb\u00fc");
    }

    @Test
    public void testGermanUmlauts() throws Exception {
        runTest(" Some text \u00df with \u00fc special \u00f6 chars \u00e4.");
    }

    @Test
    public void testWelcomeUnicode1() throws Exception {
        // welcome in several languages
        runTest(
                "Chinese (trad.) : \u6b61\u8fce  ");
    }

    @Test
    public void testWelcomeUnicode2() throws Exception {
        // welcome in several languages
        runTest(
                "Greek : \u03ba\u03b1\u03bb\u03ce\u03c2 \u03bf\u03c1\u03af\u03c3\u03b1\u03c4\u03b5");
    }

    @Test
    public void testWelcomeUnicode3() throws Exception {
        // welcome in several languages
        runTest(
                "Japanese : \u3088\u3046\u3053\u305d");
    }

    private void runTestWithUTF8(String input, String output) throws Exception {
        runTestWithEncoding(input, output, null);  // no encoding means to use default, UTF-8
    }

    private void runTestWithEncoding(String input, String output, String encoding) throws Exception {
        JAXBStringPortType myPort = (new JAXBStringService()).getJAXBStringPort();
        BindingProvider p = (BindingProvider) myPort;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                server.getEndpoint("JAXBStringService.JAXBStringPortTypeImplPort"));

        if (encoding != null) {
            p.getRequestContext().put(org.apache.axis2.Constants.Configuration.CHARACTER_SET_ENCODING, encoding);
        }

        Echo request = new Echo();
        request.setArg(input);
        EchoResponse response = myPort.echoString(request);
        TestLogger.logger.debug(response.getResponse());
        assertEquals(output, response.getResponse());
    }
   
}
