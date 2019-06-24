package com.ksy.shortbreak.persistent.repository;

import com.ksy.shortbreak.persistent.entity.Pomodoro;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

public @Repository interface PomodoroRepo extends JpaRepository<Pomodoro, UUID> {
    List<Pomodoro> findAllByUserOrderByTimingEndedDesc(String user, Pageable pageable);

    long countByUser(String user);
}
