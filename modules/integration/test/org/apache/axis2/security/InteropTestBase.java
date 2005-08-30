///*
//* Copyright 2004,2005 The Apache Software Foundation.
//*
//* Licensed under the Apache License, Version 2.0 (the "License");
//* you may not use this file except in compliance with the License.
//* You may obtain a copy of the License at
//*
//*      http://www.apache.org/licenses/LICENSE-2.0
//*
//* Unless required by applicable law or agreed to in writing, software
//* distributed under the License is distributed on an "AS IS" BASIS,
//* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//* See the License for the specific language governing permissions and
//* limitations under the License.
//*/
//
//package org.apache.axis2.security;
//
//import junit.framework.TestCase;
//
//import org.apache.axis2.Constants;
//import org.apache.axis2.integration.UtilServer;
///**
// *
// * @author Ruchith Fernando (ruchith.fernando@gmail.com)
// */
//public class InteropTestBase extends TestCase {
//
//    protected static final String SCENARIO1_SERVICE_REPOSITORY = "scenario1_service_repo";
//
//    protected static final String SCENARIO1_CLIENT_REPOSITORY = "scenario1_client_repo";
//
//    protected static final String SCENARIO2_SERVICE_REPOSITORY = "scenario2_service_repo";
//
//    protected static final String SCENARIO2_CLIENT_REPOSITORY = "scenario2_client_repo";
//
//    protected static final String SCENARIO2a_SERVICE_REPOSITORY = "scenario2a_service_repo";
//
//    protected static final String SCENARIO2a_CLIENT_REPOSITORY = "scenario2a_client_repo";
//
//    protected static final String SCENARIO3_SERVICE_REPOSITORY = "scenario3_service_repo";
//
//    protected static final String SCENARIO3_CLIENT_REPOSITORY = "scenario3_client_repo";
//
//    protected static final String SCENARIO4_SERVICE_REPOSITORY = "scenario4_service_repo";
//
//    protected static final String SCENARIO4_CLIENT_REPOSITORY = "scenario4_client_repo";
//
//    protected static final String SCENARIO5_SERVICE_REPOSITORY = "scenario5_service_repo";
//
//    protected static final String SCENARIO5_CLIENT_REPOSITORY = "scenario5_client_repo";
//
//    protected static final String SCENARIO6_SERVICE_REPOSITORY = "scenario6_service_repo";
//
//    protected static final String SCENARIO6_CLIENT_REPOSITORY = "scenario6_client_repo";
//
//    protected static final String SCENARIO7_SERVICE_REPOSITORY = "scenario7_service_repo";
//
//    protected static final String SCENARIO7_CLIENT_REPOSITORY = "scenario7_client_repo";
//
//    /*
//     * We have to create different a client repository and a service repository
//     * for each scenarion since we dont have the support to get the parameter
//     * values off the service.xml yet
//     */
//    private String serviceRepo;
//
//    private String clientRepo;
//
//    private String targetEpr = "http://127.0.0.1:" +
//    		UtilServer.TESTING_PORT +
//    	"/axis2/services/PingPort";
//
//	public InteropTestBase() {
//		super();
//	}
//
//	public InteropTestBase(String arg0) {
//		super(arg0);
//	}
//
//	/**
//	 * set up the service
//	 */
//	protected void setUp() throws Exception {
//		UtilServer.start(Constants.TESTING_PATH + serviceRepo);
//	}
//
//	/**
//	 * Cleanup
//	 */
//	protected void tearDown() throws Exception {
//        UtilServer.stop();
//    }
//
//	protected void setClientRepo(String clientRepo) {
//		this.clientRepo = clientRepo;
//	}
//
//	public String getClientRepo() {
//		return clientRepo;
//	}
//
//	protected void setServiceRepo(String serviceRepo) {
//		this.serviceRepo = serviceRepo;
//	}
//
//	/**
//	 * Do test
//	 */
//    public void testInterop() {
//    	try {
//    		InteropScenarioClient.main(new String[]{Constants.TESTING_PATH + clientRepo,targetEpr});
//    	} catch (Exception e) {
//    		e.printStackTrace();
//    		fail("Error in introperating with " + targetEpr + ", client configuration: " + clientRepo);
//    	}
//    }
//
//}
