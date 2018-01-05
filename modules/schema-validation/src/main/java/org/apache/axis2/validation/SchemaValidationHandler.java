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
package org.apache.axis2.validation;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.axiom.om.OMException;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisService;
import org.apache.axis2.description.Parameter;
import org.apache.axis2.handlers.AbstractHandler;
import org.apache.axis2.util.JavaUtils;
import org.apache.ws.commons.schema.XmlSchema;
import org.xml.sax.SAXException;

public class SchemaValidationHandler extends AbstractHandler {
    public InvocationResponse invoke(MessageContext msgContext) throws AxisFault {
        AxisService service = msgContext.getAxisService();
        Parameter parameter = service.getParameter("disableSchemaValidation");
        if (parameter != null && JavaUtils.isTrueExplicitly(parameter.getValue())) {
            return InvocationResponse.CONTINUE;
        }
        List<XmlSchema> schemas = service.getSchema();
        if (schemas.isEmpty()) {
            return InvocationResponse.CONTINUE;
        }
        SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        List<Source> schemaSources = new ArrayList<Source>();
        for (XmlSchema schema : schemas) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                schema.write(baos);
            } catch (UnsupportedEncodingException ex) {
                throw AxisFault.makeFault(ex);
            }
            schemaSources.add(new StreamSource(new ByteArrayInputStream(baos.toByteArray())));
        }
        Schema schema;
        try {
            schema = schemaFactory.newSchema(schemaSources.toArray(new Source[schemaSources.size()]));
        } catch (SAXException ex) {
            throw new AxisFault("Failed to compile schemas", ex);
        }
        try {
            schema.newValidator().validate(msgContext.getEnvelope().getBody().getFirstElement().getSAXSource(true));
        } catch (SAXException ex) {
            throw new AxisFault("Failed to validate message", ex);
        } catch (IOException ex) {
            throw new AxisFault("Failed to validate message", ex);
        } catch (OMException ex) {
            throw AxisFault.makeFault(ex);
        }
        return InvocationResponse.CONTINUE;
    }
}
