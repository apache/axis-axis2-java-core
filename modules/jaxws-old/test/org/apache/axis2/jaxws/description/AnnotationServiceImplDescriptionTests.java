/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * Copyright 2006 International Business Machines Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.axis2.jaxws.description;

import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

import junit.framework.TestCase;

import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.ws.axis2.tests.EchoPort;
import org.apache.ws.axis2.tests.EchoServiceImplWithSEI;

/**
 * Tests the creation of the Description classes based on 
 * a service implementation bean and various combinations of
 * annotations
 */
public class AnnotationServiceImplDescriptionTests extends TestCase {
    /**
     * Create the description classes with a service implementation that
     * contains the @WebService JSR-181 annotation which references an SEI. 
     */
    public void testServiceImplWithSEI() {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescriptionFromServiceImpl(EchoServiceImplWithSEI.class, null);
        assertNotNull(serviceDesc);
        
        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(endpointDesc.length, 1);

        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointInterfaceDescription endpointIntfDesc = endpointDesc[0].getEndpointInterfaceDescription();
        assertNotNull(endpointIntfDesc);
        assertEquals(endpointIntfDesc.getSEIClass(), EchoPort.class);
        
        OperationDescription[] operations = endpointIntfDesc.getOperation("badMethodName");
        assertNull(operations);
        operations = endpointIntfDesc.getOperation("");
        assertNull(operations);
        operations = endpointIntfDesc.getOperation((String) null);
        assertNull(operations);
        operations = endpointIntfDesc.getOperation("echo");
        assertNotNull(operations);
        assertEquals(operations.length, 1);
        assertEquals(operations[0].getJavaMethodName(), "echo");

        String[] paramTypes = operations[0].getJavaParameters();
        assertNotNull(paramTypes);
        assertEquals(paramTypes.length, 1);
        assertEquals("javax.xml.ws.Holder", paramTypes[0]);
        
        // Test RequestWrapper annotations
        assertEquals(operations[0].getRequestWrapperLocalName(), "Echo");
        assertEquals(operations[0].getRequestWrapperTargetNamespace(), "http://ws.apache.org/axis2/tests");
        assertEquals(operations[0].getRequestWrapperClassName(), "org.apache.ws.axis2.tests.Echo");
        
        // Test ResponseWrapper annotations
        assertEquals(operations[0].getResponseWrapperLocalName(), "EchoResponse");
        assertEquals(operations[0].getResponseWrapperTargetNamespace(), "http://ws.apache.org/axis2/tests");
        assertEquals(operations[0].getResponseWrapperClassName(), "org.apache.ws.axis2.tests.EchoResponse");
        
        // Test SOAPBinding default; that annotation is not present in the SEI
        // Note that annotation could occur on the operation or the type
        // (although on this SEI it doesn't occur either place).
        assertEquals(SOAPBinding.Style.DOCUMENT, operations[0].getSoapBindingStyle());
        assertEquals(SOAPBinding.Style.DOCUMENT, endpointIntfDesc.getSoapBindingStyle());
        
    }
    
    public void testAxisServiceBackpointer() {
        // Test that the AxisService points back to the EndpointDesc
        // TODO: Temporary: Create an AxisService to pass in using WSDL
        // TODO: Eventually remove AxisService paramater from the factory; AxisService should be created (using annotations/wsdl/wsm etc)
        
        // Creating the AxisService this way is temporary; it should be created as part of creating the EndpointDescription from the
        // Service Impl.  For now, though, create a service-request-based ServiceDesc using WSDL.  Then specificy that AxisService
        // on the creation of the ServiceDesc from the service impl.  Verify that the AxisService points to the ServiceDesc.
        
        AxisService axisService = new AxisService();
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescriptionFromServiceImpl(EchoServiceImplWithSEI.class, axisService);
        EndpointDescription endpointDesc = serviceDesc.getEndpointDescriptions()[0];
        assertNotNull(serviceDesc);
        Parameter endpointDescParam = axisService.getParameter(EndpointDescription.AXIS_SERVICE_PARAMETER);
        assertNotNull(endpointDescParam);
        assertEquals(endpointDesc, endpointDescParam.getValue());
        
    }
    
