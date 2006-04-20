<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.AxisOperation"%>
<%@ page import="org.apache.axis2.description.AxisService"%>
<%@ page import="java.util.Hashtable"%>
<%@ page import="java.util.Iterator"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head>
    <base href="<%= request.getScheme() %>://<%= request.getServerName() %>:<%= request.getServerPort()%><%= request.getContextPath() %>/" />
    <title>List Single service</title>
    <link href="axis2-web/css/axis-style.css" rel="stylesheet" type="text/css">
  </head>
  <body>
  <jsp:include page="include/header.inc"></jsp:include>
    <jsp:include page="include/link-footer.jsp"></jsp:include>
  <%String IP=request.getRequestURL().toString();
        int lastindex = IP.lastIndexOf('/');
        IP = IP.substring(0,lastindex);
        String prifix = IP + "/services/";
        String restprefix = IP + "/rest/";
    %>
        <%
            String isFault = (String)request.getSession().getAttribute(Constants.IS_FAULTY);
            String servicName = request.getParameter("serviceName");
            if(Constants.IS_FAULTY.equals(isFault)){
                Hashtable errornessservices =(Hashtable)request.getSession().getAttribute(Constants.ERROR_SERVICE_MAP);
                %>
                    <h3>This Web axisService has deployment faults</h3><%
                     %><font color="red" ><%=(String)errornessservices.get(servicName) %></font>
                <%

                    }else {

                    AxisService axisService =
                            (AxisService) request.getSession().getAttribute(Constants.SINGLE_SERVICE);
                    if(axisService!=null){
           Iterator opItr = axisService.getOperations();
            //operationsList = operations.values();
          String  serviceName = axisService.getName();
            %><h2><font color="blue"><a href="<%=prifix + axisService.getName()%>?wsdl"><%=serviceName%></a></font></h2>
           <font color="blue">Service EPR : </font><font color="black"><%=prifix + axisService.getName()%></font><br>
               <font color="blue">Service REST epr :</font><font color="black"><%=restprefix + axisService.getName()%></font>
           <h4>Service Description : <font color="black"><%=axisService.getServiceDescription()%></h4>
           <i><font color="blue">Service Status : <%=axisService.isActive()?"Active":"InActive"%></font></i><br>
           <%
            if (opItr.hasNext()) {
                %><i>Available operations</i><%
            } else {
                %><i> There are no Operations specified</i><%
            }
               opItr = axisService.getOperations();
           %><ul><%
            while (opItr.hasNext()) {
                AxisOperation axisOperation = (AxisOperation) opItr.next();
                %><li><%=axisOperation.getName().getLocalPart()%></li>
<%--                <br>Operation EPR : <%=prifix + axisService.getName().getLocalPart() + "/"+ axisOperation.getName().getLocalPart()%>--%>
                <%
            }
           %></ul>
           <%
                    } else{
                           %>
                <h3><font color="red" >No service found in this location</font></h3>
 <%
                    }

            }
        %>
<jsp:include page="include/footer.inc"></jsp:include>
        </body>
</html>
