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

import org.apache.axiom.om.OMAttribute;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.soap.SOAPBody;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axiom.soap.SOAPFactory;
import org.apache.axis2.AxisFault;
import org.apache.axis2.Constants;
import org.apache.axis2.context.MessageContext;
import org.apache.axis2.description.AxisBindingOperation;
import org.apache.axis2.description.WSDL2Constants;
import org.apache.axis2.transport.http.HTTPConstants;
import org.apache.commons.fileupload.FileItemFactory;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.fileupload.servlet.ServletRequestContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.commons.schema.XmlSchema;
import org.apache.ws.commons.schema.XmlSchemaComplexType;
import org.apache.ws.commons.schema.XmlSchemaElement;
import org.apache.ws.commons.schema.XmlSchemaImport;
import org.apache.ws.commons.schema.XmlSchemaInclude;
import org.apache.ws.commons.schema.XmlSchemaObjectCollection;
import org.apache.ws.commons.schema.XmlSchemaParticle;
import org.apache.ws.commons.schema.XmlSchemaSequence;
import org.apache.ws.commons.schema.XmlSchemaType;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpUtils;
import javax.xml.namespace.QName;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 
 */
public class SchemaUtil {
    private static final Log log = LogFactory.getLog(SchemaUtil.class);


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
                    if (schema1 != null) {
                        traverseSchemas(schema1, map);
                    }
                }
                if (o instanceof XmlSchemaInclude) {
                    XmlSchema schema1 = ((XmlSchemaInclude) o).getSchema();
                    if (schema1 != null) {
                        traverseSchemas(schema1, map);
                    }
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

        SOAPEnvelope soapEnvelope = soapFactory.getDefaultEnvelope();
        SOAPBody body = soapEnvelope.getBody();
        String queryParameterSeparator = null;
        AxisBindingOperation axisBindingOperation = (AxisBindingOperation)msgCtxt.getProperty(Constants.AXIS_BINDING_OPERATION);
        if (axisBindingOperation != null) {
            queryParameterSeparator = (String)axisBindingOperation.getProperty(WSDL2Constants.ATTR_WHTTP_QUERY_PARAMETER_SEPARATOR);
        }
        Map requestParameterMap = getParameterMap(request, queryParameterSeparator);

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

                    // Now we are going to extrac information from the binding operation. WSDL 2.0
                    // http bindiing allows to define a query parameter separator. To capture it
                    // create a variable wit the default as "&"

                    MultipleEntryHashMap httpLocationParameterMap = new MultipleEntryHashMap();
                    if (axisBindingOperation != null) {

                        // get the http location property
                        String httpLocation = (String) axisBindingOperation
                                .getProperty(WSDL2Constants.ATTR_WHTTP_LOCATION);

                        // parameter names can be different from the element name in the schema, due
                        // to http location. Let's filter the parameter names from it.
                        httpLocationParameterMap = createHttpLocationParameterMap(httpLocation,
                                                                                  queryParameterSeparator,
                                                                                  request,
                                                                                  requestParameterMap);

                    }

                    while (iterator.hasNext()) {
                        XmlSchemaElement innerElement = (XmlSchemaElement) iterator.next();
                        QName qName = innerElement.getQName();
                        long minOccurs = innerElement.getMinOccurs();
                        boolean nillable = innerElement.isNillable();
                        while (minOccurs != 0) {
                            String name =
                                    qName != null ? qName.getLocalPart() : innerElement.getName();

                            // check whether this has a mapping in httpLocationParameterMap.
                            String value = (String) httpLocationParameterMap.get(name);
                            OMNamespace ns = (qName == null ||
                                            qName.getNamespaceURI() == null
                                            || qName.getNamespaceURI().length() == 0) ?
                                            null : soapFactory.createOMNamespace(
                                            qName.getNamespaceURI(), null);
                            if (value == null) {
                                String[] parameterValuesArray =
                                        (String[]) requestParameterMap.get(name);
                                if (parameterValuesArray != null &&
                                        !"".equals(parameterValuesArray[0]) &&
                                        parameterValuesArray[0] != null) {
                                    value = parameterValuesArray[0];

                                    for (int i = 0 ; i < parameterValuesArray.length ; i++) {
                                        soapFactory.createOMElement(name, ns,
                                                bodyFirstChild).setText(parameterValuesArray[i]);
                                    }
                                }
                            } else {
                                 soapFactory.createOMElement(name, ns,
                                                bodyFirstChild).setText(value);
                            }

                            if (value == null) {

                                if (nillable) {

                                    OMNamespace xsi = soapFactory.createOMNamespace(
                                            Constants.URI_DEFAULT_SCHEMA_XSI,
                                            Constants.NS_PREFIX_SCHEMA_XSI);
                                    OMAttribute omAttribute =
                                            soapFactory.createOMAttribute("nil", xsi, "true");
                                    soapFactory.createOMElement(name, ns,
                                                                bodyFirstChild)
                                            .addAttribute(omAttribute);

                                } else {
                                    throw new AxisFault("Required element " + qName +
                                            " defined in the schema can not be found in the request");
                                }
                            }
                            minOccurs--;
                        }
                    }
                }
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
    protected static MultipleEntryHashMap createHttpLocationParameterMap(String httpLocation,
                                                                         String queryParameterSeparator,
                                                                         HttpServletRequest request,
                                                                         Map parameterMap)
            throws AxisFault {

        MultipleEntryHashMap httpLocationParameterMap = new MultipleEntryHashMap();

        if (httpLocation != null) {

            // let's handle query parameters and the path separately
            String[] urlParts = httpLocation.split("\\?");
            String templatedPath = urlParts[0];

            if (urlParts.length > 1) {
                String templatedQueryParams = urlParts[1];
                // first extract parameters from the query part
                extractParametersFromQueryPart(templatedQueryParams, queryParameterSeparator,
                                               httpLocationParameterMap, parameterMap);
            }

            // now let's do the difficult part, extract parameters from the path element.
            extractParametersFromPath(templatedPath, httpLocationParameterMap,
                                      request.getRequestURI());
        }
        return httpLocationParameterMap;
    }

    protected static void extractParametersFromQueryPart(String templatedQueryParams,
                                                         String queryParameterSeparator,
                                                         MultipleEntryHashMap httpLocationParameterMap,
                                                         Map parameterMap) {
        // now let's tokenize the string with query parameter separator
        String[] nameValuePairs = templatedQueryParams.split(queryParameterSeparator);
        for (int i = 0; i < nameValuePairs.length; i++) {
            StringBuffer buffer = new StringBuffer(nameValuePairs[i]);
            // this name value pair will be either name=value or
            // name={SchemaElementName}. The first case is handled above
            // let's handle the second case
            if (buffer.indexOf("{") > 0 && buffer.indexOf("}") > 0) {
                String parameterName = buffer.substring(0, buffer.indexOf("="));
                String schemaElementName =
                        buffer.substring(buffer.indexOf("=") + 2, buffer.length() - 1);
                String[] parameterValues = (String[]) parameterMap.get(parameterName);
                String value;
                if (parameterValues != null && (value =parameterValues[0]) != null) {
                    httpLocationParameterMap.put(schemaElementName, value);
                }
            }

        }
    }

    /**
     * Here is what I will try to do here. I will first try to identify the location of the first
     * template element in the request URI. I am trying to deduce the location of that location
     * using the httpLocation element of the binding (it is passed in to this
     * method).
     * If there is a contant part in the httpLocation, then I will identify it. For this, I get
     * the index of {, from httpLocation param, and whatever to the left of it is the contant part.
     * Then I search for this constant part inside the url. This will give us the access to the first
     * template parameter.
     * To find the end of this parameter, we need to get the index of the next constant, from
     * httpLocation attribute. Likewise we keep on discovering parameters.
     * <p/>
     * Assumptions :
     * 1. User will always append the value of httpLocation to the address given in the
     * endpoint.
     * 2. I was talking about the constants in the httpLocation. Those constants will not occur,
     * to a reasonable extend, before the constant we are looking for.
     *
     * @param templatedPath
     * @param httpLocationParameterMap
     */
    protected static void extractParametersFromPath(String templatedPath,
                                                    MultipleEntryHashMap httpLocationParameterMap,
                                                    String requestURL) throws AxisFault {


        if (templatedPath != null && !"".equals(templatedPath) && templatedPath.indexOf("{") > -1) {
            StringBuffer pathTemplate = new StringBuffer(templatedPath);

            // this will hold the index, from which we need to process the request URI
            int startIndex = 0;
            int templateStartIndex = 0;
            int templateEndIndex = 0;
            int indexOfNextConstant = 0;

            StringBuffer requestURIBuffer ;
            try {
                requestURIBuffer = new StringBuffer(URLDecoder.decode(requestURL, "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                log.error("Could not decode the query String in the HttpServletRequest");
                throw new AxisFault("Could not decode the query String in the HttpServletRequest");
            }

            while (startIndex < requestURIBuffer.length()) {
                // this will always hold the starting index of a template parameter
                templateStartIndex = pathTemplate.indexOf("{", templateStartIndex);

                if (templateStartIndex > 0) {
                    // get the preceding constant part from the template
                    String constantPart =
                            pathTemplate.substring(templateEndIndex + 1, templateStartIndex);

                    // get the index of the end of this template param
                    templateEndIndex = pathTemplate.indexOf("}", templateStartIndex);

                    String parameterName =
                            pathTemplate.substring(templateStartIndex + 1, templateEndIndex);
                    // next try to find the next constant
                    templateStartIndex = pathTemplate.indexOf("{", templateEndIndex);

                    int endIndexOfConstant = requestURIBuffer
                            .indexOf(constantPart, indexOfNextConstant) + constantPart.length();

                    if (templateEndIndex == pathTemplate.length() - 1 || templateStartIndex == -1) {

                        constantPart =
                                pathTemplate.substring(templateEndIndex + 1, pathTemplate.length());
                        indexOfNextConstant =
                                requestURIBuffer.indexOf(constantPart, endIndexOfConstant);

                        httpLocationParameterMap.put(parameterName, requestURIBuffer.substring(
                                endIndexOfConstant, indexOfNextConstant));
                        startIndex = requestURIBuffer.length();
                    } else {

                        // this is the next constant from the template
                        constantPart =
                                pathTemplate.substring(templateEndIndex + 1, templateStartIndex);

                        indexOfNextConstant =
                                requestURIBuffer.indexOf(constantPart, endIndexOfConstant);
                        httpLocationParameterMap.put(parameterName, requestURIBuffer.substring(
                                endIndexOfConstant, indexOfNextConstant));
                        startIndex = indexOfNextConstant;

                    }

                }

            }

        }
    }

    private static Map getParameterMap(HttpServletRequest request, String queryParamSeparator)
            throws AxisFault {

        String encodedQueryString = request.getQueryString();
        String queryString ;
        Map parameterMap = new HashMap();

        if (encodedQueryString != null) {

            try {
                queryString = URLDecoder.decode(encodedQueryString, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("Could not decode the query String in the HttpServletRequest");
                throw new AxisFault("Could not decode the query String in the HttpServletRequest");
            }

            if (queryParamSeparator == null || queryParamSeparator.equals("&")) {
                parameterMap = HttpUtils.parseQueryString(queryString);
            } else {
                String parts[] = queryString.split(queryParamSeparator);
                for (int i = 0; i < parts.length; i++) {
                    int separator = parts[i].indexOf("=");
                    String[] value = new String[1];
                    value[0] = parts[i].substring(separator + 1);
                    parameterMap.put(parts[i].substring(0, separator), value);
                }
            }
        }

        String contentType = request.getContentType();
        if (contentType != null && contentType.indexOf(HTTPConstants.MEDIA_TYPE_MULTIPART_FORM_DATA) > -1) {
            ServletRequestContext servletRequestContext = new ServletRequestContext(request);
            try {
                List items = parseRequest(servletRequestContext);
                Iterator iter = items.iterator();
                while (iter.hasNext()) {
                    String[] value = new String[1];
                    DiskFileItem diskFileItem = (DiskFileItem) iter.next();
                    value[0] = diskFileItem.getString();
                    parameterMap.put(diskFileItem.getFieldName(), value);
                }
            } catch (FileUploadException e) {
                log.error("Unable to extract data from Multipart request");
                throw new AxisFault("Unable to extract data from Multipart request");
            }
        } else {

            Enumeration enumeration = request.getParameterNames();
            while (enumeration.hasMoreElements()) {
                String paramName = (String) enumeration.nextElement();
                if (parameterMap.get(paramName) == null) {
                    parameterMap.put(paramName, request.getParameterValues(paramName));
                }
            }
        }

        return parameterMap;
    }

    private static List parseRequest(ServletRequestContext requestContext)
            throws FileUploadException {
        // Create a factory for disk-based file items
        FileItemFactory factory = new DiskFileItemFactory();
        // Create a new file upload handler
        ServletFileUpload upload = new ServletFileUpload(factory);
        // Parse the request
        return upload.parseRequest(requestContext);
    }

}
