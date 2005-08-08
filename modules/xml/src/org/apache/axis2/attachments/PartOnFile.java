/**
 * Copyright 2001-2004 The Apache Software Foundation.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * <p/>
 */
package org.apache.axis2.attachments;

import org.apache.axis2.om.OMException;

import javax.activation.DataHandler;
import javax.activation.FileDataSource;
import javax.mail.MessagingException;
import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;

public class PartOnFile implements Part {

    File cacheFile;

    Part bodyPart;

    String contentType;

    String contentID;

    HashMap headers;

    public PartOnFile(MIMEBodyPartInputStream inStream, String repoDir)
             {
        super();

        headers = new HashMap();

        if (repoDir == null) {
            repoDir = ".";
        }
        try {
            cacheFile = java.io.File.createTempFile("Axis2", ".att",
                    (repoDir == null) ? null : new File(repoDir));
        
        FileOutputStream fileOutStream = new FileOutputStream(cacheFile);
        int value;
        value = parseTheHeaders(inStream);
        
        if(value!=-1)
        {
        do {
            fileOutStream.write(value);
        }while ((value = inStream.read()) != -1);
        }
        fileOutStream.flush();
        fileOutStream.close();
        } catch (IOException e) {
            throw new OMException("Error creating temporary File."+e);
        }
    }
    
    private int parseTheHeaders(InputStream inStream) throws IOException
    {
        int value;
        boolean readingHeaders = true;
        StringBuffer header = new StringBuffer();
        while (readingHeaders & (value = inStream.read()) != -1) {
            if (value == 13) {
                if ((value = inStream.read()) == 10) {
                    if ((value = inStream.read()) == 13) {
                        if ((value = inStream.read()) == 10) {
                            putToMap(header);
                            readingHeaders = false;
                        }
                    } else {
                        putToMap(header);
                        header = new StringBuffer();
                        header.append((char) value);
                    }
                } else {
                    header.append(13);
                    header.append(value);
                }
            } else {
                header.append((char) value);
            }
        }
        return value;
    }

    private void putToMap(StringBuffer header) {
        String headerString = header.toString();
        int delimiter = headerString.indexOf(":");
        headers.put(headerString.substring(0, delimiter).trim(),
                headerString.substring(delimiter + 1, headerString.length())
                        .trim());
    }

    public String getContentID() {
        String cID = (String)headers.get("Content-ID");;
        if (cID==null)
        {
            cID= (String)headers.get("Content-Id");
            if (cID==null)
            {
                cID= (String)headers.get("Content-id");
                if (cID==null)
                {
                    cID= (String)headers.get("content-id");
                }
            }
            
        }
        return cID;
    }

    public int getSize() throws MessagingException {
        return (int)cacheFile.length();
    }

    public int getLineCount() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public String getDescription() throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public void setDescription(String arg0) throws MessagingException {
        throw new UnsupportedOperationException();
    }

    public String getFileName() throws MessagingException {
        return cacheFile.getAbsolutePath();
    }

    public InputStream getInputStream() throws IOException, MessagingException {
        FileInputStream inStream = new FileInputStream(cacheFile);
        return inStream;
    }

    public DataHandler getDataHandler() throws MessagingException {
        return new DataHandler(new FileDataSource(cacheFile));
    }

    public Object getContent() throws IOException, MessagingException {
        return getDataHandler().getContent();
    }

    public void writeTo(OutputStream outStream) throws IOException,
            MessagingException {
        getDataHandler().writeTo(outStream);
    }

    public String getHeader(String arg0) throws MessagingException {
        ArrayList selectedHeader = null;
        String header;
        header = (String) headers.get(arg0);
        return header;
    }

    public Enumeration getAllHeaders() throws MessagingException {
        return null;
    }

    public String getContentType() throws MessagingException {
        String cType= (String)headers.get("Content-Type");
        if (cType==null)
        {
            cType= (String)headers.get("Content-type");
            if (cType==null)
            {
                cType= (String)headers.get("content-type");
            }
        }
        return cType; 
    }

}