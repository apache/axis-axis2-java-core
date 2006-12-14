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
import org.apache.axis2.Constants;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.WSDL2Constants;
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
     * This method is designed for REST handling. Parameters of a REST request comes in the URL or in
     * the body of the message (if it is POST). Since those parameters may not be in the proper order,
     * we need to retrieve the schema of the operation and construct the message according to the
     * parameters received as the REST request.
     * This method will carry out that function and it is assumed that this method is called in that
     * scenarios only.
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
            // if there is no schema its piece of cake !! add these to the soap body in any order you like.
            // Note : if there are parameters in the path of the URL, there is no way this can add them
            // to the message.
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

            // first get the target namespace from the schema and the wrapping element.
            // create an OMElement out of those information. We are going to extract parameters from
            // url, create OMElements and add them as children to this wrapping element.
            String targetNamespace = xmlSchemaElement.getQName().getNamespaceURI();
            QName bodyFirstChildQName;
            if (targetNamespace != null && !"".equals(targetNamespace)) {
                bodyFirstChildQName = new QName(targetNamespace, xmlSchemaElement.getName());
            } else {
                bodyFirstChildQName = new QName(xmlSchemaElement.getName());
            }
            OMElement bodyFirstChild = soapFactory.createOMElement(bodyFirstChildQName, body);

            // we handle only GET and POST methods. Do a sanity check here, first
            String httpMethod = request.getMethod();
            if (org.apache.axis2.transport.http.HTTPConstants.HTTP_METHOD_POST.equals(httpMethod)
                    || (org.apache.axis2.transport.http.HTTPConstants.HTTP_METHOD_GET.equals(httpMethod)))
            {
                // Schema should adhere to the IRI style in this. So assume IRI style and dive in to
                // schema
                XmlSchemaType schemaType = xmlSchemaElement.getSchemaType();
                if (schemaType instanceof XmlSchemaComplexType) {
                    XmlSchemaComplexType complexType = ((XmlSchemaComplexType) schemaType);
                    XmlSchemaParticle particle = complexType.getParticle();
                    if (particle instanceof XmlSchemaSequence) {
                        XmlSchemaSequence xmlSchemaSequence = (XmlSchemaSequence) particle;
                        Iterator iterator = xmlSchemaSequence.getItems().getIterator();

                        // now we need to know some information from the binding operation.

                        // First retrieve the binding operations from message context
                        AxisBindingOperation axisBindingOperation = (AxisBindingOperation) msgCtxt.getProperty(Constants.AXIS_BINDING_OPERATION);

                        // Now we are going to extrac information from the binding operation. WSDL 2.0
                        // http bindiing allows to define a query parameter separator. To capture it
                        // create a variable wit the default as "&"
                        String queryParameterSeparator = "&";

                        Map httpLocationParameterMap = new HashMap(1);
                        if (axisBindingOperation != null) {

                            // now check whether we have a query parameter separator defined in this
                            // operation
                            String queryParamSeparatorTemp = (String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
                            if (queryParamSeparatorTemp != null && !"".equals(queryParamSeparatorTemp))
                            {
                                queryParameterSeparator = queryParamSeparatorTemp;
                            }

                            // get the http location property
                            String httpLocation = (String) axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION);

                            // parameter names can be different from the element name in the schema, due
                            // to http location. Let's filter the parameter names from it.
                            httpLocationParameterMap = createHttpLocationParameterMap(httpLocation, queryParameterSeparator);

                        }

                        Map parameterMap = request.getParameterMap();

                        while (iterator.hasNext()) {
                            XmlSchemaElement innerElement = (XmlSchemaElement) iterator.next();
                            QName qName = innerElement.getQName();
                            String name = qName != null ? qName.getLocalPart() : innerElement.getName();

                            // check whether this has a mapping in httpLocationParameterMap.
                            String mappingParamName = (String) httpLocationParameterMap.get(name);
                            if (mappingParamName != null) {
                                name = mappingParamName;
                            }

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

    /**
     * WSDL 2.0 HTTP binding introduces the concept of http location. User can provide some thing like
     * ?first={FirstName}, where FirstName is what is defined in the schema. In this case, when you
     * want to get the parameter value from the request parameter map, you have to ask for "first" and
     * not "FirstName".
     * <p/>
     * This method will create a map from the schema name to the name visible in the query string.
     * Eg: FirstName ==> first
     *
     * @param httpLocation
     * @param queryParameterSeparator
     */
    protected static Map createHttpLocationParameterMap(String httpLocation, String queryParameterSeparator) {

        Map httpLocationParameterMap = new HashMap();
        // if there is a questions mark in the front, remove it
        if (httpLocation.startsWith("?")) {
            httpLocation = httpLocation.substring(1, httpLocation.length());
        }

// now let's tokenize the string with query parameter separator
        String[] nameValuePairs = httpLocation.split(queryParameterSeparator);
        for (int i = 0; i < nameValuePairs.length; i++) {
            StringBuffer buffer = new StringBuffer(nameValuePairs[i]);
            // this name value pair will be either name=value or
            // name={SchemaElementName}. The first case is handled above
            // let's handle the second case
            if (buffer.indexOf("{") > 0 && buffer.indexOf("}") > 0) {
                String parameterName = buffer.substring(0, buffer.indexOf("="));
                String schemaElementName = buffer.substring(buffer.indexOf("=") + 2, buffer.length() - 1);
                httpLocationParameterMap.put(schemaElementName, parameterName);

            }

        }

        return httpLocationParameterMap;
    }
}
