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
package org.apache.axis2.util;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.context.MessageContext;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;

import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
/**
 * 
 */
public class SchemaUtil {

    public static XmlSchema[] getAllSchemas(XmlSchema schema) {
        HashMap map = new HashMap();
        traverseSchemas(schema, map);
        return (XmlSchema[]) map.values().toArray(new XmlSchema[map.values().size()]);
    }

    private static void traverseSchemas(XmlSchema schema, HashMap map) {
        String key = schema.getTargetNamespace() + ":" + schema.getSourceURI();
        if (map.containsKey(key)) {
            return;
        }
        map.put(key, schema);

        XmlSchemaObjectCollection includes = schema.getIncludes();
        if (includes != null) {
            Iterator tempIterator = includes.getIterator();
            while (tempIterator.hasNext()) {
                Object o = tempIterator.next();
                if (o instanceof XmlSchemaImport) {
                    XmlSchema schema1 = ((XmlSchemaImport) o).getSchema();
                    if (schema1 != null) traverseSchemas(schema1, map);
                }
                if (o instanceof XmlSchemaInclude) {
                    XmlSchema schema1 = ((XmlSchemaInclude) o).getSchema();
                    if (schema1 != null) traverseSchemas(schema1, map);
                }
            }
        }
    }

    /**
     * This method is designed for REST handling. Parameter of a REST request comes in the URL or in
     * the body of the message (if it is POST). Since those parameters may not be in the proper order,
     * we need to retrieve the schema of the operation and construct the message according to that
     * from the parameters received as the REST request.
     * This method will carry out that function and it is assumed that this method is called in that scenarios only.
     *
     * @param msgCtxt
     * @param request
     * @param xmlSchemaElement
     * @param soapFactory
     * @throws AxisFault
     */
    public static SOAPEnvelope handleMediaTypeURLEncoded(MessageContext msgCtxt,
                                                         HttpServletRequest request,
                                                         XmlSchemaElement xmlSchemaElement,
                                                         SOAPFactory soapFactory) throws AxisFault {

        Map requestParameterMap = request.getParameterMap();
        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        SOAPBody body = soapEnvelope.getBody();

        if (xmlSchemaElement == null) {
            // if there is no schema its piece of cake !! add these to the soap body in any order you like
            OMElement bodyFirstChild = soapFactory.createOMElement(msgCtxt.getAxisOperation().getName(), body);

            // first add the parameters in the URL
            if (requestParameterMap != null) {
                Iterator requestParamMapIter = requestParameterMap.keySet().iterator();
                while (requestParamMapIter.hasNext()) {
                    String key = (String) requestParamMapIter.next();

                    String value = (String) ((Object[]) requestParameterMap.get(key))[0];
                    soapFactory.createOMElement(key, null, bodyFirstChild).setText(value);
                }
            }
        } else {

            String targetNamespace = xmlSchemaElement.getQName().getNamespaceURI();
            QName bodyFirstChildQName;
            if (targetNamespace != null && !"".equals(targetNamespace)) {
                bodyFirstChildQName = new QName(targetNamespace, xmlSchemaElement.getName());
            } else {
                bodyFirstChildQName = new QName(xmlSchemaElement.getName());
            }
            OMElement bodyFirstChild = soapFactory.createOMElement(bodyFirstChildQName, body);

            if (org.apache.axis2.transport.http.HTTPConstants.HTTP_METHOD_POST.equals(request.getMethod())
                || (org.apache.axis2.transport.http.HTTPConstants.HTTP_METHOD_GET.equals(request.getMethod()))) {
                XmlSchemaType schemaType = xmlSchemaElement.getSchemaType();
                if (schemaType instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = ((XmlSchemaComplexType) schemaType);
                    XmlSchemaParticle particle = complexType.getParticle();
                    if (particle instanceof XmlSchemaSequence) {
                        XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) particle;
                        Iterator iterator = xmlSchemaSequence.getItems().getIterator();

                        Map parameterMap = request.getParameterMap();

                        while (iterator.hasNext()) {
                            XmlSchemaElement innerElement = (XmlSchemaElement) iterator.next();
                            QName qName = innerElement.getQName();
                            String name = qName != null ? qName.getLocalPart() : innerElement.getName();
                            String[] parameterValuesArray = (String[]) parameterMap.get(name);
                            if (parameterValuesArray != null &&
                                !"".equals(parameterValuesArray[0]) && parameterValuesArray[0] != null)
                            {
                                OMNamespace ns = (qName == null || qName.getNamespaceURI() == null || qName.getNamespaceURI().length() == 0) ?
                                        null :
                                        soapFactory.createOMNamespace(qName.getNamespaceURI(), null);
                                soapFactory.createOMElement(name, ns,
                                                            bodyFirstChild).setText(parameterValuesArray[0]);
                            } else {
                                throw new AxisFault("Required element " + qName +
                                                    " defined in the schema can not be found in the request");
                            }
                        }
                    }
                }
            } else {
                throw new AxisFault("According to WSDL 2.0 rules, we support complex types only");
            }

        }
        return soapEnvelope;
    }
}
