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
package org.apache.axis2.jaxws.provider;

import java.awt.*;
import java.io.File;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;

import org.apache.axiom.attachments.ByteArrayDataSource;
import org.test.mtom.ImageDepot;
import org.test.mtom.ObjectFactory;
import org.test.mtom.SendImage;
import org.test.mtom.SendImageResponse;

/**
 * The intended purpose of this testcase is to test the MTOM functions in Axis2. 
 * It demostrate an alternative way of sending an attachment using DataHandler.
 * 
 * This testcase uses a JAXWS Dispatch invocation with JAXB generated request object
 * as parameter. The endpoint for these testcase is a JAXWS Source Provider.
 * 
 * These JAXB generated artifacts is based on jaxws\test-resources\xsd\samplemtom.xsd
 * schema.
 * 
 * Available Content types are:
 *       "image/gif"
 *       "image/jpeg"
 *       "text/plain"
 *       "multipart/*"
 *       "text/xml"
 *       "application/xml"
 * This initial testcase only covers the "multipart/*" and  "text/plain" mime types.
 * The ultimate goal is to provide testcases for the remaining mime types. 
 *
 */
public class JAXBProviderTests extends ProviderTestCase {

    String endpointUrl = "http://localhost:8080/axis2/services/JAXBProviderService";
    private QName serviceName = new QName("http://ws.apache.org/axis2", "JAXBProviderService");
    DataSource stringDS, imageDS;
    
    protected void setUp() throws Exception {
        super.setUp();
        
        //Create a DataSource from a String
        String string = "Sending a JAXB generated string object to Source Provider endpoint";
        stringDS = new ByteArrayDataSource(string.getBytes(),"text/plain");
    	
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
    	ImageInputStream fiis = new FileImageInputStream(file);
    	Image image = ImageIO.read(fiis);
    	imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
    	
    }

    protected void tearDown() throws Exception {
            super.tearDown();
    }
    
    public JAXBProviderTests(String name) {
        super(name);
    }
    
    /**
     * test String
     * @throws Exception
     */
    public void testMTOMAttachmentString() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(stringDS);
    	
        //Store the data handler in ImageDepot bean
    	ImageDepot imageDepot = new ObjectFactory().createImageDepot();
    	imageDepot.setImageData(dataHandler);
        
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, endpointUrl);
        
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        Dispatch<Object> dispatch = svc
                .createDispatch(portName, jbc, Service.Mode.PAYLOAD);
        
        //Create a request bean with imagedepot bean as value
        ObjectFactory factory = new ObjectFactory();
        SendImage request = factory.createSendImage();
        request.setInput(imageDepot);
        
        System.out.println(">> Invoking Dispatch<Object> JAXBProviderService");
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
        
        System.out.println(">> Response [" + response.toString() + "]");
    }
    
    /**
     * test Image
     * @throws Exception
     */
    public void testMTOMAttachmentImage() throws Exception {
        System.out.println("---------------------------------------");
        System.out.println("test: " + getName());
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
    	
        //Store the data handler in ImageDepot bean
    	ImageDepot imageDepot = new ObjectFactory().createImageDepot();
    	imageDepot.setImageData(dataHandler);
        
        Service svc = Service.create(serviceName);
        svc.addPort(portName, null, endpointUrl);
        
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        Dispatch<Object> dispatch = svc
                .createDispatch(portName, jbc, Service.Mode.PAYLOAD);
        
        //Create a request bean with imagedepot bean as value
        ObjectFactory factory = new ObjectFactory();
        SendImage request = factory.createSendImage();
        request.setInput(imageDepot);
        
        System.out.println(">> Invoking Dispatch<Object> JAXBProviderService");
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
        
        System.out.println(">> Response [" + response.toString() + "]");
    }
}
