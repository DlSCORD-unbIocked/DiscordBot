package org.smartscholars.projectmanager.commands;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class CommandManager extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String command = event.getName();
        if (command.equals("ping")) {
            event.reply("Pong!").queue();
        }
    }

    // Guild commands
    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();
        commands.add(Commands.slash("ping", "test"));
        event.getGuild().updateCommands().addCommands(commands).queue();
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        List<CommandData> commands = new ArrayList<>();
        commands.add(Commands.slash("ping", "test"));
        event.getGuild().updateCommands().addCommands(commands).queue();
    }

    // Global commands
    @Override
    public void onReady(@NotNull ReadyEvent event) {
        List<CommandData> commands = new ArrayList<>();
        commands.add(Commands.slash("ping", "test"));
        event.getJDA().updateCommands().addCommands(commands).queue();
    }
}