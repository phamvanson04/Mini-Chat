package com.minichat.chat.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPresence {
    private String username;
    private Status status;
    private LocalDateTime timestamp;

    public enum Status {
        ONLINE,
        OFFLINE
    }
}

