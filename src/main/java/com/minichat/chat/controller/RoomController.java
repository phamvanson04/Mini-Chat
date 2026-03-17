package com.minichat.chat.controller;

import com.minichat.shared.response.BaseResponse;
import com.minichat.shared.response.ResponseFactory;
import com.minichat.chat.dto.CreateRoomRequest;
import com.minichat.chat.dto.JoinRoomRequest;
import com.minichat.chat.model.ChatRoom;
import com.minichat.chat.service.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
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
}

