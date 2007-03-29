
package org.apache.axis2.jaxws.sample.wsgen.jaxws;

// SERVER-SIDE CLASS

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "echoString", namespace = "http://wsgen.sample.jaxws.axis2.apache.org")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "echoString", namespace = "http://wsgen.sample.jaxws.axis2.apache.org")
public class EchoString {

    @XmlElement(name = "arg0", namespace = "")
    private String arg0;

    /**
     * 
     * @return
     *     returns String
     */
    public String getArg0() {
        return this.arg0;
    }

    /**
     * 
     * @param arg0
     *     the value for the arg0 property
     */
    public void setArg0(String arg0) {
        this.arg0 = arg0;
    }

}
