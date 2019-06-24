package com.ksy.shortbreak.persistent.entity;

import com.ksy.shortbreak.persistent.entity.Pomodoro.Timing;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static com.ksy.shortbreak.config.Security.USER_ROLE;
import static com.ksy.shortbreak.persistent.entity.Pomodoro.Timing.DEFAULT_DURATION;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.NANOS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;

public class PomodoroTest {
    private static final String USER = "bob";
    private SecurityContext securityContext;

    @Before
    public void setUp() {
        securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);
    }

    public @Test void builder_withoutName_pomodoroCreated() {
        // given
        givenAuthorizedUser();
        var timing = Timing.of();

        // when
        var pomodoro = Pomodoro.builder().timing(timing).build();

        // then
        assertNull(pomodoro.getName());
        assertEquals(USER, pomodoro.getUser());
        assertEquals(timing, pomodoro.getTiming());
    }

    @Test(expected = NullPointerException.class)
    public void builder_noTiming_exceptionThrown() {
        // given
        givenAuthorizedUser();

        // when
        Pomodoro.builder().build();
    }


    @Test(expected = NullPointerException.class)
    public void ofPomodoro_noPomodoro_exceptionThrown() {
        // given
        Pomodoro pomodoro = null;

        // when
        Pomodoro.of(pomodoro);
    }

    public @Test void builder_allValues_allValuesAreSet() {
        // given
        var id = randomUUID();
        var name = "pomo name";
        var timing = Timing.of();

        // when
        var pomodoro = Pomodoro.builder().id(id).user(USER).name(name).timing(timing).build();

        // then
        assertEquals(id, pomodoro.getId());
        assertEquals(USER, pomodoro.getUser());
        assertEquals(name, pomodoro.getName());
        assertEquals(timing, pomodoro.getTiming());
    }

    public @Test void ofPomodoro_pomodoroSpecified_copyOfPomodoroReturned() {
        // given
        var id = randomUUID();
        var name = "pomo name";
        var timing = Timing.of();

        var pomodoro = Pomodoro.builder().id(id).user(USER).name(name).timing(timing).build();

        // when
        var copy = Pomodoro.of(pomodoro);

        // then
        assertNotSame(pomodoro, copy);
        assertEquals(pomodoro, copy);
        assertNotSame(pomodoro.getTiming(), copy.getTiming());
    }

    public @Test void builder_allValuesWithoutUserName_allValuesAreSet() {
        // given
        givenAuthorizedUser();
        var id = randomUUID();
        var name = "pomo name";
        var timing = Timing.of();

        // when
        var pomodoro = Pomodoro.builder().id(id).name(name).timing(timing).build();

        // then
        assertEquals(id, pomodoro.getId());
        assertEquals(USER, pomodoro.getUser());
        assertEquals(name, pomodoro.getName());
        assertEquals(timing, pomodoro.getTiming());
    }

    @Test(expected = AccessDeniedException.class)
    public void builder_userIsAnon_exceptionThrown() {
        // given
        securityContext.setAuthentication(new AnonymousAuthenticationToken(USER, "smth", List.of(new SimpleGrantedAuthority("ANON"))));
        var id = randomUUID();
        var name = "pomo name";
        var timing = Timing.of();

        // when
        Pomodoro.builder().id(id).name(name).timing(timing).build();
    }

    @Test(expected = AccessDeniedException.class)
    public void builder_unauthorized_exceptionThrown() {
        // given
        securityContext.setAuthentication(null);
        var id = randomUUID();
        var name = "pomo name";
        var timing = Timing.of();

        // when
        Pomodoro.builder().id(id).name(name).timing(timing).build();
    }

    @Test(expected = AccessDeniedException.class)
    public void builder_userWithoutRole_exceptionThrown() {
        // given
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(USER, "pswd", List.of(new SimpleGrantedAuthority("UNKNOWN"))));
        var id = randomUUID();
        var name = "pomo name";
        var timing = Timing.of();

        // when
        Pomodoro.builder().id(id).name(name).timing(timing).build();
    }

    private void givenAuthorizedUser() {
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(USER, "pswd", List.of(USER_ROLE)));
    }

    public @Test void timingOf_nothingSpecified_defaultDurationUsed() {
        // when
        var timing = Timing.of();

        // then
        assertEquals(DEFAULT_DURATION, timing.getDuration());
        assertNull(timing.getStarted());
        assertNull(timing.getEnded());
    }

    @Test(expected = IllegalArgumentException.class)
    public void timingOf_durationIsNegative_exceptionThrown() {
        // given
        var invalidDuration = Duration.of(-5, SECONDS);

        // when
        Timing.of(invalidDuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void timingOf_durationIsNull_exceptionThrown() {
        // given
        var invalidDuration = Duration.of(0, NANOS);

        // when
        Timing.of(invalidDuration);
    }

    public @Test void timingOf_validDurationSpecified_timingCreated() {
        // given
        var duration = Duration.of(10, SECONDS);

        // when
        var timing = Timing.of(duration);

        // then
        assertEquals(duration, timing.getDuration());
        assertNull(timing.getStarted());
        assertNull(timing.getEnded());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDuration_negativeDuration_exceptionThrown() {
        // given
        var timing = Timing.of();
        var illegalDuration = Duration.of(-3, SECONDS);

        // when
        timing.setDuration(illegalDuration);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setDuration_zeroDuration_exceptionThrown() {
        // given
        var timing = Timing.of();
        var illegalDuration = Duration.of(0, SECONDS);

        // when
        timing.setDuration(illegalDuration);
    }

    public @Test void setDuration_validDuration_valueSet() {
        // given
        var timing = Timing.of();
        var validDuration = Duration.of(1, SECONDS);

        // when
        timing.setDuration(validDuration);

        // then
        assertEquals(validDuration, timing.getDuration());
    }

    @Test(expected = IllegalArgumentException.class)
    public void setStarted_endedAlreadySetAndNewStartedIsBiggerThenEnded_exceptionThrown() {
        // given
        var duration = Duration.of(10, SECONDS);
        var started = now();
        var ended = started.plusSeconds(1);
        var timing = Timing.of(duration, started, ended);

        OffsetDateTime invalidStarted = ended.plusSeconds(1);

        // when
        timing.setStarted(invalidStarted);
    }

    @Test(expected = IllegalArgumentException.class)
    public void timingOf_endedLessThenStarted_exceptionThrown() {
        // given
        var duration = Duration.of(10, SECONDS);
        var started = now();
        var ended = started.minusSeconds(1);

        // when
        Timing.of(duration, started, ended);
    }

    @Test(expected = IllegalArgumentException.class)
    public void setEnded_endedLessThenStarted_exceptionThrown() {
        // given
        var duration = Duration.of(10, SECONDS);
        var started = now();
        var timing = Timing.of(duration, started, null);

        var ended = started.minusSeconds(1);

        // when
        timing.setEnded(ended);
    }

    public @Test void timingOf_endedEqualsToStarted_timingOfValuesReturned() {
        // given
        var duration = Duration.of(10, SECONDS);
        var started = now();
        var ended = started;

        // when
        var timing = Timing.of(duration, started, ended);

        // then
        assertEquals(duration, timing.getDuration());
        assertEquals(started, timing.getStarted());
        assertEquals(ended, timing.getEnded());
    }

    public @Test void setStarted_endedEqualsToStarted_valueSet() {
        // given
        var duration = Duration.of(10, SECONDS);
        var started = now();
        var ended = started.plusSeconds(1);
        var timing = Timing.of(duration, started, ended);

        var newStarted = ended;

        // when
        timing.setStarted(newStarted);

        // then
        assertEquals(duration, timing.getDuration());
        assertEquals(newStarted, timing.getStarted());
        assertEquals(timing.getStarted(), timing.getStarted());
    }

    public @Test void setEnded_endedEqualsToStarted_valueSet() {
        // given
        var duration = Duration.of(10, SECONDS);
        var started = now();
        var timing = Timing.of(duration, started, null);

        var ended = started;

        // when
        timing.setEnded(ended);

        // then
        assertEquals(duration, timing.getDuration());
        assertEquals(started, timing.getStarted());
        assertEquals(timing.getStarted(), timing.getStarted());
    }

    public @Test void timingOf_endedBiggerThenStarted_timingOfValuesReturned() {
        // given
        var duration = Duration.of(10, SECONDS);
        var started = now();
        var ended = started.plusSeconds(1);

        // when
        var timing = Timing.of(duration, started, ended);

        // then
        assertEquals(duration, timing.getDuration());
        assertEquals(started, timing.getStarted());
        assertEquals(ended, timing.getEnded());
    }

    public @Test void setStarted_endedBiggerThenStarted_startedSet() {
        // given
        var duration = Duration.of(10, SECONDS);
        var started = now();
        var ended = started.plusSeconds(1);
        var timing = Timing.of(duration, started, ended);

        var newStarted = ended.minusSeconds(2);

        // when
        timing.setStarted(newStarted);

        // then
        assertEquals(duration, timing.getDuration());
        assertEquals(newStarted, timing.getStarted());
        assertEquals(ended, timing.getEnded());
    }

    public @Test void setEnded_endedBiggerThenStarted_endedSet() {
        // given
        var duration = Duration.of(10, SECONDS);
        var started = now();
        var timing = Timing.of(duration, started, null);

        var ended = started.plusSeconds(1);

        // when
        timing.setEnded(ended);

        // then
        assertEquals(duration, timing.getDuration());
        assertEquals(started, timing.getStarted());
        assertEquals(ended, timing.getEnded());
    }

    @Test(expected = IllegalArgumentException.class)
    public void timingOf_negativeDuration_exceptionThrown() {
        // given
        var duration = Duration.of(-10, SECONDS);
        var started = now();
        var ended = started.plusSeconds(1);

        // when
        Timing.of(duration, started, ended);
    }

    public @Test void timingOf_timingSpecified_copyIsNotTheSame() {
        // given
        var duration = Duration.of(10, SECONDS);
        var started = now();
        var ended = started.plusSeconds(1);
        var timing = Timing.of(duration, started, ended);

        // when
        var copy = Timing.of(timing);

        // then
        assertEquals(timing, copy);
        assertNotSame(timing, copy);
    }

    public @Test void updateBy_partOfChangesSpecified_listedChangesApplied() {
        // given
        givenAuthorizedUser();

        var expected = Pomodoro.builder()
                .id(randomUUID())
                .name("name")
                .user("user")
                .timing(Timing.of(Duration.ofSeconds(1), now().minusYears(2), now()))
                .build();
        var toUpdate = Pomodoro.builder()
                .id(expected.getId())
                .name("initial")
                .user(expected.getUser())
                .timing(Timing.of(expected.getTiming().getDuration()))
                .build();
        var update = Pomodoro.builder()
                .id(expected.getId())
                .name(expected.getName())
                .timing(Timing.of(null, expected.getTiming().getStarted(), expected.getTiming().getEnded()))
                .build();

        // when
        var result = toUpdate.updateBy(update);

        // then
        assertSame(toUpdate, result);
        assertEquals(expected, toUpdate);
    }

    public @Test void timingUpdateBy_partOfChangesSpecified_listedChangesApplied() {
        // given
        var expected = Timing.of(Duration.ofSeconds(1), now().minusYears(2), now());
        var toUpdate = Timing.of(expected.getDuration());
        var update = Timing.of(null, expected.getStarted(), expected.getEnded());

        // when
        var result = toUpdate.updateBy(update);

        // then
        assertSame(toUpdate, result);
        assertEquals(expected, toUpdate);
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBy_entitiesHaveDifferentIds_exceptionThrown() {
        // given
        givenAuthorizedUser();
        var toUpdate = Pomodoro.builder()
                .id(randomUUID())
                .name("initial")
                .timing(Timing.of())
                .build();
        var update = Pomodoro.builder()
                .id(randomUUID())
                .name("some")
                .timing(Timing.of())
                .build();

        // when
        toUpdate.updateBy(update);
    }
}