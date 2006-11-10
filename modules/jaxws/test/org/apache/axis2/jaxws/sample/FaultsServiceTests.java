/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import javax.xml.ws.BindingProvider;

import org.apache.axis2.jaxws.sample.faultsservice.BaseFault_Exception;
import org.test.polymorphicfaults.ComplexFault;
import org.apache.axis2.jaxws.sample.faultsservice.ComplexFault_Exception;
import org.test.polymorphicfaults.DerivedFault2;
import org.apache.axis2.jaxws.sample.faultsservice.FaultsService;
import org.apache.axis2.jaxws.sample.faultsservice.FaultsServicePortType;
import junit.framework.TestCase;


public class FaultsServiceTests extends TestCase {
    
    String axisEndpoint = "http://localhost:8080/axis2/services/FaultsService";
    
    public void testFaultsService1(){
        //FaultyWebServiceFault_Exception exception = null;
        Exception exception = null;
        try{
            System.out.println("----------------------------------");
            System.out.println("test: " + getName());
            FaultsService service = new FaultsService();
            FaultsServicePortType proxy = service.getFaultsPort();
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);

            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "a", 2);
            
        }catch(BaseFault_Exception e){
            exception = e;
        } catch (ComplexFault_Exception e) {
            fail("Should not get ComplexFault_Exception in this testcase");
        }
        
        System.out.println("----------------------------------");
        
        assertNotNull(exception);
        assertTrue(((BaseFault_Exception)exception).getFaultInfo() instanceof DerivedFault2);
        
    }
    
    public void testFaultsService2(){
        //FaultyWebServiceFault_Exception exception = null;
        Exception exception = null;
        try{
            System.out.println("----------------------------------");
            System.out.println("test: " + getName());
            FaultsService service = new FaultsService();
            FaultsServicePortType proxy = service.getFaultsPort();
            BindingProvider p = (BindingProvider)proxy;
            p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);

            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "Complex", 2);  // "Complex" will cause service to throw ComplexFault_Exception
            
        }catch(BaseFault_Exception e){
            fail("Should not get BaseFault_Exception in this testcase");
        } catch (ComplexFault_Exception e) {
            exception = e;
        }
        
        System.out.println("----------------------------------");
        
        assertNotNull(exception);
        assertTrue(((BaseFault_Exception)exception).getFaultInfo() instanceof ComplexFault);
        
    }

}
