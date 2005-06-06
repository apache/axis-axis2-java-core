/*
 * Created on Mar 15, 2005
 *
 */
package org.apache.axis.saaj;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;

/**
 * Class Prefixed QName
 * 
 * Took this implementation from Axis 1.2 code
 */
public class PrefixedQName implements Name {
    /** comment/shared empty string */
    private static final String emptyString = "".intern();
    
    /**
     * Field prefix
     */
    private String prefix;
    /**
     * Field qName
     */
    private QName qName;
    
    /**
     * Constructor PrefixedQName
     * @param uri
     * @param localName
     * @param pre
     */
    public PrefixedQName(String uri, String localName, String pre) {
        qName = new QName(uri, localName);
        prefix = (pre == null)
                            ? emptyString
                            : pre.intern();
    }

    /**
     * Constructor qname
     * @param qname
     * @return
     */
    public PrefixedQName(QName qname) {
        this.qName = qname;
        prefix = emptyString;
    }

    /**
     * Method getLocalName
     * @return
     */
    public String getLocalName() {
        return qName.getLocalPart();
    }
    
    /**
     * Method getQualifiedName
     * @return
     */
    public String getQualifiedName() {
        StringBuffer buf = new StringBuffer(prefix);
        if(prefix != emptyString)
            buf.append(':');
        buf.append(qName.getLocalPart());
        return buf.toString();
    }
    
    /**
     * Method getURI
     * @return
     */
    public String getURI() {
        return qName.getNamespaceURI();
    }
    
    /**
     * Method getPrefix
     * @return
     */
    public String getPrefix() {
        return prefix;
    }
   
    /**
     * Method equals
     * @param obj
     * @return
     */
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof PrefixedQName)) {
            return false;
        }
        if (!qName.equals(((PrefixedQName)obj).qName)) {
            return false;
        }
        if (prefix == ((PrefixedQName) obj).prefix) {
            return true;
        }
        return false;
    }

    /**
     * Method hasCode
     * @return
     */
    public int hashCode() {
        return prefix.hashCode() + qName.hashCode();
    }
    
    /**
     * Method toString
     * @return
     */
    public String toString() {
        return qName.toString();
    }
}
