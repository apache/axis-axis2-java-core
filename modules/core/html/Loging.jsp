<%@ page import="java.util.HashMap,
                 org.apache.axis.Constants,
                 java.util.Collection,
                 java.util.Iterator,
                 org.apache.axis.description.ModuleDescription"%>
 <%--
  Created by IntelliJ IDEA.
  User: me
  Date: May 31, 2005
  Time: 2:53:56 PM
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Loging to Axis2:: Administartion page</title></head>
  <body>
  <jsp:include page="include/header.inc"></jsp:include>
    <form method="get" name="LogingForm" action="adminloging">
        <table border="0" width="100%" cellspacing="1" cellpadding="1">
            <tr>
                <td>
                &nbsp;
                &nbsp;
                </td>
            </tr>
            <tr>
                <td>User Name : <INPUT align="left" TYPE=TEXT NAME="userName">
                </td>
             </tr>
             <tr>
                <td>Password     : <INPUT align="left" TYPE=PASSWORD NAME="password">
                </td>
             </tr>
             <tr>
             <td>
             <input align="right" name="submit" type="submit" value=" Logging " >
             <input align="left" name="cancel" type="reset" value=" Clear " >
             </td>
             </tr>
         </table>
       </form>
       <jsp:include page="include/footer.inc"></jsp:include>
     </body>
  </html>


