/**
 * 
 */
package org.apache.axis2.jaxws.wrapper.beans;


import org.apache.axis2.jaxws.Constants;
import org.apache.axis2.jaxws.description.DescriptionFactory;
import org.apache.axis2.jaxws.description.EndpointDescription;
import org.apache.axis2.jaxws.description.EndpointDescriptionJava;
import org.apache.axis2.jaxws.description.OperationDescription;
import org.apache.axis2.jaxws.description.ServiceDescription;
import org.apache.axis2.jaxws.marshaller.MethodMarshaller;
import org.apache.axis2.jaxws.marshaller.factory.MethodMarshallerFactory;
import org.apache.axis2.jaxws.marshaller.impl.alt.DocLitWrappedMinimalMethodMarshaller;
import org.apache.axis2.jaxws.unitTest.TestLogger;

import java.io.File;

import junit.framework.TestCase;

public class WrapperBeanMarshallTests extends TestCase {
    /**
     * This is the negative test case, when we do not use generated artifacts from cache
     * and user did not package wrapper beans, we should see use of DocLitWrappedMinumumMarshaller.
     */
    public void testGetMarshallerOperationDescriptionBooleanNegative() {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());

        String cache_location = "/target/wscache/classes";
        try{
            String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
            cache_location = new File(baseDir+cache_location).getAbsolutePath();
            TestLogger.logger.debug("cache location ="+cache_location);
            
            //Get EndpointDescription.
            //Set location on AxisConfiguraiton.
            Class sei = AddNumbersService.class;
            EndpointDescription description = getEndpointDesc(sei);
            TestLogger.logger.debug("description objects where created successfully");
            OperationDescription[] ops =description.getEndpointInterfaceDescription().getOperations();
            assertNotNull( "OperationDescriptions where null", ops);
            assertTrue("No Operation Descriptions where found", ops.length>0);
            OperationDescription op = ops[0];
            TestLogger.logger.debug("operation found, java methodName="+op.getJavaMethodName());
            //Don not Set cache on AxisConfiguration.
            //get Marshaller, verify its not DoclitWrappedMinimum.
            TestLogger.logger.debug("ws_cache location NOT set on AxisConfigContext, location="+cache_location);
            MethodMarshaller mm = MethodMarshallerFactory.getMarshaller(op, false);
            assertNotNull("getMarshaller returned null", mm );
            TestLogger.logger.debug("MethodMarshaller was created, type="+mm.getClass().getName());
            assertTrue("Generated artifacts not found, Method marshaller should be DocLitWrappedMinimumMarshaller", (mm instanceof DocLitWrappedMinimalMethodMarshaller));
        }catch(Exception e){
            TestLogger.logger.debug("Exception ="+e.getMessage());
            fail(e.getMessage());
        }
        
    }
    /**
     * In this test case user did not package wrapper beans but we add generated artifacts from cache, we should see use of
     * DocLitWrappedMarshaller. 
     * Test method for {@link org.apache.axis2.jaxws.marshaller.factory.MethodMarshallerFactory#getMarshaller(org.apache.axis2.jaxws.description.OperationDescription, boolean)}.
     */
    public void testGetMarshallerOperationDescriptionBoolean() {
        TestLogger.logger.debug("---------------------------------------");
        TestLogger.logger.debug("test: " + getName());

        String cache_location = "/target/wscache/classes";
        try{
            String baseDir = new File(System.getProperty("basedir",".")).getCanonicalPath();
            cache_location = new File(baseDir+cache_location).getAbsolutePath();
            TestLogger.logger.debug("cache location ="+cache_location);
            
            //Get EndpointDescription.
            //Set location on AxisConfiguraiton.
            Class sei = AddNumbersService.class;
            EndpointDescription description = getEndpointDesc(sei);
            TestLogger.logger.debug("description objects where created successfully");
            OperationDescription[] ops =description.getEndpointInterfaceDescription().getOperations();
            assertNotNull( "OperationDescriptions where null", ops);
            assertTrue("No Operation Descriptions where found", ops.length>0);
            OperationDescription op = ops[0];
            TestLogger.logger.debug("operation found, java methodName="+op.getJavaMethodName());
            //Set cache on AxisConfiguration.
            //get Marshaller, verify its not DoclitWrappedMinimum.
            description.getServiceDescription().getAxisConfigContext().setProperty(Constants.WS_CACHE, cache_location);
            TestLogger.logger.debug("ws_cache location set on AxisConfigContext, location="+cache_location);
            MethodMarshaller mm = MethodMarshallerFactory.getMarshaller(op, false);
            assertNotNull("getMarshaller returned null", mm );
            TestLogger.logger.debug("MethodMarshaller was created, type="+mm.getClass().getName());
            assertTrue("Generated artifacts are in cache, Method marshaller should not be DocLitWrappedMinimumMarshaller", !(mm instanceof DocLitWrappedMinimalMethodMarshaller));
        }catch(Exception e){
            TestLogger.logger.debug("Exception ="+e.getMessage());
            fail(e.getMessage());
        }
        
    }

    private EndpointDescription getEndpointDesc(Class implementationClass) {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc =
                DescriptionFactory.createServiceDescription(implementationClass);
        assertNotNull(serviceDesc);

        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescription testEndpointDesc = endpointDesc[0];
        return testEndpointDesc;
    }
}
