package com.ksy.shortbreak.service;

import com.ksy.shortbreak.persistent.entity.Pomodoro;
import com.ksy.shortbreak.persistent.repository.PomodoroRepo;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;

import java.time.Duration;
import java.util.List;
import java.util.Optional;

import static com.ksy.shortbreak.config.Security.USER_ROLE;
import static java.time.OffsetDateTime.now;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.UUID.randomUUID;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class PomodoroServiceTest {

    private SecurityContext securityContext;

    private static final String USER = "Bob";
    private static final String HACKER_USER = "Alice";

    private @Mock PomodoroRepo repo;
    private @InjectMocks PomodoroService service;

    @Before
    public void setUp() {
        securityContext = new SecurityContextImpl();
        SecurityContextHolder.setContext(securityContext);
    }


    @Test(expected = NullPointerException.class)
    public void pomodorosOfUser_noUser_exceptionThrown() {
        // given
        String user = null;

        // when
        service.pomodorosOfUser(user, 0, 10);
    }

    @Test(expected = IllegalArgumentException.class)
    public void pomodorosOfUser_invalidPageAndSize_exceptionThrown() {
        // given
        var page = -1;
        var size = 0;

        // when
        service.pomodorosOfUser(USER, page, size);
    }

    public @Test void pomodorosOfUser_userAndPageInfoSpecified_userPomodorosReturned() {
        // given
        var page = 2;
        var size = 1;


        var expectedPomodoros = List.of(generatePomodoroOfUser());
        given(repo.findAllByUserOrderByTimingEndedDesc(USER, PageRequest.of(page, size))).willReturn(expectedPomodoros);

        // when
        var pomodoros = service.pomodorosOfUser(USER, page, size);

        // then
        assertEquals(expectedPomodoros, pomodoros);
    }

    @Test(expected = AccessDeniedException.class)
    public void initialize_unauthorized_exceptionThrown() throws Exception {
        // given
        securityContext.setAuthentication(null);

        // when
        service.initialize();
    }

    public @Test void initialize_userAuthorized_pomodoroWithIdReturned() throws Exception {
        // given
        givenAuthorizedUser(USER);

        var pomodoroCaptor = ArgumentCaptor.forClass(Pomodoro.class);
        given(repo.saveAndFlush(pomodoroCaptor.capture()))
                .willAnswer(call -> copyOfPomodoroEnrichedById(call.getArgument(0)));

        // when
        var initialized = service.initialize();

        // then
        assertNotNull(initialized.getId());
        assertNull(pomodoroCaptor.getValue().getId());
    }

    @Test(expected = AccessDeniedException.class)
    public void initialize_userUnauthorizedAuthorized_exceptionThrown() {
        // given
        givenUnauthorizedUser();

        // when
        service.initialize();
    }

    @Test(expected = NullPointerException.class)
    public void update_noPomodoro_exceptionThrown() {
        // given
        Pomodoro pomodoro = null;

        // when
        service.update(pomodoro);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_pomodoroNotSavedBefore_exceptionThrown() {
        // given
        Pomodoro pomodoro = generatePomodoroOfUser();

        // when
        service.update(pomodoro);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_pomodoroWithoutId_exceptionThrown() throws Exception {
        // given
        Pomodoro pomodoro = generatePomodoroOfUser();

        // when
        service.update(pomodoro);
    }

    @Test(expected = AccessDeniedException.class)
    public void update_pomodoroWithIdBelongsToAnotherUser_exceptionThrown() throws Exception {
        // given
        Pomodoro pomodoro = generatePomodoroOfUserWithId();
        Pomodoro toApply = Pomodoro.of(pomodoro);
        toApply.setName("Alice climbs to Bob's pomodoro");

        given(repo.findById(toApply.getId())).willReturn(Optional.of(pomodoro));

        givenAuthorizedUser(HACKER_USER);

        // when
        service.update(toApply);
    }

    public @Test void update_allChangesSpecified_updatedPomodoroReturned() {
        // given
        givenAuthorizedUser(USER);
        var expected = Pomodoro.builder()
                .id(randomUUID())
                .name("new name here")
                .timing(Pomodoro.Timing.of(
                        Duration.of(1, SECONDS),
                        now().minusSeconds(10),
                        now().minusSeconds(1))).build();

        var currentPomodoro = Pomodoro.builder().id(expected.getId()).timing(Pomodoro.Timing.of()).build();

        var update = Pomodoro.of(currentPomodoro);
        update.setName(expected.getName());
        update.getTiming().setDuration(expected.getTiming().getDuration());
        update.getTiming().setStarted(expected.getTiming().getStarted());
        update.getTiming().setEnded(expected.getTiming().getEnded());

        given(repo.findById(update.getId())).willReturn(Optional.of(currentPomodoro));
        given(repo.save(any())).willAnswer(call -> call.getArgument(0));

        // when
        var result = service.update(update);

        // then
        assertEquals(expected, result);
        verify(repo).save(expected);
    }

    public @Test void update_partOfChangesSpecified_pomodoroWithAppliedChangesSavedAndReturned() {
        // given
        givenAuthorizedUser(USER);
        var expected = Pomodoro.builder()
                .id(randomUUID())
                .name("new name here")
                .timing(Pomodoro.Timing.of(
                        Duration.of(1, SECONDS),
                        now().minusYears(10),
                        now().minusYears(1))).build();

        var currentPomodoro = Pomodoro.builder()
                .id(expected.getId())
                .timing(Pomodoro.Timing.of(expected.getTiming().getDuration(), expected.getTiming().getStarted(), now().minusSeconds(1)))
                .build();

        var update = Pomodoro.builder()
                .id(expected.getId())
                .name(expected.getName())
                .timing(Pomodoro.Timing.of(null, null, expected.getTiming().getEnded()))
                .build();

        given(repo.findById(update.getId())).willReturn(Optional.of(currentPomodoro));
        given(repo.save(any())).willAnswer(call -> call.getArgument(0));

        // when
        var result = service.update(update);

        // then
        assertEquals(expected, result);
        verify(repo).save(expected);
    }

    @Test(expected = IllegalArgumentException.class)
    public void update_startedTimeAlreadySetButEndedTimeInUpdateIsBefore_failedToApplyUpdate() {
        // given
        givenAuthorizedUser(USER);
        var id = randomUUID();
        var started = now();
        var currentPomodoro = Pomodoro.builder()
                .id(id)
                .timing(Pomodoro.Timing.of(null, started, null))
                .build();

        var update = Pomodoro.builder()
                .id(id)
                .timing(Pomodoro.Timing.of(null, null, started.minusSeconds(10)))
                .build();

        given(repo.findById(update.getId())).willReturn(Optional.of(currentPomodoro));

        // when
        service.update(update);
    }

    public @Test void countPomodorosOfUser_userSpecified_valueFromRepositoryReturned() {
        // given
        given(repo.countByUser(USER)).willReturn(11L);

        // when
        var count = service.countPomodorosOfUser(USER);

        // then
        assertEquals(11L, count);
    }

    private Pomodoro generatePomodoroOfUser() {
        return Pomodoro.builder().user(USER).name("some name").timing(Pomodoro.Timing.of()).build();
    }

    private Pomodoro generatePomodoroOfUserWithId() {
        return Pomodoro.builder().id(randomUUID()).user(USER).name("some name").timing(Pomodoro.Timing.of()).build();
    }

    private Pomodoro copyOfPomodoroEnrichedById(Pomodoro pomodoro) throws Exception {
        var copy = Pomodoro.of(pomodoro);
        var idField = Pomodoro.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(copy, randomUUID());

        return copy;
    }


    private void givenAuthorizedUser(String user) {
        securityContext.setAuthentication(new UsernamePasswordAuthenticationToken(user, "pswd", List.of(USER_ROLE)));
    }

    private void givenUnauthorizedUser() {
        securityContext.setAuthentication(new AnonymousAuthenticationToken(USER, "smth", List.of(new SimpleGrantedAuthority("ANON"))));
    }
}