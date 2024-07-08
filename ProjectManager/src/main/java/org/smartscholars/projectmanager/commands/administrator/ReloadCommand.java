package org.smartscholars.projectmanager.commands.administrator;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.Permission;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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


                commandManager.reloadCommands(event.getGuild());


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
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}