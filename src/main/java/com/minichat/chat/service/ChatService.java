package com.minichat.chat.service;

import com.minichat.shared.error.BadRequestException;
import com.minichat.chat.model.ChatMessage;
import com.minichat.chat.repository.ChatMessageRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

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

    public List<ChatMessage> getRoomMessages(String roomId) {
        if (!StringUtils.hasText(roomId)) {
            throw new BadRequestException("Room id must not be blank");
        }

        return chatMessageRepository.findByRoomIdOrderByTimestampAsc(roomId.trim());
    }

    public Page<ChatMessage> getRoomMessages(String roomId, Pageable pageable) {
        if (!StringUtils.hasText(roomId)) {
            throw new BadRequestException("Room id must not be blank");
        }

        return chatMessageRepository.findByRoomId(roomId.trim(), pageable);
    }

    public List<ChatMessage> getLatestRoomMessages(String roomId, int limit) {
        String normalizedRoomId = normalizeRoomId(roomId);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return chatMessageRepository.findByRoomIdOrderByIdDesc(normalizedRoomId, pageable);
    }

    public List<ChatMessage> getRoomMessagesBefore(String roomId, String beforeId, int limit) {
        String normalizedRoomId = normalizeRoomId(roomId);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "id"));
        return chatMessageRepository.findByRoomIdAndIdLessThanOrderByIdDesc(
                normalizedRoomId,
                beforeId,
                pageable
        );
    }

    public List<ChatMessage> getRoomMessagesAfter(String roomId, String afterId, int limit) {
        String normalizedRoomId = normalizeRoomId(roomId);
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.ASC, "id"));
        return chatMessageRepository.findByRoomIdAndIdGreaterThanOrderByIdAsc(
                normalizedRoomId,
                afterId,
                pageable
        );
    }

    private String normalizeRoomId(String roomId) {
        if (!StringUtils.hasText(roomId)) {
            throw new BadRequestException("Room id must not be blank");
        }

        return roomId.trim();
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

