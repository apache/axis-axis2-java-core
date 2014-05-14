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
import static org.junit.Assert.assertEquals;
import static org.ops4j.pax.exam.CoreOptions.frameworkProperty;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.provision;
import static org.ops4j.pax.exam.CoreOptions.url;
import static org.ops4j.pax.tinybundles.core.TinyBundles.bundle;

import java.util.concurrent.CountDownLatch;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.addressing.EndpointReference;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.osgi.module.Handler1;
import org.apache.axis2.osgi.module.Handler2;
import org.apache.axis2.osgi.module.SimpleModule;
import org.apache.axis2.osgi.service.Activator;
import org.apache.axis2.osgi.service.Calculator;
import org.apache.axis2.osgi.service.Version;
import org.apache.axis2.testutils.PortAllocator;
import org.apache.felix.framework.FrameworkFactory;
import org.junit.Test;
import org.ops4j.pax.exam.ExamSystem;
import org.ops4j.pax.exam.nat.internal.NativeTestContainer;
import org.ops4j.pax.exam.spi.DefaultExamSystem;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.Constants;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

public class OSGiTest {
    @Test
    public void test() throws Throwable {
        int httpPort = PortAllocator.allocatePort();
        ExamSystem system = DefaultExamSystem.create(options(
                url("link:classpath:META-INF/links/org.ops4j.pax.logging.api.link"),
                url("link:classpath:META-INF/links/org.osgi.compendium.link"),
                url("link:classpath:org.apache.felix.configadmin.link"),
                url("link:classpath:org.apache.servicemix.bundles.wsdl4j.link"),
                url("link:classpath:org.apache.geronimo.specs.geronimo-activation_1.1_spec.link"), // TODO: should not be necessary on Java 6
                url("link:classpath:org.apache.geronimo.specs.geronimo-jms_1.1_spec.link"), // TODO: why the heck is this required???
                url("link:classpath:org.apache.geronimo.specs.geronimo-ws-metadata_2.0_spec.link"),
                url("link:classpath:org.apache.geronimo.specs.geronimo-javamail_1.4_spec.link"), // TODO: should no longer be necessary
                url("link:classpath:org.apache.geronimo.specs.geronimo-servlet_2.5_spec.link"),
                url("link:classpath:org.apache.geronimo.specs.geronimo-jaxrs_1.1_spec.link"), // TODO: shouldn't this be optional???
                url("link:classpath:org.apache.servicemix.specs.stax-api-1.0.link"),
                url("link:classpath:stax2-api.link"),
                url("link:classpath:woodstox-core-asl.link"),
                url("link:classpath:org.apache.james.apache-mime4j-core.link"),
                url("link:classpath:org.apache.ws.commons.axiom.axiom-api.link"),
                url("link:classpath:org.apache.ws.commons.axiom.axiom-impl.link"),
                url("link:classpath:org.apache.commons.fileupload.link"),
                url("link:classpath:org.apache.commons.io.link"),
                url("link:classpath:org.apache.servicemix.bundles.commons-httpclient.link"), // TODO: still necessary???
                url("link:classpath:org.apache.servicemix.bundles.commons-codec.link"), // TODO: still necessary???
                url("link:classpath:org.apache.httpcomponents.httpcore.link"),
                url("link:classpath:org.apache.httpcomponents.httpclient.link"),
                url("link:classpath:org.apache.neethi.link"),
                url("link:classpath:org.apache.woden.core.link"),
                url("link:classpath:org.apache.ws.xmlschema.core.link"),
                url("link:classpath:org.apache.felix.http.jetty.link"),
                url("link:classpath:org.apache.felix.http.whiteboard.link"),
                url("link:classpath:org.apache.axis2.osgi.link"),
                provision(bundle()
                    .add(Handler1.class)
                    .add(Handler2.class)
                    .add(SimpleModule.class)
                    .add("META-INF/module.xml", OSGiTest.class.getResource("/META-INF/module.xml"))
                    .set(Constants.BUNDLE_SYMBOLICNAME, "simple.module")
                    .set(Constants.DYNAMICIMPORT_PACKAGE, "*")
                    .build()),
                provision(bundle()
                    .add(Activator.class)
                    .add(Calculator.class)
                    .add(Version.class)
                    .add("META-INF/services.xml", OSGiTest.class.getResource("/META-INF/services.xml"))
                    .set(Constants.BUNDLE_SYMBOLICNAME, "version.service")
                    .set(Constants.BUNDLE_ACTIVATOR, Activator.class.getName())
                    .set(Constants.DYNAMICIMPORT_PACKAGE, "*")
                    .build()),
                frameworkProperty("org.osgi.service.http.port").value(String.valueOf(httpPort))));
        NativeTestContainer container = new NativeTestContainer(system, new FrameworkFactory());
        container.start();
        try {
            OMFactory factory = OMAbstractFactory.getOMFactory();
            OMElement payload = factory.createOMElement("getVersion", factory.createOMNamespace("http://service.osgi.axis2.apache.org", "ns"));
            Options options = new Options();
            options.setTo(new EndpointReference("http://localhost:" + httpPort + "/services/Version"));
            ServiceClient serviceClient = new ServiceClient();
            serviceClient.setOptions(options);
            OMElement result = serviceClient.sendReceive(payload);
            assertEquals("getVersionResponse", result.getLocalName());
            // Stop the Axis2 bundle explicitly here so that we can test that it cleanly shuts down (see AXIS2-5646)
            stopBundle(getAxis2Bundle(container));
        } finally {
            container.stop();
        }
    }
    
    private static Bundle getAxis2Bundle(NativeTestContainer container) {
        for (Bundle bundle : container.getSystemBundle().getBundleContext().getBundles()) {
            if (bundle.getSymbolicName().equals("org.apache.axis2.osgi")) {
                return bundle;
            }
        }
        throw new Error("Axis2 bundle not found");
    }

    static class Listener implements FrameworkListener, BundleListener {
        private final Bundle bundle;
        private final CountDownLatch latch = new CountDownLatch(1);
        private Throwable throwable;
        
        Listener(Bundle bundle) {
            this.bundle = bundle;
        }

        public void frameworkEvent(FrameworkEvent event) {
            if (event.getType() == FrameworkEvent.ERROR && event.getSource() == bundle && throwable == null) {
                throwable = event.getThrowable();
            }
        }

        public void bundleChanged(BundleEvent event) {
            if (event.getType() == BundleEvent.STOPPED && event.getSource() == bundle) {
                latch.countDown();
            }
        }
        
        void check() throws Throwable {
            latch.await();
            if (throwable != null) {
                throw throwable;
            }
        }
    }
    
    /**
     * Stop the given bundle and throw any exception triggered during the stop operation.
     */
    private static void stopBundle(Bundle bundle) throws Throwable {
        // The listener must be registered on the system bundle; registering it on the bundle
        // passed as parameter won't work because a stopping bundle can't receive asynchronous events.
        BundleContext systemBundleContext = bundle.getBundleContext().getBundle(0).getBundleContext();
        Listener listener = new Listener(bundle);
        // Need a framework listener to intercept errors that would otherwise end up only being logged
        systemBundleContext.addFrameworkListener(listener);
        systemBundleContext.addBundleListener(listener);
        try {
            // Note: the stop method may also throw exceptions
            bundle.stop();
            listener.check();
        } finally {
            systemBundleContext.removeFrameworkListener(listener);
            systemBundleContext.removeBundleListener(listener);
        }
    }
}
