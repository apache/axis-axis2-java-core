<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<%
/*
* Copyright 2002,2004 The Apache Software Foundation.
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
*/
%>
<head>
<title>Untitled Document</title>
<style type="text/css">
<!--
body {
	background-color: #FFFFCC;
}
-->
</style></head>

<body>
<p><a href="listService" target="mainFrame">List Available Services</a></p>
<p><a href="listModules" target="mainFrame">List Available Modules</a></p>
<p><a href="globalModules" target="mainFrame">List Globally Engaged Modules</a></p>
<p>Enage Module</p>
<blockquote>
  <p><a href="engagingglobally.jsp" target="mainFrame">Gloabally</a></p>
  <p><a href="engagingtoaservice.jsp" target="mainFrame">To A service</a></p>
  <p><a href="enaggingtoanopeartion.jsp" target="mainFrame">To an Operation</a></p>
  <p>&nbsp;</p>
</blockquote>
<p><a href="listPhases" target="mainFrame">View Available Phases</a> </p>
</body>
</html>
