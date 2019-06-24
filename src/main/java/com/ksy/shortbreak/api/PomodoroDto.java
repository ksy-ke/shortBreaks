package com.ksy.shortbreak.api;

import com.ksy.shortbreak.persistent.entity.Pomodoro;

import javax.validation.constraints.NotNull;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

import static java.util.Objects.requireNonNull;

public final class PomodoroDto {
    private @NotNull UUID id;
    private String name;
    private Integer durationSeconds;
    private Integer durationMinutes;
    private Integer durationHours;
    private String started;
    private String ended;

    public PomodoroDto() {}

    public UUID getId() { return id; }

    public String getName() { return name; }

    public Integer getDurationSeconds() { return durationSeconds; }

    public Integer getDurationMinutes() { return durationMinutes; }

    public Integer getDurationHours() { return durationHours; }

    public String getStarted() { return started; }

    public String getEnded() { return ended; }

    public PomodoroDto setId(UUID id) { this.id = id; return this; }

    public PomodoroDto setName(String name) { this.name = name; return this; }

    public PomodoroDto setDurationSeconds(Integer durationSeconds) { this.durationSeconds = durationSeconds; return this; }

    public PomodoroDto setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; return this; }

    public PomodoroDto setDurationHours(Integer durationHours) { this.durationHours = durationHours; return this; }

    public PomodoroDto setStarted(String started) { this.started = started; return this; }

    public PomodoroDto setEnded(String ended) { this.ended = ended; return this; }

    public Pomodoro toPomodoro() {
        var duration = calculateDuration();
        var timing = Pomodoro.Timing.of(
                duration,
                started == null || started.isBlank() ? null : OffsetDateTime.parse(started),
                ended == null || ended.isBlank() ? null : OffsetDateTime.parse(ended));
        return Pomodoro.builder().id(id).name(name).timing(timing).build();
    }

    public static PomodoroDto ofPomodoro(Pomodoro pomodoro) {
        requireNonNull(pomodoro);
        var dto = new PomodoroDto()
                .setId(pomodoro.getId())
                .setName(pomodoro.getName());
        if (pomodoro.getTiming() != null) {
            var started = pomodoro.getTiming().getStarted();
            var ended = pomodoro.getTiming().getEnded();
            dto.setStarted(started != null ? started.toString() : null)
                    .setEnded(ended != null ? ended.toString() : null);

            var duration = pomodoro.getTiming().getDuration();
            if (duration != null) {
                dto.setDurationHours(duration.toHoursPart())
                        .setDurationMinutes(duration.toMinutesPart())
                        .setDurationSeconds(duration.toSecondsPart());
            }
        }
        return dto;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PomodoroDto)) return false;
        var that = (PomodoroDto) o;
        return durationSeconds == that.durationSeconds &&
                durationMinutes == that.durationMinutes &&
                durationHours == that.durationHours &&
                Objects.equals(id, that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(started, that.started) &&
                Objects.equals(ended, that.ended);
    }

    @Override
    public int hashCode() { return Objects.hash(id, name, durationSeconds, durationMinutes, durationHours, started, ended); }

    @Override
    public String toString() {
        return "PomodoroDto{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", durationSeconds=" + durationSeconds +
                ", durationMinutes=" + durationMinutes +
                ", durationHours=" + durationHours +
                ", started='" + started + '\'' +
                ", ended='" + ended + '\'' +
                '}';
    }

    private Duration calculateDuration() {
        if (durationSeconds == null && durationMinutes == null && durationHours == null) return null;

        var duration = Duration.ZERO;
        if (durationHours != null) duration = duration.plusHours(durationHours);
        if (durationMinutes != null) duration = duration.plusMinutes(durationMinutes);
        if (durationSeconds != null) duration = duration.plusSeconds(durationSeconds);
        return duration;
    }
}
