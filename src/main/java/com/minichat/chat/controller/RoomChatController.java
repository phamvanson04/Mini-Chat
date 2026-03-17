package com.minichat.chat.controller;

import com.minichat.chat.model.ChatMessage;
import com.minichat.chat.service.ChatService;
import com.minichat.chat.service.PresenceService;
import com.minichat.chat.service.RoomService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;

@Controller
public class RoomChatController {
    private final ChatService chatService;
    private final RoomService roomService;
    private final PresenceService presenceService;
    private final SimpMessageSendingOperations messagingTemplate;

    public RoomChatController(
            ChatService chatService,
            RoomService roomService,
            PresenceService presenceService,
            SimpMessageSendingOperations messagingTemplate
    ) {
        this.chatService = chatService;
        this.roomService = roomService;
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/room/{roomId}/send")
    public void sendRoomMessage(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
        roomService.getRoom(roomId);
        presenceService.markActivity(chatMessage.getSender());
        ChatMessage outbound = chatService.buildRoomChatMessage(chatMessage, roomId);
        messagingTemplate.convertAndSend(roomTopic(roomId), outbound);
    }

    @MessageMapping("/room/{roomId}/join")
    public void joinRoom(@DestinationVariable String roomId, @Payload ChatMessage chatMessage) {
        roomService.joinRoom(roomId, chatMessage.getSender());
        presenceService.markActivity(chatMessage.getSender());
        ChatMessage joinMessage = chatService.buildJoinMessage(chatMessage.getSender());
        joinMessage.setRoomId(roomId);
        messagingTemplate.convertAndSend(roomTopic(roomId), joinMessage);
    }

    private String roomTopic(String roomId) {
        return "/topic/rooms/" + roomId;
    }
}

