package org.smartscholars.projectmanager.service;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SchedulerService {

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
        if (channel != null) {
            channel.sendMessage(message).queue();
        } else {
            System.err.println("Channel not found");
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
                if (!scheduler.awaitTermination(60, TimeUnit.SECONDS)) {
                    System.err.println("Scheduler did not terminate");
                }
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}