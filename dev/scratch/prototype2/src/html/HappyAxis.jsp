<html>
<%@ page import="java.io.InputStream,
                 java.io.IOException,
                 javax.xml.parsers.SAXParser,
                 javax.xml.parsers.SAXParserFactory,
                 org.apache.axis.clientapi.Call,
                 javax.xml.stream.XMLStreamConstants,
                 javax.xml.stream.XMLStreamReader,
                 org.apache.axis.addressing.AddressingConstants,
                 org.apache.axis.addressing.EndpointReference,
                 org.apache.axis.clientapi.Call,
                 org.apache.axis.engine.EngineUtils,
                 org.apache.axis.testUtils.ObjectToOMBuilder,
                 org.apache.axis.testUtils.SimpleTypeEncoder,
                 org.apache.axis.om.*,
                 org.apache.axis.testUtils.SimpleTypeEncodingUtils"
   session="false" %>
 <%
    /*
 * Copyright 2002,2004 The Apache Software Foundation.
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
%>
<head>
<title>Axis2 Happiness Page</title>
<link href="css/axis-style.css" rel="stylesheet" type="text/css">
</head>
<body bgcolor='#ffffff'>
<%port =request.getServerPort();%>
<%!
    /*
     * Happiness tests for axis. These look at the classpath and warn if things
     * are missing. Normally addng this much code in a JSP page is mad
     * but here we want to validate JSP compilation too, and have a drop-in
     * page for easy re-use
     */
    int port = 0;
    /**
     * Get a string providing install information.
     * TODO: make this platform aware and give specific hints
     */
    public String getInstallHints(HttpServletRequest request) {

        String hint=
            "<B><I>Note:</I></B> On Tomcat 4.x and Java1.4, you may need to put libraries that contain "
            +"java.* or javax.* packages into CATALINA_HOME/common/lib"
            +"<br>jaxrpc.jar and saaj.jar are two such libraries.";
        return hint;
    }

    /**
     * test for a class existing
     * @param classname
     * @return class iff present
     */
    Class classExists(String classname) {
        try {
            return Class.forName(classname);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * test for resource on the classpath
     * @param resource
     * @return true iff present
     */
    boolean resourceExists(String resource) {
        boolean found;
        InputStream instream=this.getClass().getResourceAsStream(resource);
        found=instream!=null;
        if(instream!=null) {
            try {
                instream.close();
            } catch (IOException e) {
            }
        }
        return found;
    }

    /**
     * probe for a class, print an error message is missing
     * @param out stream to print stuff
     * @param category text like "warning" or "error"
     * @param classname class to look for
     * @param jarFile where this class comes from
     * @param errorText extra error text
     * @param homePage where to d/l the library
     * @return the number of missing classes
     * @throws IOException
     */
    int probeClass(JspWriter out,
                   String category,
                   String classname,
                   String jarFile,
                   String description,
                   String errorText,
                   String homePage) throws IOException {
        try {
            Class clazz = classExists(classname);
            if(clazz == null)  {
               String url="";
               if(homePage!=null) {
                  url="<br>  See <a href="+homePage+">"+homePage+"</a>";
               }
               out.write("<p>"+category+": could not find class "+classname
                   +" from file <b>"+jarFile
                   +"</b><br>  "+errorText
                   +url
                   +"<p>");
               return 1;
            } else {
               String location = getLocation(out, clazz);
               if(location == null) {
                  out.write("Found "+ description + " (" + classname + ")<br>");
               }
               else {
                  out.write("Found "+ description + " (" + classname + ") at " + location + "<br>");
               }
               return 0;
            }
        } catch(NoClassDefFoundError ncdfe) {
            String url="";
            if(homePage!=null) {
                url="<br>  See <a href="+homePage+">"+homePage+"</a>";
            }
            out.write("<p>"+category+": could not find a dependency"
                    +" of class "+classname
                    +" from file <b>"+jarFile
                    +"</b><br> "+errorText
                    +url
                    +"<br>The root cause was: "+ncdfe.getMessage()
                    +"<br>This can happen e.g. if "+classname+" is in"
                    +" the 'common' classpath, but a dependency like "
                    +" activation.jar is only in the webapp classpath."
                    +"<p>");
            return 1;
        }
    }

    /**
     * get the location of a class
     * @param out
     * @param clazz
     * @return the jar file or path where a class was found
     */

    String getLocation(JspWriter out,
                       Class clazz) {
        try {
            java.net.URL url = clazz.getProtectionDomain().getCodeSource().getLocation();
            String location = url.toString();
            if(location.startsWith("jar")) {
                url = ((java.net.JarURLConnection)url.openConnection()).getJarFileURL();
                location = url.toString();
            }

            if(location.startsWith("file")) {
                java.io.File file = new java.io.File(url.getFile());
                return file.getAbsolutePath();
            } else {
                return url.toString();
            }
        } catch (Throwable t){
        }
        return "an unknown location";
    }

    /**
     * a class we need if a class is missing
     * @param out stream to print stuff
     * @param classname class to look for
     * @param jarFile where this class comes from
     * @param errorText extra error text
     * @param homePage where to d/l the library
     * @throws IOException when needed
     * @return the number of missing libraries (0 or 1)
     */
    int needClass(JspWriter out,
                   String classname,
                   String jarFile,
                   String description,
                   String errorText,
                   String homePage) throws IOException {
        return probeClass(out,
                "<b>Error</b>",
                classname,
                jarFile,
                description,
                errorText,
                homePage);
    }

    /**
     * print warning message if a class is missing
     * @param out stream to print stuff
     * @param classname class to look for
     * @param jarFile where this class comes from
     * @param errorText extra error text
     * @param homePage where to d/l the library
     * @throws IOException when needed
     * @return the number of missing libraries (0 or 1)
     */
    int wantClass(JspWriter out,
                   String classname,
                   String jarFile,
                   String description,
                   String errorText,
                   String homePage) throws IOException {
        return probeClass(out,
                "<b>Warning</b>",
                classname,
                jarFile,
                description,
                errorText,
                homePage);
    }

    /**
     * probe for a resource existing,
     * @param out
     * @param resource
     * @param errorText
     * @throws Exception
     */
    int wantResource(JspWriter out,
                      String resource,
                      String errorText) throws Exception {
        if(!resourceExists(resource)) {
            out.write("<p><b>Warning</b>: could not find resource "+resource
                        +"<br>"
                        +errorText);
            return 0;
        } else {
            out.write("found "+resource+"<br>");
            return 1;
        }
    }


    /**
     *  get servlet version string
     *
     */

    public String getServletVersion() {
        ServletContext context=getServletConfig().getServletContext();
        int major = context.getMajorVersion();
        int minor = context.getMinorVersion();
        return Integer.toString(major) + '.' + Integer.toString(minor);
    }



    /**
     * what parser are we using.
     * @return the classname of the parser
     */
    private String getParserName() {
        SAXParser saxParser = getSAXParser();
        if (saxParser == null) {
            return "Could not create an XML Parser";
        }

        // check to what is in the classname
        String saxParserName = saxParser.getClass().getName();
        return saxParserName;
    }

    /**
     * Create a JAXP SAXParser
     * @return parser or null for trouble
     */
    private SAXParser getSAXParser() {
        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        if (saxParserFactory == null) {
            return null;
        }
        SAXParser saxParser = null;
        try {
            saxParser = saxParserFactory.newSAXParser();
        } catch (Exception e) {
        }
        return saxParser;
    }

    /**
     * get the location of the parser
     * @return path or null for trouble in tracking it down
     */

    private String getParserLocation(JspWriter out) {
        SAXParser saxParser = getSAXParser();
        if (saxParser == null) {
            return null;
        }
        String location = getLocation(out,saxParser.getClass());
        return location;
    }

    OMFactory fac = OMFactory.newInstance();
    OMNamespace ns =
        fac.createOMNamespace("http://apache.ws.apache.org/samples", "samples");
    private SOAPEnvelope createRawMessage(
            String method,
            OMElement parameters) {
            SOAPEnvelope envelope = fac.getDefaultEnvelope();

            OMElement responseMethodName = fac.createOMElement(method, ns);
            envelope.getBody().addChild(responseMethodName);
            responseMethodName.addChild(parameters);
            return envelope;

        }

    private boolean validateService(String error){
        try{
            OMNamespace arrayNs =
                    fac.createOMNamespace(
                            OMConstants.ARRAY_ITEM_NSURI,
                            OMConstants.ARRAY_ITEM_NS_PREFIX);
            String message = "Hello testing";

            OMElement returnelement = fac.createOMElement("param1", ns);
            returnelement.setBuilder(
                    new ObjectToOMBuilder(
                            returnelement,
                            new SimpleTypeEncoder(message)));
            returnelement.declareNamespace(arrayNs);
            SOAPEnvelope envelope = createRawMessage("echoString", returnelement);
            XMLStreamReader xpp = invokeTheService(envelope);
            String value = SimpleTypeEncodingUtils.deserializeString(xpp);
            error = value;
            return true;
        }catch(Exception e){
            error = e.getMessage();
            return false;
        }
    }

    private XMLStreamReader invokeTheService(SOAPEnvelope envelope)
            throws Exception {
            EndpointReference targetEPR =
                new EndpointReference(
                    AddressingConstants.WSA_TO,
                    "http://127.0.0.1:"
                        + (port)
                        + "/axis2/services/echo");    //listServices       /axis2/services/service1
            Call call = new Call();
            call.setTo(targetEPR);
            SOAPEnvelope responseEnv = call.sendReceive(envelope);

            SOAPBody body = responseEnv.getBody();
            if(body.hasFault()){
                throw body.getFault().getException();
            }
            XMLStreamReader xpp = body.getPullParser(true);

            int event = xpp.next();
            while (event != XMLStreamConstants.START_ELEMENT) {
                event = xpp.next();
            }
            event = xpp.next();
            while (event != XMLStreamConstants.START_ELEMENT) {
                event = xpp.next();
            }
            event = xpp.next();
            while (event != XMLStreamConstants.START_ELEMENT) {
                event = xpp.next();
            }
            return xpp;
        }

    %>
<html><head><title>Axis2 Happiness Page</title></head>
<body>
<h1>Axis2 Happiness Page</h1>
<h2>Examining webapp configuration</h2>

<p>
<h3>Needed Components</h3>
<%
    int needed=0,wanted=0;

    /**
     * the essentials, without these Axis is not going to work
     */
     needed=needClass(out, "org.apache.axis.transport.http.AxisServlet",
            "axis2-M1.jar",
            "Apache-Axis",
            "Axis2 will not work",
            "http://xml.apache.org/axis/");
    needed+=needClass(out, "org.apache.commons.logging.Log",
            "commons-logging.jar",
            "Jakarta-Commons Logging",
            "Axis2 will not work",
            "http://jakarta.apache.org/commons/logging.html");

    needed+=needClass(out, "org.apache.log4j.Layout",
            "log4j-1.2.8.jar",
            "Log4j",
            "Axis2 may not work",
            "http://jakarta.apache.org/log4j");
    needed+=needClass(out, "javax.xml.stream.XMLStreamReader",
            "stax-api-1.0.jar",
            "Streaming API for XML",
            "Axis2 will not work",
            "http://dist.codehaus.org/stax/jars/");
     needed+=needClass(out, "javax.xml.stream.XMLStreamWriter",
            "stax-1.1.1-dev.jar",
            "Streaming API for XML",
            "Axis2 will not work",
            "http://dist.codehaus.org/stax/jars/");

%>

<%
    /*
     * resources on the classpath path
     */
    /* broken; this is a file, not a resource
    wantResource(out,"/server-config.wsdd",
            "There is no server configuration file;"
            +"run AdminClient to create one");
    */
    /* add more libraries here */

    out.write("<h3>");
    //is everythng we need here
    if(needed==0) {
       //yes, be happy
        out.write("<i>The core axis libraries are present. </i>");
    } else {
        //no, be very unhappy
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        out.write("<i>"
                +needed
                +" core axis librar"
                +(needed==1?"y is":"ies are")
                +" missing</i>");
    }
    //now look at wanted stuff

    out.write("</h3>");
    %>
    <p>
    <B><I>Note:</I></B> Even if everything this page probes for is present, there is no guarantee your
    web service will work, because there are many configuration options that we do
    not check for. These tests are <i>necessary</i> but not <i>sufficient</i>
    <hr>
    <h2>Examining echo service</h2>
    <%
    String error ="";
    boolean serviceStatus = validateService(error);
        if(serviceStatus){
   %>
       <p>
       <font color="blue" >
       Found the echo service and Axis2 is working properly, and now you can either drop any web service in
       to axis2/WEB-INF/service or use the upload utility in this application to upload a service
        and check whether it's working.
       <br>
       <%= error%> </font>
       </p>
       <hr>
   <%
        }   else {
    %>
     <p>
      <font color="red" >
     Either the echo service not found or Axis2 is not working properly. Check whether the echo.jar is in
     webapps/axis2/WEB-INF/service if it is then report the following error to the mailing list.
      <br>
     <%= error%></font>
     </p>
     <hr>
    <%
        }
    %>
    <h2>Examining Application Server</h2>
    <%
        String servletVersion=getServletVersion();
        String xmlParser=getParserName();
    %>
    <table>
        <tr><td>Servlet version</td><td><%= servletVersion %></td></tr>
        <tr><td>XML Parser</td><td><%= xmlParser %></td></tr>
        <tr><td>Platform</td><td><%= getServletConfig().getServletContext().getServerInfo()%></td></tr>
    </table>
    <h2>Examining System Properties</h2>
<%
    /**
     * Dump the system properties
     */
    java.util.Enumeration e=null;
    try {
        e= System.getProperties().propertyNames();
    } catch (SecurityException se) {
    }
    if(e!=null) {
        out.write("<pre>");
        for (;e.hasMoreElements();) {
            String key = (String) e.nextElement();
            out.write(key + "=" + System.getProperty(key)+"\n");
        }
        out.write("</pre><p>");
    } else {
        out.write("System properties are not accessible<p>");
    }
%>
    <hr>


    <jsp:include page="include/link-footer.inc"></jsp:include>
    <jsp:include page="include/footer.inc"></jsp:include>
</body>
</html>


