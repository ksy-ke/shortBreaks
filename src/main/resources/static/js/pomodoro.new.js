var timerInterval = null;
var pomodoroEndedSound = new Audio("/resources/audio/pomodoro.ended.wav");

$(document).ready(function () {
    setHours(initialDurationHours);
    setMinutes(initialDurationMinutes);
    setSeconds(initialDurationSeconds);

    $('#pomodoro_name').val(pomodoroDefaultName);
});

function update(timeStarted, timeEnded) {
    var name = $('#pomodoro_name').val();

    var dataValues = {
        id: pomodoroId,
        name: name,
        started: timeStarted ? timeStarted : null,
        ended: timeEnded ? timeEnded : null
    };

    console.log("Going to send for update:");
    console.log(dataValues);

    $.post({
        url: "update",
        data: dataValues,
        cache: false,
        timeout: 10000,
        success: function (data) {
            console.log("update performed");
        },
        error: function (e) {
            console.log("update failed")
        }
    });
}

function toHomePage() {
    document.location.href = "/";
}

function startTimer() {
    disableElement("start", true);
    $('#home').hide();

    update(currentDateTime(), null);
    timerInterval = setInterval(function() {
        var decreased = decreaseTime();
        if (decreased) return;

        pomodoroEndedSound.play();
        alert("Pomodoro ended");
        stopTimer();
    }, 1000);

    disableElement("stop", false);
}

function stopTimer() {
    disableElement("stop", true);
    clearInterval(timerInterval);

    update(null, currentDateTime());

    toHomePage();
}

function decreaseTime() {
    var seconds = getSeconds();
    if (seconds == 0) {
        var minutesDecreased = decreaseMinutes();
        if (!minutesDecreased) return false;
        setSeconds(59);
    } else {
        setSeconds(seconds - 1);
    }
    return true;
}

function decreaseMinutes() {
    var minutes = getMinutes();

    if (minutes == 0) {
        var hoursDecreased = decreaseHours();
        if (!hoursDecreased) return false;
        setMinutes(59);
    } else {
        setMinutes(minutes - 1);
    }
    return true;
}

function decreaseHours() {
    var hours = getHours();
    if (hours <= 0) return false;

    setHours(--hours);
    return true;
}


function formatDurationValue(value) { return value < 10 ? "0" + value : value }

function setSeconds(seconds) { $('#seconds').text(formatDurationValue(seconds)); }
function getSeconds() { return parseInt($('#seconds').text(), 10); }

function setMinutes(minutes) { $('#minutes').text(formatDurationValue(minutes)); }
function getMinutes() { return parseInt($('#minutes').text(), 10); }

function setHours(hours) { $('#hours').text(formatDurationValue(hours)); }
function getHours() { return parseInt($('#hours').text(), 10); }


function disableElement(id, disable) { $("#" + id).prop("disabled", disable); }

function currentDateTime() { return new Date().toISOString(); }