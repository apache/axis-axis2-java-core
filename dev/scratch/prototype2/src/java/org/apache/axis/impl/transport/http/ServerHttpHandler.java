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

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;

import org.apache.axis.context.MessageContext;
import org.apache.axis.engine.AxisEngine;
import org.apache.axis.engine.AxisFault;
import org.apache.axis.engine.ServiceLocator;
import org.apache.axis.engine.TransportSenderLocator;
import org.apache.axis.impl.encoding.Base64;
import org.apache.axis.impl.llom.wrapper.OMXPPWrapper;
import org.apache.axis.registry.EngineRegistry;
import org.apache.axis.registry.Service;
import org.apache.axis.om.OMXMLParserWrapper;
import org.apache.axis.om.SOAPEnvelope;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

/**
 * @version $Rev: $ $Date: $
 */

public class ServerHttpHandler extends SimpleHTTPHandler{
    private AxisEngine engine;
    private SimpleAxisServer server;
    private Socket socket;
    private String serviceFromURI;
    
    public ServerHttpHandler(SimpleAxisServer server, Socket socket,AxisEngine engine) {
        this.server = server;
        this.socket = socket;
        this.engine = engine;
    }

    
    public MessageContext parseHTTPHeaders () throws AxisFault {
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

        try{
            // assume the best
            byte[] status = OK;

            // assume we're not getting WSDL
            boolean doWsdl = false;

            // cookie for this session, if any
            String cooky = null;

            String methodName = null;

            authInfo.delete(0, authInfo.length());

            // read headers
            is.setInputStream(socket.getInputStream());
            // parse all headers into hashtable
//                MimeHeaders requestHeaders = new MimeHeaders();
            int contentLength = parseHeaders(is, buf, contentType,
                    contentLocation, soapAction,
                    httpRequest, fileName,
                    cookie, cookie2, authInfo);
//                        cookie, cookie2, authInfo, requestHeaders);
            is.setContentLength(contentLength);

            int paramIdx = fileName.toString().indexOf('?');
            if (paramIdx != -1) {
                // Got params
                String params = fileName.substring(paramIdx + 1);
                fileName.setLength(paramIdx);


                if ("wsdl".equalsIgnoreCase(params))
                    doWsdl = true;

                if (params.startsWith("method=")) {
                    methodName = params.substring(7);
                }
            }



            String filePart = fileName.toString();
            if (filePart.startsWith("axis/services/")) {
                String servicePart = filePart.substring(14);
                int separator = servicePart.indexOf('/');
                if (separator > -1) {
                    msgContext.setProperty("objectID",
                                   servicePart.substring(separator + 1));
                    servicePart = servicePart.substring(0, separator);
                }
               this.serviceFromURI = servicePart;
            }

            if (authInfo.length() > 0) {
                // Process authentication info
                //authInfo = new StringBuffer("dXNlcjE6cGFzczE=");
                byte[] decoded = Base64.decode(authInfo.toString());
                StringBuffer userBuf = new StringBuffer();
                StringBuffer pwBuf = new StringBuffer();
                StringBuffer authBuf = userBuf;
                for (int i = 0; i < decoded.length; i++) {
                    if ((char) (decoded[i] & 0x7f) == ':') {
                        authBuf = pwBuf;
                        continue;
                    }
                    authBuf.append((char) (decoded[i] & 0x7f));
                }


                msgContext.setProperty(MessageContext.USER_NAME,userBuf.toString());
                msgContext.setProperty(MessageContext.PASSWARD,pwBuf.toString());
            }
            if (httpRequest.toString().equals("GET")) {
                    throw new UnsupportedOperationException("GET not supported"); 
            } else {

                // this may be "" if either SOAPAction: "" or if no SOAPAction at all.
                // for now, do not complain if no SOAPAction at all
                String soapActionString = soapAction.toString();
                if (soapActionString != null) {
                    msgContext.setProperty(MessageContext.SOAP_ACTION,soapActionString);
                }
                
                Service service = ServiceLocator.locateService(serviceFromURI,soapActionString,msgContext);
                msgContext.setService(service);
                // Send it on its way...
                OutputStream out = socket.getOutputStream();
                out.write(HTTP);
                out.write(status);
                out.write("\n\n".getBytes());
                log.info("status written");
                
                
                
                //We do not have any Addressing Headers to put
                //let us put the information about incoming transport
                msgContext.setProperty(MessageContext.TRANSPORT_TYPE,
                    TransportSenderLocator.TRANSPORT_TCP);
                msgContext.setProperty(MessageContext.TRANSPORT_DATA,out);

                XmlPullParserFactory pf = XmlPullParserFactory.newInstance();
                pf.setNamespaceAware(true);
                XmlPullParser  parser = pf.newPullParser();
                parser.setInput(new InputStreamReader(is));
                
                OMXMLParserWrapper parserWrapper = new OMXPPWrapper(parser);
                msgContext.setEnvelope((SOAPEnvelope) parserWrapper.getRootElement());
                EngineRegistry reg = engine.getRegistry();
                // invoke the Axis engine
//                    engine.recive(msgContext);
//                    log.info("revice done");
//                    out.flush();
                return  msgContext;
            }    
            }catch(IOException e){
                throw AxisFault.makeFault(e); 
            } catch(XmlPullParserException e){
                throw AxisFault.makeFault(e); 
            }   
    }


}
