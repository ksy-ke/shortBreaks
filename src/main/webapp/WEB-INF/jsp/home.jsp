<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>Short break</title>
</head>

<body>
<sec:authorize var="isAuthenticated" access="isAuthenticated()"/>
<c:choose>
    <c:when test="${isAuthenticated}">
        <p style="text-align:center">
            <big>Hello, <%= request.getUserPrincipal().getName() %>!</big>
            <small><a href="<c:url value="/logout" />">logout</a></small>
        </p>
        <form  name="new_pomodoro" action="${pageContext.request.contextPath}/pomodoro/new" method="POST" style="text-align:center">
            <input type="submit" value="New pomodoro">
        </form>

        <form name="list_pomodoros" action="${pageContext.request.contextPath}/pomodoro/all" method="GET" style="text-align:center">
            <input type="hidden" name="page" value="0" >
            <input type="hidden" name="size" value="10" >
            <input type="submit" value="History">
        </form>
    </c:when>
    <c:otherwise>
        <h2>Please, <a href="<c:url value="/login"/>">log in</a></h2>
    </c:otherwise>
</c:choose>

</body>
</html>