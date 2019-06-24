<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>My pomodoros</title>
    <link rel="stylesheet" type="text/css" href="/resources/css/pomodoros.user.css">
    <script type="text/javascript" src="/webjars/jquery/3.3.1-1/jquery.js"></script>
    <script type="text/javascript" src="/resources/js/pomodoros.user.js"></script>
</head>

<body>
<sec:authorize/>
<table id="pomodoros">
        <thead>
        <tr>
            <th>Name</th>
            <th>Duration <small>(hh:mm:ss)</small></th>
            <th>Started</th>
            <th>Ended</th>
        </tr>
        </thead>

        <c:if test="${not empty pomodoros}">
        <tbody>
            <c:forEach items="${pomodoros}" var="pomodoro">
                <tr>
                    <td> ${pomodoro.name} </td>
                    <td> ${pomodoro.durationHours} : ${pomodoro.durationMinutes} : ${pomodoro.durationSeconds} </td>
                    <td> ${pomodoro.started} </td>
                    <td> ${pomodoro.ended} </td>
                </tr>
            </c:forEach>
        </tbody>
        </c:if>

</table>
<div id="control">
    <c:if test="${page > 0}">
    <form name="list_pomodoros" class="control_element" action="${pageContext.request.contextPath}/pomodoro/all" method="GET">
        <input type="hidden" name="page" value="${page - 1}" >
        <input type="hidden" name="size" value="${size}" >
        <input type="submit" value="previous">
    </form>
    </c:if>

    <div class="control_element">${page}</div>

    <c:if test="${page < ((total / size) - 1)}">
    <form name="list_pomodoros" class="control_element" action="${pageContext.request.contextPath}/pomodoro/all" method="GET">
        <input type="hidden" name="page" value="${page + 1}" >
        <input type="hidden" name="size" value="${size}" >
        <input type="submit" value="next">
    </form>
    </c:if>

    <form name="home" class="control_element" action="${pageContext.request.contextPath}/home" method="GET">
        <input type="submit" value="Home">
    </form>
</div>

</body>
</html>