    public void testOverloadedServiceImplWithSEI() {
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescriptionFromServiceImpl(DocLitWrappedImplWithSEI.class, null);
        assertNotNull(serviceDesc);
        
        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(endpointDesc.length, 1);
        // TODO: Using hardcoded endpointDesc[0] from ServiceDesc
        EndpointInterfaceDescription endpointIntfDesc = endpointDesc[0].getEndpointInterfaceDescription();
        assertNotNull(endpointIntfDesc);
        assertEquals(endpointIntfDesc.getSEIClass(), DocLitWrappedProxy.class);

        // Test for overloaded methods
        // SEI defines two Java methods with this name
        OperationDescription[] operations = endpointIntfDesc.getOperation("invokeAsync");
        assertNotNull(operations);
        assertEquals(operations.length, 2);
        assertEquals(operations[0].getJavaMethodName(), "invokeAsync");
        assertEquals(operations[1].getJavaMethodName(), "invokeAsync");
        
        // Check the Java parameters, WebParam names, and WebResult (actually lack thereof) for each of these operations
        
        // Note regarding WebParam names:
        // Unlike the Java paramaters, the WebParam names will remove the JAX-WS AsyncHandler
        // parameter.  That is because it is NOT part of the contract, and thus it is NOT part of
        // the JAXB object constructed for the method invocation.  The AsyncHandler is part of the 
        // JAX-WS programming model to support an asynchronous callback to receive the response.
        
        // Note regarding WebResult annotation:
        // The async methods on this SEI do not carry a WebResult annotations.
        boolean twoArgSignatureChecked = false;
        boolean oneArgSignatureChecked = false;
        for (OperationDescription operation:operations) {
            String[] checkParams = operation.getJavaParameters();
            String[] webParamNames = operation.getWebParamNames();
            if (checkParams.length == 1) {
                // Check the one arguement signature
                if (oneArgSignatureChecked) {
                    fail("One Arg signature occured more than once");
                }
                else {
                    oneArgSignatureChecked = true;
                    // Check the Java parameter
                    assertEquals(checkParams[0], "java.lang.String");
                    // Check the WebParam Names (see note above) 
                    assertEquals(1, webParamNames.length);
                    assertEquals("invoke_str", webParamNames[0]);
                    // Check the lack of a WebResult annotation
                    assertEquals(false, operation.isWebResultAnnotationSpecified());
                    assertEquals(null, operation.getWebResultName());
                }
            }
            else if (checkParams.length == 2) {
                // Check the two arguement signature
                if (twoArgSignatureChecked) {
                    fail("Two Arg signature occured more than once");
                }
                else {
                    twoArgSignatureChecked = true;
                    // Check the Java parameter
                    assertEquals(checkParams[0], "java.lang.String" );
                    assertEquals(checkParams[1], "javax.xml.ws.AsyncHandler");
                    // Check the WebParam Names (see note above) 
                    assertEquals(1, webParamNames.length);
                    assertEquals("invoke_str", webParamNames[0]);
                    // Check the lack of a WebResult annotation
                    assertEquals(false, operation.isWebResultAnnotationSpecified());
                    assertEquals(null, operation.getWebResultName());
                }
            }
            else {
                fail("Wrong number of parameters returned");
            }
        }

        // Test for a method with parameters of primitive types.  Note
        // this method IS overloaded
        operations = endpointIntfDesc.getOperation("twoWayHolderAsync");
        assertNotNull(operations);
        assertEquals(operations.length, 2);
        assertEquals(operations[0].getJavaMethodName(), "twoWayHolderAsync");
        assertEquals(operations[1].getJavaMethodName(), "twoWayHolderAsync");
        
        // Check the parameters for each operation
        twoArgSignatureChecked = false;
        boolean threeArgSignatureChecked = false;
        for (OperationDescription operation:operations) {
            String[] checkParams = operation.getJavaParameters();
            String[] webParamNames = operation.getWebParamNames();
            if (checkParams.length == 3) {
                // Check the one arguement signature
                if (threeArgSignatureChecked) {
                    fail("Three Arg signature occured more than once");
                }
                else {
                    threeArgSignatureChecked = true;
                    assertEquals(checkParams[0], "java.lang.String");
                    assertEquals(checkParams[1], "int");
                    assertEquals(checkParams[2], "javax.xml.ws.AsyncHandler");
                    // Check the WebParam Names (see note above) 
                    assertEquals(2, webParamNames.length);
                    assertEquals("twoWayHolder_str", webParamNames[0]);
                    assertEquals("twoWayHolder_int", webParamNames[1]);
                    // Check the lack of a WebResult annotation
                    assertEquals(false, operation.isWebResultAnnotationSpecified());
                    assertEquals(null, operation.getWebResultName());
                }
            }
            else if (checkParams.length == 2) {
                // Check the two arguement signature
                if (twoArgSignatureChecked) {
                    fail("Two Arg signature occured more than once");
                }
                else {
                    twoArgSignatureChecked = true;
                    assertEquals(checkParams[0], "java.lang.String" );
                    assertEquals(checkParams[1], "int");
                    // Check the WebParam Names (see note above) 
                    assertEquals(2, webParamNames.length);
                    assertEquals("twoWayHolder_str", webParamNames[0]);
                    assertEquals("twoWayHolder_int", webParamNames[1]);
                    // Check the lack of a WebResult annotation
                    assertEquals(false, operation.isWebResultAnnotationSpecified());
                    assertEquals(null, operation.getWebResultName());
                }
            }
            else {
                fail("Wrong number of parameters returned");
            }
        }

        // Test for a one-way, void method with no parameters which also is not overloaded
        operations = endpointIntfDesc.getOperation("oneWayVoid");
        assertNotNull(operations);
        assertEquals(operations.length, 1);
        assertEquals(operations[0].getJavaMethodName(), "oneWayVoid");
        String[] checkEmptyParams = operations[0].getJavaParameters();
        assertNotNull(checkEmptyParams);
        assertEquals(checkEmptyParams.length, 0);
        assertEquals(true, operations[0].isOneWay());
        assertEquals(false, operations[0].isWebResultAnnotationSpecified());
        assertEquals(null, operations[0].getWebResultName());
        
        // Test two-way method for lack of OneWay annotation and WebResult annotation
        operations = endpointIntfDesc.getOperation("invoke");
        assertNotNull(operations);
        assertEquals(1, operations.length);
        assertEquals(false, operations[0].isOneWay());
        assertEquals(true, operations[0].isWebResultAnnotationSpecified());
        assertEquals("return_str", operations[0].getWebResultName());
    }
    
