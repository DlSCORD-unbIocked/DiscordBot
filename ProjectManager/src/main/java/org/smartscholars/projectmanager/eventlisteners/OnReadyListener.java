package org.smartscholars.projectmanager.eventlisteners;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.commands.misc.ImageOptionsCommand;

import java.awt.*;

public class OnReadyListener extends ListenerAdapter implements IEvent {
    private final CommandManager commandManager;

    public OnReadyListener(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void execute(GenericEvent event) {
        if (event instanceof ReadyEvent) {
            onReady((ReadyEvent) event);
        } else if (event instanceof GuildReadyEvent) {
            onGuildReady((GuildReadyEvent) event);
        }
    }

    public void onReady(@NotNull ReadyEvent event) {
        commandManager.setJda(event.getJDA());
        commandManager.registerCommands(event.getJDA());
    }

    public void onGuildReady(@NotNull GuildReadyEvent event) {
        commandManager.registerCommands(event.getGuild());
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        //to add more buttons, add more else if statements
        if (event.getComponentId().equals("left_image")) {
            EmbedBuilder embed = ImageOptionsCommand.nextPage(-1);
        }
//        if (event.getComponentId().equals("right_image")) {
//            ImageOptionsCommand imageOptionsCommand = new ImageOptionsCommand();
//            imageOptionsCommand.
//        }

//        else if (event.getComponentId().equals("help")) {
//            ImageOptionsCommand imageOptionsCommand = new ImageOptionsCommand();
//            EmbedBuilder embed = imageOptionsCommand.buildPageEmbed(pages.get(pageRequested - 1), pageRequested, pages.size());
//        }



    }


}