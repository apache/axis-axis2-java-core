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

import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.impl.llom.wrapper.OMXPPWrapper;
import org.apache.axis.registry.EngineRegistry;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPEnvelope;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @version $Rev: $ $Date: $
 */

public class ClientHttpHandler extends SimpleHTTPHandler{
    private AxisEngine engine;
    private SimpleAxisServer server;
    private Socket socket;
    private String serviceFromURI;
    
    public ClientHttpHandler(SimpleAxisServer server, Socket socket,AxisEngine engine) {
        this.server = server;
        this.socket = socket;
        this.engine = engine;
    }

    
    public MessageContext execute () throws AxisFault {
        byte buf[] = new byte[BUFSIZ];
        // create an Axis server

        MessageContext msgContext = new MessageContext(engine.getRegistry());
        msgContext.setServerSide(true);

        // Reusuable, buffered, content length controlled, InputStream
        NonBlockingBufferedInputStream is =
                new NonBlockingBufferedInputStream();

        // buffers for the headers we care about
        StringBuffer soapAction = new StringBuffer();
        StringBuffer httpRequest = new StringBuffer();
        StringBuffer fileName = new StringBuffer();
        StringBuffer cookie = new StringBuffer();
        StringBuffer cookie2 = new StringBuffer();
        StringBuffer authInfo = new StringBuffer();
        StringBuffer contentType = new StringBuffer();
        StringBuffer contentLocation = new StringBuffer();


        // assume the best
        byte[] status = OK;

        // assume we're not getting WSDL
        boolean doWsdl = false;

        // cookie for this session, if any
        String cooky = null;

        String methodName = null;

        try {
            authInfo.delete(0, authInfo.length());

            // read headers
            is.setInputStream(socket.getInputStream());
            int contentLength = parseHeaders(is, buf, contentType,
                    contentLocation, soapAction,
                    httpRequest, fileName,
                    cookie, cookie2, authInfo);
//                        cookie, cookie2, authInfo, requestHeaders);
            is.setContentLength(contentLength);

            if (httpRequest.toString().equals("GET")) {
                    throw new UnsupportedOperationException("GET not supported"); 
            } else {

//
//                    // this may be "" if either SOAPAction: "" or if no SOAPAction at all.
//                    // for now, do not complain if no SOAPAction at all
//                    String soapActionString = soapAction.toString();
//                    if (soapActionString != null) {
//                        msgContext.setProperty(MessageContext.SOAP_ACTION,soapActionString);
//                    }
//                    
//                    Service service = ServiceLocator.locateService(serviceFromURI,soapActionString,msgContext);
//                    msgContext.setService(service);
//                    // Send it on its way...
//                    OutputStream out = socket.getOutputStream();
//                    out.write(HTTP);
//                    out.write(status);
//                    log.info("status written");
                
                

                XmlPullParserFactory pf = XmlPullParserFactory.newInstance();
                pf.setNamespaceAware(true);
                XmlPullParser  parser = pf.newPullParser();
                parser.setInput(new InputStreamReader(is));
                
                OMXMLParserWrapper parserWrapper = new OMXPPWrapper(parser);
                msgContext.setEnvelope((SOAPEnvelope) parserWrapper.getRootElement());
                EngineRegistry reg = engine.getRegistry();
                // invoke the Axis engine
                
            }
            return msgContext;
        } catch (Exception e) {
            throw AxisFault.makeFault(e);
        }
    }


}