    // ===========================================
    // The following tests use implementation classes defined below
    // in order to test various specific annotation settings
    // ===========================================
    
    public void testSOAPBindingDefault() {
        EndpointInterfaceDescription testEndpointInterfaceDesc = getEndpointInterfaceDesc(SOAPBindingDefaultTestImpl.class);
        
        assertNull(testEndpointInterfaceDesc.getSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT, testEndpointInterfaceDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.LITERAL, testEndpointInterfaceDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED, testEndpointInterfaceDesc.getSoapBindingParameterStyle());
    }

    public void testSOAPBindingDocEncBare() {
        EndpointInterfaceDescription testEndpointInterfaceDesc = getEndpointInterfaceDesc(SOAPBindingDocEncBareTestImpl.class);
        
        assertNotNull(testEndpointInterfaceDesc.getSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT, testEndpointInterfaceDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.ENCODED, testEndpointInterfaceDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.BARE, testEndpointInterfaceDesc.getSoapBindingParameterStyle());
    }
    
    public void testSOAPBindingMethodAnnotation() {
        // Verify that an impl without the method annotation uses the settings from the type
        EndpointInterfaceDescription testEndpointInterfaceDesc = getEndpointInterfaceDesc(SOAPBindingDocEncBareTestImpl.class);

        assertNotNull(testEndpointInterfaceDesc.getSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT, testEndpointInterfaceDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.ENCODED, testEndpointInterfaceDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.BARE, testEndpointInterfaceDesc.getSoapBindingParameterStyle());

        OperationDescription operationDesc = testEndpointInterfaceDesc.getOperation("echoString")[0];
        assertNotNull(operationDesc);
        assertNull(operationDesc.getSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT, operationDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.ENCODED, operationDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.BARE, operationDesc.getSoapBindingParameterStyle());
        
        // Verify that the method annotation setting overrides the type annotatino setting
        testEndpointInterfaceDesc = getEndpointInterfaceDesc(SOAPBindingDefaultMethodTestImpl.class);
        
        assertNull(testEndpointInterfaceDesc.getSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT, testEndpointInterfaceDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.LITERAL, testEndpointInterfaceDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED, testEndpointInterfaceDesc.getSoapBindingParameterStyle());

        operationDesc = testEndpointInterfaceDesc.getOperation("echoString")[0];
        assertNotNull(operationDesc);
        assertNotNull(operationDesc.getSoapBinding());
        assertEquals(javax.jws.soap.SOAPBinding.Style.DOCUMENT, operationDesc.getSoapBindingStyle());
        assertEquals(javax.jws.soap.SOAPBinding.Use.ENCODED, operationDesc.getSoapBindingUse());
        assertEquals(javax.jws.soap.SOAPBinding.ParameterStyle.BARE, operationDesc.getSoapBindingParameterStyle());
    }
    
