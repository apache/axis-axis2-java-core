/**
 * Copyright 2001-2004 The Apache Software Foundation. <p/>Licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at <p/>
 * http://www.apache.org/licenses/LICENSE-2.0 <p/>Unless required by applicable
 * law or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License. <p/>
 */
package org.apache.axis2.attachments;

import org.apache.axis2.om.OMException;
import org.apache.axis2.om.impl.MTOMConstants;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.internet.ContentType;
import javax.mail.internet.ParseException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.HashMap;

/**
 * @author <a href="mailto:thilina@opensource.lk"> Thilina Gunarathne </a>
 */
public class MIMEHelper {

    /**
     * <code>ContentType</code> of the MIME message
     */
    ContentType contentType;

    /**
     * Mime <code>boundary</code> which seperates mime parts
     */
    byte[] boundary;

    /**
     * <code>applicationType</code> used to distinguish between MTOM & SWA If
     * the message is MTOM optimised type is application/xop+xml If the message
     * is SWA, type is ??have to find out
     */
    String applicationType = null;

    /**
     * <code>pushbackInStream</code> stores the reference to the incoming
     * stream A PushbackStream has the ability to "push back" or "unread" one
     * byte.
     */
    PushbackInputStream pushbackInStream;

    /**
     * <code>mimeBodyPartsMap</code> stores the already parsed Mime Body
     * Parts. This Map will be keyed using the content-ID's
     */
    HashMap bodyPartsMap;

    /**
     * <code>partIndex</code>- Number of Mime parts parsed
     */
    int partIndex = 0;

    /**
     * <code>endOfStreamReached</code> flag which is to be set by
     * MIMEBodyPartStream when MIME message terminator is found.
     */
    boolean endOfStreamReached = false;

    String firstPartId = null;

    boolean fileCacheEnable = false;

    String attachmentRepoDir = null;

    int fileStorageThreshold;

    protected Log log = LogFactory.getLog(getClass());


    /**
     * @param inStream
     * @param contentTypeString
     * @param fileCacheEnable
     * @param attachmentRepoDir
     * @throws OMException
     * Will move the pointer to the begining of the first MIME part. Will
     *      read till first MIME boundary is found or end of stream reached.
     */
    public MIMEHelper(InputStream inStream, String contentTypeString,
                      boolean fileCacheEnable, String attachmentRepoDir,
                      String fileThreshold) throws OMException {
        this.attachmentRepoDir = attachmentRepoDir;
        this.fileCacheEnable = fileCacheEnable;
        if (fileThreshold != null && (!fileThreshold.equals(""))) {
            this.fileStorageThreshold = Integer.parseInt(fileThreshold);
        } else {
            this.fileStorageThreshold = 1;
        }
        bodyPartsMap = new HashMap();
        try {
            contentType = new ContentType(contentTypeString);
        } catch (ParseException e) {
            throw new OMException(
                    "Invalid Content Type Field in the Mime Message"
                            ,e);
        }
        // Boundary always have the prefix "--".
        this.boundary = ("--" + contentType.getParameter("boundary"))
                .getBytes();

        // do we need to wrap InputStream from a BufferedInputStream before
        // wrapping from PushbackStream
        pushbackInStream = new PushbackInputStream(inStream,
                (this.boundary.length + 2));

        // Move the read pointer to the begining of the first part
        // read till the end of first boundary
        while (true) {
            int value;
            try {
                value = pushbackInStream.read();
                if ((byte) value == boundary[0]) {
                    int boundaryIndex = 0;
                    while ((boundaryIndex < boundary.length)
                            && ((byte) value == boundary[boundaryIndex])) {
                        value = pushbackInStream.read();
                        if (value == -1)
                            throw new OMException(
                                    "Unexpected End of Stream while searching for first Mime Boundary");
                        boundaryIndex++;
                    }
                    if (boundaryIndex == boundary.length) { // boundary found
                        pushbackInStream.read();
                        break;
                    }
                } else if ((byte) value == -1) {
                    throw new OMException(
                            "Mime parts not found. Stream ended while searching for the boundary");
                }
            } catch (IOException e1) {
                throw new OMException("Stream Error" + e1.toString(), e1);
            }
        }
    }

