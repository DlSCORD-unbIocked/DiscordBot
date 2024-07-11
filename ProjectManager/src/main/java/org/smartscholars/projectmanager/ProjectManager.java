package org.smartscholars.projectmanager;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.eventlisteners.*;
import org.smartscholars.projectmanager.service.SchedulerService;
import javax.security.auth.login.LoginException;
import java.util.List;

public class ProjectManager {
    private final ShardManager shardManager;
    private static final Logger logger = LoggerFactory.getLogger(CommandManager.class);
    public ProjectManager() throws LoginException {
        Dotenv dotenv = Dotenv.load();
        String botToken = dotenv.get("BOT_TOKEN");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(botToken);
        //audio player

        builder.setActivity(Activity.playing("with your projects"));
        builder.setStatus(OnlineStatus.ONLINE);
        builder.enableIntents(GatewayIntent.MESSAGE_CONTENT);

        shardManager = builder.build();
        JDA jda = shardManager.getShards().getFirst();
        //options
        SchedulerService schedulerService = new SchedulerService(jda);

        CommandManager commandManager = new CommandManager();
        shardManager.addEventListener(commandManager);
        List<IEvent> eventListeners = List.of(
                new OnReadyListener(commandManager),
                new ModalInteractionListener(schedulerService),
                new ButtonListener(),
                new ReactionListener(),
                new ActivityReactionListener()
        );
        logger.info("Registering event listeners.");
        EventListenerManager loader = new EventListenerManager(shardManager, eventListeners);
        loader.registerEventListeners();
        logger.info("Bot is online.");
        Runtime.getRuntime().addShutdownHook(new Thread(schedulerService::shutdown));
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
}