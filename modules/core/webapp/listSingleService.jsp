<%@ page import="java.util.Iterator,
                 java.util.HashMap,
                 java.util.Collection,
                 org.apache.axis2.Constants,
                 java.util.Hashtable,
                 org.apache.axis2.description.ServiceDescription,
                 org.apache.axis2.description.OperationDescription,
                 javax.xml.namespace.QName,
                 java.io.StringWriter,
                 java.io.PrintWriter"%>
<%@ page contentType="text/xml;charset=UTF-8" language="java" %>
<%
String wsdl_value = (String)request.getSession().getAttribute(Constants.WSDL_CONTENT);
String isFault = (String)request.getSession().getAttribute(Constants.IS_FAULTY);
if(wsdl_value!=null){%>
<%=wsdl_value.trim()%>
                <%
            } else if(Constants.IS_FAULTY.equals(isFault)){
                Hashtable errornessservices =(Hashtable)request.getSession().getAttribute(Constants.ERROR_SERVICE_MAP);
                String servicName = (String)request.getParameter("serviceName");
                %> <html>
                        <head>
                        <title>Single Service List</title>
                        <link href="css/axis-style.css" rel="stylesheet" type="text/css">
                    </head>
                    <body>
                    <h3>This Web service has deployment faults</h3><%
                     %><font color="red" ><%=(String)errornessservices.get(servicName) %></font>
                 </body>
            </html>
                <%

                    }else{
                %><html>
                    <head>
                        <title>Single Service List</title>
                        <link href="css/axis-style.css" rel="stylesheet" type="text/css">
                    </head>
                <body>
             Oh! this place seems to be empty!!!</body>
</html> <%
            }
        %>


<%--        <jsp:include page="include/link-footer.inc"></jsp:include>--%>
<%--        <jsp:include page="include/footer.inc"></jsp:include>--%>
