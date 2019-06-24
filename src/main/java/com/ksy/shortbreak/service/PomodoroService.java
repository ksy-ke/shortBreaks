package com.ksy.shortbreak.service;

import com.ksy.shortbreak.persistent.entity.Pomodoro;
import com.ksy.shortbreak.persistent.repository.PomodoroRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

public @Service class PomodoroService {
    private static final Logger LOG = LoggerFactory.getLogger(PomodoroService.class);

    private final PomodoroRepo repository;

    public @Autowired PomodoroService(PomodoroRepo repository) { this.repository = repository; }

    @Transactional(readOnly = true)
    public List<Pomodoro> pomodorosOfUser(String user, int page, int size) {
        requireNonNull(user);

        var request = PageRequest.of(page, size);

        return repository.findAllByUserOrderByTimingEndedDesc(user, request);
    }

    @Transactional(readOnly = true)
    public long countPomodorosOfUser(String user) {
        requireNonNull(user);
        return repository.countByUser(user);
    }

    @Transactional
    public Pomodoro initialize() {
        var pomodoro = repository.saveAndFlush(Pomodoro.builder().timing(Pomodoro.Timing.of()).build());
        LOG.debug("Initialized pomodoro = {}", pomodoro);
        return pomodoro;
    }

    @Transactional
    public Pomodoro update(Pomodoro changes) throws AccessDeniedException {
        if (changes.getId() == null) throw new IllegalArgumentException("Pomodoro not initialized yet: " + changes);
        var current = repository.findById(changes.getId())
                .orElseThrow(() -> new IllegalArgumentException("Pomodoro with ID " + changes.getId() + " was not initialized before"));
        Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .map(Principal::getName)
                .filter(current.getUser()::equals)
                .orElseThrow(() -> new AccessDeniedException("Unable to update pomodoro by changes " + changes + " by user " + SecurityContextHolder.getContext().getAuthentication()));

        return repository.save(current.updateBy(changes));
    }
}