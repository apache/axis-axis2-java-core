/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
package test.wsdl.wombuilder;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.wsdl.WSDLException;

import junit.framework.TestCase;

import org.apache.wsdl.wom.WSDLDefinitions;
import org.apache.wsdl.wsdltowom.WOMBuilder;
import org.apache.wsdl.wsdltowom.WOMBuilderFactory;

/**
 * @author chathura@opensource.lk
 *
 */
public class WSDL1BuilderTest extends TestCase {

    public void testBuilderFactory() throws IOException, WSDLException{
        InputStream in = new FileInputStream(new File("./samples/InteropTest.wsdl"));
        WOMBuilder builder = WOMBuilderFactory.getBuilder(in);
        InputStream in1 = new FileInputStream(new File("./samples/InteropTest.wsdl"));
        try {
            WSDLDefinitions wsdlDoc = builder.build(in1);
        } catch (UnsupportedOperationException e) {
            
            System.out.println("To be implemented stuff remain msg: " +e.getMessage());
            // TODO Auto-generated catch block
           // e.printStackTrace();
        }
         
    }
}
