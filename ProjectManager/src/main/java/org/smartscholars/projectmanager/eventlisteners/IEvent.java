package org.smartscholars.projectmanager.eventlisteners;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface IEvent {
    void execute(GenericEvent event);
//    void initialize();
//    void cleanup();
}
