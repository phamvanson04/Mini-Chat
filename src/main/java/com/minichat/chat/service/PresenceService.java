package com.minichat.chat.service;

import com.minichat.chat.event.UserPresenceEvent;
import com.minichat.chat.model.UserPresence;
import com.minichat.chat.repository.UserPresenceRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class PresenceService {
    private final UserPresenceRepository repository;
    private final ApplicationEventPublisher eventPublisher;
    private final Duration presenceTtl;

    public PresenceService(
            UserPresenceRepository repository,
            ApplicationEventPublisher eventPublisher,
            @Value("${app.presence.ttl-seconds}") long ttlSeconds
    ) {
        this.repository = repository;
        this.eventPublisher = eventPublisher;
        this.presenceTtl = Duration.ofSeconds(ttlSeconds);
    }

    public void userOnline(String username) {
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername == null) {
            return;
        }

        repository.setOnline(normalizedUsername, presenceTtl);
        publishEvent(normalizedUsername, UserPresence.Status.ONLINE);
    }

    public void userOffline(String username) {
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername == null) {
            return;
        }

        if (!repository.isOnline(normalizedUsername)) {
            return;
        }

        repository.deleteOnline(normalizedUsername);
        publishEvent(normalizedUsername, UserPresence.Status.OFFLINE);
    }

    public void markActivity(String username) {
        String normalizedUsername = normalizeUsername(username);
        if (normalizedUsername == null) {
            return;
        }

        if (repository.isOnline(normalizedUsername)) {
            repository.refreshTtl(normalizedUsername, presenceTtl);
            return;
        }

        userOnline(normalizedUsername);
    }

    private void publishEvent(String username, UserPresence.Status status) {
        UserPresence presence = UserPresence.builder()
                .username(username)
                .status(status)
                .timestamp(LocalDateTime.now())
                .build();
        eventPublisher.publishEvent(new UserPresenceEvent(this, presence));
    }

    private String normalizeUsername(String username) {
        if (username == null) {
            return null;
        }

        String normalizedUsername = username.trim();
        if (normalizedUsername.isEmpty()) {
            return null;
        }
        return normalizedUsername;
    }
}

