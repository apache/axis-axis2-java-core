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
package org.apache.axis2.jaxws.framework;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.log4j.BasicConfigurator;

public class JAXWSTest extends TestCase {
    
    static {
        // Note you will probably need to increase the java heap size, for example
        // -Xmx512m.  This can be done by setting maven.junit.jvmargs in project.properties.
        // To change the settings, edit the log4j.property file
        // in the test-resources directory.
        BasicConfigurator.configure();
    }
    
    /**
     * suite
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        
        // Add each of the test suites
        /*
        suite.addTestSuite(PropertyValueTests.class);
        suite.addTestSuite(ClientConfigTests.class);
        
        suite.addTestSuite(BlockTests.class);
        suite.addTestSuite(MessageTests.class);
        suite.addTestSuite(MessagePersistanceTests.class);
        suite.addTestSuite(MessageContextTests.class);
        suite.addTestSuite(FaultTests.class);
        suite.addTestSuite(SAAJConverterTests.class);
        suite.addTestSuite(MTOMSerializationTests.class);
        suite.addTestSuite(BindingToProtocolTests.class);
        
        // ------ Addressing Tests ------
        suite.addTestSuite(EndpointReferenceUtilsTests.class);
        suite.addTestSuite(ReferenceParameterListTests.class);
        
        // ------ Metadata Tests ------
        suite.addTestSuite(WSDLTests.class);
        suite.addTestSuite(WSDLDescriptionTests.class);
        suite.addTestSuite(AnnotationDescriptionTests.class);
        suite.addTestSuite(GetDescFromBindingProviderTests.class);
        suite.addTestSuite(ServiceTests.class);
        suite.addTestSuite(PortSelectionTests.class);
        
        // ------ Handler Test/Cls ------
        suite.addTestSuite(LogicalMessageContextTests.class);
        suite.addTestSuite(SOAPMessageContextTests.class);
        
        suite.addTestSuite(HandlerPrePostInvokerTests.class);
        suite.addTestSuite(CompositeMessageContextTests.class);
        suite.addTestSuite(HandlerChainProcessorTests.class);
        suite.addTestSuite(HandlerResolverTests.class);
        
        // ------ Message Tests ------
        suite.addTestSuite(JaxwsMessageBundleTests.class);
        suite.addTestSuite(ExceptionFactoryTests.class);

        
        suite.addTestSuite(EndpointLifecycleTests.class);
        suite.addTestSuite(NS2PkgTest.class);
        suite.addTestSuite(SchemaReaderTests.class);
        suite.addTestSuite(BindingProviderTests.class);
        
        // ------ Endpoint Tests ------
        suite.addTestSuite(BasicEndpointTests.class);        
        suite.addTestSuite(JAXWSServerTests.class);
        suite.addTestSuite(SOAP12Tests.class);

*/
       // --------- Integration Tests --------------
        /*
        suite = DispatchTestSuite.addTestSuites(suite);
        suite.addTestSuite(JAXBContextTest.class);
        suite.addTestSuite(SOAP12DispatchTest.class);
        suite.addTestSuite(DispatchSoapActionTest.class);
        suite.addTestSuite(ProxySoapActionTest.class);
        suite.addTestSuite(StringProviderTests.class);
        suite.addTestSuite(SOAPFaultProviderTests.class);
        suite.addTestSuite(StringMessageProviderTests.class);
        suite.addTestSuite(SourceProviderTests.class);
        suite.addTestSuite(SourceMessageProviderTests.class);
        // TODO FIXME: Test fails
        //suite.addTestSuite(SoapMessageProviderTests.class);
        suite.addTestSuite(SoapMessageMUProviderTests.class);
        suite.addTestSuite(JAXBProviderTests.class);
        suite.addTestSuite(ProxyTests.class);
        //TODO: FIXME - Was working, now doesn't
        //suite.addTestSuite(ProxyNonWrappedTests.class);
        suite.addTestSuite(RPCProxyTests.class);
        suite.addTestSuite(RPCLitSWAProxyTests.class);
        suite.addTestSuite(GorillaDLWProxyTests.class);
        suite.addTestSuite(SOAP12ProxyTests.class);
        suite.addTestSuite(BasicAuthSecurityTests.class);

        suite.addTestSuite(AddressBookTests.class);
        //suite.addTestSuite(MtomSampleTests.class);

        // This test fails only on Solaris
        //suite.addTestSuite(MtomSampleByteArrayTests.class);
        suite.addTestSuite(BareTests.class);
        
        // Intermittent failure, logged bug AXIS2-2605
        //suite.addTestSuite(DocLitBareMinTests.class);
        //TODO: FIXME - Was working, now doesn't
        //suite.addTestSuite(NonWrapTests.class);
        suite.addTestSuite(WSGenTests.class);
        suite.addTestSuite(DLWMinTests.class);
        suite.addTestSuite(NonAnonymousComplexTypeTests.class);
        suite.addTestSuite(AddNumbersTests.class);
        suite.addTestSuite(AddNumbersHandlerTests.class);

        // TODO: This test intermittently fails on Linux and with trace enabled.
        //suite.addTestSuite(ParallelAsyncTests.class);
        // TODO: FIXME - Was working, now doesn't
        //suite.addTestSuite(FaultyWebServiceTests.class);
        suite.addTestSuite(FaultsServiceTests.class);
        suite.addTestSuite(ResourceInjectionTests.class);
        suite.addTestSuite(AnyTypeTests.class);
        suite.addTestSuite(PolymorphicTests.class);
        
//MADE IT TO HERE        
        suite.addTestSuite(DispatchXPayloadString.class);
        suite.addTestSuite(DispatchXMessageString.class);
        suite.addTestSuite(DispatchXPayloadSource.class);
        suite.addTestSuite(DispatchXMessageSource.class);
        suite.addTestSuite(DispatchXPayloadJAXB.class);
        suite.addTestSuite(DispatchXMessageDataSource.class);
        suite.addTestSuite(RPCLitEnumTests.class);
        // Commented due to test failure...
        //        suite.addTestSuite(StringListTests.class);
        suite.addTestSuite(RPCLitStringArrayTests.class);
*/
//      END OF Integration Tests
        
        
        // Start (and stop) the server only once for all the tests
        TestSetup testSetup = new TestSetup(suite) {
            public void setUp() {
                TestLogger.logger.debug("Starting the server.");
  //              StartServer startServer = new StartServer("server1");
  //              startServer.testStartServer();
            }
            public void tearDown() {
                TestLogger.logger.debug("Stopping the server");
    //            StopServer stopServer = new StopServer("server1");
     //           stopServer.testStopServer();
            }
        };
        return testSetup;
    }
}
