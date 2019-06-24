<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<!DOCTYPE HTML>
<html>
<head>
    <meta charset="UTF-8"/>
    <title>New pomodoro</title>

    <link rel="stylesheet" type="text/css" href="/resources/css/pomodoro.new.css">
    <script type="text/javascript">
        var pomodoroId = "${pomodoro.id}";
        var pomodoroDefaultName = "${pomodoro.name}";
        var initialDurationHours = "${pomodoro.durationHours}";
        var initialDurationMinutes = "${pomodoro.durationMinutes}";
        var initialDurationSeconds = "${pomodoro.durationSeconds}";
    </script>
    <script type="text/javascript" src="/webjars/jquery/3.3.1-1/jquery.js"></script>
    <script type="text/javascript" src="/resources/js/pomodoro.new.js"></script>

</head>

<body>
<sec:authorize/>
<div id="timer">
  <div id="hours"></div>:<div id="minutes"></div>:<div id="seconds"></div>
</div>

<div id="pomodoro_name_block">
    <input type="text" id="pomodoro_name">
</div>

<div id="buttons">
  <button id="start" onclick="startTimer()">start</button>
  <button id="stop" disabled onclick="stopTimer()" >stop</button>

  <button id="home" onclick="toHomePage()">home</button>
</div>

</body>
</html>