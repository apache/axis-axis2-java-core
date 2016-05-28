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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<jsp:include page="/WEB-INF/include/adminheader.jsp"/>
<h1>Edit Service Parameters</h1>
<t:status/>
<c:if test="${not empty requestScope.serviceName}">
    <form method="post" action="<c:url value="axis2-admin/updateServiceParameters"/>">
        <input type="hidden" name="axisService" value="<c:out value="${requestScope.serviceName}"/>">
        <table summary="main content table" width="100%">
            <tr>
                <td colspan="2" ><b>Service Parameters :: <c:out value="${requestScope.serviceName}"/></b></td>
            </tr>
            <c:forEach items="${requestScope.parameters}" var="parameter">
                <tr>
                    <td><c:out value="${parameter.key}"/></td>
                    <td><input type="text" name="<c:out value="${requestScope.serviceName}_${parameter.key}"/>" value="<c:out value="${parameter.value}"/>" size="50"></td>
                </tr>
            </c:forEach>
            <c:if test="${not empty requestScope.operations}">
                <tr>
                    <td colspan="2">&nbsp</td>
                </tr>
                <tr>
                   <td colspan="2"><b>Operation Parameters ::</b></td>
                </tr>
                <c:forEach items="${requestScope.operations}" var="operation">
                    <tr>
                        <td colspan="2">&nbsp</td>
                    </tr>
                    <tr>
                        <td colspan="2"><b>Operation : <c:out value="${operation.key}"/></b></td>
                    </tr>
                    <c:forEach items="${operation.value}" var="parameter">
                        <tr>
                            <td><c:out value="${parameter.key}"/></td>
                            <td><input type="text" name="<c:out value="${operation.key}_${parameter.key}"/>" value="<c:out value="${parameter.value}"/>" size="50"></td>
                        </tr>
                    </c:forEach>
                </c:forEach>
            </c:if>
            <tr>
                <td>&nbsp;</td>
                <td><input name="changePara" type="submit" value=" Change " ></td>
            </tr>
        </table>
   </form>
</c:if>
<jsp:include page="/WEB-INF/include/adminfooter.jsp"/>
