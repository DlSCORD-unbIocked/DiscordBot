package org.smartscholars.projectmanager.commands;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public interface ICommand {
    void execute(SlashCommandInteractionEvent event);
    default void fallback(SlashCommandInteractionEvent event, Exception e) {
        event.reply("An error occurred while executing the command. Please try again later.").queue();
    }
}
