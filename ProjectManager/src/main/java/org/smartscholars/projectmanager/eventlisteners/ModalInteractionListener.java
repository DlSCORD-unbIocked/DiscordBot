package org.smartscholars.projectmanager.eventlisteners;

import com.google.gson.*;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.service.SchedulerService;
import org.smartscholars.projectmanager.util.DateTimeConverter;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ModalInteractionListener extends ListenerAdapter implements IEvent {

    private final Logger logger = LoggerFactory.getLogger(ModalInteractionListener.class);
    private final SchedulerService schedulerService;

    public ModalInteractionListener(SchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    @Override
    public void execute(GenericEvent event) {
        onModalInteraction((ModalInteractionEvent) event);
    }

    public void onModalInteraction(@NotNull ModalInteractionEvent event) {
        String[] parts = event.getModalId().split(":", 2);
        String userId = event.getUser().getId();
        if (parts[0].equals("activityDateModal") && parts.length > 1) {
            String activity = parts[1];
            AtomicReference<String> date = new AtomicReference<>(Objects.requireNonNull(event.getValue("date")).getAsString());
            AtomicReference<String> time = new AtomicReference<>(Objects.requireNonNull(event.getValue("time")).getAsString());

            if (!isValidDateTime(date.get(), time.get())) {
                event.reply("`Please Provide valid times and dates`").queue();
                return;
            }
            else {
                try {
                    date.set(DateTimeConverter.parseDate(date.get()));
                    time.set(DateTimeConverter.parseTime(time.get()));
                } catch (Exception e) {
                    logger.error("Error parsing date or time", e);
                    event.reply("`Please Provide valid times and dates`").queue();
                    return;
                }
            }

            event.reply("@everyone `react to do " + activity + " on " + date + " at " + time + "`").queue(interactionHook -> {
                interactionHook.retrieveOriginal().queue(message -> {
                    message.addReaction(Emoji.fromUnicode("U+1F44D")).queue();
                    String filePath = "ProjectManager/src/main/resources/activities.json";

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
                    usersArray.add(userId);
                    newActivity.addProperty("messageId", message.getId());
                    newActivity.addProperty("activity", activity);
                    newActivity.addProperty("date", date.get());
                    newActivity.addProperty("time", time.get());
                    newActivity.add("users", usersArray);

                    activitiesArray.add(newActivity);
                    rootObject.add("activities", activitiesArray);

                    try (FileWriter file = new FileWriter(filePath)) {
                        Gson gson = new GsonBuilder().setPrettyPrinting().create();
                        file.write(gson.toJson(rootObject));
                        file.flush();
                        //initiate scheduler
                        String pattern = "yyyy-MM-dd HH:mm";
                        long timeInMillis = DateTimeConverter.convertToMillis(date.get() + " " + time.get(), pattern);
                        schedulerService.scheduleAnnouncement("1086834972876345495", "`Pull up for " + activity + " now!`", message.getId(), timeInMillis);
                    }
                    catch (IOException e) {
                        logger.error("Error writing to file", e);
                        interactionHook.editOriginal("`Error please try again`").queue();
                    }
                });
            });
        }
    }
    public boolean isValidTime(String time) {
        return time.matches("([01]?[0-9]|2[0-3]):[0-5][0-9]");
    }
    public boolean isValidDate(String date) {
        return date.matches("([0-9]{2})/([0-9]{2})/([0-9]{4})");
    }
    public boolean isValidDateTime(String date, String time) {
        if (!isValidDate(date) || !isValidTime(time)) {
            return false;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy HH:mm");
        LocalDateTime dateTime = LocalDateTime.parse(date + " " + time, formatter);
        Instant instant = dateTime.atZone(ZoneId.systemDefault()).toInstant();
        return instant.toEpochMilli() > System.currentTimeMillis();
    }
}