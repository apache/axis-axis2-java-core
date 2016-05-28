<%@ tag body-content="empty" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:if test="${not empty requestScope.status}">
<p style="color: <c:out value="${requestScope.status.success ? 'green' : 'red'}"/>">
<c:out value="${requestScope.status.message}"/>
</p>
</c:if>
