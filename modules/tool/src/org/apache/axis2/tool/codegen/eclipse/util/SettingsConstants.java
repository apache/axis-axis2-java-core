/*
 * Copyright 2004,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.axis2.tool.codegen.eclipse.util;

public interface SettingsConstants {

    // ######################################################################
    //WSDL Selection page constants
    /**
     * The key for storing the WSDL location in the dialog settings of the
     * WSDLFileSelectionPage
     */
    static final String PREF_WSDL_LOCATION = "PREF_WSDL_LOCATION";

    // ######################################################################
    // Tools selection page
    static final String PREF_TOOL_SELECTION_WSDL2JAVA = "PREF_TOOL_SELECTION_WSDL2JAVA";

    static final String PREF_TOOL_SELECTION_JAVA2WSDL = "PREF_TOOL_SELECTION_JAVA2WSDL";

    // ######################################################################
    // Output selection page
    /**
     * The key to store the output location in the settings
     *  
     */
    static final String PREF_OUTPUT_LOCATION = "PREF_OUTPUT_LOCATION";
    static final String PREF_CHECK_BROWSE_PROJECTS = "PREF_CHECK_BROWSE_PROJECTS";

    //Options page constants
    /**
     * Position in the combox for choosing the target programming language. Default is 0
     */
    static final String PREF_LANGUAGE_INDEX = "PREF_LANGUAGE_INDEX";

    /**
     * Three radio buttons: Generate Code for Sync calls, Async and Both. Both is default.
     */
    static final String PREF_RADIO_SYNC_AND_ASYNC = "PREF_RADIO_SYNC_AND_ASYNC";

    /**
     * Three radio buttons: Generate Code for Sync calls, Async and Both. Both is default.
     */
    static final String PREF_RADIO_SYNC_ONLY = "PREF_RADIO_SYNC_ONLY";

    /**
     * Three radio buttons: Generate Code for Sync calls, Async and Both. Both is default.
     */
    static final String PREF_RADIO_ASYNC_ONLY = "PREF_RADIO_ASYNC_ONLY";

    /**
     * Specifies the full qualified package name for the generated source code.
     */
    static final String PREF_PACKAGE_NAME = "PREF_PACKAGE_NAME";

    /**
     * A boolean value whether JUnit test classes are generated or not.
     */
    static final String PREF_CHECK_GENERATE_TESTCASE = "PREF_CHECK_GENERATE_TESTCASE";

    /**
     * A boolean value whether the server-side skeletons are generated or not
     */
    static final String PREF_CHECK_GENERATE_SERVERSIDE = "PREF_CHECK_GENERATE_SERVERSIDE";

    /**
     * A boolean value whether the server-side configuration file for Axis2 (server.xml) will be generated or not.
     */
    static final String PREF_CHECK_GENERATE_SERVERCONFIG = "PREF_CHECK_GENERATE_SERVERCONFIG";
    
    static final String PREF_COMBO_PORTNAME_INDEX = "PREF_TEXT_PORTNAME";
    
    static final String PREF_COMBO_SERVICENAME_INDEX = "PREF_TEXT_SERVICENAME";
    
    static final String PREF_DATABINDER_INDEX = "PREF_DATABINDER_INDEX";
    
    static final String PREF_GEN_ALL = "PREF_GEN_ALL";

    // ##################################################################################
    // Java source file selection page
    static final String JAVA_CLASS_NAME = "JAVA_CLASS_NAME";

    static final String JAVA_CLASS_LOCATION_NAME = "JAVA_CLASS_LOCATION_NAME";
    
    // ##################################################################################
    // Java2wsdl options selection page
    static final String PREF_JAVA_TARGET_NS = "INPUT_WSDL";
    
    static final String PREF_JAVA_TARGET_NS_PREF = "LOCATION_URL";
    static final String PREF_JAVA_SCHEMA_TARGET_NS = "BINDING_NAME";
    static final String PREF_JAVA_SERVICE_NAME = "MODE_INDEX";
    static final String PREF_JAVA_STYLE_INDEX = "STYLE_INDEX";
    static final String PREF_JAVA_SCHEMA_TARGET_NS_PREF = "PORTYPE_NAME";
    
    // ##################################################################################
    //output page
    static final String JAVA_OUTPUT_WSDL_NAME = "OUTPUT_WSDL";
    static final String PREF_JAVA_OUTPUT_WSDL_LOCATION = "OUTPUT_WSDL_LOCATION";
    // ##################################################################################
    // Page constants
     static final int WSDL_2_JAVA_TYPE = 1;
     static final int JAVA_2_WSDL_TYPE = 2;
     static final int UNSPECIFIED_TYPE = 3;
    
    // ##################################################################################
    // WSDL Mode constants
     static final String WSDL_ALL = "All";
     static final String WSDL_INTERFACE_ONLY = "Interface only";
     static final String WSDL_IMPLEMENTATION_ONLY = "Implementation only";
     
     // ###########################################################
     static final String WSDL_STYLE_DOCUMENT="Document";
     static final String WSDL_STYLE_RPC="rpc";
     static final String WSDL_STYLE_WRAPPED="wrapped";
    
}
