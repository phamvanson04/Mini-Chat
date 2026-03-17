package com.minichat.chat.listener;

import com.minichat.chat.event.UserPresenceEvent;
import com.minichat.chat.model.ChatMessage;
import com.minichat.chat.model.UserPresence;
import com.minichat.chat.service.ChatService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Component;

@Component
public class PresenceEventListener {
    private final SimpMessageSendingOperations messagingTemplate;
    private final ChatService chatService;

    public PresenceEventListener(
            SimpMessageSendingOperations messagingTemplate,
            ChatService chatService
    ) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    @EventListener
    public void handlePresenceEvent(UserPresenceEvent event) {
        UserPresence presence = event.getPresence();
        messagingTemplate.convertAndSend("/topic/presence", presence);

        if (presence.getStatus() == UserPresence.Status.OFFLINE) {
            ChatMessage leaveMessage = chatService.buildLeaveMessage(presence.getUsername());
            messagingTemplate.convertAndSend("/topic/public", leaveMessage);
        }
    }
}

