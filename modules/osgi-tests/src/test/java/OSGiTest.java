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
import static org.junit.Assert.assertTrue;
import static org.ops4j.pax.exam.CoreOptions.junitBundles;
import static org.ops4j.pax.exam.CoreOptions.options;
import static org.ops4j.pax.exam.CoreOptions.url;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Configuration;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.PaxExam;
import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
import org.ops4j.pax.exam.spi.reactors.PerClass;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

@RunWith(PaxExam.class)
@ExamReactorStrategy(PerClass.class)
public class OSGiTest {
    @Configuration
    public static Option[] configuration() {
        return options(
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
                url("link:classpath:org.apache.axis2.osgi.link"),
                junitBundles());
    }
    
    @Inject
    private BundleContext context;
    
    @Test
    public void test() {
        boolean found = false;
        for (Bundle bundle : context.getBundles()) {
            if (bundle.getSymbolicName().equals("org.apache.axis2.osgi")) {
                found = true;
                Assert.assertEquals(Bundle.ACTIVE, bundle.getState());
                break;
            }
        }
        assertTrue(found);
    }
}
