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
package org.apache.axis2.jaxbri.mtom;

import javax.activation.DataHandler;
import javax.activation.DataSource;

import org.apache.axiom.testutils.activation.RandomDataSource;
import org.apache.axiom.testutils.io.IOTestUtils;
import org.apache.axis2.testutils.Axis2Server;
import org.junit.ClassRule;
import org.junit.Test;

public class MtomTest {
    @ClassRule
    public static Axis2Server server = new Axis2Server("target/repo/mtom");
    
    @Test
    public void test() throws Exception {
        MtomStub stub = new MtomStub(server.getConfigurationContext(), server.getEndpoint("mtom"));
        UploadDocument uploadRequest = new UploadDocument();
        DataSource contentDS = new RandomDataSource(1234567L, 1024);
        uploadRequest.setContent(new DataHandler(contentDS));
        UploadDocumentResponse uploadResponse = stub.uploadDocument(uploadRequest);
        RetrieveDocument retrieveRequest = new RetrieveDocument();
        retrieveRequest.setId(uploadResponse.getId());
        RetrieveDocumentResponse retrieveResponse = stub.retrieveDocument(retrieveRequest);
        IOTestUtils.compareStreams(contentDS.getInputStream(), retrieveResponse.getContent().getInputStream());
    }
}
