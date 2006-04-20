<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
  <base href="<%= request.getScheme() %>://<%= request.getServerName() %>:<%= request.getServerPort()%><%= request.getContextPath() %>/" />
	<title>Axis2 :: Administration Page</title>
	<link href="axis2-web/css/axis-style.css" rel="stylesheet" type="text/css"/>
  <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1"/>
</head>
<body>
<jsp:include page="header.inc"></jsp:include>
<table class="FULL_BLANK"><tr><td valign="top" width="20%">
   <jsp:include page="../LeftFrame.jsp"></jsp:include>
</td>
<td valign="top" align="left" width="80%">
<table width="100%">
    <tr>
   <td align="right" colspan="2"><a href="#" onclick="javaacript:history.back();">Back</a>&nbsp;&nbsp;|&nbsp;&nbsp;<a href="admin/logout">Log out</a></td>
   </tr>
</table>
