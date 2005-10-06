/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package org.apache.axis2.databinding.symbolTable;

import javax.xml.namespace.QName;

/**
 * This class simply collects
 */
public class Parameter {

    // constant values for the parameter mode.

    /** Field IN */
    public static final byte IN = 1;

    /** Field OUT */
    public static final byte OUT = 2;

    /** Field INOUT */
    public static final byte INOUT = 3;

    // The QName of the element associated with this param.  Defaults to
    // null, which means it'll be new QName("", name)

    /** Field qname */
    private QName qname;

    // The part name of this parameter, just a string.

    /** Field name */
    private String name;

    // The MIME type of this parameter, null if it isn't a MIME type.

    /** Field mimeInfo */
    private MimeInfo mimeInfo = null;

    /** Field type */
    private TypeEntry type;

    /** Field mode */
    private byte mode = IN;

    // Flags indicating whether the parameter goes into the soap message as
    // a header.

    /** Field inHeader */
    private boolean inHeader = false;

    /** Field outHeader */
    private boolean outHeader = false;
    
    /** Is this an omittable param? */
    private boolean omittable = false;

    /**
     * Method toString
     * 
     * @return 
     */
    public String toString() {

        return "(" + type + ((mimeInfo == null)
                ? ""
                : "(" + mimeInfo + ")") + ", " + getName() + ", "
                + ((mode == IN)
                ? "IN)"
                : (mode == INOUT)
                ? "INOUT)"
                : "OUT)" + (inHeader
                ? "(IN soap:header)"
                : "") + (outHeader
                ? "(OUT soap:header)"
                : ""));
    }    // toString

    /**
     * Get the fully qualified name of this parameter.
     * 
     * @return 
     */
    public QName getQName() {
        return qname;
    }

    /**
     * Get the name of this parameter.  This call is equivalent to
     * getQName().getLocalPart().
     * 
     * @return 
     */
    public String getName() {

        if ((name == null) && (qname != null)) {
            return qname.getLocalPart();
        }

        return name;
    }

    /**
     * Set the name of the parameter.  This replaces both the
     * name and the QName (the namespaces becomes "").
     * 
     * @param name 
     */
    public void setName(String name) {

        this.name = name;

        if (qname == null) {
            this.qname = new QName("", name);
        }
    }

    /**
     * Set the QName of the parameter.
     * 
     * @param qname 
     */
    public void setQName(QName qname) {
        this.qname = qname;
    }

    /**
     * Get the MIME type of the parameter.
     * 
     * @return 
     */
    public MimeInfo getMIMEInfo() {
        return mimeInfo;
    }    // getMIMEType

    /**
     * Set the MIME type of the parameter.
     * 
     * @param mimeInfo 
     */
    public void setMIMEInfo(MimeInfo mimeInfo) {
        this.mimeInfo = mimeInfo;
    }    // setMIMEType

    /**
     * Get the TypeEntry of the parameter.
     * 
     * @return 
     */
    public TypeEntry getType() {
        return type;
    }

    /**
     * Set the TypeEntry of the parameter.
     * 
     * @param type 
     */
    public void setType(TypeEntry type) {
        this.type = type;
    }

    /**
     * Get the mode (IN, INOUT, OUT) of the parameter.
     * 
     * @return 
     */
    public byte getMode() {
        return mode;
    }

    /**
     * Set the mode (IN, INOUT, OUT) of the parameter.  If the input
     * to this method is not one of IN, INOUT, OUT, then the value
     * remains unchanged.
     * 
     * @param mode 
     */
    public void setMode(byte mode) {

        if (mode <= INOUT && mode >= IN) {
            this.mode = mode;
        }
    }

    /**
     * Is this parameter in the input message header?
     * 
     * @return 
     */
    public boolean isInHeader() {
        return inHeader;
    }    // isInHeader

    /**
     * Set the inHeader flag for this parameter.
     * 
     * @param inHeader 
     */
    public void setInHeader(boolean inHeader) {
        this.inHeader = inHeader;
    }    // setInHeader

    /**
     * Is this parameter in the output message header?
     * 
     * @return 
     */
    public boolean isOutHeader() {
        return outHeader;
    }    // isOutHeader

    /**
     * Set the outHeader flag for this parameter.
     * 
     * @param outHeader 
     */
    public void setOutHeader(boolean outHeader) {
        this.outHeader = outHeader;
    }    // setOutHeader

    public boolean isOmittable() {
        return omittable;
    }

    public void setOmittable(boolean omittable) {
        this.omittable = omittable;
    }
}    // class Parameter
