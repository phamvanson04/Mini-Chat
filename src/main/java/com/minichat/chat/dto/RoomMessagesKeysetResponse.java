package com.minichat.chat.dto;

import com.minichat.chat.model.ChatMessage;

import java.util.List;

public record RoomMessagesKeysetResponse(
        List<ChatMessage> messages,
        String beforeCursor,
        String afterCursor,
        boolean hasMoreBefore,
        boolean hasMoreAfter
) {
}
