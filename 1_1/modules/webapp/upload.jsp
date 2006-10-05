<%@ page contentType="text/html;charset=UTF-8" language="java" %>
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

<jsp:include page="include/adminheader.jsp"/>
<h2>Upload a AxisService Archive File</h2>

<p>You can upload a packaged Axis 2 AxisService from this page in two small steps.</p>
<ul>
  <li>Browse to the location and select the axisService archive file you wish to upload</li>
  <li>Click Upload button</li>
</ul>
<p>Simple as that!</p>

<% if ("success".equals(request.getAttribute("status"))) { %>
<font color="green">File <%= request.getAttribute("filename") %> successfully uploaded </font><br/><br/>
<%
} else if ("failure".equals(request.getAttribute("status"))) {
%>
<font color="red">The following error occurred <br/> <%= request.getAttribute("cause") %></font><br/>
<% } %>

<form method="post" name="Axis2upload" action="axis2-admin/upload"
      enctype="multipart/form-data">
  <table>
    <tr>
      <td>Service archive : </td>
      <td><input type="file" name="filename" size="50"/></td>
    </tr>
    <tr>
      <td>&nbsp;</td>
      <td><input name="upload" type="submit" value=" Upload "/></td>
    </tr>
  </table>
</form>
<jsp:include page="include/adminfooter.jsp"/>
