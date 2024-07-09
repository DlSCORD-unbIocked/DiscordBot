package org.smartscholars.projectmanager.eventlisteners;

import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.service.ActivitiesManager;
import org.smartscholars.projectmanager.service.SchedulerService;
import org.smartscholars.projectmanager.util.DateTimeConverter;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class ModalInteractionListener extends ListenerAdapter implements IEvent {

    private final Logger logger = LoggerFactory.getLogger(ModalInteractionListener.class);
    private final ActivitiesManager activitiesManager;

    public ModalInteractionListener(SchedulerService schedulerService) {
        this.activitiesManager = new ActivitiesManager(schedulerService);
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
                event.reply("**`Please provide valid times and dates`**").queue();
                return;
            }
            else {
                try {
                    date.set(DateTimeConverter.parseDate(date.get()));
                    time.set(DateTimeConverter.parseTime(time.get()));
                }
                catch (Exception e) {
                    logger.error("Error parsing date or time", e);
                    event.reply("**`Please provide valid times and dates`**").queue();
                    return;
                }
            }
            String pattern = "yyyy-MM-dd HH:mm";
            long timeInMillis = DateTimeConverter.convertToMillis(date.get() + " " + time.get(), pattern);

            event.reply("@everyone **`react to do " + activity + " on`** <t:" + timeInMillis/1000 + ":F>").queue(interactionHook -> interactionHook.retrieveOriginal().queue(message -> {
                message.addReaction(Emoji.fromUnicode("U+1F44D")).queue();
                try {
                    activitiesManager.addActivity(activity, userId, message, date.get(), time.get(), timeInMillis);
                    SchedulerService.setActivityManager(activitiesManager);
                } catch (Exception e) {
                    logger.error("Error adding activity", e);
                    event.reply("**`Error adding activity`**").queue();
                }
            }));
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