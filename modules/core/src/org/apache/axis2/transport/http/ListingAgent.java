/*
* Copyright 2004,2006 The Apache Software Foundation.
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


package org.apache.axis2.transport.http;

import org.apache.axis2.Constants;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.description.AxisService;
import org.apache.ws.commons.schema.XmlSchema;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

public class ListingAgent extends AbstractAgent {

    private static final String LIST_MULTIPLE_SERVICE_JSP_NAME = "listServices.jsp";
    private static final String LIST_SINGLE_SERVICE_JSP_NAME = "listSingleService.jsp";

    public static final String RUNNING_PORT = "RUNNING_PORT";

    public ListingAgent(ConfigurationContext aConfigContext) {
        super(aConfigContext);
    }

    public void handle(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException, ServletException {
        if (httpServletRequest.getParameter("wsdl") != null || httpServletRequest.getParameter("xsd") != null) {
            processListService(httpServletRequest, httpServletResponse);
        } else {
            super.handle(httpServletRequest, httpServletResponse);
        }
    }


    protected void processIndex(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws IOException, ServletException {
        processListServices(httpServletRequest, httpServletResponse);
    }

    protected void processListService(HttpServletRequest req, HttpServletResponse res)
            throws IOException, ServletException {

        String filePart = req.getRequestURL().toString();
        String serviceName = filePart.substring(filePart.lastIndexOf("/") + 1, filePart.length());
        HashMap services = configContext.getAxisConfiguration().getServices();
        String wsdl = req.getParameter("wsdl");
        String xsd = req.getParameter("xsd");
        if ((services != null) && !services.isEmpty()) {
            Object serviceObj = services.get(serviceName);
            if (serviceObj != null) {
                if (wsdl != null) {
                    OutputStream out = res.getOutputStream();
                    res.setContentType("text/xml");
                    int ipindex = filePart.indexOf("//");
                    String ip = null;
                    if (ipindex >= 0) {
                        ip = filePart.substring(ipindex + 2, filePart.length());
                        int seperatorIndex = ip.indexOf(":");
                        int slashIndex = ip.indexOf("/");
                        String port = ip.substring(seperatorIndex + 1, slashIndex);
                        System.setProperty(RUNNING_PORT, port);
                        if (seperatorIndex > 0) {
                            ip = ip.substring(0, seperatorIndex);
                        }
                    }
                    ((AxisService) serviceObj).printWSDL(out, ip);
                    out.flush();
                    out.close();
                    return;
                } else if (xsd != null) {
                    OutputStream out = res.getOutputStream();
                    res.setContentType("text/xml");

                    AxisService axisService = (AxisService) serviceObj;
                    ArrayList scheams = axisService.getSchema();
                    if (!"".equals(xsd)) {
//                        if (xsd.endsWith(".xsd")) {
//                            ClassLoader loader = axisService.getClassLoader();
//                            InputStream in = loader.getResourceAsStream("META-INF/" + xsd);
//                            if (in != null) {
//                                byte[] buf = new byte[1024];
//                                int read;
//                                while ((read = in.read(buf)) > 0) {
//                                    out.write(buf, 0, read);
//                                }
//                                out.flush();
//                                out.close();
//                            }
//                            return;
//                        }
                        xsd = xsd.replaceAll("xsd", "").trim();
                        try {
                            int index = Integer.parseInt(xsd);
                            XmlSchema scheam = axisService.getSchema(index);
                            if (scheam != null) {
                                scheam.write(out);
                                out.flush();
                                out.close();
                            }
                        } catch (Exception e) {

                        }
                        return;
                    }
                    if (scheams.size() > 1) {
                        res.sendRedirect("");
                    } else if ("".equals(xsd)) {
                        XmlSchema scheam = axisService.getSchema(0);
                        if (scheam != null) {
                            scheam.write(out);
                            out.flush();
                            out.close();
                        }
                    }
                    return;
                } else {
                    req.getSession().setAttribute(Constants.SINGLE_SERVICE, serviceObj);
                }
            } else {
                req.getSession().setAttribute(Constants.SINGLE_SERVICE, null);
            }
        }

        renderView(LIST_SINGLE_SERVICE_JSP_NAME, req, res);
    }

    protected void processListServices(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException {
        HashMap services = configContext.getAxisConfiguration().getServices();

        req.getSession().setAttribute(Constants.SERVICE_MAP, services);
        req.getSession().setAttribute(Constants.ERROR_SERVICE_MAP,
                configContext.getAxisConfiguration().getFaultyServices());

        renderView(LIST_MULTIPLE_SERVICE_JSP_NAME, req, res);
    }

}
