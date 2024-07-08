package org.smartscholars.projectmanager.commands.administrator;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.Permission;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@CommandInfo(name = "reload", description = "Reloads the commands", permissions = {Permission.ADMINISTRATOR})
public class ReloadCommand implements ICommand {

    private final CommandManager commandManager;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(ReloadCommand.class);

    public ReloadCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!Objects.requireNonNull(event.getGuild()).getId().equals("1086425022245118033")) {
            return;
        }
        // Acknowledge the interaction immediately
        event.deferReply().queue();

        new Thread(() -> {
            StringBuilder responseBuilder = new StringBuilder();
            try {
                String mavenPath = "C:\\Program Files\\JetBrains\\IntelliJ IDEA 2024.1.4\\plugins\\maven\\lib\\maven3\\bin\\mvn.cmd";
                String pomPath = System.getProperty("user.dir") + "\\ProjectManager\\pom.xml";
                String projectDirectory = System.getProperty("user.dir") + "\\ProjectManager";
                ProcessBuilder builder = new ProcessBuilder(mavenPath, "-f", pomPath, "compile");
                builder.redirectErrorStream(true);
                Process process = builder.start();

                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        logger.info(line); // Log Maven output for debugging
                    }
                }

                int exitCode = process.waitFor();
                if (exitCode == 0) {
                    responseBuilder.append("Maven compilation successful.\n");
                }
                else {
                    responseBuilder.append("Maven compilation failed.\n");
                }

                if (exitCode == 0) {
                    Process processRun = getProcess(mavenPath, projectDirectory);
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(processRun.getInputStream()))) {
                        String line;
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line); // Optionally log the output
                        }
                    }
                    int exitCodeRun = processRun.waitFor();
                    if (exitCodeRun == 0) {
                        responseBuilder.append("Project run successfully.\n");
                    } else {
                        responseBuilder.append("Project run failed.\n");
                    }
                }

                if (event.isAcknowledged()) {
                    event.getHook().editOriginal(responseBuilder.toString()).queue();
                }
                else {
                    event.reply(responseBuilder.toString()).setEphemeral(true).queue();
                }
            }
            catch (IOException | InterruptedException e) {
                Thread.currentThread().interrupt();
                logger.error("An error occurred: ", e);
            }
        }).start();
    }

    private static @NotNull Process getProcess(String mavenPath, String projectDirectory) throws IOException {
        Path envPath = Paths.get(System.getProperty("user.dir"), ".env");
        List<String> lines = Files.readAllLines(envPath);
        Map<String, String> env = new HashMap<>();
        for (String line : lines) {
            String[] parts = line.split("=", 2);
            if (parts.length == 2) {
                env.put(parts[0].trim(), parts[1].trim());
            }
        }

        ProcessBuilder builderRun = new ProcessBuilder(mavenPath, "exec:java", "-Dexec.mainClass=\"org.smartscholars.projectmanager.ProjectManager\"");
        builderRun.directory(new File(projectDirectory));
        builderRun.redirectErrorStream(true);
        Map<String, String> processEnvironment = builderRun.environment();
        processEnvironment.putAll(env); // Set environment variables for the process
        return builderRun.start();
    }
}