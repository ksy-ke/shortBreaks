package com.ksy.shortbreak.api;

import com.ksy.shortbreak.Application;
import com.ksy.shortbreak.persistent.entity.Pomodoro;
import com.ksy.shortbreak.persistent.repository.PomodoroRepo;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;

import static org.hamcrest.Matchers.both;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.lessThan;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class PomodoroControllerTest {
    private static final String USER_1 = "Alice";
    private static final String USER_2 = "Bob";

    private @Autowired MockMvc mvc;
    private @Autowired PomodoroRepo pomodoroRepository;

    public @After void tearDown() { pomodoroRepository.deleteAll(); }

    @Test
    @WithMockUser(value = USER_1, roles = "USER")
    public void newPomodoro_noAdditionalDataSpecified_newPomodoroSavedInDbAndPomodoroIdReturned() throws Exception {
        var pomodoroValue = mvc.perform(post("/pomodoro/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("new_pomodoro"))
                .andReturn().getModelAndView().getModel().get("pomodoro");

        var pomodorosInDb = pomodoroRepository.findAll();
        assertThat(pomodorosInDb, hasSize(1));
        var pomodoroInDb = pomodorosInDb.get(0);
        assertEquals(USER_1, pomodoroInDb.getUser());

        var expected = new PomodoroDto()
                .setId(pomodoroInDb.getId())
                .setDurationHours(pomodoroInDb.getTiming().getDuration().toHoursPart())
                .setDurationMinutes(pomodoroInDb.getTiming().getDuration().toMinutesPart())
                .setDurationSeconds(pomodoroInDb.getTiming().getDuration().toSecondsPart());
        assertEquals(expected, pomodoroValue);
    }

    @Test
    @WithMockUser(value = USER_1, roles = "USER")
    public void updatePomodoro_allFieldsSpecified_pomodoroUpdated() throws Exception {
        final int hours = 3;
        final int minutes = 4;
        final int seconds = 5;
        var initialPomodoro = givenPomodoroInBaseOf(USER_1, "initial name");
        var nameToSet = "new name";
        var startToSet = OffsetDateTime.now().minusMinutes(1);
        var endToSet = OffsetDateTime.now();

        mvc.perform(post("/pomodoro/update")
                .param("id", initialPomodoro.getId().toString())
                .param("name", nameToSet)
                .param("durationSeconds", String.valueOf(seconds))
                .param("durationMinutes", String.valueOf(minutes))
                .param("durationHours", String.valueOf(hours))
                .param("started", startToSet.toString())
                .param("ended", endToSet.toString())
        ).andExpect(status().isOk());

        var pomodorosInDb = pomodoroRepository.findAll();
        assertThat(pomodorosInDb, hasSize(1));
        var pomodoroInDb = pomodorosInDb.get(0);

        assertEquals(initialPomodoro.getId(), pomodoroInDb.getId());
        assertEquals(nameToSet, pomodoroInDb.getName());
        assertEquals(USER_1, pomodoroInDb.getUser());
        assertEquals(Duration.ofHours(hours).plusMinutes(minutes).plusSeconds(seconds), pomodoroInDb.getTiming().getDuration());
        assertThat(pomodoroInDb.getTiming().getStarted(), both(greaterThan(startToSet.minusNanos(1000))).and(lessThan(startToSet.plusSeconds(1000))));
        assertThat(pomodoroInDb.getTiming().getEnded(), both(greaterThan(endToSet.minusNanos(1000))).and(lessThan(endToSet.plusSeconds(1000))));
    }

    @Test
    @WithMockUser(value = USER_2, roles = "USER")
    public void updatePomodoro_pomodoroBelongsToAnotherUser_failedToUpdate() throws Exception {
        var initialPomodoro = givenPomodoroInBaseOf(USER_1, "initial name");
        var nameToSet = "new name";

        mvc.perform(post("/pomodoro/update")
                .param("id", initialPomodoro.getId().toString())
                .param("name", nameToSet)
        ).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(value = USER_2, roles = "USER")
    public void userPomodoros_userHasTwoPomodorosAndTwoBelongsToAnotherUser_requestedPomodorosReturned() throws Exception {
        givenPomodoroInBaseOf(USER_1, "first of " + USER_1);
        givenPomodoroInBaseOf(USER_2, "first of " + USER_2);
        givenPomodoroInBaseOf(USER_1, "second of " + USER_1);
        var expected = PomodoroDto.ofPomodoro(givenPomodoroInBaseOf(USER_2, "second of " + USER_2));

        mvc.perform(get("/pomodoro/all").param("page", "1").param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("pomodoros", List.of(expected)))
                .andExpect(model().attribute("total", 2L))
                .andExpect(model().attribute("page", 1))
                .andExpect(model().attribute("size", 1))
                .andExpect(view().name("user_pomodoros"));
    }

    private Pomodoro givenPomodoroInBaseOf(String user, String name) {
        return pomodoroRepository.save(Pomodoro.builder().user(user).name(name).timing(Pomodoro.Timing.of()).build());
    }
}