/*
* Copyright 2003,2004 The Apache Software Foundation.
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
package org.apache.axis.transport.http;


import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.axis.Constants;
import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.context.SessionContext;
import org.apache.axis.context.SimpleSessionContext;
import org.apache.axis.description.AxisOperation;
import org.apache.axis.description.AxisService;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.EngineRegistry;
import org.apache.axis.engine.EngineRegistryFactory;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.SOAPEnvelope;
import org.apache.axis.om.impl.llom.builder.StAXBuilder;
import org.apache.axis.om.impl.llom.builder.StAXSOAPModelBuilder;


public class AxisServlet extends HttpServlet {
    private EngineRegistry engineRegistry;
    private final String LISTSERVICES = "listServices";


    public void init(ServletConfig config) throws ServletException {
        try {
            ServletContext context = config.getServletContext();
            String repoDir = context.getRealPath("/WEB-INF");
            engineRegistry = EngineRegistryFactory.createEngineRegistry(repoDir);
        } catch (AxisFault e) {
            throw new ServletException(e);
        }
    }

    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        doPost(httpServletRequest,httpServletResponse);
    }

    /* (non-Javadoc)
    * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
    */
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {
        try {
            res.setContentType("text/xml; charset=utf-8");
            AxisEngine engine = new AxisEngine(engineRegistry);
            
            
            Object sessionContext = req.getSession().getAttribute(Constants.SESSION_CONTEXT_PROPERTY);
            if(sessionContext == null){
                sessionContext = new SimpleSessionContext();
                req.getSession().setAttribute(Constants.SESSION_CONTEXT_PROPERTY,sessionContext);
            }
            
            MessageContext msgContext = new MessageContext(engineRegistry, null,(SessionContext)sessionContext);
            
            msgContext.setServerSide(true);
            String filePart = req.getRequestURL().toString();
            if (filePart != null && filePart.endsWith(LISTSERVICES)) {
                listServices(res);
                return;
            }
            msgContext.setTo(new EndpointReference(AddressingConstants.WSA_TO, filePart));

            String soapActionString = req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);

            if (soapActionString != null) {
                msgContext.setProperty(MessageContext.SOAP_ACTION, soapActionString);
            }

            XMLStreamReader reader =
                    XMLInputFactory.newInstance().createXMLStreamReader(new InputStreamReader(req.getInputStream()));
            StAXBuilder builder =
                    new StAXSOAPModelBuilder(OMFactory.newInstance(), reader);
            msgContext.setEnvelope((SOAPEnvelope) builder.getDocumentElement());

            msgContext.setProperty(MessageContext.TRANSPORT_TYPE,
                    Constants.TRANSPORT_HTTP);
            msgContext.setProperty(MessageContext.TRANSPORT_WRITER,
                    res.getWriter());

            engine.receive(msgContext);
        } catch (AxisFault e) {
            throw new ServletException(e);
        } catch (XMLStreamException e) {
            throw new ServletException(e);
        } catch (FactoryConfigurationError e) {
            throw new ServletException(e);
        }

    }

    //todo this neeeds to be re-written as a JSP
    private void listServices(HttpServletResponse res) throws IOException {
        HashMap services = engineRegistry.getServices();
        HashMap operations;
        String serviceName = "";
        Collection servicecol = services.values();
        Collection operationsList;
        PrintWriter out = res.getWriter();
        res.setContentType("text/html");
        String html = "<HTML>\n" +
                "<HEAD><TITLE>Avalilable services</TITLE>" +
                "<link href=\"css/axis-style.css\" rel=\"stylesheet\" type=\"text/css\">" +
                "</HEAD>\n" +
                "<BODY>\n" +
                "<H1>Avalilable services</H1>\n";


        for (Iterator iterator = servicecol.iterator(); iterator.hasNext();) {
            AxisService axisService = (AxisService) iterator.next();
            operations = axisService.getOperations();
            operationsList = operations.values();
            serviceName = axisService.getName().getLocalPart();
            html += "<hr><h3><font color=\"blue\">" + serviceName + "</font><h3>";
            if (operationsList.size() > 0) {
                html += "<i> Availble operations</i>";
            } else {
                html += "<i> There is no any opeartion specified</i>";
            }
            for (Iterator iterator1 = operationsList.iterator(); iterator1.hasNext();) {
                AxisOperation axisOperation = (AxisOperation) iterator1.next();
                html += "<li>" + axisOperation.getName().getLocalPart() + "</li>";
            }
        }
        out.println(html + "</BODY></HTML>");
    }


}
