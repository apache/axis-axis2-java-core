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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.activation.DataHandler;
import javax.mail.util.ByteArrayDataSource;

import org.apache.axiom.attachments.lifecycle.DataHandlerExt;
import org.apache.commons.io.IOUtils;

public class MtomImpl implements MtomSkeletonInterface {
    private final Map<String,byte[]> documents = new HashMap<String,byte[]>();
    
    public UploadDocumentResponse uploadDocument(UploadDocument uploadDocument) {
        String id = UUID.randomUUID().toString();
        try {
            // If we don't get a DataHandlerExt here, then we know that we are not using MTOM
            documents.put(id, IOUtils.toByteArray(((DataHandlerExt)uploadDocument.getContent()).readOnce()));
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
        UploadDocumentResponse response = new UploadDocumentResponse();
        response.setId(id);
        return response;
    }

    public RetrieveDocumentResponse retrieveDocument(RetrieveDocument retrieveDocument) {
        RetrieveDocumentResponse response = new RetrieveDocumentResponse();
        response.setContent(new DataHandler(new ByteArrayDataSource(documents.get(retrieveDocument.getId()), "application/octet-stream")));
        return response;
    }
}
