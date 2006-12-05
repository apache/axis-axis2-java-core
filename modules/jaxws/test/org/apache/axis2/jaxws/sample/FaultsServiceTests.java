/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import javax.xml.ws.BindingProvider;

import org.apache.axis2.jaxws.sample.faultsservice.BaseFault_Exception;
import org.test.polymorphicfaults.BaseFault;
import org.test.polymorphicfaults.ComplexFault;
import org.test.polymorphicfaults.DerivedFault1;
import org.apache.axis2.jaxws.sample.faultsservice.ComplexFault_Exception;
import org.test.polymorphicfaults.DerivedFault2;
import org.apache.axis2.jaxws.sample.faultsservice.DerivedFault1_Exception;
import org.apache.axis2.jaxws.sample.faultsservice.DerivedFault2_Exception;
import org.apache.axis2.jaxws.sample.faultsservice.FaultsService;
import org.apache.axis2.jaxws.sample.faultsservice.FaultsServicePortType;
import org.apache.axis2.jaxws.sample.faultsservice.InvalidTickerFault_Exception;
import org.apache.axis2.jaxws.sample.faultsservice.SimpleFault;

import junit.framework.TestCase;


public class FaultsServiceTests extends TestCase {
    
    String axisEndpoint = "http://localhost:8080/axis2/services/FaultsService";
    
