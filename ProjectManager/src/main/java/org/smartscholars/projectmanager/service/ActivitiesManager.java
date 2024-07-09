package org.smartscholars.projectmanager.service;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ActivitiesManager {

    private final Logger logger = LoggerFactory.getLogger(ActivitiesManager.class);
    private final String filePath = "ProjectManager/src/main/resources/activities.json";
    private final SchedulerService schedulerService;

    public ActivitiesManager(SchedulerService schedulerService) {
        logger.info("ActivitiesManager initialized");
        this.schedulerService = schedulerService;
    }

    public void addActivity(String activity, String authorId, Message message, String date, String time, long timeInMillis) {
        JsonElement fileElement;
        try (FileReader reader = new FileReader(filePath)) {
            fileElement = JsonParser.parseReader(reader);
        }
        catch (IOException e) {
            fileElement = new JsonObject();
            logger.error("Error reading from file", e);
        }
        JsonObject rootObject = fileElement.isJsonObject() ? fileElement.getAsJsonObject() : new JsonObject();
        JsonArray activitiesArray = rootObject.has("activities") ? rootObject.getAsJsonArray("activities") : new JsonArray();

        JsonObject newActivity = new JsonObject();
        JsonArray usersArray = new JsonArray();
        usersArray.add(authorId);
        newActivity.addProperty("messageId", message.getId());
        newActivity.addProperty("activity", activity);
        newActivity.addProperty("date", date);
        newActivity.addProperty("time", time);
        newActivity.add("users", usersArray);

        activitiesArray.add(newActivity);
        rootObject.add("activities", activitiesArray);

        try (FileWriter file = new FileWriter(filePath)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            file.write(gson.toJson(rootObject));
            file.flush();
            //initiate scheduler (in the future don't hardcode the announcements channel id)
            schedulerService.scheduleAnnouncement("1086834972876345495", "**`Pull up for " + activity + " now!`**", message.getId(), timeInMillis);
        }
        catch (IOException e) {
            logger.error("Error writing to file", e);
        }
    }

    public void deleteActivity(String messageId) {
        try {
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
            logger.info("Activity deleted");
        }
        catch (Exception e) {
            logger.error("Error reading from file", e);
        }
    }

    public List<String> findUsersByMessageId(String messageId) {
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
