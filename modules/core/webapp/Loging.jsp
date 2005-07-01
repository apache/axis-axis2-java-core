<%@ page import="java.util.HashMap,
                 org.apache.axis2.Constants,
                 java.util.Collection,
                 java.util.Iterator,
                 org.apache.axis2.description.ModuleDescription"%>
 <%--
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
                <td align="right" >User Name :</td>
                <td> <INPUT align="left" TYPE=TEXT NAME="userName">
                </td>
             </tr>
             <tr>
                <td align="right">Password     : </td>
                <td><INPUT align="left" TYPE=PASSWORD NAME="password">
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
       <jsp:include page="include/footer.inc"></jsp:include>
     </body>
  </html>


