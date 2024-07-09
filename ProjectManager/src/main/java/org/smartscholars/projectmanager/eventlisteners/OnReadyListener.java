package org.smartscholars.projectmanager.eventlisteners;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;


import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.smartscholars.projectmanager.commands.CommandManager;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;


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

    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        event.getGuild().createCategory("STARBOARD").queue((category) -> category.createTextChannel("starboard").queue());

        String filePath = "ProjectManager/src/main/resources/starboard.json";
        FileReader reader;
        try {
            reader = new FileReader(filePath);
        }
        catch (FileNotFoundException e) {
            JsonObject jsonObject = new JsonObject();

            Gson gson = new Gson();
            String json = gson.toJson(jsonObject);
            FileWriter writer;
            try {
                writer = new FileWriter(filePath);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            try {
                writer.write(json);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            try {
                writer.close();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            throw new RuntimeException(e);
        }
        Type type = new TypeToken<JsonObject>() {}.getType();
        JsonObject jsonObject = new Gson().fromJson(reader, type);
        JsonArray starboardArray = jsonObject.getAsJsonArray("starboard");
    }

}