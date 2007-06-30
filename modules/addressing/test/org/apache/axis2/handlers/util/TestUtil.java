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

package org.apache.axis2.handlers.util;

import org.apache.axiom.om.util.StAXUtils;
import org.apache.axiom.soap.impl.builder.StAXSOAPModelBuilder;

import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileReader;

public class TestUtil {

    protected static final String IN_FILE_NAME = "soapmessage.xml";
    protected StAXSOAPModelBuilder builder;
    protected String testResourceDir = System.getProperty("basedir", ".") + "/" + "test-resources";


    public StAXSOAPModelBuilder getOMBuilder(String fileName) throws Exception {
        if ("".equals(fileName) || fileName == null) {
            fileName = IN_FILE_NAME;
        }
        XMLStreamReader parser = StAXUtils
                .createXMLStreamReader(
                        new FileReader(getTestResourceFile(fileName)));
        builder = new StAXSOAPModelBuilder(parser, null);
        return builder;
    }

    protected File getTestResourceFile(String relativePath) {
        return new File(testResourceDir, relativePath);
    }
}
