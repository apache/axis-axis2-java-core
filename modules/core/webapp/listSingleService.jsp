<%@ page import="java.util.Iterator,
                 java.util.HashMap,
                 java.util.Collection,
                 org.apache.axis.Constants,
                 java.util.Hashtable,
                 org.apache.axis.description.ServiceDescription,
                 org.apache.axis.description.OperationDescription,
                 javax.xml.namespace.QName,
                 java.io.StringWriter,
                 java.io.PrintWriter"%>
 <%--
  Created by IntelliJ IDEA.
  User: Ajith
  Date: Feb 17, 2005
  Time: 8:47:13 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--        <jsp:include page="include/header.inc"></jsp:include>--%>

        <%
            ServiceDescription service = (ServiceDescription)request.getSession().getAttribute(Constants.SINGLE_SERVICE);
            String wsdl_value = (String)request.getSession().getAttribute(Constants.WSDL_CONTENT);
            //System.out.println("service = " + service);
            String isFault = (String)request.getSession().getAttribute(Constants.IS_FAULTY);
            String wsdl = request.getParameter("wsdl");
            boolean isWsdl = false;
            if(wsdl_value!=null){
                %>
                <%=wsdl_value%>
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
