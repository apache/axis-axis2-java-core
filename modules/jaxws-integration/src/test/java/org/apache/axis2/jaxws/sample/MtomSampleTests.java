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

package org.apache.axis2.jaxws.sample;

import org.apache.axis2.datasource.jaxb.JAXBAttachmentUnmarshallerMonitor;
import org.apache.axis2.jaxws.TestLogger;
import org.apache.axis2.jaxws.provider.DataSourceImpl;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;
import org.test.mtom.ImageDepot;
import org.test.mtom.ObjectFactory;
import org.test.mtom.SendImage;
import org.test.mtom.SendImageResponse;

import jakarta.activation.DataHandler;
import jakarta.activation.DataSource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import jakarta.xml.bind.JAXBContext;
import javax.xml.namespace.QName;
import jakarta.xml.soap.SOAPConstants;
import jakarta.xml.soap.SOAPFault;
import jakarta.xml.ws.Dispatch;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.Service.Mode;
import jakarta.xml.ws.soap.MTOMFeature;
import jakarta.xml.ws.soap.SOAPBinding;
import jakarta.xml.ws.soap.SOAPFaultException;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Image;
import java.io.File;
import java.util.List;

public class MtomSampleTests {
    @ClassRule
    public static final Axis2Server server = new Axis2Server("target/repo");

    private static final QName QNAME_SERVICE = new QName("urn://mtom.test.org", "MtomSampleService");
    private static final QName QNAME_PORT    = new QName("urn://mtom.test.org", "MtomSample");
    
    private static final String IMAGE_DIR = System.getProperty("basedir",".")+"/"+"test-resources"+File.separator+"image";   
    
    private static boolean CHECK_VERSIONMISMATCH = true;
    
