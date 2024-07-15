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

import java.util.*;
import java.util.concurrent.BlockingQueue;

public class SelectionMenuListener extends ListenerAdapter implements IEvent{

    private static final Map<String, String> messageIdToUserIdMap = new HashMap<>();
    private static SelectionMenuListener instance;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(SelectionMenuListener.class);
    @Override
    public void execute(GenericEvent event) {
        onStringSelectInteraction((StringSelectInteractionEvent) event);

    }
    @Override
    public void onStringSelectInteraction(@NotNull StringSelectInteractionEvent event) {
        String userId = messageIdToUserIdMap.get(event.getMessageId());
        logger.info("Button interaction detected from user: {}", userId);
        if (userId == null || !userId.equals(event.getUser().getId())) {
            event.reply("You are not allowed to interact with this button.").setEphemeral(true).queue();
            return;
        }

        if (event.getComponentId().equals("remove-song")) {
            event.deferEdit().queue();
            List<String> selectedTracks = event.getValues();
            BlockingQueue<AudioTrack> queue = PlayerManager.get().getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().getQueue();

            int currentPage = Integer.parseInt(Objects.requireNonNull(event.getMessage().getEmbeds().getFirst().getDescription()).split("\\s+")[1]);

            List<AudioTrack> list = new ArrayList<>(queue);
            List<AudioTrack> songsToKeep = new ArrayList<>();
            //logger.info(selectedTracks.toString());

            for (AudioTrack track : list) {
                //logger.info(track.getInfo().uri);
                boolean isSimilar = false;
                for (String uri : selectedTracks) {
                    if (isSimilar(track.getInfo().title, uri)) {
                        isSimilar = true;
                        break;
                    }
                }
                if (!isSimilar) {
                    songsToKeep.add(track);
                }
//                else {
//                    logger.info("Removing similar song: " + track.getInfo().uri);
//                }
            }

            queue.clear();
            queue.addAll(songsToKeep);
            ListQueueCommand.updatePartitions(event.getGuild());
            EmbedBuilder embed = new EmbedBuilder();

            if (queue.isEmpty()) {

                embed.setTitle("Queue is empty");
                event.getHook().editOriginalEmbeds(embed.build()).setComponents().queue();
                return;
            }

            boolean isFirstPage = currentPage <= 1;
            boolean isLastPage = currentPage >= ListQueueCommand.pages.size();
            if (currentPage >= 1 && currentPage <= ListQueueCommand.pages.size()) {
                embed = ListQueueCommand.buildPageEmbed(ListQueueCommand.pages.get(currentPage - 1), currentPage, ListQueueCommand.pages.size());
            }

            StringSelectMenu menu = ListQueueCommand.buildStringSelectMenu(ListQueueCommand.pages.get(currentPage - 1), currentPage).build();
            Button prev = Button.of(ButtonStyle.PRIMARY, "prev_queue_page", Emoji.fromUnicode("◀")).withDisabled(isFirstPage);
            Button next = Button.of(ButtonStyle.PRIMARY, "next_queue_page", Emoji.fromUnicode("▶")).withDisabled(isLastPage);

            event.getHook().editOriginalEmbeds(embed.build()).setComponents(ActionRow.of(prev, next), ActionRow.of(menu)).queue();
            //event.reply("Selected songs removed successfully, new queue is: " + queue.size() + " songs").setEphemeral(true).queue();
        }
    }

    private boolean isSimilar(String track, String selected) {

        return track.equalsIgnoreCase(selected);
    }

    public Map<String, String> getMessageIdToUserIdMap() {
        return messageIdToUserIdMap;
    }

    public static synchronized SelectionMenuListener getInstance() {
        if (instance == null) {
            instance = new SelectionMenuListener();
        }
        return instance;
    }
}
