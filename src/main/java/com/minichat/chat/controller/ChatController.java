package com.minichat.chat.controller;

import com.minichat.chat.dto.PresenceHeartbeatRequest;
import com.minichat.chat.model.ChatMessage;
import com.minichat.chat.service.ChatService;
import com.minichat.chat.service.PresenceService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {
    private final ChatService chatService;
    private final PresenceService presenceService;

    public ChatController(ChatService chatService, PresenceService presenceService) {
        this.chatService = chatService;
        this.presenceService = presenceService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(@Payload ChatMessage chatMessage) {
        presenceService.markActivity(chatMessage.getSender());
        return chatService.buildChatMessage(chatMessage);
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(
            @Payload ChatMessage chatMessage,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        headerAccessor.getSessionAttributes().put("username", chatMessage.getSender());
        presenceService.markActivity(chatMessage.getSender());
        return chatService.buildJoinMessage(chatMessage.getSender());
    }

    @MessageMapping("/presence.heartbeat")
    public void heartbeat(@Payload PresenceHeartbeatRequest request) {
        if (request != null) {
            presenceService.markActivity(request.username());
        }
    }
}

