/*
 * Copyright 2004,2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.axis2.attachments;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.MimeBodyPart;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

public class PartOnMemory implements Part{

    MimeBodyPart part;
    public PartOnMemory(InputStream partInStream) throws MessagingException
    {
        part =  new MimeBodyPart(partInStream);
    }

    public int getSize() throws MessagingException {
        return part.getSize();
    }

    public String getContentType() throws MessagingException {
        return part.getContentType();
    }

    public String getFileName() throws MessagingException {
        return part.getFileName();
    }

    public InputStream getInputStream() throws IOException, MessagingException {
        return part.getInputStream();
    }

    public DataHandler getDataHandler() throws MessagingException {
        return part.getDataHandler();
    }

    public String getHeader(String arg0) throws MessagingException {
       return part.getHeader(arg0)[0];
    }

    public Enumeration getAllHeaders() throws MessagingException {
       return part.getAllHeaders();
    }


    public String getContentID() throws MessagingException {
        return part.getContentID();
    }
}
