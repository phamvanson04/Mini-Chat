package com.minichat.chat.event;

import com.minichat.chat.model.UserPresence;
import org.springframework.context.ApplicationEvent;

public class UserPresenceEvent extends ApplicationEvent {
    private final UserPresence presence;
    public UserPresenceEvent(Object source, UserPresence presence) {
        super(source);
        this.presence = presence;
    }

    public UserPresence getPresence() {
        return presence;
    }
}