    public void testDefaultReqRspWrapper() {

        // Test paramaterStyle = WRAPPED set a the type level with various combinations of method annotation setting
        EndpointInterfaceDescription testEndpointInterfaceDesc = getEndpointInterfaceDesc(DefaultReqRspWrapperTestImpl.class);
        OperationDescription operationDesc = testEndpointInterfaceDesc.getOperation("wrappedParams")[0];
        assertNotNull(operationDesc);
        assertEquals("wrappedParams", operationDesc.getRequestWrapperLocalName());
        assertEquals("wrappedParamsResponse", operationDesc.getResponseWrapperLocalName());
        // TODO: Tests for request and response wrapper namespace; currently throws UnsupportedOperationException
        assertEquals("org.apache.axis2.jaxws.description.WrappedParams", operationDesc.getRequestWrapperClassName());
        assertEquals("org.apache.axis2.jaxws.description.WrappedParams", operationDesc.getResponseWrapperClassName());

        operationDesc = testEndpointInterfaceDesc.getOperation("bareParams")[0];
        assertNotNull(operationDesc);
        assertNull(operationDesc.getRequestWrapperLocalName());
        assertNull(operationDesc.getResponseWrapperLocalName());
        assertNull(operationDesc.getRequestWrapperTargetNamespace());
        assertNull(operationDesc.getResponseWrapperTargetNamespace());
        assertNull(operationDesc.getRequestWrapperClassName());
        assertNull(operationDesc.getResponseWrapperClassName());

        // Test paramaterStyle = BARE set a the type level with various combinations of method annotation setting
        testEndpointInterfaceDesc = getEndpointInterfaceDesc(DefaultReqRspWrapperBareTestImpl.class);
        operationDesc = testEndpointInterfaceDesc.getOperation("wrappedParams")[0];
        assertNotNull(operationDesc);
        assertEquals("wrappedParams", operationDesc.getRequestWrapperLocalName());
        assertEquals("wrappedParamsResponse", operationDesc.getResponseWrapperLocalName());
        // TODO: Tests for request and response wrapper namespace; currently throws UnsupportedOperationException
        assertEquals("org.apache.axis2.jaxws.description.WrappedParams", operationDesc.getRequestWrapperClassName());
        assertEquals("org.apache.axis2.jaxws.description.WrappedParams", operationDesc.getResponseWrapperClassName());

        operationDesc = testEndpointInterfaceDesc.getOperation("bareParams")[0];
        assertNotNull(operationDesc);
        assertNull(operationDesc.getRequestWrapperLocalName());
        assertNull(operationDesc.getResponseWrapperLocalName());
        assertNull(operationDesc.getRequestWrapperTargetNamespace());
        assertNull(operationDesc.getResponseWrapperTargetNamespace());
        assertNull(operationDesc.getRequestWrapperClassName());
        assertNull(operationDesc.getResponseWrapperClassName());
    }
    
    public void testReqRspWrapper() {
        EndpointInterfaceDescription testEndpointInterfaceDesc = getEndpointInterfaceDesc(ReqRspWrapperTestImpl.class);
        OperationDescription operationDesc = testEndpointInterfaceDesc.getOperation("method1")[0];
        assertNotNull(operationDesc);
        assertEquals("method1ReqWrapper", operationDesc.getRequestWrapperLocalName());
        assertEquals("method1RspWrapper", operationDesc.getResponseWrapperLocalName());
        assertEquals("http://a.b.c.method1ReqTNS", operationDesc.getRequestWrapperTargetNamespace());
        assertEquals("http://a.b.c.method1RspTNS", operationDesc.getResponseWrapperTargetNamespace());
        assertEquals("org.apache.axis2.jaxws.description.method1ReqWrapper", operationDesc.getRequestWrapperClassName());
        assertEquals("org.apache.axis2.jaxws.description.method1RspWrapper", operationDesc.getResponseWrapperClassName());

        operationDesc = testEndpointInterfaceDesc.getOperation("method2")[0];
        assertEquals("method2", operationDesc.getRequestWrapperLocalName());
        assertEquals("method2RspWrapper", operationDesc.getResponseWrapperLocalName());
        assertEquals("http://a.b.c.method2ReqTNS", operationDesc.getRequestWrapperTargetNamespace());
        assertEquals("http://a.b.c.method2RspTNS", operationDesc.getResponseWrapperTargetNamespace());
        assertEquals("org.apache.axis2.jaxws.description.method2ReqWrapper", operationDesc.getRequestWrapperClassName());
        assertEquals("org.apache.axis2.jaxws.description.Method2", operationDesc.getResponseWrapperClassName());
    }

