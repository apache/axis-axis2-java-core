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
package org.apache.axis2.om.impl;

import org.apache.axis2.om.OMText;

import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.LinkedList;
import java.util.Random;

/**
 * For the moment this assumes that transport takes the decision of whether
 * to optimise or not by looking at whether the MTOM optimise is enabled &
 * also looking at the OM tree whether it has any optimisable content
 */
public class OMOutputImpl {
    private XMLStreamWriter xmlWriter;
    private boolean doOptimize;
    private OutputStream outStream;
    private LinkedList binaryNodeList;
    private ByteArrayOutputStream bufferedSoapOutStream;
    private String mimeBoundary = null;

    public OMOutputImpl() {
    }

    public OMOutputImpl(XMLStreamWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    public OMOutputImpl(OutputStream outStream, boolean doOptimize)
            throws XMLStreamException, FactoryConfigurationError {
        setOutputStream(outStream, doOptimize);
    }

    public void setOutputStream(OutputStream outStream, boolean doOptimize)
            throws XMLStreamException, FactoryConfigurationError {
        this.doOptimize = doOptimize;
        this.outStream = outStream;
        if (doOptimize) {
            bufferedSoapOutStream = new ByteArrayOutputStream();
            xmlWriter =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(
                            bufferedSoapOutStream);
            binaryNodeList = new LinkedList();
        } else {
            xmlWriter =
                    XMLOutputFactory.newInstance().createXMLStreamWriter(
                            outStream);
        }
    }

    public void flush() throws XMLStreamException {
        xmlWriter.flush();
        if (doOptimize) {
            MIMEOutputUtils.complete(outStream, bufferedSoapOutStream,
                    binaryNodeList, getMimeBoundary());
        }
    }

    public boolean isOptimized() {
        return doOptimize;
    }

    public String getOptimizedContentType() {
        return org.apache.axis2.om.impl.MIMEOutputUtils.getContentTypeForMime(getMimeBoundary());
    }

    public void writeOptimized(OMText node) {
        binaryNodeList.add(node);
    }

    public void setXmlStreamWriter(XMLStreamWriter xmlWriter) {
        this.xmlWriter = xmlWriter;
    }

    public XMLStreamWriter getXmlStreamWriter() {
        return xmlWriter;
    }

    public String getMimeBoundary() {
        if(mimeBoundary != null) {
            mimeBoundary = "--MIMEBoundary" + getRandomStringOf18Characters();
        }
        return mimeBoundary;
    }

    private static Random myRand = null;

    /**
     * MD5 a random string with localhost/date etc will return 128 bits
     * construct a string of 18 characters from those bits.
     * @return string
     */
    private static String getRandomStringOf18Characters() {
        if (myRand == null) {
            myRand = new Random();
        }
        long rand = myRand.nextLong();
        String sid;
        try {
            sid = InetAddress.getLocalHost().toString();
        } catch (UnknownHostException e) {
            sid = Thread.currentThread().getName();
        }
        long time = System.currentTimeMillis();
        StringBuffer sb = new StringBuffer();
        sb.append(sid);
        sb.append(":");
        sb.append(Long.toString(time));
        sb.append(":");
        sb.append(Long.toString(rand));
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("Error: " + e);
        }
        md5.update(sb.toString().getBytes());
        byte[] array = md5.digest();
        StringBuffer sb2 = new StringBuffer();
        for (int j = 0; j < array.length; ++j) {
            int b = array[j] & 0xFF;
            sb2.append(Integer.toHexString(b));
        }
        int begin = myRand.nextInt();
        if(begin < 0) begin = begin * -1;
        begin = begin % 8;
        return new String("--" + sb2.toString().substring(begin, begin + 18)).toUpperCase();
    }
}
