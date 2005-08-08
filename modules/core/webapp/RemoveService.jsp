<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.ServiceDescription,
                 java.util.Collection,
                 java.util.HashMap,
                 java.util.Iterator"%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<title>Engaging Module to a Service</title>
<style type="text/css">

</style></head>

<body>
<form method="get" name="serviceRemove" action="removeService">
  <table width="100%"  border="0">
<tr>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>
<tr>
  <td colspan="2" >
    <b>Turn off service :</b> Will be removed  the web service from the running system , and if the system
    &nbsp; restart then the service will be available again      &nbsp;
    <br>
<%--    <b>Remove Service :</b> The selected service will be removed from the file system and if the--%>
<%--    &nbsp; system restart it wont be there next time--%>
  </td>
  </tr>
   <tr>
    <td>&nbsp;<hr></td>
    <td>&nbsp;
    <hr>
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
  <td>.</td>
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
</body>

</html>