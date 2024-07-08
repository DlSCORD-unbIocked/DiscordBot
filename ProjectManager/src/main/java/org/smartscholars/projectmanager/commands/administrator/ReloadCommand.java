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
import java.util.concurrent.TimeUnit;

@CommandInfo(name = "reload", description = "Reloads the commands", permissions = {Permission.ADMINISTRATOR})
public class ReloadCommand implements ICommand {

    private final CommandManager commandManager;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(ReloadCommand.class);
    private Process currentProcess = null;

    public ReloadCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!Objects.requireNonNull(event.getGuild()).getId().equals("1086425022245118033")) {
            return;
        }
        event.deferReply().queue();

        new Thread(() -> {
            StringBuilder responseBuilder = new StringBuilder();
            try {
                // Assuming terminateExistingProcess() and getProcess() are defined elsewhere
                terminateExistingProcess(); // Terminate any existing process

                // Define the command and arguments for the new process
                List<String> command = List.of("java", "-jar", "path/to/your/application.jar");

                // Create a new ProcessBuilder
                ProcessBuilder builder = new ProcessBuilder(command);
                builder.redirectErrorStream(true); // Redirect error stream to standard output

                // Start the new process
                Process newProcess = builder.start();
                currentProcess = newProcess; // Store the new process reference for management

                // Optionally, read the output of the new process
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(newProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        System.out.println(line); // Or log the line
                    }
                }

                // Note: We do not call newProcess.waitFor() here to allow the original process to end independently

                // Respond to the event
                responseBuilder.append("New process started.");
                if (event.isAcknowledged()) {
                    event.getHook().editOriginal(responseBuilder.toString()).queue();
                } else {
                    event.reply(responseBuilder.toString()).setEphemeral(true).queue();
                }
            } catch (IOException e) {
                Thread.currentThread().interrupt();
                logger.error("An error occurred: ", e);
            }
        }).start();
    }

    private static @NotNull Process getProcess(String mavenPath, String projectDirectory) throws IOException {
        Path envPath = Paths.get(System.getProperty("user.dir"), ".", ".env").normalize();
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

    public void terminateExistingProcess() {
        try {
            // Command to find the process ID (PID) of the running application
            List<String> findProcessCommand = List.of("cmd", "/c", "tasklist | findstr java.exe");
            ProcessBuilder findProcessBuilder = new ProcessBuilder(findProcessCommand);
            Process findProcess = findProcessBuilder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(findProcess.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                // Extract the PID from the command output
                String[] parts = line.split("\\s+");
                String pid = parts[1]; // Assuming the PID is in the second column

                // Command to kill the process using its PID
                List<String> killCommand = List.of("cmd", "/c", "taskkill /F /PID " + pid);
                ProcessBuilder killProcessBuilder = new ProcessBuilder(killCommand);
                Process killProcess = killProcessBuilder.start();
                killProcess.waitFor(); // Wait for the kill command to complete
                System.out.println("Terminated process with PID: " + pid);
            }
            reader.close();
        } catch (IOException | InterruptedException e) {
            logger.error("Failed to terminate existing process.", e);
            Thread.currentThread().interrupt(); // Restore the interrupted status
        }
    }
}