    /**
     * @param inStream
     * @param contentTypeString
     * @throws OMException
     * Will set file cache to false
     */
    public MIMEHelper(InputStream inStream, String contentTypeString)
            throws OMException {
        this(inStream, contentTypeString, false, null, null);
    }

    /**
     * @return whether Message Type is SOAP with Attachments or MTOM optimised
     *         by checking the application type parameter in the Contant Type
     */
    public String getAttachmentSpecType() {
        if (this.applicationType == null) {
            applicationType = contentType.getParameter("type");
            if (applicationType.equalsIgnoreCase(MTOMConstants.MTOM_TYPE)) {
                this.applicationType = MTOMConstants.MTOM_TYPE;
            } else if (applicationType.equalsIgnoreCase(MTOMConstants.SWA_TYPE)) {
                this.applicationType = MTOMConstants.SWA_TYPE;
            } else {
                throw new OMException(
                        "Invalid Application type. Support available for MTOM/SOAP 1.2 & SwA/SOAP 1.l only.");
            }
        }
        return this.applicationType;
    }

    /**
     * @return the InputStream which includes the SOAP Envelope We assumes that
     *         the root mime part is always pointed by "start" parameter in
     *         content-type
     */
    public InputStream getSOAPPartInputStream() throws OMException {
        DataHandler dh;
        try {
            dh = getDataHandler(getSOAPPartContentID());
            if (dh == null) {
                throw new OMException(
                        "Mandatory Root MIME part containing the SOAP Envelope is missing");
            }
            return dh.getInputStream();
        } catch (IOException e) {
            throw new OMException(
                    "Problem with DataHandler of the Root Mime Part. ",e);
        }
    }

    /**
     * @return the Content-ID of the SOAP part It'll be the value Start
     *         Parameter of Content-Type header if given in the Content type of
     *         the MIME message. Else it'll be the content-id of the first MIME
     *         part of the MIME message
     */
    private String getSOAPPartContentID() {
        String rootContentID = contentType.getParameter("start");

        // to handle the Start parameter not mentioned situation
        if (rootContentID == null) {
            if (partIndex == 0) {
                getNextPart();
            }
            rootContentID = firstPartId;
        } else {
            rootContentID.trim();

            if ((rootContentID.indexOf("<") > -1)
                    & (rootContentID.indexOf(">") > -1))
                rootContentID = rootContentID.substring(1, (rootContentID
                        .length() - 1));
        }
        // Strips off the "cid" part from content-id
        if (rootContentID.substring(0, 3).equalsIgnoreCase("cid")) {
            rootContentID = rootContentID.substring(4);
        }
        return rootContentID;
    }

    public String getSOAPPartContentType() {
        Part soapPart = getPart(getSOAPPartContentID());
        try {
            return soapPart.getContentType();
        } catch (MessagingException e) {
            log.error(e.getMessage());
            throw new OMException(e);
        }
    }

    /**
     * @param blobContentID
     *            (without the surrounding angle brackets and "cid:" prefix)
     * @return The DataHandler of the mime part refered by the content-Id
     * @throws OMException
     */
    public DataHandler getDataHandler(String blobContentID) throws OMException {

        try {
            return getPart(blobContentID).getDataHandler();
        } catch (MessagingException e) {
            throw new OMException("Problem with Mime Body Part No " + partIndex
                    + ".  ", e);
        }

    }

