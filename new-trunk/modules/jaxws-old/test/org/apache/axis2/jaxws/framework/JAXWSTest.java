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

import org.apache.axis2.jaxws.DispatchTestSuite;
import org.apache.axis2.jaxws.attachments.MTOMSerializationTests;
import org.apache.axis2.jaxws.description.AnnotationDescriptionTests;
import org.apache.axis2.jaxws.description.AnnotationProviderImplDescriptionTests;
import org.apache.axis2.jaxws.description.AnnotationServiceImplDescriptionTests;
import org.apache.axis2.jaxws.description.ServiceDescriptionTests;
import org.apache.axis2.jaxws.description.WSDLDescriptionTests;
import org.apache.axis2.jaxws.description.WSDLTests;
import org.apache.axis2.jaxws.description.builder.DescriptionBuilderTests;
import org.apache.axis2.jaxws.dispatch.SOAP12Dispatch;
import org.apache.axis2.jaxws.exception.ExceptionFactoryTests;
import org.apache.axis2.jaxws.handler.HandlerChainProcessorTests;
import org.apache.axis2.jaxws.i18n.JaxwsMessageBundleTests;
import org.apache.axis2.jaxws.message.BlockTests;
import org.apache.axis2.jaxws.message.MessageTests;
import org.apache.axis2.jaxws.message.SAAJConverterTests;
import org.apache.axis2.jaxws.message.SOAP12Tests;
import org.apache.axis2.jaxws.message.XMLStreamReaderSplitterTests;
import org.apache.axis2.jaxws.provider.*;
import org.apache.axis2.jaxws.sample.AddNumbersTests;
import org.apache.axis2.jaxws.sample.AddressBookTests;
import org.apache.axis2.jaxws.sample.BareTests;
import org.apache.axis2.jaxws.sample.MtomSampleTests;
import org.apache.axis2.jaxws.sample.NonWrapTests;
import org.apache.axis2.jaxws.sample.WrapTests;
import org.apache.axis2.proxy.ProxyNonWrappedTests;
import org.apache.axis2.proxy.ProxyTests;

public class JAXWSTest extends TestCase {
    /**
     * suite
     * @return
     */
    public static Test suite() {
        TestSuite suite = new TestSuite();
        
        // Add each of the test suites
        suite = DispatchTestSuite.addTestSuites(suite);
        suite.addTestSuite(SOAP12Dispatch.class);
        
        suite.addTestSuite(BlockTests.class);
        suite.addTestSuite(MessageTests.class);
        suite.addTestSuite(SAAJConverterTests.class);
        suite.addTestSuite(XMLStreamReaderSplitterTests.class);
        suite.addTestSuite(SOAP12Tests.class);
        suite.addTestSuite(MTOMSerializationTests.class);
        
        suite.addTestSuite(WSDLTests.class);
        suite.addTestSuite(DescriptionBuilderTests.class);
        suite.addTestSuite(ServiceDescriptionTests.class);
        suite.addTestSuite(WSDLDescriptionTests.class);
        suite.addTestSuite(AnnotationDescriptionTests.class);
        suite.addTestSuite(AnnotationServiceImplDescriptionTests.class);
        suite.addTestSuite(AnnotationProviderImplDescriptionTests.class);
        
        suite.addTestSuite(HandlerChainProcessorTests.class);
        suite.addTestSuite(JaxwsMessageBundleTests.class);
        
        suite.addTestSuite(StringProviderTests.class);
        suite.addTestSuite(StringMessageProviderTests.class);
        suite.addTestSuite(SourceProviderTests.class);
        suite.addTestSuite(SourceMessageProviderTests.class);
        suite.addTestSuite(SoapMessageProviderTests.class);
        suite.addTestSuite(JAXBProviderTests.class);
        suite.addTestSuite(ProxyTests.class);
        suite.addTestSuite(ProxyNonWrappedTests.class);
        suite.addTestSuite(ExceptionFactoryTests.class);

        suite.addTestSuite(AddressBookTests.class);
        suite.addTestSuite(MtomSampleTests.class);
        suite.addTestSuite(NonWrapTests.class);
        suite.addTestSuite(WrapTests.class);
        suite.addTestSuite(AddNumbersTests.class);
        suite.addTestSuite(BareTests.class);
        
        // Start (and stop) the server only once for all the tests
        TestSetup testSetup = new TestSetup(suite) {
            public void setUp() {
                System.out.println("Starting the server.");
                StartServer startServer = new StartServer("server1");
                startServer.testStartServer();
            }
            public void tearDown() {
                System.out.println("Stopping the server");
                StopServer stopServer = new StopServer("server1");
                stopServer.testStopServer();
            }
        };
        return testSetup;
    }
}
