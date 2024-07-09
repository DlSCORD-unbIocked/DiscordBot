package org.smartscholars.projectmanager.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    private final JDA jda;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public SchedulerService(JDA jda) {
        this.jda = jda;
    }

    public void scheduleAnnouncement(String channelId, String message, String messageId, long scheduledTimeMillis) {
        long delay = scheduledTimeMillis - System.currentTimeMillis();
        scheduler.schedule(() -> sendAnnouncement(channelId, message, messageId), delay, TimeUnit.MILLISECONDS);
    }

    private void sendAnnouncement(String channelId, String message, String messageId) {
        TextChannel channel = jda.getTextChannelById(channelId);
        StringBuilder users = new StringBuilder();
        List<String> userIds = findUsersByMessageId(messageId);
        for (String userId : userIds) {
            users.append("<@").append(userId).append("> ");
        }

        if (channel != null) {
            if (userIds.isEmpty()) {
                channel.sendMessage("**`No one can go LMAO`**" + " ||@everyone|| ").queue();
                return;
            }
            channel.sendMessage(message + " || " + users + " ||").queue();
            deleteActivity(messageId);
        }
        else {
            System.err.println("Channel not found");
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    logger.error("Scheduler did not terminate");
                }
            }
        }
        catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public static void deleteActivity(String messageId) {
        try {
            String filePath = "ProjectManager/src/main/resources/activities.json";
            FileReader reader = new FileReader(filePath);
            Type type = new TypeToken<JsonObject>() {}.getType();
            JsonObject jsonObject = new Gson().fromJson(reader, type);
            JsonArray activitiesArray = jsonObject.getAsJsonArray("activities");

            for (int i = 0; i < activitiesArray.size(); i++) {
                JsonObject activity = activitiesArray.get(i).getAsJsonObject();
                String currentMessageId = activity.get("messageId").getAsString();
                if (currentMessageId.equals(messageId)) {
                    activitiesArray.remove(i);
                    break;
                }
            }

            reader.close();
            Gson gson = new Gson();
            String json = gson.toJson(jsonObject);
            FileWriter writer = new FileWriter(filePath);
            writer.write(json);
            writer.close();
        }
        catch (Exception e) {
            logger.error("Error reading from file", e);
        }
    }

    public static List<String> findUsersByMessageId(String messageId) {
        try {
            String filePath = "ProjectManager/src/main/resources/activities.json";
            FileReader reader = new FileReader(filePath);
            Type type = new TypeToken<JsonObject>() {}.getType();
            JsonObject jsonObject = new Gson().fromJson(reader, type);
            JsonArray activitiesArray = jsonObject.getAsJsonArray("activities");

            for (int i = 0; i < activitiesArray.size(); i++) {
                JsonObject activity = activitiesArray.get(i).getAsJsonObject();
                String currentMessageId = activity.get("messageId").getAsString();
                if (currentMessageId.equals(messageId)) {
                    JsonArray usersArray = activity.getAsJsonArray("users");
                    List<String> users = new ArrayList<>();
                    for (int j = 0; j < usersArray.size(); j++) {
                        users.add(usersArray.get(j).getAsString());
                    }
                    return users;
                }
            }
        }
        catch (Exception e) {
            logger.error("Error reading from file", e);
        }
        return new ArrayList<>();
    }
}