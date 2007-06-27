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
package org.apache.axis2.jaxws.framework;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.apache.axis2.jaxws.addressing.util.EndpointReferenceConverterTests;
import org.apache.axis2.jaxws.anytype.tests.AnyTypeTests;
import org.apache.axis2.jaxws.attachments.MTOMSerializationTests;
import org.apache.axis2.jaxws.client.ClientConfigTests;
import org.apache.axis2.jaxws.client.DispatchSoapActionTests;
import org.apache.axis2.jaxws.client.PropertyValueTests;
import org.apache.axis2.jaxws.client.ProxySoapActionTests;
import org.apache.axis2.jaxws.databinding.BindingToProtocolTests;
import org.apache.axis2.jaxws.description.AnnotationDescriptionTests;
import org.apache.axis2.jaxws.description.GetDescFromBindingProviderTests;
import org.apache.axis2.jaxws.description.PortSelectionTests;
import org.apache.axis2.jaxws.description.ServiceTests;
import org.apache.axis2.jaxws.description.WSDLDescriptionTests;
import org.apache.axis2.jaxws.description.WSDLTests;
import org.apache.axis2.jaxws.dispatch.DispatchTestSuite;
import org.apache.axis2.jaxws.dispatch.SOAP12Dispatch;
import org.apache.axis2.jaxws.endpoint.BasicEndpointTests;
import org.apache.axis2.jaxws.exception.ExceptionFactoryTests;
import org.apache.axis2.jaxws.handler.HandlerChainProcessorTests;
import org.apache.axis2.jaxws.handler.context.LogicalMessageContextTests;
import org.apache.axis2.jaxws.i18n.JaxwsMessageBundleTests;
import org.apache.axis2.jaxws.injection.ResourceInjectionTests;
import org.apache.axis2.jaxws.lifecycle.EndpointLifecycleTests;
import org.apache.axis2.jaxws.message.BlockTests;
import org.apache.axis2.jaxws.message.FaultTests;
import org.apache.axis2.jaxws.message.MessageTests;
import org.apache.axis2.jaxws.message.SAAJConverterTests;
import org.apache.axis2.jaxws.message.SOAP12Tests;
import org.apache.axis2.jaxws.message.XMLStreamReaderSplitterTests;
import org.apache.axis2.jaxws.misc.JAXBContextTest;
import org.apache.axis2.jaxws.misc.NS2PkgTest;
import org.apache.axis2.jaxws.nonanonymous.complextype.NonAnonymousComplexTypeTests;
import org.apache.axis2.jaxws.polymorphic.shape.tests.PolymorphicTests;
import org.apache.axis2.jaxws.provider.JAXBProviderTests;
import org.apache.axis2.jaxws.provider.SOAPFaultProviderTests;
import org.apache.axis2.jaxws.provider.SoapMessageProviderTests;
import org.apache.axis2.jaxws.provider.SourceMessageProviderTests;
import org.apache.axis2.jaxws.provider.SourceProviderTests;
import org.apache.axis2.jaxws.provider.StringMessageProviderTests;
import org.apache.axis2.jaxws.provider.StringProviderTests;
import org.apache.axis2.jaxws.proxy.GorillaDLWProxyTests;
import org.apache.axis2.jaxws.proxy.ProxyNonWrappedTests;
import org.apache.axis2.jaxws.proxy.ProxyTests;
import org.apache.axis2.jaxws.proxy.RPCProxyTests;
import org.apache.axis2.jaxws.proxy.SOAP12ProxyTests;
import org.apache.axis2.jaxws.rpclit.enumtype.tests.RPCLitEnumTests;
import org.apache.axis2.jaxws.rpclit.stringarray.tests.RPCLitStringArrayTests;
import org.apache.axis2.jaxws.sample.AddNumbersHandlerTests;
import org.apache.axis2.jaxws.sample.AddNumbersTests;
import org.apache.axis2.jaxws.sample.AddressBookTests;
import org.apache.axis2.jaxws.sample.BareTests;
import org.apache.axis2.jaxws.sample.DLWMinTests;
import org.apache.axis2.jaxws.sample.DocLitBareMinTests;
import org.apache.axis2.jaxws.sample.FaultsServiceTests;
import org.apache.axis2.jaxws.sample.FaultyWebServiceTests;
import org.apache.axis2.jaxws.sample.MtomSampleByteArrayTests;
import org.apache.axis2.jaxws.sample.MtomSampleTests;
import org.apache.axis2.jaxws.sample.NonWrapTests;
import org.apache.axis2.jaxws.sample.StringListTests;
import org.apache.axis2.jaxws.sample.WSGenTests;
import org.apache.axis2.jaxws.sample.WrapTests;
import org.apache.axis2.jaxws.security.BasicAuthSecurityTests;
import org.apache.axis2.jaxws.spi.BindingProviderTests;
import org.apache.axis2.jaxws.wsdl.schemareader.SchemaReaderTests;
import org.apache.axis2.jaxws.xmlhttp.clientTests.dispatch.datasource.DispatchXMessageDataSource;
import org.apache.axis2.jaxws.xmlhttp.clientTests.dispatch.jaxb.DispatchXPayloadJAXB;
import org.apache.axis2.jaxws.xmlhttp.clientTests.dispatch.source.DispatchXMessageSource;
import org.apache.axis2.jaxws.xmlhttp.clientTests.dispatch.source.DispatchXPayloadSource;
import org.apache.axis2.jaxws.xmlhttp.clientTests.dispatch.string.DispatchXMessageString;
import org.apache.axis2.jaxws.xmlhttp.clientTests.dispatch.string.DispatchXPayloadString;
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
        suite = DispatchTestSuite.addTestSuites(suite);
        suite.addTestSuite(SOAP12Dispatch.class);
        suite.addTestSuite(DispatchSoapActionTests.class);
        suite.addTestSuite(ProxySoapActionTests.class);
        suite.addTestSuite(PropertyValueTests.class);
        suite.addTestSuite(ClientConfigTests.class);
        
        suite.addTestSuite(BlockTests.class);
        suite.addTestSuite(MessageTests.class);
        suite.addTestSuite(FaultTests.class);
        suite.addTestSuite(SAAJConverterTests.class);
        suite.addTestSuite(XMLStreamReaderSplitterTests.class);
        suite.addTestSuite(SOAP12Tests.class);
        suite.addTestSuite(MTOMSerializationTests.class);
        suite.addTestSuite(BindingToProtocolTests.class);
        
        // ------ Addressing Tests ------
        suite.addTestSuite(EndpointReferenceConverterTests.class);
        
        // ------ Metadata Tests ------
        suite.addTestSuite(WSDLTests.class);
        suite.addTestSuite(WSDLDescriptionTests.class);
        suite.addTestSuite(AnnotationDescriptionTests.class);
        suite.addTestSuite(GetDescFromBindingProviderTests.class);
        suite.addTestSuite(ServiceTests.class);
        suite.addTestSuite(PortSelectionTests.class);
        
        // ------ Handler Tests ------
        suite.addTestSuite(LogicalMessageContextTests.class);
        suite.addTestSuite(HandlerChainProcessorTests.class);
        
        // ------ Message Tests ------
        suite.addTestSuite(JaxwsMessageBundleTests.class);
        
        suite.addTestSuite(StringProviderTests.class);
        suite.addTestSuite(SOAPFaultProviderTests.class);
        suite.addTestSuite(StringMessageProviderTests.class);
        suite.addTestSuite(SourceProviderTests.class);
        suite.addTestSuite(SourceMessageProviderTests.class);
        // TODO FIXME: Test fails
        //suite.addTestSuite(SoapMessageProviderTests.class);
        suite.addTestSuite(JAXBProviderTests.class);
        suite.addTestSuite(ProxyTests.class);
        //TODO: FIXME - Was working, now doesn't
        //suite.addTestSuite(ProxyNonWrappedTests.class);
        suite.addTestSuite(RPCProxyTests.class);
        suite.addTestSuite(GorillaDLWProxyTests.class);
        suite.addTestSuite(SOAP12ProxyTests.class);
        suite.addTestSuite(ExceptionFactoryTests.class);
        suite.addTestSuite(BasicAuthSecurityTests.class);

        suite.addTestSuite(AddressBookTests.class);
        suite.addTestSuite(MtomSampleTests.class);
        
        // This test fails only on Solaris
        //suite.addTestSuite(MtomSampleByteArrayTests.class);
        suite.addTestSuite(BareTests.class);
        // Intermittent failure, logged bug AXIS2-2605
        //suite.addTestSuite(DocLitBareMinTests.class);
        //TODO: FIXME - Was working, now doesn't
        //suite.addTestSuite(NonWrapTests.class);
        suite.addTestSuite(WSGenTests.class);
        suite.addTestSuite(WrapTests.class);
        suite.addTestSuite(DLWMinTests.class);
        suite.addTestSuite(NonAnonymousComplexTypeTests.class);
        suite.addTestSuite(AddNumbersTests.class);
        suite.addTestSuite(AddNumbersHandlerTests.class);
        
        // TODO: This test intermittently fails on Linux and with trace enabled.
        //suite.addTestSuite(ParallelAsyncTests.class);
        // TODO: FIXME - Was working, now doesn't
        //suite.addTestSuite(FaultyWebServiceTests.class);
        suite.addTestSuite(FaultsServiceTests.class);

        suite.addTestSuite(EndpointLifecycleTests.class);
        suite.addTestSuite(ResourceInjectionTests.class);
        suite.addTestSuite(AnyTypeTests.class);
        suite.addTestSuite(PolymorphicTests.class);
        suite.addTestSuite(NS2PkgTest.class);
        suite.addTestSuite(JAXBContextTest.class);
        
        suite.addTestSuite(DispatchXPayloadString.class);
        suite.addTestSuite(DispatchXMessageString.class);
        suite.addTestSuite(DispatchXPayloadSource.class);
        suite.addTestSuite(DispatchXMessageSource.class);
        suite.addTestSuite(DispatchXPayloadJAXB.class);
        suite.addTestSuite(DispatchXMessageDataSource.class);
        suite.addTestSuite(SchemaReaderTests.class);
        suite.addTestSuite(RPCLitEnumTests.class);
        suite.addTestSuite(BindingProviderTests.class);
        // Commented due to test failure...
//        suite.addTestSuite(StringListTests.class);
        suite.addTestSuite(RPCLitStringArrayTests.class);
        // ------ Endpoint Tests ------
        suite.addTestSuite(BasicEndpointTests.class);

        // Start (and stop) the server only once for all the tests
        TestSetup testSetup = new TestSetup(suite) {
            public void setUp() {
                TestLogger.logger.debug("Starting the server.");
                StartServer startServer = new StartServer("server1");
                startServer.testStartServer();
            }
            public void tearDown() {
                TestLogger.logger.debug("Stopping the server");
                StopServer stopServer = new StopServer("server1");
                stopServer.testStopServer();
            }
        };
        return testSetup;
    }
}
