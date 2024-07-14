package org.smartscholars.projectmanager.eventlisteners;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.smartscholars.projectmanager.commands.vc.ListQueueCommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.PlayerManager;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.TrackScheduler;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

public class SelectionMenuListener extends ListenerAdapter implements IEvent{

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(SelectionMenuListener.class);
    @Override
    public void execute(GenericEvent event) {
        onStringSelectInteraction((StringSelectInteractionEvent) event);

    }
    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        if (event.getComponentId().equals("remove-song")) {
            List<String> selectedURIs = event.getValues();
            BlockingQueue<AudioTrack> queue = PlayerManager.get().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().getQueue();

            List<AudioTrack> list = new ArrayList<>(queue);
            List<AudioTrack> songsToKeep = new ArrayList<>();

            for (AudioTrack track : list) {
                if (!selectedURIs.contains(track.getInfo().uri)) {
                    songsToKeep.add(track);
                    logger.info("Keeping song: " + track.getInfo().uri);
                }
            }

            queue.clear();
            queue.addAll(songsToKeep);
            ListQueueCommand.updatePartitions(event.getGuild());

            event.reply("Selected songs removed successfully, new queue is: " + queue.size() + " songs").setEphemeral(true).queue();
        }
    }
}
