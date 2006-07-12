
package org.apache.axis2.jaxws.jaxb.mfquote;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for getPrice complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getPrice">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="fund" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="10holdings" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="nav" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getPrice", propOrder = {
    "fund",
    "_10Holdings",
    "nav"
})
public class GetPrice {

    @XmlElement()
    protected String fund;
    @XmlElement(name = "10holdings")
    protected String _10Holdings;
    @XmlElement()
    protected String nav;

    /**
     * Gets the value of the fund property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getFund() {
        return fund;
    }

    /**
     * Sets the value of the fund property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFund(String value) {
        this.fund = value;
    }

    /**
     * Gets the value of the 10Holdings property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String get10Holdings() {
        return _10Holdings;
    }

    /**
     * Sets the value of the 10Holdings property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void set10Holdings(String value) {
        this._10Holdings = value;
    }

    /**
     * Gets the value of the nav property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getNav() {
        return nav;
    }

    /**
     * Sets the value of the nav property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setNav(String value) {
        this.nav = value;
    }

}
