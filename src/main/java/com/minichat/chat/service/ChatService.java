package com.minichat.chat.service;

import com.minichat.shared.error.BadRequestException;
import com.minichat.chat.model.ChatMessage;
import com.minichat.chat.repository.ChatMessageRepository;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatService(ChatMessageRepository chatMessageRepository) {
        this.chatMessageRepository = chatMessageRepository;
    }

    public ChatMessage buildChatMessage(ChatMessage incoming) {
        incoming.setTimestamp(LocalDateTime.now());
        incoming.setType(ChatMessage.MessageType.CHAT);
        chatMessageRepository.save(incoming);
        return incoming;
    }

    public ChatMessage buildRoomChatMessage(ChatMessage incoming, String roomId) {
        if (!StringUtils.hasText(roomId)) {
            throw new BadRequestException("Room id must not be blank");
        }

        incoming.setRoomId(roomId);
        incoming.setTimestamp(LocalDateTime.now());
        incoming.setType(ChatMessage.MessageType.CHAT);
        chatMessageRepository.save(incoming);
        return incoming;
    }

    public ChatMessage buildJoinMessage(String sender) {
        return ChatMessage.builder()
                .type(ChatMessage.MessageType.JOIN)
                .sender(sender)
                .timestamp(LocalDateTime.now())
                .build();
    }

    public ChatMessage buildLeaveMessage(String sender) {
        return ChatMessage.builder()
                .type(ChatMessage.MessageType.LEAVE)
                .sender(sender)
                .timestamp(LocalDateTime.now())
                .build();
    }
}

