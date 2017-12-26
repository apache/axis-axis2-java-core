/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/**
 * 
 */
package org.apache.axis2.jaxws.sample;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.sample.faultsservice.BaseFault_Exception;
import org.apache.axis2.jaxws.sample.faultsservice.ComplexFault_Exception;
import org.apache.axis2.jaxws.sample.faultsservice.DerivedFault1_Exception;
import org.apache.axis2.jaxws.sample.faultsservice.DerivedFault2_Exception;
import org.apache.axis2.jaxws.sample.faultsservice.FaultsService;
import org.apache.axis2.jaxws.sample.faultsservice.FaultsServicePortType;
import org.apache.axis2.jaxws.sample.faultsservice.InvalidTickerFault_Exception;
import org.apache.axis2.jaxws.sample.faultsservice.SimpleFault;
import org.test.polymorphicfaults.BaseFault;
import org.test.polymorphicfaults.ComplexFault;
import org.test.polymorphicfaults.DerivedFault1;
import org.test.polymorphicfaults.DerivedFault2;

import javax.xml.soap.DetailEntry;
import javax.xml.soap.SOAPFault;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.soap.SOAPFaultException;

public class FaultsServiceTests extends AbstractTestCase {
    
    String axisEndpoint = "http://localhost:6060/axis2/services/FaultsService.FaultsPort";
    
