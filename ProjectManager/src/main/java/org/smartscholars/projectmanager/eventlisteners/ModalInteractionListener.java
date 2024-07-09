package org.smartscholars.projectmanager.eventlisteners;

import com.google.gson.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.util.DateTimeConverter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class ModalInteractionListener extends ListenerAdapter implements IEvent {

    private final Logger logger = LoggerFactory.getLogger(ModalInteractionListener.class);

    @Override
    public void execute(GenericEvent event) {
        onModalInteraction((ModalInteractionEvent) event);
    }

    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String[] parts = event.getModalId().split(":", 2);
        String userId = event.getUser().getId();
        if (parts[0].equals("activityDateModal") && parts.length > 1) {
            String activity = parts[1];
            String date = Objects.requireNonNull(event.getValue("date")).getAsString();
            String time = Objects.requireNonNull(event.getValue("time")).getAsString();
            event.reply("@everyone react to do " + activity + " on " + date + " at " + time).queue(interactionHook -> {
                interactionHook.retrieveOriginal().queue(message -> {
                    message.addReaction(Emoji.fromUnicode("U+1F44D")).queue();
                    String filePath = "ProjectManager/src/main/resources/activities.json";

                    JsonElement fileElement;
                    try (FileReader reader = new FileReader(filePath)) {
                        fileElement = JsonParser.parseReader(reader);
                    }
                    catch (IOException e) {
                        fileElement = new JsonObject(); // Create a new JSON object if an error occurs
                        logger.error("Error reading from file", e);
                    }

                    JsonObject rootObject = fileElement.isJsonObject() ? fileElement.getAsJsonObject() : new JsonObject();
                    JsonArray activitiesArray = rootObject.has("activities") ? rootObject.getAsJsonArray("activities") : new JsonArray();

                    // Create new activity JSON object
                    JsonObject newActivity = new JsonObject();
                    JsonArray usersArray = new JsonArray();
                    usersArray.add(userId);
                    newActivity.addProperty("messageId", message.getId());
                    newActivity.addProperty("activity", activity);
                    newActivity.addProperty("date", DateTimeConverter.parseDate(date));
                    newActivity.addProperty("time", DateTimeConverter.parseTime(time));
                    newActivity.add("users", usersArray);

                    activitiesArray.add(newActivity);
                    rootObject.add("activities", activitiesArray);

                    try (FileWriter file = new FileWriter(filePath)) {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        file.write(gson.toJson(rootObject));
                        file.flush();
                    }
                    catch (IOException e) {
                        logger.error("Error writing to file", e);
                        interactionHook.editOriginal("Error please try again").queue();
                    }
                });
            });
        }
    }
}