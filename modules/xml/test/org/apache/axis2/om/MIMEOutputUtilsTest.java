/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.axis2.om;

import junit.framework.TestCase;
import org.apache.axis2.attachments.ByteArrayDataSource;
import org.apache.axis2.soap.SOAPFactory;
import org.apache.axis2.om.impl.MIMEOutputUtils;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimePartDataSource;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Thilina
 */
public class MIMEOutputUtilsTest extends TestCase {
    byte[] buffer;
    byte[] byteArray = new byte[]{13, 56, 65, 32, 12, 12, 7, -3, -2, -1,
                                  98};

    protected void setUp() throws Exception {
        super.setUp();
        SOAPFactory factory = OMAbstractFactory.getSOAP11Factory();
        ByteArrayOutputStream outStream;
        String boundary = "----TemporaryBoundary";

        String contentType = org.apache.axis2.om.impl.MIMEOutputUtils.getContentTypeForMime(boundary, "cid:0.632569289925808400@example.org");
        DataHandler dataHandler;
        dataHandler = new DataHandler(new ByteArrayDataSource(byteArray));
        OMText textData = factory.createText(dataHandler, true);

        DataHandler dataHandler2 = new DataHandler(
                "Apache Software Foundation", "text/plain");
        OMText text = factory.createText(dataHandler2, true);
        outStream = new ByteArrayOutputStream();
        outStream.write(("Content-Type: " + contentType).getBytes());
        outStream.write(new byte[]{13, 10});
        //outStream.write("\n\n".getBytes());
        MIMEOutputUtils.startWritingMime(outStream, boundary);
        MimeBodyPart part1 = MIMEOutputUtils.createMimeBodyPart(textData);
        MIMEOutputUtils.writeBodyPart(outStream, part1, boundary);
        MimeBodyPart part2 = MIMEOutputUtils.createMimeBodyPart(text);
        MIMEOutputUtils.writeBodyPart(outStream, part2, boundary);
        MIMEOutputUtils.finishWritingMime(outStream);
        buffer = outStream.toByteArray();
        System.out.println(new String(buffer));
        System.out.println("Axis2");
    }

    public void testMIMEWriting() throws IOException, MessagingException {
        ByteArrayInputStream inStream = new ByteArrayInputStream(buffer);
        Properties props = new Properties();
        javax.mail.Session session = javax.mail.Session
                .getInstance(props, null);
        MimeMessage mimeMessage = new MimeMessage(session, inStream);
        DataHandler dh = mimeMessage.getDataHandler();
        MimeMultipart multiPart = new MimeMultipart(
                (MimePartDataSource) dh
                .getDataSource());
        MimeBodyPart mimeBodyPart2 = (MimeBodyPart) multiPart.getBodyPart(0);
        Object object = mimeBodyPart2.getContent();
        MimeBodyPart mimeBodyPart1 = (MimeBodyPart) multiPart.getBodyPart(0);

    }

}