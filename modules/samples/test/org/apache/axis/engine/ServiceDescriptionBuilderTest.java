package org.apache.axis.engine;

import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.util.Iterator;

import org.apache.axis.description.ServiceDescription;
import org.apache.axis.description.AxisDescWSDLComponentFactory;
import org.apache.axis.wsdl.builder.WOMBuilderFactory;
import org.apache.wsdl.WSDLDescription;
import junit.framework.TestCase;
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
*
*
*/

/**
 * Author : Deepal Jayasinghe
 * Date: Jun 20, 2005
 * Time: 4:48:22 PM
 */
public class ServiceDescriptionBuilderTest extends TestCase {
    private ServiceDescription service = null;

    private void initialize() throws Exception {

        if (null == this.service) {
           InputStream in = new FileInputStream(new File("./test-resources/service.wsdl")) ;
            if(in == null){
                throw new Exception("Input Stream is null , fileNot Found") ;
            }
            WSDLDescription womDescription = WOMBuilderFactory.getBuilder(WOMBuilderFactory.WSDL11).build(in, new AxisDescWSDLComponentFactory());
            Iterator iterator = womDescription.getServices().keySet().iterator();
            if(iterator.hasNext()){
                this.service = (ServiceDescription)iterator.next();
            }
            //todo fix me ajith , deepal
           // assertNotNull(this.service);
        }

    }

    public void test(){
        try {
            this.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

