
package org.apache.axis2.jaxws.sample.wsgen.jaxws;

// SERVER-SIDE CLASS

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlRootElement(name = "echoStringResponse", namespace = "http://wsgen.sample.jaxws.axis2.apache.org")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "echoStringResponse", namespace = "http://wsgen.sample.jaxws.axis2.apache.org")
public class EchoStringResponse {

    @XmlElement(name = "return", namespace = "")
    private String _return;

    /**
     * 
     * @return
     *     returns String
     */
    public String get_return() {
        return this._return;
    }

    /**
     * 
     * @param _return
     *     the value for the _return property
     */
    public void set_return(String _return) {
        this._return = _return;
    }

}
