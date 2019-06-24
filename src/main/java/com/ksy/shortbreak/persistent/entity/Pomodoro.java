package com.ksy.shortbreak.persistent.entity;

import org.hibernate.annotations.GenericGenerator;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.persistence.*;
import java.security.Principal;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static com.ksy.shortbreak.config.Security.USER_ROLE;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.util.Objects.requireNonNull;

public final @Entity class Pomodoro {
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(name = "UUID", strategy = "org.hibernate.id.UUIDGenerator")
    private @Id UUID id;
    private @Column(nullable = false) String user;
    private @Column String name;
    private @Embedded Timing timing;

    public static Pomodoro of(Pomodoro pomodoro) {
        return Pomodoro.builder()
                .id(pomodoro.getId())
                .user(pomodoro.getUser())
                .name(pomodoro.getName())
                .timing(Timing.of(pomodoro.getTiming()))
                .build();
    }

    public Pomodoro updateBy(Pomodoro update) {
        if (!this.id.equals(update.id)) throw new IllegalArgumentException("ID should be same");
        if (update.name != null) this.setName(update.name);
        this.timing.updateBy(update.timing);
        return this;
    }

    public static Builder builder() { return new Builder(); }

    public UUID getId() { return id; }

    public String getUser() { return user; }

    public String getName() { return name; }

    public Timing getTiming() { return timing; }

    public void setName(String name) { this.name = requireNonNull(name); }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pomodoro)) return false;
        var that = (Pomodoro) o;
        return Objects.equals(this.id, that.id) &&
                Objects.equals(this.user, that.user) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.timing, that.timing);
    }

    @Override
    public int hashCode() { return Objects.hash(id, user, name, timing); }

    @Override
    public String toString() { return "Pomodoro{id=" + id + ", name='" + name + "', timing=" + timing + '}'; }

    public static final @Embeddable class Timing {
        static final Duration DEFAULT_DURATION = Duration.of(5, SECONDS);  // TODO: move to config

        private @Column(nullable = false) Duration duration;
        private @Column OffsetDateTime started;
        private @Column OffsetDateTime ended;

        public static Timing of() { return Timing.of(DEFAULT_DURATION); }

        public static Timing of(Duration duration) { return Timing.of(duration, null, null); }

        public static Timing of(Duration duration, OffsetDateTime started, OffsetDateTime ended) {
            verifyDuration(duration);
            verifyStartedAndEnded(started, ended);

            return new Timing(duration, started, ended);
        }

        public static Timing of(Timing timing) {
            return Timing.of(timing.getDuration(), timing.getStarted(), timing.getEnded());
        }

        public Timing updateBy(Timing update) {
            if (update.duration != null) this.setDuration(update.duration);
            if (update.started != null) this.setStarted(update.started);
            if (update.ended != null) this.setEnded(update.ended);
            return this;
        }

        public Duration getDuration() { return duration; }

        public void setDuration(Duration duration) {
            verifyDuration(duration);
            this.duration = duration;
        }

        public OffsetDateTime getStarted() { return started; }

        public void setStarted(OffsetDateTime started) {
            verifyStartedAndEnded(started, ended);
            this.started = started;
        }

        public OffsetDateTime getEnded() { return ended; }

        public void setEnded(OffsetDateTime ended) {
            verifyStartedAndEnded(started, ended);
            this.ended = ended;
        }

        @Override
        public String toString() { return "Timing{duration=" + duration + ", started=" + started + ", ended=" + ended + '}'; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Timing)) return false;
            var that = (Timing) o;
            return Objects.equals(this.duration, that.duration) &&
                    Objects.equals(this.started, that.started) &&
                    Objects.equals(this.ended, that.ended);
        }

        @Override
        public int hashCode() { return Objects.hash(duration, started, ended); }

        private Timing() {}

        private Timing(Duration duration, OffsetDateTime started, OffsetDateTime ended) {
            this.duration = duration;
            this.started = started;
            this.ended = ended;
        }

        private static void verifyDuration(Duration duration) {
            if (duration == null) return;
            if (duration.isNegative() || duration.isZero())
                throw new IllegalArgumentException("Pomodoro duration should be positive");
        }

        private static void verifyStartedAndEnded(OffsetDateTime started, OffsetDateTime ended) {
            if (started != null && ended != null && started.isAfter(ended))
                throw new IllegalArgumentException("Unable to set started = " + started + " that is after ended = " + ended);
        }
    }

    public static final class Builder {
        private UUID id;
        private String user;
        private String name;
        private Timing timing;

        public Builder id(UUID id) { this.id = id; return this; }

        public Builder user(String user) { this.user = user; return this; }

        public Builder name(String name) { this.name = name; return this; }

        public Builder timing(Timing timing) { this.timing = timing; return this; }

        public Pomodoro build() { return new Pomodoro(this); }

        private Builder() {}
    }

    private Pomodoro(Builder builder) {
        id = builder.id;
        name = builder.name;
        user = builder.user;
        timing = builder.timing;

        if (timing == null) throw new NullPointerException("Timing have to be specified");
        if (user == null) user = getCurrentUser();
    }

    private String getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
                .filter(Authentication::isAuthenticated)
                .filter(authentication -> authentication.getAuthorities().stream().anyMatch(USER_ROLE::equals))
                .map(Principal::getName)
                .orElseThrow(() -> new AccessDeniedException("Failed to initialize pomodoro for unauthenticated user"));
    }

    private Pomodoro() {}
}
