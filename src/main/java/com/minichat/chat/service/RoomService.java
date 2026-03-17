package com.minichat.chat.service;

import com.minichat.shared.error.BadRequestException;
import com.minichat.shared.error.NotFoundException;
import com.minichat.chat.model.ChatRoom;
import com.minichat.chat.repository.ChatRoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
    private final ChatRoomRepository chatRoomRepository;

    public RoomService(ChatRoomRepository chatRoomRepository) {
        this.chatRoomRepository = chatRoomRepository;
    }

    public ChatRoom createRoom(String name, String owner) {
        validate(name, "Room name must not be blank");
        validate(owner, "Owner must not be blank");

        ChatRoom room = ChatRoom.builder()
                .name(name.trim())
                .owner(owner.trim())
                .build();
        room.getMembers().add(owner.trim());
        return chatRoomRepository.save(room);
    }

    public ChatRoom joinRoom(String roomId, String username) {
        validate(username, "Username must not be blank");

        ChatRoom room = getRoom(roomId);
        room.getMembers().add(username.trim());
        return chatRoomRepository.save(room);
    }

    public ChatRoom getRoom(String roomId) {
        validate(roomId, "Room id must not be blank");
        return chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new NotFoundException("Room not found: " + roomId));
    }

    public List<ChatRoom> getRooms() {
        return chatRoomRepository.findAll();
    }

    private void validate(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
    }
}

