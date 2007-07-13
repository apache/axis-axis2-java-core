/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.axis2.jaxws.message.attachments;

import org.apache.axis2.jaxws.ExceptionFactory;
import org.apache.axis2.jaxws.message.Message;
import org.apache.axis2.jaxws.utility.JavaUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.xml.bind.attachment.AttachmentUnmarshaller;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * JAXBAttachmentUnmarshaller
 * <p/>
 * An implementation of the <link>javax.xml.bind.attachment.AttachmentUnmarshaller</link> that is
 * used for deserializing XOP elements into their corresponding binary data packages.
 */
public class JAXBAttachmentUnmarshaller extends AttachmentUnmarshaller {

    private static final Log log = LogFactory.getLog(JAXBAttachmentUnmarshaller.class);

    private Message message;

    public JAXBAttachmentUnmarshaller(Message message) {
        this.message = message;
    }

    @Override
    public boolean isXOPPackage() {
        
        // Any message that is received might contain MTOM.
        // So always return true.
        boolean value = true;
    
        if (log.isDebugEnabled()){ 
            log.debug("isXOPPackage returns " + value);
        }
        return value;
    }

    @Override
    public byte[] getAttachmentAsByteArray(String cid) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve attachment [" + cid + "] as a byte[]");
        }
        DataHandler dh = getAttachmentAsDataHandler(cid);
        if (dh != null) {
            try {
                return convert(dh);
            } catch (IOException ioe) {
                if (log.isDebugEnabled()) {
                    log.debug("Exception occurred while getting the byte[] " + ioe);
                }
                throw ExceptionFactory.makeWebServiceException(ioe);
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("returning null byte[]");
        }
        return null;
    }

    @Override
    public DataHandler getAttachmentAsDataHandler(String cid) {
        if (log.isDebugEnabled()) {
            log.debug("Attempting to retrieve attachment [" + cid + "] as a DataHandler");
        }

        DataHandler dh = message.getDataHandler(cid);
        if (dh != null) {
            return dh;
        } else {
            String cid2 = getNewCID(cid);
            if (log.isDebugEnabled()) {
                log.debug("A dataHandler was not found for [" + cid + "] trying [" + cid2 + "]");
            }
            dh = message.getDataHandler(cid2);
            if (dh != null) {
                return dh;
            }
        }
        // No Data Handler found
        throw ExceptionFactory.makeWebServiceException("A data handler was not found " +
                        "for content id " + cid);
    }
    
    /**
     * @param cid
     * @return cid with translated characters
     */
    private String getNewCID(String cid) {
        // TODO This method only converts : and /
        // A more complete fix is needed.
        String cid2 = cid;
        cid2 = JavaUtils.replace(cid2, "%3A", ":");
        cid2 = JavaUtils.replace(cid2, "%2F", "/");
        return cid2;
    }

    /**
     * Read the bytes from the DataHandler
     *
     * @param dh
     * @return byte[]
     * @throws IOException
     */
    private byte[] convert(DataHandler dh) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug("Reading byte[] from DataHandler " + dh);
        }
        InputStream is = dh.getInputStream();
        if (log.isDebugEnabled()) {
            log.debug("DataHandler InputStream " + is);
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        byte[] b = new byte[1024];
        int num = is.read(b);
        if (log.isDebugEnabled()) {
            if (num <= 0) {
                log.debug("DataHandler InputStream contains no data. num=" + num);
            }
        }
        while (num > 0) {
            baos.write(b, 0, num);
            num = is.read(b);
        }
        return baos.toByteArray();
    }
}
