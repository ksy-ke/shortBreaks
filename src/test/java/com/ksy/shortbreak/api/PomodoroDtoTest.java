package com.ksy.shortbreak.api;

import com.ksy.shortbreak.persistent.entity.Pomodoro;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.Duration;
import java.util.List;

import static com.ksy.shortbreak.config.Security.USER_ROLE;
import static java.time.OffsetDateTime.now;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PomodoroDtoTest {
    private SecurityContext securityContext;

    private static final String USER = "Alice";

    @Before
    public void setUp() {
        securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);
    }

    @Test(expected = AccessDeniedException.class)
    public void toPomodoro_unauthorized_accessDenied() {
        // given
        securityContext.setAuthentication(null);
        var dto = new PomodoroDto().setId(randomUUID());

        // when
        dto.toPomodoro();
    }

    public @Test void toPomodoro_authorized_equivalentPomodoroCreated() {
        // given
        givenAuthorizedUser(USER);
        var hours = 4;
        var minutes = 11;
        var seconds = 3;
        var time = now();
        var duration = Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds);
        var dto = new PomodoroDto().setId(randomUUID())
                .setName("some name")
                .setDurationHours(hours)
                .setDurationMinutes(minutes)
                .setDurationSeconds(seconds)
                .setStarted(time.toString())
                .setEnded(time.toString());

        // when
        var pomodoro = dto.toPomodoro();

        // then
        assertEquals(USER, pomodoro.getUser());
        assertEquals(dto.getId(), pomodoro.getId());
        assertEquals(dto.getName(), pomodoro.getName());
        assertEquals(duration, pomodoro.getTiming().getDuration());
        assertEquals(time, pomodoro.getTiming().getStarted());
        assertEquals(time, pomodoro.getTiming().getEnded());
    }

    public @Test void toPomodoro_authorizedAndNoDuration_equivalentPomodoroCreated() {
        // given
        givenAuthorizedUser(USER);
        var time = now();
        var dto = new PomodoroDto().setId(randomUUID())
                .setName("some name")
                .setStarted(time.toString())
                .setEnded(time.toString());

        // when
        var pomodoro = dto.toPomodoro();

        // then
        assertEquals(USER, pomodoro.getUser());
        assertEquals(dto.getId(), pomodoro.getId());
        assertEquals(dto.getName(), pomodoro.getName());
        assertNull(pomodoro.getTiming().getDuration());
        assertEquals(time, pomodoro.getTiming().getStarted());
        assertEquals(time, pomodoro.getTiming().getEnded());
    }

    public @Test void ofPomodoro_pomodoroSpecified_equalDtoReturned() {
        // given
        var seconds = 2;
        var minutes = 3;
        var hours = 4;
        var pomodoro = Pomodoro.builder()
                .id(randomUUID())
                .name("some name")
                .user("user")
                .timing(Pomodoro.Timing.of(
                        Duration.ofSeconds(seconds).plusMinutes(minutes).plusHours(hours),
                        now().minusYears(1),
                        now().minusSeconds(2)))
                .build();

        // when
        var dto = PomodoroDto.ofPomodoro(pomodoro);

        // then
        var expected = new PomodoroDto()
                .setId(pomodoro.getId())
                .setName(pomodoro.getName())
                .setDurationSeconds(2)
                .setDurationMinutes(3)
                .setDurationHours(4)
                .setStarted(pomodoro.getTiming().getStarted().toString())
                .setEnded(pomodoro.getTiming().getEnded().toString());

        assertEquals(expected, dto);
    }

    public @Test void ofPomodoro_pomodoroWithoutDurationSpecified_equalDtoReturned() {
        // given
        var pomodoro = Pomodoro.builder()
                .id(randomUUID())
                .name("some name")
                .user("user")
                .timing(Pomodoro.Timing.of(
                        null,
                        now().minusYears(1),
                        now().minusSeconds(2)))
                .build();

        // when
        var dto = PomodoroDto.ofPomodoro(pomodoro);

        // then
        var expected = new PomodoroDto()
                .setId(pomodoro.getId())
                .setName(pomodoro.getName())
                .setStarted(pomodoro.getTiming().getStarted().toString())
                .setEnded(pomodoro.getTiming().getEnded().toString());

        assertEquals(expected, dto);
    }

    private void givenAuthorizedUser(String user) {
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(user, "pswd", List.of(USER_ROLE)));
    }
}