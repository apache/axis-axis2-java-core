package org.apache.axis2.kernel;

import junit.framework.TestCase;
import org.apache.axis2.context.MessageContext;

import java.util.UUID;

public class TransportUtilsTest extends TestCase {

    private static final String ACTION_INSIDE_STARTINFO = "multipart/related; type=\"application/xop+xml\"; boundary=\"%s\"; start=\"<root.message@cxf.apache.org>\"; start-info=\"application/soap+xml; action=\\\"%s\\\"\"";
    private static final String ACTION_OUTSIDE_STARTINFO = "Multipart/Related;boundary=MIME_boundary;type=\"application/xop+xml\";start=\"<mymessage.xml@example.org>\";start-info=\"application/soap+xml\"; action=\"%s\"";

    public void testProcessContentTypeForAction() {

        String soapAction = "http://www.example.com/ProcessData";
        String contentType;
        MessageContext msgContext = new MessageContext();

        contentType = String.format(ACTION_INSIDE_STARTINFO, UUID.randomUUID().toString(), soapAction);
        TransportUtils.processContentTypeForAction(contentType, msgContext);
        assertEquals(soapAction, msgContext.getSoapAction());

        contentType = String.format(ACTION_OUTSIDE_STARTINFO, soapAction);
        TransportUtils.processContentTypeForAction(contentType, msgContext);
        assertEquals(soapAction, msgContext.getSoapAction());
    }
}