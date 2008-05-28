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

package org.apache.axis2.jaxws.xmlhttp.clientTests.dispatch.datasource;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.axis2.jaxws.framework.AbstractTestCase;
import org.apache.axis2.jaxws.provider.DataSourceImpl;
import org.apache.axiom.attachments.utils.IOUtils;

import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.http.HTTPBinding;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.Arrays;

public class DispatchXMessageDataSourceTests extends AbstractTestCase {

    public String HOSTPORT = "http://localhost:6060";
        
    private String ENDPOINT_URL = HOSTPORT + "/axis2/services/XMessageDataSourceProvider.XMessageDataSourceProviderPort";
    private QName SERVICE_NAME  = new QName("http://ws.apache.org/axis2", "XMessageDataSourceProvider");
    private QName PORT_NAME  = new QName("http://ws.apache.org/axis2", "XMessageDataSourceProviderPort");
 
    private DataSource imageDS;
    private DataSource xmlDS;

    public static Test suite() {
        return getTestSetup(new TestSuite(DispatchXMessageDataSourceTests.class));
    }
 
    public void setUp() throws Exception {
        String imageResourceDir = System.getProperty("basedir",".")+"/"+"test-resources"+File.separator+"image";
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);

        String xmlResourceDir = System.getProperty("basedir",".")+"/"+"test-resources";
        File file2 = new File(xmlResourceDir+File.separator+"axis2.xml");
        xmlDS = new FileDataSource(file2);
    }
    
    public Dispatch<DataSource> getDispatch() {
       Service service = Service.create(SERVICE_NAME);
       service.addPort(PORT_NAME, HTTPBinding.HTTP_BINDING,ENDPOINT_URL);
       Dispatch<DataSource> dispatch = service.createDispatch(PORT_NAME, DataSource.class, Service.Mode.MESSAGE);
       return dispatch;
    }
    
    /**
     * Simple XML/HTTP Message Test
     * @throws Exception
     */
    public void testDataSourceWithXML() throws Exception {
        Dispatch<DataSource> dispatch = getDispatch();
        DataSource request = xmlDS;
        DataSource response = dispatch.invoke(request);
        assertTrue(response != null);
        String req = new String(IOUtils.getStreamAsByteArray(request.getInputStream()));
        String res = new String(IOUtils.getStreamAsByteArray(response.getInputStream()));
        assertEquals(req, res);
    }

    /**
     * Simple XML/HTTP Message Test with an Image
     * @throws Exception
     */
    public void testDataSourceWithImage() throws Exception {
        Dispatch<DataSource> dispatch = getDispatch();
        DataSource request = imageDS;
        DataSource response = dispatch.invoke(request);
        assertTrue(response != null);
        Arrays.equals(IOUtils.getStreamAsByteArray(request.getInputStream()), IOUtils.getStreamAsByteArray(response.getInputStream()));
    }
}
