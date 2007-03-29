
package org.apache.axis2.jaxws.sample.faultsservice;

import javax.xml.ws.WebFault;

/**
 * This is an example of a legacy exception  which may be the result of a JAX-RPC emission.
 * The fault does not have the valid constructors and lacks a getFaultInfo method.
 * However (in this case) the fault has a @WebFault that identifies the faultbean
 */
@WebFault(name = "InvalidTickerFault", 
        targetNamespace = "http://org/test/polymorphicfaults",
        faultBean="org.test.polymorphicfaults.InvalidTickerFaultExceptionBean")
        
        // faultBean is intentionally not specified. It should default to 
        // faultBean="org.test.polymorphicfaults.InvalidTickerFault_ExceptionBean"
public class InvalidTickerFault_Exception
    extends Exception
{

    /**
     * Java type that goes as soapenv:Fault detail element.
     * 
     */
    private String legacyData1;
    private int legacyData2;

    /**
     * 
     * @param message
     * @param faultInfo
     */
    public InvalidTickerFault_Exception(String message, String legacyData1, int legacyData2) {
        super(message);
        this.legacyData1 = legacyData1;
        this.legacyData2 = legacyData2;
    }

    /**
     * 
     * @param cause
     * @param message
     * @param faultInfo
     */
    public InvalidTickerFault_Exception(String message, String legacyData1, int legacyData2, Throwable cause) {
        super(message, cause);
        this.legacyData1 = legacyData1;
        this.legacyData2 = legacyData2;
    }

    
    public String getLegacyData1() {
        return legacyData1;
    }
    public int getLegacyData2() {
        return legacyData2;
    }

}
