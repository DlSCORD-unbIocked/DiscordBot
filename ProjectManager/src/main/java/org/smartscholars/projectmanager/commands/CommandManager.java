package org.smartscholars.projectmanager.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.jetbrains.annotations.NotNull;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.vc.JoinVoiceChannelCommand;

import java.util.*;
import java.util.stream.Collectors;

public class CommandManager extends ListenerAdapter {

    private final HashMap<String, ICommand> commands = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    public CommandManager() {
        discoverAndRegisterCommands();
    }

    private void discoverAndRegisterCommands() {
        Reflections reflections = new Reflections("org.smartscholars.projectmanager.commands");
        Set<Class<? extends ICommand>> commandClasses = reflections.getSubTypesOf(ICommand.class);

        for (Class<? extends ICommand> commandClass : commandClasses) {
            if (commandClass.isAnnotationPresent(CommandInfo.class)) {
                CommandInfo info = commandClass.getAnnotation(CommandInfo.class);
                try {
                    ICommand commandInstance = commandClass.getDeclaredConstructor().newInstance();
                    commands.put(info.name(), commandInstance);
                }
                catch (Exception e) {
                    logger.error("Failed to instantiate command: {}", info.name(), e);
                }
            }
        }
    }

    private void registerCommands(Object target) {
        //create something to reset commands (do later)
        List<CommandData> commandDataList = commands.entrySet().stream()
                .map(entry -> Commands.slash(entry.getKey(), entry.getValue().getClass().getAnnotation(CommandInfo.class).description()))
                .collect(Collectors.toList());

        if (target instanceof Guild guild) {
            logger.info("Registering commands for guild: {}", guild.getName());
            guild.updateCommands().addCommands(commandDataList).queue();
        }
        else if (target instanceof JDA) {
            logger.info("Registering global commands");
            ((JDA) target).updateCommands().addCommands(commandDataList).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        ICommand command = commands.get(commandName);
        if (command != null) {
            try {
                command.execute(event);
            }
            catch (Exception e) {
                logger.error("Error executing command: {}", commandName, e);
            }
        }
    }

    //guild commands

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        registerCommands(event.getGuild());
    }

    //global commands

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        registerCommands(event.getJDA());
    }
}