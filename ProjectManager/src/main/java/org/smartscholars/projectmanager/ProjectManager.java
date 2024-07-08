package org.smartscholars.projectmanager;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.eventlisteners.EventListenerManager;
import org.smartscholars.projectmanager.eventlisteners.IEvent;
import org.smartscholars.projectmanager.eventlisteners.OnReadyListener;

import javax.security.auth.login.LoginException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

public class ProjectManager {

    private final ShardManager shardManager;
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);

    public ProjectManager() throws LoginException {

        Dotenv dotenv = Dotenv.configure()
                      .directory("C:\\Users\\paulc\\OneDrive\\Documents\\Custom Office Templates\\Desktop\\DiscordBot")
                      .load();
        String botToken = dotenv.get("BOT_TOKEN");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(botToken);
        builder.setActivity(Activity.playing("with your projects"));
        builder.setStatus(OnlineStatus.ONLINE);
        shardManager = builder.build();

        //options


        CommandManager commandManager = new CommandManager();
        shardManager.addEventListener(commandManager);

        List<IEvent> eventListeners = List.of(
                new OnReadyListener(commandManager)
        );

        logger.info("Registering event listeners.");
        EventListenerManager loader = new EventListenerManager(shardManager, eventListeners);
        loader.registerEventListeners();

        logger.info("Bot is online.");
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public static void main(String[] args) {
        try {
            ProjectManager projectmanager = new ProjectManager();
        } catch (LoginException e) {
            System.out.println("Failed to login to Discord with the provided token.");
        }
    }

    private String resolveEnvFilePath() {
        return Paths.get(System.getProperty("user.dir"), "..", "DiscordBot").normalize().toString();
    }
}