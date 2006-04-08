package org.apache.axis2.wsdl;

import org.apache.axis2.namespace.Constants;

import javax.xml.namespace.QName;
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

public interface WSDLConstants {

    String STYLE_RPC = "rpc";
    String STYLE_DOC = "document";
    String STYLE_MSG = "msg";
    /**
     * Field WSDL2_0_NAMESPACE
     */
    public static final String WSDL2_0_NAMESPACE = Constants.NS_URI_WSDL20;

    /**
     * Field WSDL1_1_NAMESPACE
     */
    public static final String WSDL1_1_NAMESPACE = Constants.NS_URI_WSDL11;


    /**
     * Field WSDL_MESSAGE_DIRECTION_IN
     */
    public static final String WSDL_MESSAGE_DIRECTION_IN = "in";


    /**
     * Field WSDL_MESSAGE_DIRECTION_OUT
     */
    public static final String WSDL_MESSAGE_DIRECTION_OUT = "out";

    // ////////////////////////////////////////////////////////////////
    // /////////////Message Exchange Pattern Constants/////////////////
    // ////////////////////////////////////////////////////////////////

    /**
     * Field MEP_URI_IN_ONLY
     */
    public static final String MEP_URI_IN_ONLY = "http://www.w3.org/2004/08/wsdl/in-only";

    public static final int MEP_CONSTANT_IN_ONLY = 10;

    /**
     * Field MEP_URI_ROBUST_IN_ONLY
     */
    public static final String MEP_URI_ROBUST_IN_ONLY = "http://www.w3.org/2004/08/wsdl/robust-in-only";

    public static final int MEP_CONSTANT_ROBUST_IN_ONLY = 11;

    /**
     * Field MEP_URI_IN_OUT
     */
    public static final String MEP_URI_IN_OUT = "http://www.w3.org/2004/08/wsdl/in-out";

    public static final int MEP_CONSTANT_IN_OUT = 12;

    /**
     * Field MEP_URI_IN_OPTIONAL_OUT
     */
    public static final String MEP_URI_IN_OPTIONAL_OUT = "http://www.w3.org/2004/08/wsdl/in-opt-out";

    public static final int MEP_CONSTANT_IN_OPTIONAL_OUT = 13;

    /**
     * Field MEP_URI_OUT_ONLY
     */
    public static final String MEP_URI_OUT_ONLY = "http://www.w3.org/2004/08/wsdl/out-only";

    public static final int MEP_CONSTANT_OUT_ONLY = 14;

    /**
     * Field MEP_URI_ROBUST_OUT_ONLY
     */
    public static final String MEP_URI_ROBUST_OUT_ONLY = "http://www.w3.org/2004/08/wsdl/robust-out-only";

    public static final int MEP_CONSTANT_ROBUST_OUT_ONLY = 15;

    /**
     * Field MEP_URI_OUT_IN
     */
    public static final String MEP_URI_OUT_IN = "http://www.w3.org/2004/08/wsdl/out-in";

    public static final int MEP_CONSTANT_OUT_IN = 16;

    /**
     * Field MEP_URI_OUT_OPTIONL_IN
     */
    public static final String MEP_URI_OUT_OPTIONAL_IN = "http://www.w3.org/2004/08/wsdl/out-opt-in";

    public static final int MEP_CONSTANT_OUT_OPTIONAL_IN = 17;


    public static final int MEP_CONSTANT_INVALID = -1;

    //////////////////////////////////////////////////
    //////////////// Message Labels///////////////////
    //////////////////////////////////////////////////

    /**
     * Constant to represent the message label "In" which is used by the
     * following WSDL 2.0 defined MEPs: In-Only, Robust In-Only, In-Out,
     * In-Optional-Out, Out-In, Out-Optional-In.
     */
    public static final byte MESSAGE_LABEL_IN = 0;

    public static final String MESSAGE_LABEL_IN_VALUE = "In";
    public static final String MESSAGE_LABEL_FAULT_VALUE = "Fault";

    /**
     * Constant to represent the message label "Out" which is used by the
     * following WSDL 2.0 defined MEPs: In-Out, In-Optional-Out, Out-Only,
     * Robust Out-Only, Out-In, Out-Optional-In.
     */
    public static final int MESSAGE_LABEL_OUT = 1;

    public static final String MESSAGE_LABEL_OUT_VALUE = "Out";

    /**
     *
     */
    public static final String WSDL_USE_LITERAL = "literal";
    public static final String WSDL_USE_ENCODED = "encoded";


    int WSDL_1_1 = 1;
    int WSDL_2_0 = 2;

    /**
     * The Type name for the SOAP Address defined in the Port/Endpoint
     */
    public static final QName SOAP_11_ADDRESS = new QName(
            Constants.URI_WSDL11_SOAP, "address");
    public static final QName SOAP_12_ADDRESS = new QName(
            Constants.URI_WSDL12_SOAP, "address");


    public static final QName SOAP_11_OPERATION = new QName(
            Constants.URI_WSDL11_SOAP, "operation");
    public static final QName SOAP_12_OPERATION = new QName(
            Constants.URI_WSDL12_SOAP, "operation");

    public static final QName SCHEMA = new QName(
            Constants.URI_2001_SCHEMA_XSD, "schema");

    public static final QName SOAP_11_BODY = new QName(
            Constants.URI_WSDL11_SOAP, "body");
    public static final QName SOAP_12_BODY = new QName(
            Constants.URI_WSDL12_SOAP, "body");

    public static final QName SOAP_11_HEADER = new QName(
            Constants.URI_WSDL11_SOAP, "header");
    public static final QName SOAP_12_HEADER = new QName(
            Constants.URI_WSDL12_SOAP, "header");

    public static final QName SOAP_11_BINDING = new QName(
            Constants.URI_WSDL11_SOAP, "binding");
    public static final QName SOAP_12_BINDING = new QName(
            Constants.URI_WSDL12_SOAP, "binding");

    public static final QName POLICY = new QName(
            Constants.URI_POLICY, "Policy");
    public static final QName POLICY_REFERENCE = new QName(
            Constants.URI_POLICY, "PolicyReference");
}
