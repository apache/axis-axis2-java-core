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

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.HashMap;

import javax.activation.DataHandler;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.ParseException;

import org.apache.axis2.om.OMException;

/**
 * @author <a href="mailto:thilina@opensource.lk"> Thilina Gunarathne </a>
 */
public class MIMEHelper {
    /**
     * if the Message is MTOM optimised then <code>MTOM_TYPE</code>
     */
    public static final String MTOM_TYPE = "application/xop+xml";
    
    /**
     * If the message is Soap with Attachments <code>SwA_TYPE</code>
     */
    public static final String SWA_TYPE = "text/xml";
    
    /**
     * <code>rootPart</code> is used as the key for the root BodyPart in the
     * Parts HashMap
     */
    public static final String ROOT_PART = "SoapPart";
    
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
    
    boolean endOfStreamReached = false;
    
    String firstPartId = null;
    
    boolean fileCacheEnable = false;
    
    String attachmentRepoDir = null;
    
    public MIMEHelper(InputStream inStream, String contentTypeString,
            boolean fileCacheEnable, String attachmentRepoDir)
    throws OMException {
        this.attachmentRepoDir = attachmentRepoDir;
        this.fileCacheEnable = fileCacheEnable;
        bodyPartsMap = new HashMap();
        try {
            contentType = new ContentType(contentTypeString);
        } catch (ParseException e) {
            throw new OMException(
                    "Invalid Content Type Field in the Mime Message"
                    + e.toString());
        }
        // Boundary always have the prefix "--".
        this.boundary = ("--" + contentType.getParameter("boundary"))
        .getBytes();
        
        //TODO do we need to wrap InputStream from a BufferedInputStream before
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
                throw new OMException("Stream Error" + e1.toString());
            }
        }
    }
    
    public MIMEHelper(InputStream inStream, String contentTypeString)
    throws OMException {
        this(inStream, contentTypeString, false, null);
    }
    
    /**
     * @return whether Message Type is SOAP with Attachments or MTOM optimised
     *         by checking the application type parameter in the Contant Type
     */
    public String getAttachmentSpecType() {
        if (this.applicationType == null) {
            applicationType = contentType.getParameter("type");
            if (applicationType.equalsIgnoreCase(MTOM_TYPE)) {
                this.applicationType = MTOM_TYPE;
            } else if (applicationType.equalsIgnoreCase(SWA_TYPE)) {
                this.applicationType = SWA_TYPE;
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
        String rootContentID = contentType.getParameter("start");
        
        // to handle the Start parameter not mentioned situation
        if (rootContentID == null) {
            if (partIndex == 0) {
                getNextPart();
            }
            rootContentID = firstPartId;
        } else {
            rootContentID.trim();
            // Keep it here till we make sure the two angle brackets are
            // required or not
            if ((rootContentID.indexOf("<") > -1)
                    & (rootContentID.indexOf(">") > -1))
                rootContentID = rootContentID.substring(1, (rootContentID
                        .length() - 1));
        }
        // Strips off the "cid" part from content-id
        if (rootContentID.substring(0, 3).equalsIgnoreCase("cid")) {
            rootContentID = rootContentID.substring(4);
        }
        DataHandler dh;
        try {
            dh = getDataHandler(rootContentID);
            if (dh == null) {
                throw new OMException(
                "Mandatory Root MIME part containing the SOAP Envelope is missing");
            }
            return dh.getInputStream();
        } catch (IOException e) {
            throw new OMException(
                    "Problem with DataHandler of the Root Mime Part. " + e);
        }
    }
    
    /**
     * @param blobContentID
     * @return The DataHandler of the mime part refered by the content-Id
     * @throws OMException
     *             First checks whether the MIME part is already parsed by
     *             checking the parts HashMap. If it is not parsed yet then call
     *             the getNextPart() till we find the required part.
     */
    public DataHandler getDataHandler(String blobContentID) throws OMException {
        
        Part bodyPart;
        //	blobContentID = "<" + blobContentID + ">";
        boolean attachmentFound = false;
        
        //TODO make sure to add surrounding angle brackets to the content ID
        if (bodyPartsMap.containsKey(blobContentID)) {
            bodyPart = (Part) bodyPartsMap.get(blobContentID);
            attachmentFound = true;
            DataHandler dh;
            try {
                dh = bodyPart.getDataHandler();
            } catch (MessagingException e) {
                throw new OMException("Problem with Mime Body Part No "
                        + partIndex + ".  " + e);
            }
            return dh;
        } else {
            try {
                while (true) {
                    bodyPart = this.getNextPart();
                    if (bodyPart == null) {
                        return null;
                    }
                    if (bodyPartsMap.containsKey(blobContentID)) {
                        bodyPart = (Part) bodyPartsMap.get(blobContentID);
                        DataHandler dh = bodyPart.getDataHandler();
                        return dh;
                    }
                }
            } catch (MessagingException e) {
                throw new OMException("Invalid Mime Message " + e);
            }
        }
        
    }
    
    protected void setEndOfStream(boolean value)
    {
        this.endOfStreamReached = value;
        
    }
    
    /**
     * @return The next MIME Body part in the stream Uses the MimeBodyPartStream
     *         to obtain streams delimited by boundaries.
     * @throws OMException
     */
    private MimeBodyPart getMimeBodyPart() throws OMException {
        // endOfStreamReached will be set to true if the message ended in MIME Style
        // having "--" suffix with the last mime boundary
        if (endOfStreamReached)
            throw new OMException(
            "Referenced MIME part not found.End of Stream reached.");
        
        MimeBodyPart mimeBodyPart = null;
        MimeBodyPartInputStream partStream;
        partStream = new MimeBodyPartInputStream(pushbackInStream, boundary,
                this);
        try {
            mimeBodyPart = new MimeBodyPart(partStream);
            
            // This will take care if stream ended without having MIME message terminator
            if (mimeBodyPart.getSize() <= 0) {
                throw new OMException(
                "Referenced MIME part not found.End of Stream reached.");
            }
        } catch (MessagingException e) {
            throw new OMException("Problem reading Mime Part No "
                    + (partIndex + 1) + ". " + e);
        }
        partIndex++;
        return mimeBodyPart;
    }
    
    /**
     * @return
     * @throws OMException
     */
    private MimeBodyPart getRootMimeBodyPart() throws OMException {
        MimeBodyPart rootMimeBodyPart;
        if (bodyPartsMap.isEmpty()) {
            rootMimeBodyPart = getMimeBodyPart();
            bodyPartsMap.put(ROOT_PART, rootMimeBodyPart);
        } else {
            rootMimeBodyPart = (MimeBodyPart) bodyPartsMap.get(ROOT_PART);
        }
        return rootMimeBodyPart;
    }
    
    private Part getNextPart() throws OMException {
        MimeBodyPart nextMimeBodyPart;
        nextMimeBodyPart = getMimeBodyPart();
        if (nextMimeBodyPart != null) {
            String partContentID;
            try {
                partContentID = nextMimeBodyPart.getContentID();
                
                if (partContentID == null & partIndex == 1) {
                    bodyPartsMap.put("firstPart", nextMimeBodyPart);
                    firstPartId = "firstPart";
                    return nextMimeBodyPart;
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
                if (bodyPartsMap.containsKey(partContentID))
                {
                    throw new OMException("Two MIME parts with the same Content-ID not allowed.");
                }
       /*
        * Temporary File storage support is deffered till 1.0
        */         
                //                if (fileCacheEnable) {
                //                    PartOnFile part = new PartOnFile(nextMimeBodyPart,
                //                            partContentID,
                //                            attachmentRepoDir);
                //                    return part;
                //                } else {
                bodyPartsMap.put(partContentID, nextMimeBodyPart);
                return nextMimeBodyPart;
                //                }
            } catch (MessagingException e) {
                throw new OMException(
                        "Error Reading Content-ID from Mime Part No "
                        + partIndex + ". " + e);
            }
            //            } catch (Exception e) {
            //                throw new OMException("Error Creating File Storage Part"
            //                        + partIndex + ". " + e);
            //            }
        } else
            return null;
    }
    
}