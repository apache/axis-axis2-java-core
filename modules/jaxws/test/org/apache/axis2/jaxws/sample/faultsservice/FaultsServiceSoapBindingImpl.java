//
// IBM Confidential OCO Source Material
// (C) COPYRIGHT International Business Machines Corp. 2006
// The source code for this program is not published or otherwise divested
// of its trade secrets, irrespective of what has been deposited with the
// U.S. Copyright Office.
//
// Change History:
// Date        UserId         Defect            Description
// ----------------------------------------------------------------------------
// 08/28/2006  mzheng         LIDB3296-46.01    New File
//

package org.apache.axis2.jaxws.sample.faultsservice;

import org.test.polymorphicfaults.*;
import javax.xml.ws.Holder;
import javax.xml.ws.WebServiceException;
import javax.jws.WebService;


/**
 * This class provides server side implementation for the 
 * FaultsServicePortType.
 */
@WebService (targetNamespace="http://org/test/polymorphicfaults",
             wsdlLocation="WEB-INF/wsdl/FaultsService.wsdl",
             serviceName="FaultsService",
             portName="FaultsPort",
             endpointInterface="org.apache.axis2.jaxws.sample.faultsservice.FaultsServicePortType")

public class FaultsServiceSoapBindingImpl implements FaultsServicePortType {

    /**
     * Throws wrapper exceptions for fault beans
     */
    public float getQuote(String tickerSymbol) throws 
        BaseFault_Exception, DerivedFault1_Exception, 
        DerivedFault2_Exception, InvalidTickerFault, SimpleFault {
System.out.println("\nIn getQuote(): " + tickerSymbol + "\n");
        if (tickerSymbol.equals("ABC")) {
            throw new SimpleFault("Server throws SimpleFault", 100);
        } else if (tickerSymbol.equals("XYZ")) {
            throw new InvalidTickerFault("Server throws InvalidTickerFault", tickerSymbol);
        } else if (tickerSymbol.equals("one")) {
            DerivedFault1 df = new DerivedFault1();
            df.setA(100);
            df.setB(tickerSymbol);
            throw new DerivedFault1_Exception("Server throws DerivedFault1_Exception", df);
        }  else if (tickerSymbol.equals("two")) {
            DerivedFault2 df = new DerivedFault2();
            df.setA(200);
            df.setB(tickerSymbol);
            df.setC(80.0F);
            throw new DerivedFault2_Exception("Server throws DerivedFault2_Exception", df);
        } else {
            BaseFault bf = new BaseFault();
            bf.setA(400);
            throw new BaseFault_Exception("Server throws BaseFault_Exception", bf);
        }
    }


    /**
     * Throws wrapper exceptions for derived fault beans
     */
    public int throwFault(int a, String b, float c) throws 
        BaseFault_Exception, ComplexFault_Exception {
        if(b.equals("Complex")) {
            ComplexFault cf = new ComplexFault();
            cf.setA(a); 
            cf.setB(b); 
            cf.setC(c); 
            cf.setD(5); 
            throw new ComplexFault_Exception("Server throws ComplexFault_Exception", cf);
        } 
        DerivedFault2 df = new DerivedFault2();
        df.setA(a); 
        df.setB(b); 
        df.setC(c); 
        throw new BaseFault_Exception("Server throws BaseFault_Exception", df);
    }


    /**
     * Returns a fault bean or throws a wrapper exception
     */
    public void returnFault(int a, String b, float c, 
                            Holder<DerivedFault1> fault) throws EqualFault {

        DerivedFault2 newFault = new DerivedFault2();
        newFault.setA(fault.value.getA());            
        newFault.setB(fault.value.getB());            
        newFault.setC(c);

        if(fault.value.getB().equals("fault")) {
            fault.value = newFault;
            return;
        } else if (fault.value.getB().equals("exception")) {
            throw new EqualFault("Server throws EqualFault", newFault);            
        }
        DerivedFault1 df = new DerivedFault1();
        df.setA(a + 1); 
        df.setB("Server: " + b); 
        throw new EqualFault("Server throws EqualFault", df);
    }
}
