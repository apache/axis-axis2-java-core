<%@ page import="java.util.Iterator,
                 org.apache.axis.description.AxisOperation,
                 java.util.HashMap,
                 java.util.Collection,
                 org.apache.axis.description.AxisService,
                 org.apache.axis.Constants,
                 java.util.Hashtable"%>
 <%--
  Created by IntelliJ IDEA.
  User: Ajith
  Date: Feb 17, 2005
  Time: 8:47:13 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
        <head>
        <title>Single Service List</title>
        <link href="css/axis-style.css" rel="stylesheet" type="text/css">
        </head>
  <body>
        <jsp:include page="include/header.inc"></jsp:include>

        <%
            AxisService service = (AxisService)request.getSession().getAttribute(Constants.SINGLE_SERVICE);
            //System.out.println("service = " + service);
            String isFault = (String)request.getSession().getAttribute(Constants.IS_FAULTY);
            System.out.println("isFault = " + isFault);
            if(Constants.IS_FAULTY.equals(isFault)){
                Hashtable errornessservices =(Hashtable)request.getSession().getAttribute(Constants.ERROR_SERVICE_MAP);
                String servicName = (String)request.getParameter("serviceName");
                %> <h3>This Web service has deployment faults</h3><%
                %><font color="red" ><%=(String)errornessservices.get(servicName) %></font><%

            }else  if (service!=null){
            %> <h2>This location contains a web service</h2><%
                HashMap operations;
                Collection operationsList;


                operations = service.getOperations();
                operationsList = operations.values();


        %>
        <hr><h3><font color="blue"><%=service.getName().getLocalPart()%></font></h3>
        <%

                if (operationsList.size() > 0) {
        %>
                    <i>Available operations</i>
        <%
                } else {
        %>
                    <i> There are no opeartions specified!!</i>
        <%
                }
        %><ul><%
                for (Iterator iterator1 = operationsList.iterator(); iterator1.hasNext();) {
                    AxisOperation axisOperation = (AxisOperation) iterator1.next();
        %><li>
<%=axisOperation.getName().getLocalPart()%></li>
<%
                }
        %>
    </ul>
        <%
           request.getSession().removeAttribute(Constants.SINGLE_SERVICE);
            }else{
        %> Oh! this place seems to be empty!!! <%
}
       %>


        <jsp:include page="include/link-footer.inc"></jsp:include>
        <jsp:include page="include/footer.inc"></jsp:include>
</body>
</html>