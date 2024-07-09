package org.smartscholars.projectmanager.eventlisteners;

import com.google.gson.*;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class ActivityReactionListener extends ListenerAdapter implements IEvent {

    private final Logger logger = LoggerFactory.getLogger(ActivityReactionListener.class);

    @Override
    public void execute(GenericEvent event) {
        if (event instanceof MessageReactionRemoveEvent) {
            onMessageReactionRemove((MessageReactionRemoveEvent) event);
        }
        else if (event instanceof MessageReactionAddEvent) {
            onMessageReactionAdd((MessageReactionAddEvent) event);
        }
    }

    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event) {
        if (event.getReaction().getEmoji().getAsReactionCode().equals("👍") && !Objects.requireNonNull(event.getUser()).isBot()) {
            String messageId = event.getMessageId();
            String userId = event.getUser().getId();
            if (isValidMessage(messageId)) {
                try {
                    String filePath = "ProjectManager/src/main/resources/activities.json";
                    FileReader reader = new FileReader(filePath);
                    JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                    JsonArray activities = jsonObject.getAsJsonArray("activities");
                    reader.close();

                    boolean updated = false;
                    for (JsonElement activityElement : activities) {
                        JsonObject activityObject = activityElement.getAsJsonObject();
                        String currentMessageId = activityObject.get("messageId").getAsString();
                        if (currentMessageId.equals(messageId)) {
                            JsonArray users = activityObject.getAsJsonArray("users");
                            if (!users.contains(new Gson().toJsonTree(userId))) {
                                users.add(userId);
                                updated = true;
                                break;
                            }
                        }
                    }

                    if (updated) {
                        FileWriter writer = new FileWriter(filePath);
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        gson.toJson(jsonObject, writer);
                        writer.close();
                        event.getChannel().sendMessage(event.getUser().getAsMention() + " reacted with a thumbs up!").queue();
                    }

                }
                catch (IOException e) {
                    logger.error("Error updating activities.json", e);
                }
            }
        }
    }

    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event) {
        if (event.getReaction().getEmoji().getAsReactionCode().equals("👍") && !Objects.requireNonNull(event.getUser()).isBot()) {
            String messageId = event.getMessageId();
            String userId = Objects.requireNonNull(event.getUser()).getId();
            try {
                String filePath = "ProjectManager/src/main/resources/activities.json";
                FileReader reader = new FileReader(filePath);
                JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
                JsonArray activities = jsonObject.getAsJsonArray("activities");
                reader.close();

                boolean updated = false;
                for (JsonElement activityElement : activities) {
                    JsonObject activityObject = activityElement.getAsJsonObject();
                    String currentMessageId = activityObject.get("messageId").getAsString();
                    if (currentMessageId.equals(messageId)) {
                        JsonArray users = activityObject.getAsJsonArray("users");
                        for (int i = 0; i < users.size(); i++) {
                            if (users.get(i).getAsString().equals(userId)) {
                                users.remove(i);
                                updated = true;
                                break;
                            }
                        }
                        if (updated) {
                            break;
                        }
                    }
                }

                if (updated) {
                    FileWriter writer = new FileWriter(filePath);
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(jsonObject, writer);
                    writer.close();
                    event.getChannel().sendMessage(event.getUser().getAsMention() + " removed a thumbs up!").queue();
                }

            }
            catch (IOException e) {
                logger.error("Error updating activities.json", e);
            }
        }
    }

    public boolean isValidMessage(String messageId) {
        String filePath = "ProjectManager/src/main/resources/activities.json";
        try (FileReader reader = new FileReader(filePath)) {
            JsonObject jsonObject = JsonParser.parseReader(reader).getAsJsonObject();
            JsonArray activities = jsonObject.getAsJsonArray("activities");
            for (JsonElement activityElement : activities) {
                JsonObject activityObject = activityElement.getAsJsonObject();
                String currentMessageId = activityObject.get("messageId").getAsString();
                if (currentMessageId.equals(messageId)) {
                    return true;
                }
            }
        }
        catch (Exception e) {
            logger.error("Error reading from file", e);
        }
        return false;
    }

}
