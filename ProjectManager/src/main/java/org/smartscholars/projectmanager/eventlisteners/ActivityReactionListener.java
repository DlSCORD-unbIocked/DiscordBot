package org.smartscholars.projectmanager.eventlisteners;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.Objects;

public class ActivityReactionListener extends ListenerAdapter implements IEvent {

    private final Logger logger = LoggerFactory.getLogger(ActivityReactionListener.class);

    @Override
    public void execute(GenericEvent event) {
        onMessageReactionAdd((MessageReactionAddEvent) event);
    }

    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        boolean validMessage = isValidMessage(event.getMessageId());
        if (event.getReaction().getEmoji().getAsReactionCode().equals("👍") && !Objects.requireNonNull(event.getUser()).isBot()) {
            event.getChannel().sendMessage("You reacted with a thumbs up!").queue();
        }
    }

    public boolean isValidMessage(String messageId) {
        return true;
    }

}
