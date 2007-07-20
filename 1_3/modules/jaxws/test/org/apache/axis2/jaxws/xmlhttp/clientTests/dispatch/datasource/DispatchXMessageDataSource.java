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

import java.awt.Image;
import java.io.File;

import javax.activation.DataSource;
import javax.imageio.ImageIO;
import javax.imageio.stream.FileImageInputStream;
import javax.imageio.stream.ImageInputStream;
import javax.xml.namespace.QName;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.http.HTTPBinding;

import org.apache.axis2.jaxws.provider.DataSourceImpl;

import junit.framework.TestCase;

public class DispatchXMessageDataSource extends TestCase {

    public String HOSTPORT = "http://localhost:8080";
        
    private String ENDPOINT_URL = HOSTPORT + "/axis2/services/XMessageDataSourceProvider";
    private QName SERVICE_NAME  = new QName("http://ws.apache.org/axis2", "XMessageDataSourceProvider");
    private QName PORT_NAME  = new QName("http://ws.apache.org/axis2", "XMessageDataSourceProviderPort");
 
    
    private DataSource imageDS;
    
    public void setUp() throws Exception {
        String imageResourceDir = System.getProperty("basedir",".")+"/"+"test-resources"+File.separator+"image";
        
        //Create a DataSource from an image 
        File file = new File(imageResourceDir+File.separator+"test.jpg");
        ImageInputStream fiis = new FileImageInputStream(file);
        Image image = ImageIO.read(fiis);
        imageDS = new DataSourceImpl("image/jpeg","test.jpg",image);
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
    public void testSimple() throws Exception {
        Dispatch<DataSource> dispatch = getDispatch();
        DataSource request = imageDS;
        
        // TODO NOT IMPLEMENTED
        
        //DataSource response = dispatch.invoke(request);
        //assertTrue(response != null);
        //assertTrue(request.equals(response));
    }
}