    /*
     * Method to return the endpoint interface description for a given implementation class.
     */
    private EndpointInterfaceDescription getEndpointInterfaceDesc(Class implementationClass) {
        // Use the description factory directly; this will be done within the JAX-WS runtime
        ServiceDescription serviceDesc = 
            DescriptionFactory.createServiceDescriptionFromServiceImpl(implementationClass, null);
        assertNotNull(serviceDesc);
        
        EndpointDescription[] endpointDesc = serviceDesc.getEndpointDescriptions();
        assertNotNull(endpointDesc);
        assertEquals(1, endpointDesc.length);
        
        // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
        EndpointDescription testEndpointDesc = endpointDesc[0];
        EndpointInterfaceDescription testEndpointInterfaceDesc = testEndpointDesc.getEndpointInterfaceDescription();
        assertNotNull(testEndpointInterfaceDesc);

        return testEndpointInterfaceDesc;
    }
}

// ============================================================================
// SOAPBindingDefaultTest service implementation class
// ============================================================================
@WebService()
class SOAPBindingDefaultTestImpl {
    public String echoString(String s) {
        return s;
    }
}
// ============================================================================
// SOAPBindingDocEncBareTestImpl service implementation class
// Note that Style should default
// ============================================================================
@WebService()
@SOAPBinding(use=javax.jws.soap.SOAPBinding.Use.ENCODED, parameterStyle=javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
class SOAPBindingDocEncBareTestImpl {
    public String echoString(String s) {
        return s;
    }
}
// ============================================================================
// SOAPBindingDefaultMethodTest service implementation class
// Note that style should default to DOCUMENT based on Type annotation
// ============================================================================
@WebService()
class SOAPBindingDefaultMethodTestImpl {
    @SOAPBinding(use=javax.jws.soap.SOAPBinding.Use.ENCODED, parameterStyle=javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
    public String echoString (String s) {
        return s;
    }
}

// =============================================================================
// testDefaultReqRspWrapper service implementation classes
// =============================================================================
@WebService
//Note the default parameterStyle is WRAPPED, so no type-level annotation is required.
class DefaultReqRspWrapperTestImpl {
    public String wrappedParams (String s) {
        return s;
    }
    
    @SOAPBinding(parameterStyle=javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
    public String bareParams (String s) {
        return s;
    }
}

@WebService
@SOAPBinding(parameterStyle=javax.jws.soap.SOAPBinding.ParameterStyle.BARE)
class DefaultReqRspWrapperBareTestImpl {
    @SOAPBinding(parameterStyle=javax.jws.soap.SOAPBinding.ParameterStyle.WRAPPED)
    public String wrappedParams (String s) {
        return s;
    }
    
    public String bareParams (String s) {
        return s;
    }
}

// =============================================================================
// testReqRspWrapper service implementation class
// =============================================================================
@WebService
//Note the default parameterStyle is WRAPPED, so no type-level annotation is required.
class ReqRspWrapperTestImpl {
    @RequestWrapper(localName="method1ReqWrapper", targetNamespace="http://a.b.c.method1ReqTNS", className="org.apache.axis2.jaxws.description.method1ReqWrapper")
    @ResponseWrapper(localName="method1RspWrapper", targetNamespace="http://a.b.c.method1RspTNS", className="org.apache.axis2.jaxws.description.method1RspWrapper")
    public String method1 (String s) {
        return s;
    }

    @RequestWrapper(targetNamespace="http://a.b.c.method2ReqTNS", className="org.apache.axis2.jaxws.description.method2ReqWrapper")
    @ResponseWrapper(localName="method2RspWrapper", targetNamespace="http://a.b.c.method2RspTNS")
    public String method2 (String s) {
        return s;
    }
}   