    /**
     * Utility method to get the proxy
     * @return proxy
     */
    private FaultsServicePortType getProxy() {
        FaultsService service = new FaultsService();
        FaultsServicePortType proxy = service.getFaultsPort();
        BindingProvider p = (BindingProvider)proxy;
        p.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY,axisEndpoint);
        return proxy;
    }
    
    /**
     * Tests that that BaseFault is thrown
     */
    public void testFaultsService0() {
        Exception exception = null;
        try{
            FaultsServicePortType proxy = getProxy();
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "BaseFault", 2);
            
        }catch(BaseFault_Exception e){
            exception = e;
        } catch (ComplexFault_Exception e) {
            fail("Should not get ComplexFault_Exception in this testcase");
        }
        
        System.out.println("----------------------------------");
        
        assertNotNull(exception);
        Object fault = ((BaseFault_Exception)exception).getFaultInfo();
        assertTrue(fault.getClass() == BaseFault.class);
        BaseFault bf = (BaseFault) fault;
        assertTrue(bf.getA() == 2);
        
    }
    /**
     * Tests that that BaseFault (DerivedFault1) is thrown
     */
    public void testFaultsService1() {
        Exception exception = null;
        try{
            FaultsServicePortType proxy = getProxy();
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "DerivedFault1", 2);
            
        }catch(BaseFault_Exception e){
            exception = e;
        } catch (ComplexFault_Exception e) {
            fail("Should not get ComplexFault_Exception in this testcase");
        }
        
        System.out.println("----------------------------------");
        
        assertNotNull(exception);
        Object fault = ((BaseFault_Exception)exception).getFaultInfo();
        assertTrue(fault.getClass() == DerivedFault1.class);
        DerivedFault1 df = (DerivedFault1) fault;
        assertTrue(df.getA() == 2);
        assertTrue(df.getB().equals("DerivedFault1"));
        
    }
    /**
     * Tests that that BaseFault (DerivedFault1) is thrown
     */
    public void testFaultsService2() {
        Exception exception = null;
        try{
            FaultsServicePortType proxy = getProxy();
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "DerivedFault2", 2);
            
        }catch(BaseFault_Exception e){
            exception = e;
        } catch (ComplexFault_Exception e) {
            fail("Should not get ComplexFault_Exception in this testcase");
        }
        
        System.out.println("----------------------------------");
        
        assertNotNull(exception);
        Object fault = ((BaseFault_Exception)exception).getFaultInfo();
        assertTrue(fault.getClass() == DerivedFault2.class);
        DerivedFault2 df = (DerivedFault2) fault;
        assertTrue(df.getA() == 2);
        assertTrue(df.getB().equals("DerivedFault2"));  
        assertTrue(df.getC() == 2);
    }
    
    /**
     * Tests that that ComplxFaultFault is thrown 
     */
    public void testFaultsService3(){
        Exception exception = null;
        try{
            FaultsServicePortType proxy = getProxy();
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "Complex", 2);  // "Complex" will cause service to throw ComplexFault_Exception
            
        }catch(BaseFault_Exception e){
            fail("Should not get BaseFault_Exception in this testcase");
        } catch (ComplexFault_Exception e) {
            exception = e;
        }
        
        System.out.println("----------------------------------");
        
        assertNotNull(exception);
        Object fault = ((ComplexFault_Exception)exception).getFaultInfo();
        assertTrue(fault.getClass() == ComplexFault.class);
        ComplexFault cf = (ComplexFault) fault;
        assertTrue(cf.getA() == 2);
        assertTrue(cf.getB().equals("Complex"));  
        assertTrue(cf.getC() == 2);
        assertTrue(cf.getD() == 5);
    }
    
    /**
     * Tests that throwing of SimpleFault
     * Disabled while I fix this test
     */
    public void _testFaultsService4(){
        Exception exception = null;
        try{
            FaultsServicePortType proxy = getProxy();
            
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("SMPL");
            fail( "Expected SimpleFault but no fault was thrown ");
        }catch(SimpleFault e){
            SimpleFault fault = (SimpleFault) e;

            int faultInfo = fault.getFaultInfo();
            assertTrue(faultInfo == 100);
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected SimpleFault but received " + e.getClass());
        }
    }
    
    
    /**
     * Test throwing legacy fault
     * Disabled while I fix this test
     */
    public void _testFaultsService5(){
        Exception exception = null;
        try{
            FaultsServicePortType proxy = getProxy();
            
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("LEGC");
            fail( "Expected InvalidTickerFault_Exception but no fault was thrown ");
        }catch(InvalidTickerFault_Exception e){
            InvalidTickerFault_Exception fault = (InvalidTickerFault_Exception) e;

            assertTrue(fault.getLegacyData1().equals("LEGC"));
            assertTrue(fault.getLegacyData2() == 123);
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected InvalidTickerFault_Exception but received " + e.getClass());
        }
    }
    
    /**
     * Tests that throwing of BaseFault_Exception
     */
    public void testFaultsService6(){
        Exception exception = null;
        try{
            FaultsServicePortType proxy = getProxy();
            
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("BASE");
            fail( "Expected BaseFault_Exception but no fault was thrown ");
        }catch(BaseFault_Exception e){
            BaseFault_Exception fault = (BaseFault_Exception) e;

            BaseFault faultInfo = fault.getFaultInfo();
            assertTrue(faultInfo != null);
            assertTrue(faultInfo.getA() == 400);
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected BaseFault_Exception but received " + e.getClass());
        }
    }

    /**
     * Tests that throwing of DerivedFault1_Exception
     */
    public void testFaultsService7(){
        Exception exception = null;
        try{
            FaultsServicePortType proxy = getProxy();
            
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("DF1");
            fail( "Expected DerivedFault1_Exception but no fault was thrown");
        }catch(DerivedFault1_Exception e){
            DerivedFault1_Exception fault = (DerivedFault1_Exception) e;

            DerivedFault1 faultInfo = fault.getFaultInfo();
            assertTrue(faultInfo != null);
            assertTrue(faultInfo.getA() == 100);
            assertTrue(faultInfo.getB().equals("DF1"));
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected DerivedFault1_Exception but received " + e.getClass());
        }
    }
    
    /**
     * Tests that throwing of DerivedFault1_Exception
     */
    public void testFaultsService8(){
        Exception exception = null;
        try{
            FaultsServicePortType proxy = getProxy();
            
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("DF2");
            fail( "Expected DerivedFault2_Exception but no fault was thrown ");
        }catch(DerivedFault2_Exception e){
            DerivedFault2_Exception fault = (DerivedFault2_Exception) e;

            DerivedFault2 faultInfo = fault.getFaultInfo();
            assertTrue(faultInfo != null);
            assertTrue(faultInfo.getA() == 200);
            assertTrue(faultInfo.getB().equals("DF2"));
            assertTrue(faultInfo.getC() == 80.0F);
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected DerivedFault1_Exception but received " + e.getClass());
        }
    }
}
