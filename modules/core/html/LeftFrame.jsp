<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<%
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
%>
<head>
<title>Untitled Document</title>
<style type="text/css">
</style></head>

<body>
<table width="100%">
  <tr>
     <td colspan="2" >
      <b> View System Components</b>
     </td>
  </tr>
 <tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td >
      <a href="listService" target="mainFrame">List Available Services</a>
    </td>
 </tr>
 <tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td >
      <a href="listModules" target="mainFrame">List Available Modules</a>
    </td>
 </tr>
 <tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td>
      <a href="globalModules" target="mainFrame">List Globally Engaged Modules</a>
    </td>
 </tr>
 <tr>
    <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
    </td>
    <td >
      <a href="listPhases" target="mainFrame">View Available Phases</a>
    </td>
 </tr>
 <tr>
    <td colspan="2">
     <br>
    </td>
 </tr>
  <tr>
     <td colspan="2" >
       <b>View Phases and Handlers</b>
     </td>
  </tr>
   <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="viewGlobalHandlers" target="mainFrame">Gloabal Phases and Handlers </a>
       </td>
    </tr>
    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="selectService" target="mainFrame">View Service Phases and Hnadlers</a>
       </td>
    </tr>
    <tr>
       <td colspan="2">
        <br>
      </td>
   </tr>
    <tr>
     <td colspan="2" >
       <b>Enage Module</b>
     </td>
  </tr>
   <tr>
       <td>
        &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="engagingglobally" target="mainFrame">Gloabally</a>
       </td>
    </tr>

    <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="engageToService" target="mainFrame">To A service</a>
       </td>
    </tr>

     <tr>
       <td>
       &nbsp;&nbsp;&nbsp;&nbsp;
       </td>
       <td>
         <a href="enaggingtoanopeartion.jsp" target="mainFrame">To an Operation</a>
       </td>
    </tr>
</table>

<%--

<p><a href="listService" target="mainFrame">List Available Services</a></p>
<p><a href="listModules" target="mainFrame">List Available Modules</a></p>
<p><a href="globalModules" target="mainFrame">List Globally Engaged Modules</a></p>
<p>View Phases and Handlers</p>
<blockquote>
  <p><a href="viewGlobalHandlers" target="mainFrame">Gloabal Phases and Handlers </a></p>
  <p><a href="selectService" target="mainFrame">View Service Phases and Hnadlers</a></p>
  <p>&nbsp;</p>
</blockquote>
<p>Enage Module</p>
<blockquote>
  <p><a href="engagingglobally" target="mainFrame">Gloabally</a></p>
  <p><a href="engageToService" target="mainFrame">To A service</a></p>
  <p><a href="enaggingtoanopeartion.jsp" target="mainFrame">To an Operation</a></p>
  <p>&nbsp;</p>
</blockquote>
<p><a href="listPhases" target="mainFrame">View Available Phases</a> </p>--%>
</body>
</html>
