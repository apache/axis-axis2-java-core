<%--
  ~ Licensed to the Apache Software Foundation (ASF) under one
  ~ or more contributor license agreements. See the NOTICE file
  ~ distributed with this work for additional information
  ~ regarding copyright ownership. The ASF licenses this file
  ~ to you under the Apache License, Version 2.0 (the
  ~ "License"); you may not use this file except in compliance
  ~ with the License. You may obtain a copy of the License at
  ~
  ~ http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing,
  ~ software distributed under the License is distributed on an
  ~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  ~ KIND, either express or implied. See the License for the
  ~ specific language governing permissions and limitations
  ~ under the License.
  --%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="t" tagdir="/WEB-INF/tags" %>
<%@ page import="org.apache.axis2.Constants,
                 org.apache.axis2.description.AxisModule,
                 java.util.Collection,
                 java.util.HashMap,
                 java.util.Iterator" %>
<%@ page import="org.apache.axis2.util.Utils" %>
<jsp:include page="/WEB-INF/include/adminheader.jsp"/>
<h1>Engage Module Globally</h1>

<p>To engage a module on all services across the system, select a module from the combo box below
    and click on the "Engage" button. Any module that needs to place handlers into the pre-dispatch
    phase needs to be engaged globally.</p>

<form method="post" name="selectModuleForm" action="<c:url value="axis2-admin/doEngageGlobally"/>">
    <table summary="main content table" border="0" style="width:100%" cellspacing="1" cellpadding="1">
        <tr>
            <td style="width: 15%">Select a Module :</td>
            <td style="width: 75%" align="left">
                <select name="module">
                    <%
                        HashMap modules = (HashMap) request.getSession().getAttribute(Constants.MODULE_MAP);
                        request.getSession().setAttribute(Constants.MODULE_MAP,null);
                        Collection moduleCol = modules.values();
                        for (Iterator iterator = moduleCol.iterator(); iterator.hasNext();) {
                            AxisModule axisOperation = (AxisModule) iterator.next();
                            String modulename = axisOperation.getName();
                    %>
                    <option value="<%=modulename%>"><%=modulename%>
                    </option>
                    <%
                        }
                    %>
                </select>
            </td>
        </tr>
        <tr><td>&nbsp;</td>
            <td>
                <input name="submit" type="submit" value=" Engage ">
            </td>
        </tr>
    </table>
</form>
<t:status/>
<jsp:include page="/WEB-INF/include/adminfooter.jsp"/>
