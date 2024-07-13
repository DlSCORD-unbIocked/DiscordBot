package org.smartscholars.projectmanager.commands.vc;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.PlayerManager;
import org.smartscholars.projectmanager.util.ListUtils;
import org.smartscholars.projectmanager.util.VcUtil;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

@CommandInfo(
    name = "listqueue",
    description = "List the songs in the queue"
)

public class ListQueueCommand implements ICommand {

    private static final int TRACKS_PER_PAGE = 10;
    public static List<List<Map.Entry<String, String>>> pages;
    public static List<Map<String, String>> tracksInfo;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        assert member != null;

        if(!VcUtil.isMemberInVoiceChannel(member)) {
            event.reply("You need to be in a voice channel").queue();
            return;
        }

        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();

        if(!VcUtil.isSelfInVoiceChannel(self)) {
            event.reply("I am not in an audio channel").queue();
            return;
        }

        if(!VcUtil.isMemberInSameVoiceChannel(member, self)) {
            event.reply("You are not in the same channel as me").queue();
            return;
        }

        BlockingQueue<AudioTrack> queue = PlayerManager.get().getGuildMusicManager(event.getGuild()).getTrackScheduler().getQueue();
        tracksInfo = convertQueueToListOfNamesAndTimes(queue);
        //pages = ListUtils.partition(tracksInfo, TRACKS_PER_PAGE);


        if (queue.isEmpty()) {
            event.reply("The queue is empty").queue();
        }
        else {
            StringBuilder sb = new StringBuilder();
            sb.append("Queue:\n");
            int i = 1;
            for (AudioTrack track : queue) {
                sb.append(i).append(". ").append(track.getInfo().title).append("\n");
                i++;
            }
            event.reply(sb.toString()).queue();
        }
    }

    public List<Map<String, String>> convertQueueToListOfNamesAndTimes(BlockingQueue<AudioTrack> queue) {
        List<Map<String, String>> tracksInfo = new ArrayList<>();

        for (AudioTrack track : queue) {
            Map<String, String> trackInfo = new HashMap<>();
            String name = track.getInfo().title;
            String time = formatDuration(track.getDuration());

            trackInfo.put("name", name);
            trackInfo.put("time", time);

            tracksInfo.add(trackInfo);
        }

        return tracksInfo;
    }

    private String formatDuration(long durationMillis) {
        long hours = TimeUnit.MILLISECONDS.toHours(durationMillis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(durationMillis) % 60;
        long seconds = TimeUnit.MILLISECONDS.toSeconds(durationMillis) % 60;
        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}
