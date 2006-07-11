
package org.apache.axis2.jaxws.jaxb.mfquote;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.apache.axis2.jaxws.jaxb.mfquote package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _GetPriceResponse_QNAME = new QName("http://com/ibm/webservices/jaxws/test", "getPriceResponse");
    private final static QName _GetPrice_QNAME = new QName("http://com/ibm/webservices/jaxws/test", "getPrice");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.apache.axis2.jaxws.jaxb.mfquote
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link StockPrice }
     * 
     */
    public StockPrice createStockPrice() {
        return new StockPrice();
    }

    /**
     * Create an instance of {@link GetPrice }
     * 
     */
    public GetPrice createGetPrice() {
        return new GetPrice();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link StockPrice }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://com/ibm/webservices/jaxws/test", name = "getPriceResponse")
    public JAXBElement<StockPrice> createGetPriceResponse(StockPrice value) {
        return new JAXBElement<StockPrice>(_GetPriceResponse_QNAME, StockPrice.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link GetPrice }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "http://com/ibm/webservices/jaxws/test", name = "getPrice")
    public JAXBElement<GetPrice> createGetPrice(GetPrice value) {
        return new JAXBElement<GetPrice>(_GetPrice_QNAME, GetPrice.class, null, value);
    }

}
