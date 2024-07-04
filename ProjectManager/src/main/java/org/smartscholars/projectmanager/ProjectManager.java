package org.smartscholars.projectmanager;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.sharding.DefaultShardManagerBuilder;
import net.dv8tion.jda.api.sharding.ShardManager;
import org.smartscholars.projectmanager.commands.CommandManager;

import javax.security.auth.login.LoginException;

public class ProjectManager {

    private final ShardManager shardManager;

    public ProjectManager() throws LoginException {
        Dotenv dotenv = Dotenv.load();
        String botToken = dotenv.get("BOT_TOKEN");
        DefaultShardManagerBuilder builder = DefaultShardManagerBuilder.createDefault(botToken);
        builder.setActivity(Activity.playing("with your projects"));
        builder.setStatus(OnlineStatus.DO_NOT_DISTURB);
        shardManager = builder.build();
        shardManager.addEventListener(new CommandManager());
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