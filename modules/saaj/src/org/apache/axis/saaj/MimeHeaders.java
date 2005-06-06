/*
 * Created on Apr 8, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package org.apache.axis.saaj;

import java.util.Iterator;
import javax.xml.soap.MimeHeader;

/**
 * @author Ashutosh Shahi	ashutosh.shahi@gmail.com
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MimeHeaders extends javax.xml.soap.MimeHeaders {
    public MimeHeaders() {
    }

    public MimeHeaders(javax.xml.soap.MimeHeaders h) {
        Iterator iterator = h.getAllHeaders();
        while (iterator.hasNext()) {
            MimeHeader hdr = (MimeHeader) iterator.next();
            addHeader(hdr.getName(), hdr.getValue());
        }
    }

    private int getHeaderSize() {
        int size = 0;
        Iterator iterator = getAllHeaders();
        while (iterator.hasNext()) {
            iterator.next();
            size++;
        }
        return size;
    }
}
