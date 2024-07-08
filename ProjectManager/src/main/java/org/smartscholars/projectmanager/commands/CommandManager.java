package org.smartscholars.projectmanager.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.administrator.ReloadCommand;
import org.smartscholars.projectmanager.util.CustomClassLoader;
import org.smartscholars.projectmanager.util.FileWatcher;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class CommandManager extends ListenerAdapter {

    private final HashMap<String, Class<? extends ICommand>> commandClasses = new HashMap<>();
    private final HashMap<String, ICommand> commandInstances = new HashMap<>();
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);
    private static JDA jda;

    private CustomClassLoader customClassLoader = new CustomClassLoader(getClass().getClassLoader());

    public CommandManager() {
        logger.info("Initializing CommandManager");
        loadCommandsFromConfiguration();
        Path configPath = Paths.get("ProjectManager/src/main/resources").toAbsolutePath();
        FileWatcher watcher = new FileWatcher(configPath, this);
        Thread watcherThread = new Thread(watcher);
        watcherThread.start();
    }

    public void reloadCommands(Guild guild) throws ClassNotFoundException {
        logger.info("Reloading commands in {}", guild.getName());

        commandClasses.clear();
        commandInstances.clear();
        customClassLoader = new CustomClassLoader(getClass().getClassLoader());
        loadCommandsFromConfiguration();

        commandClasses.keySet().forEach(commandName -> {
            ICommand commandInstance = createCommandInstance(commandName);
            if (commandInstance != null) {
                logger.info("Created instance for command: {}", commandName);
                commandInstances.put(commandName, commandInstance);
            }
            else {
                logger.error("Failed to create instance for command: {}", commandName);
            }
        });
        reloadGuildCommands(guild);
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
            CustomClassLoader customClassLoader = new CustomClassLoader(getClass().getClassLoader());
            Class<?> clazz = customClassLoader.loadClass(className, true);
            logger.info("Loaded class: {}", clazz.getName());
            Class<?>[] interfaces = clazz.getInterfaces();
            for (Class<?> iface : interfaces) {
                logger.info("Class {} implements interface: {}", clazz.getName(), iface.getName());
                if (ICommand.class.equals(iface)) {
                    logger.info("Interface {} is ICommand.", iface.getName());
                }
                else {
                    logger.info("Interface {} is not ICommand.", iface.getName());
                }
            }

            if (ICommand.class.isAssignableFrom(clazz)) {
                @SuppressWarnings("unchecked")
                Class<? extends ICommand> commandClass = (Class<? extends ICommand>) clazz;
                if (commandClass.isAnnotationPresent(CommandInfo.class)) {
                    CommandInfo info = commandClass.getAnnotation(CommandInfo.class);
                    logger.info("Loading command: {}", info.name());
                    commandClasses.put(info.name(), commandClass);
                } else {
                    logger.error("Command class does not have CommandInfo annotation: {}", className);
                }
            } else {
                logger.error("Class does not implement ICommand interface: {}", className);
            }
        }
        catch (NoClassDefFoundError | Exception e) {
            logger.error("Error loading command: {}", className, e);
        }
    }

    public void registerCommands(Object target) {
        //create something to reset commands (do later)
        List<CommandData> commandDataList = new ArrayList<>();
        for (String commandName : commandClasses.keySet()) {
            ICommand commandInstance = createCommandInstance(commandName);
            if (commandInstance != null) {
                CommandInfo info = commandInstance.getClass().getAnnotation(CommandInfo.class);
                SlashCommandData commandData = Commands.slash(info.name(), info.description());
                commandData.addOptions(Arrays.stream(info.options())
                        .map(option -> new OptionData(option.type(), option.name(), option.description(), option.required())).toArray(OptionData[]::new));
                commandDataList.add(commandData);
            }
        }

        if (target instanceof Guild guild) {
            try {
                logger.info("Registering commands for guild: {}", guild.getName());
                guild.updateCommands().addCommands(commandDataList).queue();
            }
            catch (Exception e) {
                logger.error("Failed to register commands for guild: {}", guild.getName(), e);
            }
        }
        else if (target instanceof JDA) {
            try {
                logger.info("Registering global commands");
                ((JDA) target).updateCommands().addCommands(commandDataList).queue();
            }
            catch (Exception e) {
                logger.error("Failed to register global commands", e);
            }
        }
    }
    public void reloadGuildCommands(Guild guild) {
        List<CommandData> commandDataList = commandClasses.values().stream()
            .map(this::createCommandDataFromCommandClass)
            .collect(Collectors.toList());

        guild.updateCommands().queue(
            _ -> logger.info("Successfully cleared commands in Discord"),
            failure -> logger.error("Failed to clear commands in Discord", failure)
        );

        guild.updateCommands().addCommands(commandDataList).queue(
            _ -> logger.info("Successfully registered new commands"),
            failure -> logger.error("Failed to register new commands", failure)
        );
    }

    private CommandData createCommandDataFromCommandClass(Class<? extends ICommand> commandClass) {
        CommandInfo info = commandClass.getAnnotation(CommandInfo.class);
        SlashCommandData commandData = Commands.slash(info.name(), info.description());
        for (CommandOption option : info.options()) {
            OptionData optionData = new OptionData(option.type(), option.name(), option.description(), option.required());
            commandData.addOptions(optionData);
        }
        return commandData;
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        String commandName = event.getName();
        ICommand command = commandInstances.get(commandName);
        if (command == null) {
            command = createCommandInstance(commandName);
            if (command != null) {
                commandInstances.put(commandName, command);
            }
            else {
                logger.error("Could not create command instance for: {}", commandName);
                return;
            }
        }
        CommandInfo info = command.getClass().getAnnotation(CommandInfo.class);
        boolean hasPermission = Arrays.stream(info.permissions()).allMatch(permission ->
                hasPermission(event.getMember(), permission));
        if (!hasPermission) {
            event.reply("You do not have permission to use this command").setEphemeral(true).queue();
            return;
        }
        try {
            command.execute(event);
        }
        catch (Exception e) {
            logger.error("Error executing command: {}", commandName, e);
            command.fallback(event, e);
        }
    }

    private ICommand createCommandInstance(String commandName) {
        Class<? extends ICommand> commandClass = commandClasses.get(commandName);
        if (commandClass == null) {
            logger.error("Command class not found for command: {}", commandName);
            return null;
        }
        try {
            ICommand instance;
            if (ReloadCommand.class.isAssignableFrom(commandClass)) {
                instance = commandClass.getDeclaredConstructor(CommandManager.class).newInstance(this);
            } else {
                instance = commandClass.getDeclaredConstructor().newInstance();
            }
            return instance;
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            logger.error("Failed to instantiate command: {}", commandName, e);
            return null;
        }
    }

    private boolean hasPermission(Member member, Permission permission) {
        return switch (permission) {
            case ADMINISTRATOR -> member != null && member.hasPermission(net.dv8tion.jda.api.Permission.ADMINISTRATOR);
            case MODERATOR -> member != null && member.hasPermission(net.dv8tion.jda.api.Permission.KICK_MEMBERS);
            case MEMBER -> member != null;
        };
    }

    public static HashMap<String, Class<? extends ICommand>> getCommandClasses() {
        return new CommandManager().commandClasses;
    }

    public void setJda(JDA jda) {
        CommandManager.jda = jda;
    }

    public static JDA getJda() {
        return jda;
    }

    public CustomClassLoader getCustomClassLoader() {
        return customClassLoader;
    }
}