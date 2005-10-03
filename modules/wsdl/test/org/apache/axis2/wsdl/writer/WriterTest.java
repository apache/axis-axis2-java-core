package org.apache.axis2.wsdl.writer;

import junit.framework.TestCase;
import org.apache.wsdl.WSDLDescription;
import org.apache.axis2.wsdl.builder.WOMBuilderFactory;
import org.apache.axis2.wsdl.builder.WOMBuilder;
import org.apache.axis2.wsdl.WSDLConstants;

import java.io.FileInputStream;
import java.io.File;
import java.io.PrintStream;
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

public class WriterTest extends TestCase {

    WSDLDescription description;


    protected void setUp() throws Exception {
        WOMBuilder builder = WOMBuilderFactory.getBuilder(WSDLConstants.WSDL_1_1);
//        this.description = builder.build("test-resources/BookQuote.wsdl").getDescription();
        this.description = builder.build("test-resources/wsat.wsdl").getDescription();
    }


    public void testWriting(){
        WOMWriter writer = WOMWriterFactory.createWriter(WSDLConstants.WSDL_1_1);
        try {
            PrintStream out = System.out;
            writer.writeWOM(this.description,out);

        } catch (WriterException e) {
            e.printStackTrace();
        }
    }

}
