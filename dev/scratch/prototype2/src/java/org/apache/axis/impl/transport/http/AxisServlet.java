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
package org.apache.axis.impl.transport.http;

import org.apache.axis.addressing.AddressingConstants;
import org.apache.axis.addressing.EndpointReference;
import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.*;
import org.apache.axis.impl.llom.builder.StAXBuilder;
import org.apache.axis.impl.llom.builder.StAXSOAPModelBuilder;
import org.apache.axis.om.OMFactory;
import org.apache.axis.om.SOAPEnvelope;

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
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;




public class AxisServlet extends HttpServlet{
    private EngineRegistry engineRegistry;
    
    public void init(ServletConfig config) throws ServletException {
        try {
            ServletContext context = config.getServletContext();
            String repoDir = context.getRealPath("/WEB-INF");
            engineRegistry = EngineRegistryFactory.createEngineRegistry(repoDir);
        } catch (AxisFault e) {
            throw new ServletException(e);
        }
    }

    
    

    /* (non-Javadoc)
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    protected void doPost(HttpServletRequest req, HttpServletResponse res)
        throws ServletException, IOException {
        try {
            res.setContentType("text/xml; charset=utf-8");
            AxisEngine engine  = new AxisEngine(engineRegistry);
            MessageContext msgContext = new MessageContext(engineRegistry);
            msgContext.setServerSide(true);
            String filePart = req.getRequestURL().toString();
            msgContext.setTo(new EndpointReference(AddressingConstants.WSA_TO,filePart));
            
            String soapActionString = req.getHeader(HTTPConstants.HEADER_SOAP_ACTION);
            if (soapActionString != null) {
                msgContext.setProperty(MessageContext.SOAP_ACTION, soapActionString);
            }
            
            InputStreamReader isr = new InputStreamReader(req.getInputStream());
            XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(isr);
            StAXBuilder builder = new StAXSOAPModelBuilder(OMFactory.newInstance(), reader);
            msgContext.setEnvelope((SOAPEnvelope) builder.getDocumentElement());
            
            storeOutputInfo(msgContext, res.getOutputStream());
            engine.receive(msgContext);
        } catch (AxisFault e) {
            throw new ServletException(e);
        } catch (XMLStreamException e) {
            throw new ServletException(e);
        } catch (FactoryConfigurationError e) {
            throw new ServletException(e);
        }

    }

    protected void storeOutputInfo(MessageContext msgContext,
                                   OutputStream out) throws AxisFault {
//        try {
            // Send it on its way...
//            out.write(HTTPConstants.HTTP);
//            out.write(HTTPConstants.OK);
//            out.write("\n\n".getBytes());
            //We do not have any Addressing Headers to put
            //let us put the information about incoming transport
            msgContext.setProperty(MessageContext.TRANSPORT_TYPE,
                    TransportSenderLocator.TRANSPORT_HTTP);
            msgContext.setProperty(MessageContext.TRANSPORT_DATA, out);
//        } catch (IOException e) {
//            throw AxisFault.makeFault(e);
//        }
    }


}
