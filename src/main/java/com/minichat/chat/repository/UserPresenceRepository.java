package com.minichat.chat.repository;

import java.time.Duration;

public interface UserPresenceRepository {
    void setOnline(String username, Duration ttl);

    void refreshTtl(String username, Duration ttl);

    void deleteOnline(String username);

    boolean isOnline(String username);
}

