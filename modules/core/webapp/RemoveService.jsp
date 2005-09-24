<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.ServiceDescription,
                 java.util.Collection,
                 java.util.HashMap,
                 java.util.Iterator"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="include/adminheader.jsp"></jsp:include>
<form method="get" name="serviceRemove" action="removeService">
  <table width="100%"  border="0">
<tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
<tr>
  <td colspan="2" >
    <h1>Turn off service</h1> <p>The service will be removed the from the running system, but if the system restarts then the service will be available again</p>
<%--    <b>Remove Service :</b> The selected service will be removed from the file system and if the--%>
<%--    &nbsp; system restart it wont be there next time--%>
  </td>
  </tr>
   <tr>
    <td>&nbsp;</td>
    <td>&nbsp;
    </td>
  </tr>
  <tr>
    <td width="40%"> Select Service : </td>
    <td>
       <select name="service" class="selectBoxes" >

                           <%
                       HashMap services = (HashMap)request.getSession().getAttribute(Constants.SERVICE_MAP);
                       Collection col = services.values();
                       for (Iterator iterator = col.iterator(); iterator.hasNext();) {
                           ServiceDescription serviceDescription = (ServiceDescription) iterator.next();
                   %> <option value="<%=serviceDescription.getName().getLocalPart()%>">
                           <%=serviceDescription.getName().getLocalPart()%></option> <%
                       }
                           %>
                  </td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  <tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  <tr>
    <td width="40%">Turn Off Service </td>
    <td width="60%"><input type="checkbox" name="turnoff">
    </td>
  </tr>
<%--  <tr>--%>
<%--    <td>Remove Service Permanently </td>--%>
<%--    <td><input type="checkbox" name="remove">--%>
<%--    </td>--%>
<%--  </tr>--%>
  <tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
  <tr>
  <td>&nbsp;</td>
  <td>
    <input name="submit" type="submit" value=" Save " >
   <input name="reset" type="reset" value=" Clear " >
  </td>
  </tr>

  <tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>

</table>
</form>
<jsp:include page="include/adminfooter.jsp"></jsp:include>
