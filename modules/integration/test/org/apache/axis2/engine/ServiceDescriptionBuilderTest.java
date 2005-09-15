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
 */

package org.apache.axis2.engine;

import junit.framework.TestCase;
import org.apache.axis2.description.AxisDescWSDLComponentFactory;
import org.apache.axis2.description.ServiceDescription;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.wsdl.WSDLDescription;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Iterator;

public class ServiceDescriptionBuilderTest extends TestCase {
    private ServiceDescription service = null;
    private Log log = LogFactory.getLog(getClass());

    private void initialize() throws Exception {

        if (null == this.service) {
            InputStream in = new FileInputStream(
                    new File("./test-resources/service.wsdl"));
            if (in == null) {
                throw new Exception("Input Stream is null , fileNot Found");
            }
            WSDLDescription womDescription = WOMBuilderFactory.getBuilder(
                    WOMBuilderFactory.WSDL11)
                    .build(in, new AxisDescWSDLComponentFactory())
                    .getDescription();
            Iterator iterator = womDescription.getServices().keySet().iterator();
            if (iterator.hasNext()) {
                this.service = (ServiceDescription) iterator.next();
            }
            //todo fix me ajith , deepal
            // assertNotNull(this.service);
        }

    }

    public void test() {
        try {
            this.initialize();
        } catch (Exception e) {
            log.info(e.getMessage());
        }
    }
}

