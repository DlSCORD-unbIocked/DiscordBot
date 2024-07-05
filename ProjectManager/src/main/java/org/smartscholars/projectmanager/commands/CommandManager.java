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
import org.smartscholars.projectmanager.util.FileWatcher;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CommandManager extends ListenerAdapter {

    private final HashMap<String, Class<? extends ICommand>> commandClasses = new HashMap<>();
    private final HashMap<String, ICommand> commandInstances = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    public CommandManager() {
        logger.info("Initializing CommandManager");
        loadCommandsFromConfiguration();
        Path configPath = Paths.get("ProjectManager/src/main/resources").toAbsolutePath();
        // Pass `this` to use the current instance instead of creating a new one
        FileWatcher watcher = new FileWatcher(configPath, this);
        Thread watcherThread = new Thread(watcher);
        watcherThread.start();
    }

    public void reloadCommands() {
        logger.info("Reloading commands");
        commandClasses.clear();
        commandInstances.clear();
        loadCommandsFromConfiguration();
    }

    private void loadCommandsFromConfiguration() {
        logger.info("Loading commands from configuration");
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("commands.config")) {
            assert input != null;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input))) {
                reader.lines().forEach(this::loadAndRegisterCommand);
            }
        }
        catch (Exception e) {
            logger.error("Failed to load commands from configuration", e);
        }
    }

    private void loadAndRegisterCommand(String className) {
        try {
            Class<?> clazz = Class.forName(className);
            if (ICommand.class.isAssignableFrom(clazz)) {
                @SuppressWarnings("unchecked")
                Class<? extends ICommand> commandClass = (Class<? extends ICommand>) clazz;
                if (commandClass.isAnnotationPresent(CommandInfo.class)) {
                    CommandInfo info = commandClass.getAnnotation(CommandInfo.class);
                    logger.info("Loading command: {}", info.name());
                    commandClasses.put(info.name(), commandClass);
                }
                else {
                    logger.error("Command class does not have CommandInfo annotation: {}", className);
                }
            }
            else {
                logger.error("Class does not implement ICommand interface: {}", className);
            }
        }
        catch (ClassNotFoundException e) {
            logger.error("Class not found for command: {}", className, e);
        }
        catch (ClassCastException e) {
            logger.error("Class cannot be cast to ICommand: {}", className, e);
        }
        catch (Exception e) {
            logger.error("Unexpected error while loading command: {}", className, e);
        }
    }

    private void registerCommands(Object target) {
        //create something to reset commands (do later)
        List<CommandData> commandDataList = commandClasses.entrySet().stream()
                .map(entry -> Commands.slash(entry.getKey(), entry.getValue().getAnnotation(CommandInfo.class).description()))
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
        ICommand command = commandInstances.get(commandName);
        if (command == null) {
            Class<? extends ICommand> commandClass = commandClasses.get(commandName);
            if (commandClass != null) {
                try {
                    command = commandClass.getDeclaredConstructor().newInstance();
                    commandInstances.put(commandName, command);
                } catch (Exception e) {
                    logger.error("Failed to instantiate command: {}", commandName, e);
                    return;
                }
            }
        }
        if (command != null) {
            try {
                command.execute(event);
            } catch (Exception e) {
                logger.error("Error executing command: {}", commandName, e);
                command.fallback(event, e); // Use the fallback method
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