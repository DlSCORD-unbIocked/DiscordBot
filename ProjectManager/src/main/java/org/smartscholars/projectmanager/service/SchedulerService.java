package org.smartscholars.projectmanager.service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
    private final JDA jda;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private static ActivitiesManager activitiesManager;

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
        List<String> userIds = activitiesManager.findUsersByMessageId(messageId);
        for (String userId : userIds) {
            users.append("<@").append(userId).append("> ");
        }

        if (channel != null) {
            if (userIds.isEmpty()) {
                channel.sendMessage("**`No one can go LMAO`**" + " ||@everyone|| ").queue();
                return;
            }
            channel.sendMessage(message + " || " + users + " ||").queue();
            activitiesManager.deleteActivity(messageId);
        }
        else {
            logger.error("Channel not found");
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

    public static void setActivityManager(ActivitiesManager manager) {
        activitiesManager = manager;
    }
}