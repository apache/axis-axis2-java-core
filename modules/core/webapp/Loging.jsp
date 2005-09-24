<%@ page import="org.apache.axis2.Constants"%>
<%@ page import="org.apache.axis2.transport.http.ListingAgent"%> <%--
 /*
* Copyright 2004,2005 The Apache Software Foundation.
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
*
*
*/

/**
* Author : Deepal Jayasinghe
* Date: May 26, 2005
* Time: 7:14:26 PM
*/
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
  <head><title>Login to Axis2:: Administartion page</title></head>
<link href="css/axis-style.css" rel="stylesheet" type="text/css">
  <body>
  <jsp:include page="include/header.inc"></jsp:include>
  <% String isLoged = (String)request.getSession().getAttribute(Constants.LOGGED);
   if("Yes".equals(isLoged)){
	response.sendRedirect(ListingAgent.ADMIN_JSP_NAME);
   }
%>
<table class="FULL_BLANK">
<tr><td valign="top">
<jsp:include page="happy_axis.jsp?type=min"></jsp:include>


</td>
<td valign="middle" align="left">
    <form method="get" name="LogingForm" action="adminloging">
        <table class="LOG_IN_FORM">
            <tr>
                <td align="center" colspan="2" bgcolor="#b0b0b0" color="#FFFFFF"><font color="#FFFFFF">Login</font></td>
             </tr>
            <tr>
                <td align="right" >User Name :</td>
                <td> <INPUT align="left" TYPE=TEXT NAME="userName" tabindex="1">
                </td>
             </tr>
             <tr>
                <td align="right">Password     : </td>
                <td><INPUT align="left" TYPE=PASSWORD NAME="password" tabindex="2">
                </td>
             </tr>
             <tr>
               <td colspan="2">
                 <br>
               </td>
             </tr>
             <tr>
             <td align="right">
                 <input  name="submit" type="submit" value=" Login  " >
             </td>
             <td align="left">
                <input  name="cancel" type="reset" value=" Clear " >
             </td>
             </tr>
         </table>
       </form>
<br/><br/><br/><br/><br/><br/>
</td>
</tr>
</table>
</td></tr><tr><td>
 <jsp:include page="include/link-footer.jsp"></jsp:include>
 <jsp:include page="include/footer.inc"></jsp:include>
<script language="JavaScript">
<!--
document.LogingForm.userName.focus();
//-->
</script>
     </body>
  </html>


