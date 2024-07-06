package org.smartscholars.projectmanager.eventlisteners;

import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.smartscholars.projectmanager.commands.CommandManager;

public class OnReadyListener extends ListenerAdapter {
    private final CommandManager commandManager;

    public OnReadyListener(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        commandManager.setJda(event.getJDA());
        commandManager.registerCommands(event.getJDA());
    }

    //guild commands

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        commandManager.registerCommands(event.getGuild());
    }
}
