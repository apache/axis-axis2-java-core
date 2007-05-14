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

import junit.framework.TestCase;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderComposite;
import org.apache.axis2.jaxws.description.builder.MethodDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.ParameterDescriptionComposite;
import org.apache.axis2.jaxws.description.builder.WebMethodAnnot;
import org.apache.axis2.jaxws.description.builder.WebParamAnnot;
import org.apache.axis2.jaxws.description.builder.WebServiceAnnot;

import javax.jws.WebParam;
import javax.xml.ws.WebServiceException;
import java.util.HashMap;
import java.util.List;

/**
 * Tests the creation of the Description classes based on a service implementation bean and various
 * combinations of annotations
 */
public class AnnotationServiceImplWithDBCTests extends TestCase {
    /**
     * Create the description classes with a service implementation that contains the @WebService
     * JSR-181 annotation which references an SEI.
     */

    //Test creation of a Service Description from DBC, using a basic list.
    //An implicit SEI that extends only java.lang.object
    public void testServiceImplAsImplicitSEI() {
        //org.apache.log4j.BasicConfigurator.configure();

        //Build a Hashmap of DescriptionBuilderComposites that contains the serviceImpl and
        //all necessary associated DBC's possibly including SEI and superclasses
        HashMap<String, DescriptionBuilderComposite> dbcMap =
                new HashMap<String, DescriptionBuilderComposite>();

        DescriptionBuilderComposite dbc = buildDBCNoEndpointInterface();

        dbcMap.put(dbc.getClassName(), dbc);

        // TODO: This test is invalid as is.  It does not specify WSDL, and WSDL generator is not currently available in Open Source
        //       So, an exception is currently being thrown.  It may be the correct fix is to NOT always try to generate WSDL; and only
        //       try to generate it if it is asked for.
        try {
            List<ServiceDescription> serviceDescList =
                    DescriptionFactory.createServiceDescriptionFromDBCMap(dbcMap);
            assertNotNull(serviceDescList.get(0));

            //We know this list contains only one SD, so no need to loop
            EndpointDescription[] endpointDesc = serviceDescList.get(0).getEndpointDescriptions();
            assertNotNull(endpointDesc);
            assertEquals(endpointDesc.length, 1);

            // TODO: How will the JAX-WS dispatcher get the appropriate port (i.e. endpoint)?  Currently assumes [0]
            EndpointInterfaceDescription endpointIntfDesc =
                    endpointDesc[0].getEndpointInterfaceDescription();
            assertNotNull(endpointIntfDesc);

        } catch (WebServiceException e) {
        }

        /*
        * Deprecated -- this used to be the test for
        * EndpointInterfaceDescription.getOperationForJavaMethod
        */
/*
        OperationDescription[] operations = endpointIntfDesc.getOperations();
        
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
*/
    }

    //TODO: Basic Test with EndpointInterface set to something valid, so another DBC must
    // exist in list

    //TODO: Basic Test for Provider

    //TODO: Validation Tests
    // - Setting WS and WSP
    // - Fail just one in List, But allow successful ones to pass

    /*
    * Method to return the endpoint interface description for a given implementation class.
    */

    //private EndpointInterfaceDescription getEndpointInterfaceDesc(Class implementationClass) {
    //    // Use the description factory directly; this will be done within the JAX-WS runtime
    //    return (new EndpointInterfaceDescription()); 
    //	//return testEndpointInterfaceDesc;
    //}

    static public DescriptionBuilderComposite buildDBCNoEndpointInterface() {

        //Create a WebServiceAnnot
        String WSName = "EchoServiceAnnotated";
        String WSTargetNamespace = "http://description.jaxws.axis2.apache.org/";
        String WSServiceName = "EchoServiceName";
        //String WSWsdlLocation = "http://EchoService/wsdl";
        String WSWsdlLocation = "";
        String WSEndpointInterface = "";
        String WSPortName = "EchoServiceAnnotatedPort";

        WebServiceAnnot webServiceAnnot =
                WebServiceAnnot.createWebServiceAnnotImpl(
                        WSName,
                        WSTargetNamespace,
                        WSServiceName,
                        WSWsdlLocation,
                        WSEndpointInterface,
                        WSPortName);

        //Create a WebMethodAnnot
        String operationName = "echoStringMethod";
        String action = "urn:EchoStringMethod";
        boolean exclude = false;

        WebMethodAnnot webMethodAnnot = WebMethodAnnot.createWebMethodAnnotImpl();
        webMethodAnnot.setOperationName(operationName);
        webMethodAnnot.setAction(action);
        webMethodAnnot.setExclude(exclude);

        //Create the WebParamAnnot
        String WPName = "arg0";
        String WPPartName = "sku";
        String WPTargetNamespace = "http://description.jaxws.axis2.apache.org/";
        WebParam.Mode WPMode = WebParam.Mode.IN;
        boolean WPHeader = true;

        WebParamAnnot webParamAnnot = WebParamAnnot.createWebParamAnnotImpl();
        webParamAnnot.setName(WPName);
        webParamAnnot.setPartName(WPPartName);
        webParamAnnot.setMode(WPMode);
        webParamAnnot.setTargetNamespace(WPTargetNamespace);
        webParamAnnot.setHeader(WPHeader);

        //Build up the the DBC and all necessary composites
        ParameterDescriptionComposite pdc = new ParameterDescriptionComposite();
        pdc.setParameterType("java.lang.String");
        pdc.setWebParamAnnot(webParamAnnot);

        MethodDescriptionComposite mdc = new MethodDescriptionComposite();
        mdc.setWebMethodAnnot(webMethodAnnot);
        mdc.setMethodName(operationName);
        mdc.addParameterDescriptionComposite(pdc, 0);

        DescriptionBuilderComposite dbc = new DescriptionBuilderComposite();
        dbc.setClassName("org.apache.axis2.samples.EchoServiceAnnotated");
        dbc.setWebServiceAnnot(webServiceAnnot);
        dbc.addMethodDescriptionComposite(mdc);

        return dbc;
    }
}