    public static Test suite() {
        return getTestSetup(new TestSuite(FaultsServiceTests.class));
    }
    
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
        FaultsServicePortType proxy = getProxy();
        try{
            exception = null;
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "BaseFault", 2);
            
        }catch(BaseFault_Exception e){
            exception = e;
        } catch (ComplexFault_Exception e) {
            fail("Should not get ComplexFault_Exception in this testcase");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        Object fault = ((BaseFault_Exception)exception).getFaultInfo();
        assertSame(BaseFault.class, fault.getClass());
        BaseFault bf = (BaseFault) fault;
        assertEquals(2, bf.getA());
        
        // Repeat to verify 
        try{
            exception = null;
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "BaseFault", 2);
            
        }catch(BaseFault_Exception e){
            exception = e;
        } catch (ComplexFault_Exception e) {
            fail("Should not get ComplexFault_Exception in this testcase");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        fault = ((BaseFault_Exception)exception).getFaultInfo();
        assertSame(BaseFault.class, fault.getClass());
        bf = (BaseFault) fault;
        assertEquals(2, bf.getA());
        
    }
    /**
     * Tests that that BaseFault (DerivedFault1) is thrown
     */
    public void testFaultsService1() {
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            exception = null;
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "DerivedFault1", 2);
            
        }catch(BaseFault_Exception e){
            exception = e;
        } catch (ComplexFault_Exception e) {
            fail("Should not get ComplexFault_Exception in this testcase");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        Object fault = ((BaseFault_Exception)exception).getFaultInfo();
        assertSame(DerivedFault1.class, fault.getClass());
        DerivedFault1 df = (DerivedFault1) fault;
        assertEquals(2, df.getA());
        assertEquals("DerivedFault1", df.getB());
        
        // Repeat to verify behavior
        try{
            exception = null;
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "DerivedFault1", 2);
            
        }catch(BaseFault_Exception e){
            exception = e;
        } catch (ComplexFault_Exception e) {
            fail("Should not get ComplexFault_Exception in this testcase");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        fault = ((BaseFault_Exception)exception).getFaultInfo();
        assertSame(DerivedFault1.class, fault.getClass());
        df = (DerivedFault1) fault;
        assertEquals(2, df.getA());
        assertEquals("DerivedFault1", df.getB());
        
    }
    /**
     * Tests that that BaseFault (DerivedFault1) is thrown
     */
    public void testFaultsService2() {
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            exception = null;
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "DerivedFault2", 2);
            
        }catch(BaseFault_Exception e){
            exception = e;
        } catch (ComplexFault_Exception e) {
            fail("Should not get ComplexFault_Exception in this testcase");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        Object fault = ((BaseFault_Exception)exception).getFaultInfo();
        assertSame(DerivedFault2.class, fault.getClass());
        DerivedFault2 df = (DerivedFault2) fault;
        assertEquals(2, df.getA());
        assertEquals("DerivedFault2", df.getB());  
        assertEquals(2F, df.getC());
        
        // Repeat to verify behavior
        try{
            exception = null;
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "DerivedFault2", 2);
            
        }catch(BaseFault_Exception e){
            exception = e;
        } catch (ComplexFault_Exception e) {
            fail("Should not get ComplexFault_Exception in this testcase");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        fault = ((BaseFault_Exception)exception).getFaultInfo();
        assertSame(DerivedFault2.class, fault.getClass());
        df = (DerivedFault2) fault;
        assertEquals(2, df.getA());
        assertEquals("DerivedFault2", df.getB());  
        assertEquals(2F, df.getC());
    }
    
    /**
     * Tests that that ComplxFaultFault is thrown 
     */
    public void testFaultsService3(){
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "Complex", 2);  // "Complex" will cause service to throw ComplexFault_Exception
            
        }catch(BaseFault_Exception e){
            fail("Should not get BaseFault_Exception in this testcase");
        } catch (ComplexFault_Exception e) {
            exception = e;
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        Object fault = ((ComplexFault_Exception)exception).getFaultInfo();
        assertSame(ComplexFault.class, fault.getClass());
        ComplexFault cf = (ComplexFault) fault;
        assertEquals(2, cf.getA());
        assertEquals("Complex", cf.getB());  
        assertEquals(2F, cf.getC());
        assertEquals(5, cf.getD());
        
        
        // Repeat to verify behavior
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "Complex", 2);  // "Complex" will cause service to throw ComplexFault_Exception
            
        }catch(BaseFault_Exception e){
            fail("Should not get BaseFault_Exception in this testcase");
        } catch (ComplexFault_Exception e) {
            exception = e;
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        fault = ((ComplexFault_Exception)exception).getFaultInfo();
        assertSame(ComplexFault.class, fault.getClass());
        cf = (ComplexFault) fault;
        assertEquals(2, cf.getA());
        assertEquals("Complex", cf.getB());  
        assertEquals(2F, cf.getC());
        assertEquals(5, cf.getD());
    }
    
    
    /**
     * Tests that throwing of SimpleFault
     */
    public void testFaultsService4(){
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("SMPL");
            fail( "Expected SimpleFault but no fault was thrown ");
        }catch(SimpleFault e){
            SimpleFault fault = (SimpleFault) e;

            int faultInfo = fault.getFaultInfo();
            assertEquals(100, faultInfo);
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected SimpleFault but received " + e.getClass());
        }
        
        // Repeat to verify behavior
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("SMPL");
            fail( "Expected SimpleFault but no fault was thrown ");
        }catch(SimpleFault e){
            SimpleFault fault = (SimpleFault) e;

            int faultInfo = fault.getFaultInfo();
            assertEquals(100, faultInfo);
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected SimpleFault but received " + e.getClass());
        }
    }
    
    
    /**
     * Test throwing legacy fault
     * Disabled while I fix this test
     */
    public void testFaultsService5(){
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            
            
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("LEGC");
            fail( "Expected InvalidTickerFault_Exception but no fault was thrown ");
        }catch(InvalidTickerFault_Exception e){
            InvalidTickerFault_Exception fault = (InvalidTickerFault_Exception) e;

            assertEquals("LEGC", fault.getLegacyData1());
            assertEquals(123, fault.getLegacyData2());
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected InvalidTickerFault_Exception but received " + e.getClass());
        }
        
        // Repeat to verify behavior
        try {
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("LEGC");
            fail( "Expected InvalidTickerFault_Exception but no fault was thrown ");
        }catch(InvalidTickerFault_Exception e){
            InvalidTickerFault_Exception fault = (InvalidTickerFault_Exception) e;

            assertEquals("LEGC", fault.getLegacyData1());
            assertEquals(123, fault.getLegacyData2());
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected InvalidTickerFault_Exception but received " + e.getClass());
        }
    }
    
    /**
     * Tests that throwing of BaseFault_Exception
     */
    public void testFaultsService6(){
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("BASE");
            fail( "Expected BaseFault_Exception but no fault was thrown ");
        }catch(BaseFault_Exception e){
            BaseFault_Exception fault = (BaseFault_Exception) e;

            BaseFault faultInfo = fault.getFaultInfo();
            assertNotNull(faultInfo);
            assertEquals(400, faultInfo.getA());
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected BaseFault_Exception but received " + e.getClass());
        }
        
        // Repeat to verify behavior
        try{
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("BASE");
            fail( "Expected BaseFault_Exception but no fault was thrown ");
        }catch(BaseFault_Exception e){
            BaseFault_Exception fault = (BaseFault_Exception) e;

            BaseFault faultInfo = fault.getFaultInfo();
            assertNotNull(faultInfo);
            assertEquals(400, faultInfo.getA());
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected BaseFault_Exception but received " + e.getClass());
        }
    }

    /**
     * Tests that throwing of DerivedFault1_Exception
     */
    public void testFaultsService7(){
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("DF1");
            fail( "Expected DerivedFault1_Exception but no fault was thrown");
        }catch(DerivedFault1_Exception e){
            DerivedFault1_Exception fault = (DerivedFault1_Exception) e;

            DerivedFault1 faultInfo = fault.getFaultInfo();
            assertNotNull(faultInfo);
            assertEquals(100, faultInfo.getA());
            assertEquals("DF1", faultInfo.getB());
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected DerivedFault1_Exception but received " + e.getClass());
        }
        
        // Repeat to verify behavior
        try{
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("DF1");
            fail( "Expected DerivedFault1_Exception but no fault was thrown");
        }catch(DerivedFault1_Exception e){
            DerivedFault1_Exception fault = (DerivedFault1_Exception) e;

            DerivedFault1 faultInfo = fault.getFaultInfo();
            assertNotNull(faultInfo);
            assertEquals(100, faultInfo.getA());
            assertEquals("DF1", faultInfo.getB());
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected DerivedFault1_Exception but received " + e.getClass());
        }
    }
    
    /**
     * Tests that throwing of DerivedFault1_Exception
     */
    public void testFaultsService8(){
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("DF2");
            fail( "Expected DerivedFault2_Exception but no fault was thrown ");
        }catch(DerivedFault2_Exception e){
            DerivedFault2_Exception fault = (DerivedFault2_Exception) e;

            DerivedFault2 faultInfo = fault.getFaultInfo();
            assertNotNull(faultInfo);
            assertEquals(200, faultInfo.getA());
            assertEquals("DF2", faultInfo.getB());
            assertEquals(80.0F, faultInfo.getC());
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected DerivedFault1_Exception but received " + e.getClass());
        }
        
        // Repeat to verify behavior
        try{
            // the invoke will throw an exception, if the test is performed right
            float total = proxy.getQuote("DF2");
            fail( "Expected DerivedFault2_Exception but no fault was thrown ");
        }catch(DerivedFault2_Exception e){
            DerivedFault2_Exception fault = (DerivedFault2_Exception) e;

            DerivedFault2 faultInfo = fault.getFaultInfo();
            assertNotNull(faultInfo);
            assertEquals(200, faultInfo.getA());
            assertEquals("DF2", faultInfo.getB());
            assertEquals(80.0F, faultInfo.getC());
        } catch (Exception e) {
            fail("Wrong exception thrown.  Expected DerivedFault1_Exception but received " + e.getClass());
        }
    }
    
    /**
     * Tests that that SOAPFaultException is thrown 
     */
    public void testFaultsService9a(){
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "SOAPFaultException", 2);  // "SOAPFaultException" will cause service to throw SOAPFaultException
            
        }catch(SOAPFaultException e){
            // Okay
            exception = e;
        } catch (Exception e) {
            fail("Did not get a SOAPFaultException");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        SOAPFaultException sfe = (SOAPFaultException) exception;
        SOAPFault soapFault = sfe.getFault();
        assertNotNull(soapFault);
        assertEquals("hello world", soapFault.getFaultString());
        assertEquals("actor", soapFault.getFaultActor());
        assertNull(soapFault.getDetail());
        
        // Repeat to verify behavior
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "SOAPFaultException", 2);  // "SOAPFaultException" will cause service to throw SOAPFaultException
            
        }catch(SOAPFaultException e){
            // Okay
            exception = e;
        } catch (Exception e) {
            fail("Did not get a SOAPFaultException");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        sfe = (SOAPFaultException) exception;
        soapFault = sfe.getFault();
        assertNotNull(soapFault);
        assertEquals("hello world", soapFault.getFaultString());
        assertEquals("actor", soapFault.getFaultActor());
        assertNull(soapFault.getDetail());
    }
    
    /**
     * Tests that that SOAPFaultException is thrown 
     */
    public void testFaultsService9b(){
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "SOAPFaultException2", 2);  // "SOAPFaultException" will cause service to throw SOAPFaultException
            
        }catch(SOAPFaultException e){
            // Okay
            exception = e;
        } catch (Exception e) {
            fail("Did not get a SOAPFaultException");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        SOAPFaultException sfe = (SOAPFaultException) exception;
        SOAPFault soapFault = sfe.getFault();
        assertNotNull(soapFault);
        assertEquals("hello world2", soapFault.getFaultString());
        assertEquals("actor2", soapFault.getFaultActor());
        assertNotNull(soapFault.getDetail());
        DetailEntry de = (DetailEntry) soapFault.getDetail().getDetailEntries().next();
        assertNotNull(de);
        assertEquals("urn://sample", de.getNamespaceURI());
        assertEquals("detailEntry", de.getLocalName());
        assertEquals("Texas", de.getValue());
        
        // Repeat to verify behavior
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "SOAPFaultException2", 2);  // "SOAPFaultException" will cause service to throw SOAPFaultException
            
        }catch(SOAPFaultException e){
            // Okay
            exception = e;
        } catch (Exception e) {
            fail("Did not get a SOAPFaultException");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        sfe = (SOAPFaultException) exception;
        soapFault = sfe.getFault();
        assertNotNull(soapFault);
        assertEquals("hello world2", soapFault.getFaultString());
        assertEquals("actor2", soapFault.getFaultActor());
        assertNotNull(soapFault.getDetail());
        de = (DetailEntry) soapFault.getDetail().getDetailEntries().next();
        assertNotNull(de);
        assertEquals("urn://sample", de.getNamespaceURI());
        assertEquals("detailEntry", de.getLocalName());
        assertEquals("Texas", de.getValue());
    }
    
    /**
     * Tests that that SOAPFaultException (NPE) is thrown 
     */
    public void testFaultsService10(){
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "NPE", 2);  // "NPE" will cause service to throw NPE System Exception
            
        }catch(SOAPFaultException e){
            // Okay
            exception = e;
        } catch (Exception e) {
            fail("Did not get a SOAPFaultException");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        SOAPFaultException sfe = (SOAPFaultException) exception;
        SOAPFault soapFault = sfe.getFault();
        assertNotNull(soapFault);
        assertEquals("java.lang.NullPointerException", soapFault.getFaultString());
        
        // Repeat to verify behavior
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "NPE", 2);  // "NPE" will cause service to throw NPE System Exception
            
        }catch(SOAPFaultException e){
            // Okay
            exception = e;
        } catch (Exception e) {
            fail("Did not get a SOAPFaultException");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        sfe = (SOAPFaultException) exception;
        soapFault = sfe.getFault();
        assertNotNull(soapFault);
        assertEquals("java.lang.NullPointerException", soapFault.getFaultString());
    }
    
    /**
     * Tests that that SOAPFaultException (NPE) is thrown 
     */
    public void testFaultsService10a(){
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "NPE2", 2);  // "NPE" will cause service to throw NPE System Exception
            
        }catch(SOAPFaultException e){
            // Okay
            exception = e;
        } catch (Exception e) {
            fail("Did not get a SOAPFaultException");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        SOAPFaultException sfe = (SOAPFaultException) exception;
        SOAPFault soapFault = sfe.getFault();
        assertNotNull(soapFault);
        assertEquals("Null Pointer Exception occurred", soapFault.getFaultString());
        
        
        // Repeat to verify behavior
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "NPE2", 2);  // "NPE" will cause service to throw NPE System Exception
            
        }catch(SOAPFaultException e){
            // Okay
            exception = e;
        } catch (Exception e) {
            fail("Did not get a SOAPFaultException");
        }

        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        sfe = (SOAPFaultException) exception;
        soapFault = sfe.getFault();
        assertNotNull(soapFault);
        assertEquals("Null Pointer Exception occurred", soapFault.getFaultString());
    }
    
    /**
     * Tests that that SOAPFaultException (for WebServiceException) is thrown 
     */
    public void testFaultsService11(){
        FaultsServicePortType proxy = getProxy();
        Exception exception = null;
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "WSE", 2);  // "WSE" will cause service to throw WebServiceException System Exception
            
        }catch(SOAPFaultException e){
            // Okay...on the client a SOAPFaultException should be thrown
            exception = e;
        } catch (Exception e) {
            fail("Did not get a SOAPFaultException");
        }

        
        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        SOAPFaultException sfe = (SOAPFaultException) exception;
        SOAPFault soapFault = sfe.getFault();
        assertNotNull(soapFault);
        assertEquals("This is a WebServiceException", soapFault.getFaultString());
        
        // Repeat to verify behavior
        try{
            exception = null;
            
            // the invoke will throw an exception, if the test is performed right
            int total = proxy.throwFault(2, "WSE", 2);  // "WSE" will cause service to throw WebServiceException System Exception
            
        }catch(SOAPFaultException e){
            // Okay...on the client a SOAPFaultException should be thrown
            exception = e;
        } catch (Exception e) {
            fail("Did not get a SOAPFaultException");
        }

        
        TestLogger.logger.debug("----------------------------------");
        
        assertNotNull(exception);
        sfe = (SOAPFaultException) exception;
        soapFault = sfe.getFault();
        assertNotNull(soapFault);
        assertEquals("This is a WebServiceException", soapFault.getFaultString());
    }
    
    /**
     * Tests Resource injection
     */
    public void testResourceInjection() throws Exception {
        FaultsServicePortType proxy = getProxy();
        
        float total = proxy.getQuote("INJECTION");
        
        // If resource injection occurred properly, then the a value of 1234567 is expected
        assertEquals("Resource Injection Failed", 1234567F, total);
        
        // Repeat to verify behavior
        total = proxy.getQuote("INJECTION");
        
        // If resource injection occurred properly, then the a value of 1234567 is expected
        assertEquals("Resource Injection Failed", 1234567F, total);
    }
}
