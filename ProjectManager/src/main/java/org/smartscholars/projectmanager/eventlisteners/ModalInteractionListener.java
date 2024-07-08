package org.smartscholars.projectmanager.eventlisteners;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class ModalInteractionListener extends ListenerAdapter implements IEvent{

    @Override
    public void execute(GenericEvent event) {
        onModalInteraction((ModalInteractionEvent) event);
    }

    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String[] parts = event.getModalId().split(":", 2);
        if (parts[0].equals("activityDateModal") && parts.length > 1) {
            String activity = parts[1];
            String date = Objects.requireNonNull(event.getValue("date")).getAsString();
            String time = Objects.requireNonNull(event.getValue("time")).getAsString();
            event.reply("@everyone react to do " + activity + " on " + date + " at " + time).queue(interactionHook -> {
                interactionHook.retrieveOriginal().queue(message -> {
                    message.addReaction(Emoji.fromUnicode("U+1F44D")).queue(); // 👍 reaction
                });
            });
        }
    }
}