    /**
     * @param blobContentID
     * @return The Part refered by the content-Id
     * @throws OMException
     * First checks whether the MIME part is already parsed by checking the
     * parts HashMap. If it is not parsed yet then call the getNextPart()
     * till we find the required part.
     */
    public Part getPart(String blobContentID) {
        Part bodyPart;
        boolean attachmentFound = false;
        if (bodyPartsMap.containsKey(blobContentID)) {
            bodyPart = (Part) bodyPartsMap.get(blobContentID);
            return bodyPart;
        } else {
            //This loop will be terminated by the Exceptions thrown if the Mime
            // part searching was not found
            while (true) {
                bodyPart = this.getNextPart();
                if (bodyPart == null) {
                    return null;
                }
                if (bodyPartsMap.containsKey(blobContentID)) {
                    bodyPart = (Part) bodyPartsMap.get(blobContentID);
                    return bodyPart;
                }
            }
        }
    }

    protected void setEndOfStream(boolean value) {
        this.endOfStreamReached = value;

    }

    /**
     * @return the Next valid MIME part + store the Part in the Parts List
     * @throws OMException
     *             throw if cotent id is null or if two MIME parts contain the
     *             same content-ID & the exceptions throws by getPart()
     */
    private Part getNextPart() throws OMException {
        Part nextPart;
        nextPart = getPart();
        if (nextPart != null) {
            String partContentID;
            try {
                partContentID = nextPart.getContentID();

                if (partContentID == null & partIndex == 1) {
                    bodyPartsMap.put("firstPart", nextPart);
                    firstPartId = "firstPart";
                    return nextPart;
                }
                if (partContentID == null) {
                    throw new OMException(
                            "Part content ID cannot be blank for non root MIME parts");
                }
                if ((partContentID.indexOf("<") > -1)
                        & (partContentID.indexOf(">") > -1)) {
                    partContentID = partContentID.substring(1, (partContentID
                            .length() - 1));

                } else if (partIndex == 1) {
                    firstPartId = partContentID;
                }
                if (bodyPartsMap.containsKey(partContentID)) {
                    throw new OMException(
                            "Two MIME parts with the same Content-ID not allowed.");
                }
                bodyPartsMap.put(partContentID, nextPart);
                return nextPart;
            } catch (MessagingException e) {
                throw new OMException("Error reading Content-ID from the Part."
                        + e);
            }
        } else
            return null;
    }

    /**
     * @return This will return the next available MIME part in the stream.
     * @throws OMException
     *             if Stream ends while reading the next part...
     */
    private Part getPart() throws OMException {
        // endOfStreamReached will be set to true if the message ended in MIME
        // Style having "--" suffix with the last mime boundary
        if (endOfStreamReached)
            throw new OMException(
                    "Referenced MIME part not found.End of Stream reached.");

        Part part = null;

        try {
            if (fileCacheEnable) {
                try {
                    MIMEBodyPartInputStream partStream;
                    byte[] buffer = new byte[fileStorageThreshold];
                    partStream = new MIMEBodyPartInputStream(pushbackInStream,
                            boundary, this);
                    int count = 0;
                    int value;
                    // Make sure not to modify this to a Short Circuit "&". If
                    // removed a byte will be lost
                    while (count != fileStorageThreshold
                            && (!partStream.getBoundaryStatus())) {
                        value = partStream.read();
                        buffer[count] = (byte) value;
                        count++;
                    }
                    if (count == fileStorageThreshold) {
                        PushbackFilePartInputStream filePartStream = new PushbackFilePartInputStream(
                                partStream, buffer);
                        part = new PartOnFile(filePartStream, attachmentRepoDir);
                    } else {
                        ByteArrayInputStream byteArrayInStream = new ByteArrayInputStream(
                                buffer,0,count-1);
                        part = new PartOnMemory(byteArrayInStream);
                    }
                } catch (Exception e) {
                    throw new OMException("Error creating temporary File.", e);
                }
            } else {
                MIMEBodyPartInputStream partStream;
                partStream = new MIMEBodyPartInputStream(pushbackInStream,
                        boundary, this);
                part = new PartOnMemory(partStream);
            }
            // This will take care if stream ended without having MIME
            // message terminator
            if (part.getSize() <= 0) {
                throw new OMException(
                        "Referenced MIME part not found.End of Stream reached.");
            }
        } catch (MessagingException e) {
            throw new OMException(e);
        }
        partIndex++;
        return part;
    }
}