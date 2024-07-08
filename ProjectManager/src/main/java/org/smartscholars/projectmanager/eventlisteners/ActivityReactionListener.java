package org.smartscholars.projectmanager.eventlisteners;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class ActivityReactionListener extends ListenerAdapter implements IEvent{
    @Override
    public void execute(GenericEvent event) {
        onMessageReactionAdd((MessageReactionAddEvent) event);
    }


}
