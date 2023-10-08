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

package org.apache.axis2.jaxws.xmlhttp.provider.message.datasource;

import jakarta.activation.DataSource;
import jakarta.xml.ws.BindingType;
import jakarta.xml.ws.Provider;
import jakarta.xml.ws.Service;
import jakarta.xml.ws.ServiceMode;
import jakarta.xml.ws.WebServiceProvider;
import jakarta.xml.ws.WebServiceContext;
import jakarta.xml.ws.handler.MessageContext;
import jakarta.xml.ws.http.HTTPBinding;
import javax.annotation.Resource;
import java.util.Map;

/**
 * Sample XML/HTTP DataSource Provider 
 */
@WebServiceProvider(serviceName="XMessageDataSourceProvider")
@BindingType(HTTPBinding.HTTP_BINDING)
@ServiceMode(value=Service.Mode.MESSAGE)
public class XMessageDataSourceProvider implements Provider<DataSource> {
    @Resource
    WebServiceContext wsContext;
    
    public DataSource invoke(DataSource input) {
        MessageContext ctx = wsContext.getMessageContext();
        Map attachments = (Map) ctx.get(MessageContext.INBOUND_MESSAGE_ATTACHMENTS);
        ctx.put(MessageContext.OUTBOUND_MESSAGE_ATTACHMENTS, attachments);
        return input;
    }

}
