package com.ksy.shortbreak.config;

import com.ksy.shortbreak.persistent.entity.Pomodoro;
import com.ksy.shortbreak.persistent.repository.PomodoroRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.stream.IntStream;

import static java.util.UUID.randomUUID;

public @Configuration class InitialData {
    private final PomodoroRepo pomodoroRepository;

    public @Autowired InitialData(PomodoroRepo pomodoroRepository) {
        this.pomodoroRepository = pomodoroRepository;
    }

    @Transactional
    private @PostConstruct void createData() {
        IntStream.range(0, 56)
                .mapToObj(i -> pomodoroOfUser("ksy", i))
                .forEach(pomodoroRepository::save);

        IntStream.range(0, 31)
                .mapToObj(i -> pomodoroOfUser("bob", i))
                .forEach(pomodoroRepository::save);
    }

    private Pomodoro pomodoroOfUser(String user, int id) {
        return Pomodoro.builder().id(randomUUID())
                .name("Initial of " + user + " with num " + id)
                .user(user)
                .timing(Pomodoro.Timing.of(Duration.ofMinutes(25),
                        OffsetDateTime.now().minusHours(100 - id).minusMinutes(25),
                        OffsetDateTime.now().minusHours(100 - id)))
                .build();
    }
}
