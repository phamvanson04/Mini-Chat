package com.minichat.chat.controller;

import com.minichat.shared.response.BaseResponse;
import com.minichat.shared.response.ResponseFactory;
import com.minichat.chat.dto.CreateRoomRequest;
import com.minichat.chat.dto.JoinRoomRequest;
import com.minichat.chat.dto.RoomMessagesKeysetResponse;
import com.minichat.chat.model.ChatMessage;
import com.minichat.chat.model.ChatRoom;
import com.minichat.chat.service.ChatService;
import com.minichat.chat.service.RoomService;
import com.minichat.shared.error.BadRequestException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;
    private final ChatService chatService;

    public RoomController(RoomService roomService, ChatService chatService) {
        this.roomService = roomService;
        this.chatService = chatService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BaseResponse<ChatRoom> createRoom(@RequestBody CreateRoomRequest request) {
        ChatRoom room = roomService.createRoom(request.name(), request.owner());
        return ResponseFactory.success("Create room successfully", room);
    }

    @PostMapping("/{roomId}/join")
    public BaseResponse<ChatRoom> joinRoom(@PathVariable String roomId, @RequestBody JoinRoomRequest request) {
        ChatRoom room = roomService.joinRoom(roomId, request.username());
        return ResponseFactory.success("Join room successfully", room);
    }

    @GetMapping
    public BaseResponse<List<ChatRoom>> getRooms() {
        List<ChatRoom> rooms = roomService.getRooms();
        return ResponseFactory.success("Get rooms successfully", rooms);
    }

    @GetMapping("/{roomId}")
    public BaseResponse<ChatRoom> getRoom(@PathVariable String roomId) {
        ChatRoom room = roomService.getRoom(roomId);
        return ResponseFactory.success("Get room successfully", room);
    }

    @GetMapping("/{roomId}/messages")
    public BaseResponse<Page<ChatMessage>> getRoomMessages(
            @PathVariable String roomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        roomService.getRoom(roomId);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), 100);
        Pageable pageable = PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "timestamp"));
        Page<ChatMessage> messages = chatService.getRoomMessages(roomId, pageable);
        return ResponseFactory.success("Get room messages successfully", messages);
    }

    @GetMapping("/{roomId}/messages/keyset")
    public BaseResponse<RoomMessagesKeysetResponse> getRoomMessagesByKeyset(
            @PathVariable String roomId,
            @RequestParam(required = false) String beforeId,
            @RequestParam(required = false) String afterId,
            @RequestParam(defaultValue = "50") int size
    ) {
        roomService.getRoom(roomId);

        if (beforeId != null && afterId != null) {
            throw new BadRequestException("Use either before or after cursor, not both");
        }

        int safeSize = Math.min(Math.max(size, 1), 100);
        int fetchSize = safeSize + 1;

        if (afterId != null) {
            List<ChatMessage> rawMessages = new ArrayList<>(chatService.getRoomMessagesAfter(roomId, afterId, fetchSize));
            boolean hasMoreAfter = rawMessages.size() > safeSize;
            if (hasMoreAfter) {
                rawMessages = rawMessages.subList(0, safeSize);
            }

            RoomMessagesKeysetResponse response = toKeysetResponse(rawMessages, false, hasMoreAfter);
            return ResponseFactory.success("Get room messages by keyset successfully", response);
        }

        List<ChatMessage> rawMessages = beforeId == null
                ? new ArrayList<>(chatService.getLatestRoomMessages(roomId, fetchSize))
            : new ArrayList<>(chatService.getRoomMessagesBefore(roomId, beforeId, fetchSize));

        boolean hasMoreBefore = rawMessages.size() > safeSize;
        if (hasMoreBefore) {
            rawMessages = rawMessages.subList(0, safeSize);
        }

        Collections.reverse(rawMessages);
        RoomMessagesKeysetResponse response = toKeysetResponse(rawMessages, hasMoreBefore, false);
        return ResponseFactory.success("Get room messages by keyset successfully", response);
    }

    private RoomMessagesKeysetResponse toKeysetResponse(
            List<ChatMessage> messages,
            boolean hasMoreBefore,
            boolean hasMoreAfter
    ) {
        if (messages.isEmpty()) {
            return new RoomMessagesKeysetResponse(List.of(), null, null, hasMoreBefore, hasMoreAfter);
        }

        String beforeCursor = messages.getFirst().getId();
        String afterCursor = messages.getLast().getId();
        return new RoomMessagesKeysetResponse(messages, beforeCursor, afterCursor, hasMoreBefore, hasMoreAfter);
    }
}

