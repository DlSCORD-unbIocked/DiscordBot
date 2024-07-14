package org.smartscholars.projectmanager.commands.vc;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenu;
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.PlayerManager;
import org.smartscholars.projectmanager.eventlisteners.ButtonListener;
import org.smartscholars.projectmanager.util.ListUtils;
import org.smartscholars.projectmanager.util.VcUtil;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@CommandInfo(
    name = "listqueue",
    description = "List the songs in the queue",
    options = {
        @CommandOption(
            name = "page",
            description = "The page number",
            type = OptionType.INTEGER,
            required = false
        )
    }
)

public class ListQueueCommand implements ICommand {

    private static final int TRACKS_PER_PAGE = 10;
    public static List<List<Map.Entry<String, String>>> pages;
    public static List<Map.Entry<String, String>> tracksInfo;
    private static BlockingQueue<AudioTrack> queue;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Member member = event.getMember();
        assert member != null;

        if(!VcUtil.isMemberInVoiceChannel(member)) {
            event.getHook().sendMessage("You need to be in a voice channel").queue();
            return;
        }

        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();

        if(!VcUtil.isSelfInVoiceChannel(self)) {
            event.getHook().sendMessage("I am not in an audio channel").queue();
            return;
        }

        if(!VcUtil.isMemberInSameVoiceChannel(member, self)) {
            event.getHook().sendMessage("You are not in the same channel as me").queue();
            return;
        }

        tracksInfo = new ArrayList<>();
        pages = new ArrayList<>();
        queue = PlayerManager.get().getGuildMusicManager(event.getGuild()).getTrackScheduler().getQueue();

        if (queue.isEmpty()) {
            event.getHook().sendMessage("**`The queue is empty`**").queue();
            return;
        }

        updatePartitions();

        int currentPage = event.getOption("page") != null ? Objects.requireNonNull(event.getOption("page")).getAsInt() : 1;
        int totalSize = tracksInfo.size();
        boolean isFirstPage = currentPage == 1;
        boolean isLastPage = currentPage >= (totalSize + TRACKS_PER_PAGE - 1) / TRACKS_PER_PAGE;

        if (currentPage > pages.size()) currentPage = pages.size();
        if (currentPage < 1) currentPage = 1;

        EmbedBuilder embed = buildPageEmbed(pages.get(currentPage - 1), currentPage, pages.size());

        Button prevButton = Button.of(ButtonStyle.PRIMARY,"prev_queue_page", Emoji.fromUnicode("◀"));
        Button nextButton = Button.of(ButtonStyle.PRIMARY,"next_queue_page", Emoji.fromUnicode("▶"));

        prevButton = isFirstPage ? prevButton.asDisabled() : prevButton;
        nextButton = isLastPage ? nextButton.asDisabled() : nextButton;

        StringSelectMenu menu = buildStringSelectMenu(pages.get(currentPage - 1), currentPage).build();

        event.getHook().sendMessageEmbeds(embed.build()).addActionRow(prevButton, nextButton).addActionRow(menu).queue((message) -> {
            String userId = member.getId();
            String messageId = message.getId();

            ButtonListener buttonListener = ButtonListener.getInstance();
            buttonListener.getMessageIdToUserIdMap().put(messageId, userId);
        });
    }

   public static List<Map.Entry<String, String>> convertQueueToListOfNamesAndTimes(BlockingQueue<AudioTrack> queue) {
        List<Map.Entry<String, String>> tracksInfo = new ArrayList<>();

        for (AudioTrack track : queue) {
            String name = track.getInfo().title;
            String time = formatDuration(track.getDuration());

            tracksInfo.add(new AbstractMap.SimpleEntry<>(name, time));
        }

        return tracksInfo;
    }

    private static String formatDuration(long durationMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        }
        else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    public static EmbedBuilder buildPageEmbed(List<Map.Entry<String, String>> commands, int current, int totalPages) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Current Music Queue");
        embed.setDescription("Page " + current + " of " + totalPages);
        embed.setColor(new Color(0x1F8B4C));
        commands.forEach(entry -> embed.addField(entry.getValue(), entry.getKey(), false));
        return embed;
    }

    public static boolean removeSong(int index, BlockingQueue<AudioTrack> queue) {
        if (index >= 0 && index < queue.size()) {
            List<AudioTrack> list = new ArrayList<>(queue);
            list.remove(index);
            queue.clear();
            queue.addAll(list);
            updatePartitions();
            return true;
        }
        return false;
    }

    public static void updatePartitions() {
        tracksInfo = convertQueueToListOfNamesAndTimes(queue);
        pages = ListUtils.partition(tracksInfo, TRACKS_PER_PAGE);
    }

    public static StringSelectMenu.Builder buildStringSelectMenu(List<Map.Entry<String, String>> commands, int currentPage) {
        StringSelectMenu.Builder menuBuilder = StringSelectMenu.create("remove-song")
        .setPlaceholder("Select a song to remove")
        .setRequiredRange(1, pages.get(currentPage - 1).size());
        for (Map.Entry<String, String> track : pages.get(currentPage - 1)) {
            menuBuilder.addOption(track.getKey(), track.getValue());
        }
        return menuBuilder;
    }
}
