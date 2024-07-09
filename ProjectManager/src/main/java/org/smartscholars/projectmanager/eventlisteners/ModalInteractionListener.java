package org.smartscholars.projectmanager.eventlisteners;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.util.DateTimeConverter;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Objects;

public class ModalInteractionListener extends ListenerAdapter implements IEvent{

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
                    JsonObject json = new JsonObject();
                    JsonArray usersArray = new JsonArray();
                    usersArray.add(userId);
                    json.addProperty("messageId", message.getId());
                    json.addProperty("activity", activity);
                    json.addProperty("date", DateTimeConverter.parseDate(date));
                    json.addProperty("time", DateTimeConverter.parseTime(time));
                    json.addProperty("users", usersArray.toString());

                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    String prettyJsonString = gson.toJson(json);

                    String filePath = "ProjectManager/src/main/resources/activities.json";
                    try (FileWriter file = new FileWriter(filePath)) {
                        file.write(prettyJsonString);
                        file.flush();
                    }
                    catch (IOException e) {
                        logger.error("Error writing message ID to file", e);
                    }
                });
            });
        }
    }
}

