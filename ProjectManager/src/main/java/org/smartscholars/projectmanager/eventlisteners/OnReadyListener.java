package org.smartscholars.projectmanager.eventlisteners;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;


import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.smartscholars.projectmanager.commands.CommandManager;


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

}