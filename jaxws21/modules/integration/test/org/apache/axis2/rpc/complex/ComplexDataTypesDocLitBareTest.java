package org.apache.axis2.rpc.complex;

/*
* Copyright 2004,2005 The Apache Software Foundation.
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
*
*
*/
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axiom.attachments.ByteArrayDataSource;
import org.apache.axiom.attachments.utils.IOUtils;
import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.description.java2wsdl.Java2WSDLConstants;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.axis2.integration.UtilServer;
import org.apache.axis2.integration.UtilServerBasedTestCase;
import org.tempuri.complex.xsd.ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub;

import javax.activation.DataHandler;
import javax.xml.namespace.QName;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Calendar;

/*
 *  ComplexDataTypesDocLitBareTest Junit test case
*/

public class ComplexDataTypesDocLitBareTest extends
        UtilServerBasedTestCase {

    protected QName serviceName = new QName("ComplexDataTypesDocLitBare");
    protected AxisConfiguration axisConfiguration;
    protected EndpointReference targetEPR;
    org.tempuri.complex.xsd.ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub stub;

    public ComplexDataTypesDocLitBareTest() {
        super(ComplexDataTypesDocLitBareTest.class.getName());
    }

    public ComplexDataTypesDocLitBareTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return getTestSetup(new TestSuite(ComplexDataTypesDocLitBareTest.class));
    }

    protected void setUp() throws Exception {
        targetEPR =
                new EndpointReference("http://127.0.0.1:"
                        + (UtilServer.TESTING_PORT)
//                        + 8000
                        + "/axis2/services/ComplexDataTypesDocLitBare");
        stub = new org.tempuri.complex.xsd.ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub(null, targetEPR.getAddress());
        String className = "org.tempuri.complex.ComplexDataTypesDocLitBare";
        UtilServer.start();
        Parameter generateBare = new Parameter();
        generateBare.setName(Java2WSDLConstants.DOC_LIT_BARE_PARAMETER);
        generateBare.setValue("true");
        UtilServer.getConfigurationContext().getAxisConfiguration().addParameter(generateBare);
        AxisService service = AxisService.createService(
                className, UtilServer.getConfigurationContext().getAxisConfiguration());
        service.addParameter(generateBare);
        service.setName("ComplexDataTypesDocLitBare");
        service.setClassLoader(Thread.currentThread().getContextClassLoader());
        UtilServer.deployService(service);
    }

    protected void tearDown() throws Exception {
        UtilServer.unDeployService(serviceName);
        UtilServer.unDeployClientService();
    }

    public void testretArrayInt1D() throws java.lang.Exception {
//        assertNull(stub.retArrayInt1D(null));
        stub._getServiceClient().cleanupTransport();
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayInt1D req = new
                ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayInt1D();
        assertNotNull(stub.retArrayInt1D(req));
        stub._getServiceClient().cleanupTransport();
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfint input = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfint();
        input.set_int(new int[]{0, 1, 2});
        req.setInArrayInt1D(input);
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.RetArrayInt1DResult ret = stub.retArrayInt1D(req);
        stub._getServiceClient().cleanupTransport();
        assertNotNull(ret);
        assertNotNull(ret.getRetArrayInt1DResult().get_int());
        assertEquals(ret.getRetArrayInt1DResult().get_int().length, 3);
    }

    /**
     * Auto generated test method
     */
    public void testretStructSNSAS() throws java.lang.Exception {
        //TODO Codegen issue
//        assertNull(stub.retStructSNSAS(null));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InStructSNSAS req =
                new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InStructSNSAS();
        assertNotNull(stub.retStructSNSAS(req));

        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Group input =
                new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Group();
        input.setName("xyz");
        input.setMembers(new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfPerson());
        req.setInStructSNSAS(input);
        assertNotNull(stub.retStructSNSAS(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayDateTime1D() throws java.lang.Exception {
        //TODO , this is a codegen bug
//        assertNull(stub.retArrayDateTime1D(null));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayDateTime1D req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayDateTime1D();
        assertNotNull(stub.retArrayDateTime1D(req));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfNullableOfdateTime input = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfNullableOfdateTime();
        input.setDateTime(new Calendar[]{Calendar.getInstance(), Calendar.getInstance()});
        req.setInArrayDateTime1D(input);
        assertNotNull(stub.retArrayDateTime1D(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayString2D() throws java.lang.Exception {
        //TODO codegen issue
//        assertNull(stub.retArrayString2D(null));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayString2D req =
                new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayString2D();
        assertNotNull(stub.retArrayString2D(req));

        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfArrayOfstring input = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfArrayOfstring();
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfstring a2 = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfstring();
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfstring a1 = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfstring();
        a1.setString(new String[]{"foo", "bar"});
        input.setArrayOfstring(new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfstring[]{a1, a2});
        req.setInArrayString2D(input);
        assertNotNull(stub.retArrayString2D(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayDecimal1D() throws java.lang.Exception {

//        assertNull(stub.retArrayDecimal1D(null));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayDecimal1D req =
                new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayDecimal1D();
        assertNotNull(stub.retArrayDecimal1D(req));

        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfNullableOfdecimal input = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfNullableOfdecimal();
        input.setDecimal(new BigDecimal[]{new BigDecimal(1), new BigDecimal(2)});
        req.setInArrayDecimal1D(input);
        assertNotNull(stub.retArrayDecimal1D(req));
    }

    /**
     * Auto generated test method
     */
    public void testretStructSNSA() throws java.lang.Exception {

//        assertNull(stub.retStructSNSA(null));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InStructSNSA req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InStructSNSA();
        assertNotNull(stub.retStructSNSA(req));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Employee input = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Employee();
        input.setJobID(34);
        input.setBaseDetails(new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Person());
        input.setNumbers(new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfshort());
        input.setHireDate(Calendar.getInstance());
        req.setInStructSNSA(input);
        assertNotNull(stub.retStructSNSA(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayAnyType1D() throws java.lang.Exception {

//        assertNull(stub.retArrayAnyType1D(null));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayAnyType1D req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayAnyType1D();
        assertNotNull(stub.retArrayAnyType1D(req));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfanyType input = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfanyType();

        OMFactory factory = OMAbstractFactory.getOMFactory();
        // lets create the namespace object of the Article element
        OMNamespace ns = factory.createOMNamespace("http://www.ibm.com/developerworks/library/ws-axis2soap/index.html", "article");
        // now create the Article element with the above namespace
        OMElement articleElement = factory.createOMElement("Article", ns);

        input.setAnyType(new OMElement[]{articleElement});
        req.setInArrayAnyType1D(input);
        assertNotNull(stub.retArrayAnyType1D(req));
        //TODOD : Need to fix this , seems like we are not getting the corrcet response
    }


    /**
     * Auto generated test method
     */
    public void testretStructSN() throws java.lang.Exception {

//        assertNull(stub.retStructSN(null));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InStructSN req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InStructSN();
        assertNotNull(stub.retStructSN(req));

        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Person input = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Person();
        input.setAge(23);
        input.setID(345);
        input.setMale(false);
        input.setName("Why?");
        req.setInStructSN(input);
        assertNotNull(stub.retStructSN(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArray1DSN() throws java.lang.Exception {
//TODO Codegen issue
//        assertNull(stub.retArray1DSN(null));

        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArray1DSN req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArray1DSN();
        assertNotNull(stub.retArray1DSN(req));

        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfPerson input = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfPerson();
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Person p1 = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Person();
        p1.setAge(34);
        p1.setID(2345);
        p1.setMale(true);
        p1.setName("HJHJH");
        input.setPerson(new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Person[]{p1});
        req.setInArray1DSN(input);
        assertNotNull(stub.retArray1DSN(req));
        //TODO : Need to fix this , we are not gettin corrcet reponse
    }

    /**
     * Auto generated test method
     */
    public void testretDerivedClass() throws java.lang.Exception {
//        assertNull(stub.retDerivedClass(null));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InDerivedClass req =
                new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InDerivedClass();
        assertNotNull(stub.retDerivedClass(req));

        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Furniture input = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Furniture();
        input.setColor("white");
        input.setPrice(67);
        req.setInDerivedClass(input);
        assertNotNull(stub.retDerivedClass(req));
        //TODO : Need to fix this too
    }
//
//// TODO: We need to figure out how to deal with ENUM's. Please don't remove this section.
////    /**
////     * Auto generated test method
////     */
//    public void testretEnumInt() throws java.lang.Exception {
//
//
//        assertNull(stub.retEnumInt(null));
//        String input = "";
//        assertNotNull(stub.retEnumInt(new String()));
//    }
//
//// TODO: We need to figure out how to deal with ENUM's. Please don't remove this section.
////    /**
////     * Auto generated test method
////     */
////    public void testretEnumString() throws java.lang.Exception {
////
////        org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub stub =
////                new org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub();
////
////        assertNull(stub.retEnumString(null));
////        BitMask input = new BitMask();
////        assertNull(stub.retEnumString(new BitMask()));
////    }
//

    //

    /**
     * Auto generated test method
     */
    public void testretStructS1() throws java.lang.Exception {
//        assertNull(stub.retStructS1(null));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InStructS1 req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InStructS1();
        assertNotNull(stub.retStructS1(req));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Name input = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.Name();
        input.setName("ewrterty");
        req.setInStructS1(input);
        assertNotNull(stub.retStructS1(req));
    }

    /**
     * Auto generated test method
     */
    public void testretArrayString1D() throws java.lang.Exception {
//        assertNull(stub.retArrayString1D(null));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayString1D req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InArrayString1D();
        assertNotNull(stub.retArrayString1D(req));
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfstring input = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.ArrayOfstring();
        input.setString(new String[]{"foo", "bar"});
        req.setInArrayString1D(input);
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.RetArrayString1DResult ret = stub.retArrayString1D(req);
        assertNotNull(ret);
        assertNotNull(ret.getRetArrayString1DResult().getString());
        assertEquals(ret.getRetArrayString1DResult().getString().length, 2);
    }


    /**
     * Auto generated test method
     */
    public void testretSingle() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InSingle req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InSingle();
        req.setInSingle(43.0f);
        float ret = stub.retSingle(req).getRetSingleResult();
        assertTrue(ret == 43.0f);
    }

    /**
     * Auto generated test method
     */
    public void testretDateTime() throws java.lang.Exception {

        Calendar input = Calendar.getInstance();
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InDateTime req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InDateTime();
        req.setInDateTime(input);
        Calendar ret = stub.retDateTime(req).getRetDateTimeResult();
        assertNotNull(ret);
        assertEquals(ret, input);
    }

    /**
     * Auto generated test method
     */
    public void testretGuid() throws java.lang.Exception {

        String input = "12345";
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InGuid req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InGuid();
        req.setInGuid(input);
        String ret = stub.retGuid(req).getRetGuidResult();
        assertEquals(ret, input);
    }

    /**
     * Auto generated test method
     */
    public void testretByteArray() throws java.lang.Exception {


        byte[] input = new byte[]{(byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF};
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.RetByteArray req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.RetByteArray();
        req.setInByteArray(new DataHandler(new ByteArrayDataSource(input)));
        DataHandler ret = stub.retByteArray(req).get_return();
        byte[] bytes = IOUtils.getStreamAsByteArray(ret.getInputStream());
        assertTrue(Arrays.equals(bytes, input));
    }

    /**
     * Auto generated test method
     */
    public void testretUri() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InUri req =
                new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InUri();
        req.setInUri("124");
        assertNotNull(stub.retUri(req));
    }

    /**
     * Auto generated test method
     */
//    public void testretQName() throws java.lang.Exception {
//        FIXME: Why is QName being mapped to OMElement?
//        assertNull(stub.retQName(null));
//    }

//// TODO: FIXME: Need to figure out how to do enum's. Please don't remove this following section
////    /**
////     * Auto generated test method
////     */
////    public void testretEnumInt() throws java.lang.Exception {
////
////        org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub stub =
////                new org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub();
////
////        org.tempuri.complex.xsd.xsd.RetEnumInt retEnumInt126 =
////                (org.tempuri.complex.xsd.xsd.RetEnumInt) getTestObject(org.tempuri.complex.xsd.xsd.RetEnumInt.class);
////        // todo Fill in the retEnumInt126 here
////
////        assertNotNull(stub.retEnumInt(
////                getParam0(retEnumInt126)
////        ));
////
////
////    }
//
    public void testretLong() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InLong req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InLong();
        req.setInLong(34);
        long ret = stub.retLong(req).getRetLongResult();
        assertEquals(34, ret);
    }

    //
    /**
     * Auto generated test method
     */
    public void testretUShort() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InUShort req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InUShort();
        req.setInUShort(34);
        int ret = stub.retUShort(req).getRetUShortResult();
        assertEquals(34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretUInt() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InUInt req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InUInt();
        req.setInUInt(34);
        long ret = stub.retUInt(req).getRetUIntResult();
        assertEquals(34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretByte() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InByte req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InByte();
        req.setInByte((short) 34);
        short ret = stub.retByte(req).getRetByteResult();
        assertEquals((short) 34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretSByte() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InSByte req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InSByte();
        req.setInSByte((byte) 34);
        byte ret = stub.retSByte(req).getRetSByteResult();
        assertEquals((byte) 34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretShort() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InShort req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InShort();
        req.setInShort((short) 34);
        short ret = stub.retShort(req).getRetShortResult();

        assertEquals((short) 34, ret);
    }

    //
    /**
     * Auto generated test method
     */
    public void testretObject() throws java.lang.Exception {

        OMFactory factory = OMAbstractFactory.getOMFactory();
        // lets create the namespace object of the Article element
        OMNamespace ns = factory.createOMNamespace("http://www.ibm.com/developerworks/library/ws-axis2soap/index.html", "article");
        // now create the Article element with the above namespace
        OMElement articleElement = factory.createOMElement("Article", ns);

        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InObject req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InObject();
        req.setInObject(articleElement);
        OMElement ret = stub.retObject(req).getRetObjectResult();
        assertNotNull(ret);
        assertEquals(ret.toString(), articleElement.toString());
    }

    /**
     * Auto generated test method
     */
    public void testretFloat() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InFloat req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InFloat();
        req.setInFloat((float) 34);
        float ret = stub.retFloat(req).getRetFloatResult();
        assertTrue(ret == 34);
    }

    /**
     * Auto generated test method
     */
    public void testretDouble() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InDouble req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InDouble();
        req.setInDouble(34);
        double ret = stub.retDouble(req).getRetDoubleResult();
        assertTrue(ret == 34);
    }

    /**
     * Auto generated test method
     */
    public void testretBool() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InBool req =
                new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InBool();
        req.setInBool(true);
        boolean ret = stub.retBool(req).getRetBoolResult();
        assertTrue(ret);
    }

    /**
     * Auto generated test method
     */
    public void testretDecimal() throws java.lang.Exception {

        BigDecimal input = new BigDecimal(12334);
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InDecimal req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InDecimal();
        req.setInDecimal(input);
        BigDecimal ret = stub.retDecimal(req).getRetDecimalResult();
        assertNotNull(ret);
        assertEquals(ret, input);
    }

// TODO: FIXME: Need to figure out how to do enum's. Please don't remove this following section
////    /**
////     * Auto generated test method
////     */
////    public void testretEnumString() throws java.lang.Exception {
////
////        org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub stub =
////                new org.tempuri.complex.xsd.ComplexDataTypesComplexDataTypesSOAP11Port_httpStub();
////
////        org.tempuri.complex.xsd.xsd.RetEnumString retEnumString198 =
////                (org.tempuri.complex.xsd.xsd.RetEnumString) getTestObject(org.tempuri.complex.xsd.xsd.RetEnumString.class);
////        // todo Fill in the retEnumString198 here
////
////        assertNotNull(stub.retEnumString(
////                getParam0(retEnumString198)
////        ));
////
////
////    }

    //
    /**
     * Auto generated test method
     */
    public void testretInt() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InInt req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InInt();
        req.setInInt(34);
        int ret = stub.retInt(req).getRetIntResult();
        assertEquals((int) 34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretInts() throws java.lang.Exception {

        int[] input = new int[]{34, 45};
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.RetInts req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.RetInts();
        req.setInInt(input);
        int ret[] = stub.retInts(req).get_return();
        assertTrue(Arrays.equals(input, ret));
    }

    /**
     * Auto generated test method
     */
    public void testretChar() throws java.lang.Exception {
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InChar req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InChar();
        req.setInChar(34);
        int ret = stub.retChar(req).getRetCharResult();
        assertEquals(34, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretString() throws java.lang.Exception {
        String input = "Abracadabra";
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InString req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InString();
        req.setInString(input);
        String ret = stub.retString(req).getRetStringResult();
        assertNotNull(ret);
        assertEquals(input, ret);
    }

    /**
     * Auto generated test method
     */
    public void testretStrings() throws java.lang.Exception {

        String[] ret;
        String[] input = new String[]{"Abracadabra"};
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.RetStrings req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.RetStrings();
        req.setInString(input);
        ret = stub.retStrings(req).get_return();
        assertNotNull(ret);
        ret = stub.retStrings(req).get_return();
        assertNotNull(ret);
        assertTrue(Arrays.equals(input, ret));
        input = new String[]{"Abracadabra", null, "abc"};
        req.setInString(input);
        ret = stub.retStrings(req).get_return();
        assertNotNull(ret);
        assertTrue(Arrays.equals(input, ret));

        input = new String[]{};
        req.setInString(input);
        ret = stub.retStrings(req).get_return();
        assertNull(ret);
    }

    /**
     * Auto generated test method
     */
    public void testretULong() throws java.lang.Exception {

        BigInteger input = new BigInteger("34");
        ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InULong req = new ComplexDataTypesDocLitBareComplexDataTypesDocLitBareSOAP11Port_httpStub.InULong();
        req.setInULong(input);
        BigInteger ret = stub.retULong(req).getRetULongResult();
        assertEquals(input, ret);
    }


}