    /*
     * Enable attachment Optimization through the SOAPBinding method 
     * -- setMTOMEnabled([true|false])
     * Using SOAP11
     */
    @Test
    public void testSendImageAttachmentAPI11() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING,
                server.getEndpoint("MtomSampleService.MtomSampleServicePort"));
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD);
        
        //Enable attachment optimization
        SOAPBinding binding = (SOAPBinding) dispatch.getBinding();
        binding.setMTOMEnabled(true);
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        // Repeat to verify behavior
        response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
    }
    
    /*
     * Enable attachment Optimization through the MTOMFeature
     * Using SOAP11
     */
    @Test
    public void testSendImageFeature11() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        MTOMFeature mtom21 = new MTOMFeature();

        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING,
                server.getEndpoint("MtomSampleService.MtomSampleServicePort"));
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD, mtom21);
        
        List cids = null;
        SendImageResponse response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        
        int numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected one attachment but there were:" + numCIDs, numCIDs == 1);
        
        // Repeat to verify behavior
        response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        
        numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected one attachment but there were:" + numCIDs, numCIDs == 1);
    }
    
    /*
     * Enable attachment Optimization but call an endpoint with @MTOM(enable=false)
     */
    @Test
    public void testSendImage_MTOMDisable() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        MTOMFeature mtom21 = new MTOMFeature();

        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING,
                server.getEndpoint("MtomSampleMTOMDisableService.MtomSampleMTOMDisableServicePort"));
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD, mtom21);
        
        List cids = null;
        SendImageResponse response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        
        int numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected zero attachments but there were:" + numCIDs, numCIDs == 0);
        
        
        // Repeat to verify behavior
        response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        
        numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected zero attachments but there were:" + numCIDs, numCIDs == 0);
    }
    
    /*
     * Enable attachment Optimization but call an endpoint with @MTOM(enable=false)
     * which should override the MTOM BindingType
     */
    @Test
    public void testSendImage_MTOMDisable2() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        MTOMFeature mtom21 = new MTOMFeature();

        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING,
                server.getEndpoint("MtomSampleMTOMDisable2Service.MtomSampleMTOMDisable2ServicePort"));
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD, mtom21);
        
        List cids = null;
        SendImageResponse response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        
        int numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected zero attachments but there were:" + numCIDs, numCIDs == 0);
        
        
        // Repeat to verify behavior
        response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        
        numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected zero attachments but there were:" + numCIDs, numCIDs == 0);
    }
    
    /*
     * Enable attachment Optimization but call an endpoint with @MTOM(enable=true)
     */
    @Test
    public void testSendImage_MTOMEnable() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        MTOMFeature mtom21 = new MTOMFeature();

        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING,
                server.getEndpoint("MtomSampleMTOMEnableService.MtomSampleMTOMEnableServicePort"));
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD, mtom21);
        
        List cids = null;
        SendImageResponse response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        
        int numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected one attachment but there were:" + numCIDs, numCIDs == 1);
        
        // Repeat to verify behavior
        response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        
        numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected one attachment but there were:" + numCIDs, numCIDs == 1);
    }
    
    /*
     * Enable attachment Optimization but call an endpoint with @MTOM
     */
    @Test
    public void testSendImage_MTOMDefault() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        MTOMFeature mtom21 = new MTOMFeature();

        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING,
                server.getEndpoint("MtomSampleMTOMDefaultService.MtomSampleMTOMDefaultServicePort"));
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD, mtom21);
        
        List cids = null;
        SendImageResponse response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        
        int numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected one attachment but there were:" + numCIDs, numCIDs == 1);
        
        
        // Repeat to verify behavior
        response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        
        numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected one attachment but there were:" + numCIDs, numCIDs == 1);
    }
    
    /*
     * Enable attachment optimization using the SOAP11 binding
     * property for MTOM.
     */
    @Test
    public void testSendImageAttachmentProperty11() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        // Create the JAX-WS client needed to send the request with soap 11 binding
        // property for MTOM
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_MTOM_BINDING,
                server.getEndpoint("MtomSampleService.MtomSampleServicePort"));
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD);
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        // Repeat to verify behavior
        response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
    }
    
    /*
     * Enable attachment optimization using both the SOAP11 binding
     * property for MTOM and the Binding API
     */
    @Test
    public void testSendImageAttachmentAPIProperty11() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        // Create the JAX-WS client needed to send the request with soap 11 binding
        // property for MTOM
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_MTOM_BINDING,
                server.getEndpoint("MtomSampleService.MtomSampleServicePort"));
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD);
        
        
        //Enable attachment optimization
        SOAPBinding binding = (SOAPBinding) dispatch.getBinding();
        binding.setMTOMEnabled(true);
        
        SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        // Repeat to verify behavior
        response = (SendImageResponse) dispatch.invoke(request);
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
    }
    
    /*
     * Enable attachment optimization using both the SOAP12 binding
     * property for MTOM
     * 
     * Sending SOAP12 message to SOAP11 endpoint will correctly result in exception
     * 
     */
    @Test
    public void testSendImageAttachmentProperty12() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        // Create the JAX-WS client needed to send the request with soap 11 binding
        // property for MTOM
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_MTOM_BINDING,
                server.getEndpoint("MtomSampleService.MtomSampleServicePort"));
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD);
        
        try {
            SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
            fail("Was expecting an exception due to sending SOAP12 message to SOAP11 endpoint.");
        } catch (Exception e) {
            assertNotNull(e);
            if (CHECK_VERSIONMISMATCH) {
                assertTrue("Expected SOAPFaultException, but received: "+ e.getClass(),
                           e instanceof SOAPFaultException);
                SOAPFaultException sfe = (SOAPFaultException) e;

                SOAPFault fault = sfe.getFault();

                assertTrue("SOAPFault is null ",
                           fault != null);
                QName faultCode = sfe.getFault().getFaultCodeAsQName();


                assertTrue("Expected VERSION MISMATCH but received: "+ faultCode,
                           new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "VersionMismatch", SOAPConstants.SOAP_ENV_PREFIX).equals(faultCode));

            }
        }
        
        // Repeat to verify behavior
        try {
            SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
            fail("Was expecting an exception due to sending SOAP12 message to SOAP11 endpoint.");
        } catch (Exception e) {
            assertNotNull(e);
            if (CHECK_VERSIONMISMATCH) {
                assertTrue("Expected SOAPFaultException, but received: "+ e.getClass(),
                           e instanceof SOAPFaultException);
                SOAPFaultException sfe = (SOAPFaultException) e;

                SOAPFault fault = sfe.getFault();

                assertTrue("SOAPFault is null ",
                           fault != null);
                QName faultCode = sfe.getFault().getFaultCodeAsQName();


                assertTrue("Expected VERSION MISMATCH but received: "+ faultCode,
                           new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "VersionMismatch", SOAPConstants.SOAP_ENV_PREFIX).equals(faultCode));

            }
        }
        
        

	}
    
    /*
     * Enable attachment optimization using both the SOAP12 binding API
     * for MTOM
     * 
     * Sending SOAP12 message to SOAP11 endpoint will correctly result in exception
     * 
     */
    @Test
    public void testSendImageAttachmentAPI12() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        
        // Create the JAX-WS client needed to send the request with soap 11 binding
        // property for MTOM
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP12HTTP_BINDING,
                server.getEndpoint("MtomSampleService.MtomSampleServicePort"));
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD);
        
        
        //Enable attachment optimization
        SOAPBinding binding = (SOAPBinding) dispatch.getBinding();
        binding.setMTOMEnabled(true);
        
        try {
            SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
            fail("Was expecting an exception due to sending SOAP12 message to SOAP11 endpoint.");
        } catch (Exception e) {
            assertNotNull(e);
            if (CHECK_VERSIONMISMATCH) {
                assertTrue("Expected SOAPFaultException, but received: "+ e.getClass(),
                           e instanceof SOAPFaultException);
                SOAPFaultException sfe = (SOAPFaultException) e;

                SOAPFault fault = sfe.getFault();

                assertTrue("SOAPFault is null ",
                           fault != null);
                QName faultCode = sfe.getFault().getFaultCodeAsQName();


                assertTrue("Expected VERSION MISMATCH but received: "+ faultCode,
              		  new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "VersionMismatch", SOAPConstants.SOAP_ENV_PREFIX).equals(faultCode));

            }
        }
        
        // Repeat to verify behavior
        try {
            SendImageResponse response = (SendImageResponse) dispatch.invoke(request);
            fail("Was expecting an exception due to sending SOAP12 message to SOAP11 endpoint.");
        } catch (Exception e) {
            assertNotNull(e);
            if (CHECK_VERSIONMISMATCH) {
                assertTrue("Expected SOAPFaultException, but received: "+ e.getClass(),
                           e instanceof SOAPFaultException);
                SOAPFaultException sfe = (SOAPFaultException) e;

                SOAPFault fault = sfe.getFault();

                assertTrue("SOAPFault is null ",
                           fault != null);
                QName faultCode = sfe.getFault().getFaultCodeAsQName();


                assertTrue("Expected VERSION MISMATCH but received: "+ faultCode,
                          new QName(SOAPConstants.URI_NS_SOAP_1_1_ENVELOPE, "VersionMismatch", SOAPConstants.SOAP_ENV_PREFIX).equals(faultCode));

            }
        }
       
    }
    /*
     * Enable attachment Optimization but call an endpoint with @MTOM(enable=true, Threshold = 99000)
     */
    @Test
    public void testSendImage_setMTOMThreshold() throws Exception {
        TestLogger.logger.debug("----------------------------------");
        System.out.println("testSendImage_setMTOMThreshold()");
        String imageResourceDir = IMAGE_DIR;
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        DataSource imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
        
        //Create a DataHandler with the String DataSource object
        DataHandler dataHandler = new DataHandler(imageDS);
        
        //Store the data handler in ImageDepot bean
        ImageDepot imageDepot = new ObjectFactory().createImageDepot();
        imageDepot.setImageData(dataHandler);
        
        SendImage request = new ObjectFactory().createSendImage();
        request.setInput(imageDepot);
        
        //Create the necessary JAXBContext
        JAXBContext jbc = JAXBContext.newInstance("org.test.mtom");
        //Setting Threshold to send request Inline
        int threshold = 100000;
        MTOMFeature mtom21 = new MTOMFeature(true, threshold);
        // Create the JAX-WS client needed to send the request
        Service service = Service.create(QNAME_SERVICE);
        service.addPort(QNAME_PORT, SOAPBinding.SOAP11HTTP_BINDING,
                server.getEndpoint("MtomSampleMTOMThresholdService.MtomSampleMTOMThresholdServicePort"));
        Dispatch<Object> dispatch = service.createDispatch(QNAME_PORT, jbc, Mode.PAYLOAD, mtom21);
        
        List cids = null;
        SendImageResponse response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        //There shold be no cid as attachment should be inlined.
        int numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected one attachment inlined:" + numCIDs, numCIDs == 0);
        
        // Repeat to verify behavior
        response = null;
        try {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(true);
            response = (SendImageResponse) dispatch.invoke(request);
            
            // The cids are collected in the monitor.  We will check
            // this to make sure the response mtom is not inlined
            cids = JAXBAttachmentUnmarshallerMonitor.getBlobCIDs();
        } finally {
            JAXBAttachmentUnmarshallerMonitor.setMonitoring(false);
        }
        
        
        assertNotNull(response);
        assertNotNull(response.getOutput().getImageData());
        
        //There shold be no cid as attachment should be inlined.
        numCIDs = (cids == null) ? 0 : cids.size();
        assertTrue("Expected one attachment inlined:" + numCIDs, numCIDs == 0);
    }
    
}
