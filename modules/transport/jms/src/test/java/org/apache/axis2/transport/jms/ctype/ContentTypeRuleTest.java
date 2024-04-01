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
package org.apache.axis2.transport.jms.ctype;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.InputStream;

import jakarta.jms.BytesMessage;
import jakarta.jms.Message;
import jakarta.jms.ObjectMessage;
import jakarta.jms.TextMessage;

import junit.framework.TestCase;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMXMLBuilderFactory;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.deployment.ServiceBuilder;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.engine.AxisConfiguration;

public class ContentTypeRuleTest extends TestCase {
    private ContentTypeRuleSet ruleSet;
    
    @Override
    public void setUp() throws Exception {
        AxisConfiguration axisCfg = new AxisConfiguration();
        ConfigurationContext cfgCtx = new ConfigurationContext(axisCfg);
        AxisService service = new AxisService();
        
        InputStream in = ContentTypeRuleTest.class.getResourceAsStream(getName() + ".xml");
        try {
            OMElement element = OMXMLBuilderFactory.createOMBuilder(in).getDocumentElement();
            new ServiceBuilder(cfgCtx, service).populateService(element);
        } finally {
            in.close();
        }
        
        ruleSet = ContentTypeRuleFactory.parse(service.getParameter("test"));
    }
    
    private void assertContentTypeInfo(String propertyName, String contentType, Message message)
            throws Exception {
        
        ContentTypeInfo contentTypeInfo = ruleSet.getContentTypeInfo(message);
        assertEquals(propertyName, contentTypeInfo.getPropertyName());
        assertEquals(contentType, contentTypeInfo.getContentType());
    }
    
    public void test1() throws Exception {
        Message message = mock(BytesMessage.class);
        when(message.getStringProperty("contentType")).thenReturn("application/xml");
        assertContentTypeInfo("contentType", "application/xml", message);
        
        assertContentTypeInfo(null, "text/plain", mock(TextMessage.class));
        assertContentTypeInfo(null, "application/octet-stream", mock(BytesMessage.class));
        assertEquals(null, ruleSet.getContentTypeInfo(mock(ObjectMessage.class)));
    }
    
    public void test2() throws Exception {
        Message message1 = mock(BytesMessage.class);
        when(message1.getStringProperty("contentType")).thenReturn("application/xml");
        assertContentTypeInfo("contentType", "application/xml", message1);
        
        Message message2 = mock(TextMessage.class);
        when(message2.getStringProperty("ctype")).thenReturn("application/xml");
        assertContentTypeInfo("ctype", "application/xml", message2);

        assertContentTypeInfo(null, "text/xml", mock(TextMessage.class));
        assertContentTypeInfo(null, "text/xml", mock(BytesMessage.class));
    }
}
