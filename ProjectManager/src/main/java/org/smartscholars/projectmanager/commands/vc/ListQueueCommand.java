package org.smartscholars.projectmanager.commands.vc;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.PlayerManager;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

@CommandInfo(
    name = "listqueue",
    description = "List the songs in the queue"
)

public class ListQueueCommand implements ICommand {

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Member member = event.getMember();
        assert member != null;
        GuildVoiceState memberVoiceState = member.getVoiceState();

        assert memberVoiceState != null;
        if(!memberVoiceState.inAudioChannel()) {
            event.reply("You need to be in a voice channel").queue();
            return;
        }

        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if(!selfVoiceState.inAudioChannel()) {
            event.reply("I am not in an audio channel").queue();
            return;
        }

        if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
            event.reply("You are not in the same channel as me").queue();
            return;
        }

        BlockingQueue<AudioTrack> queue = PlayerManager.get().getGuildMusicManager(event.getGuild()).getTrackScheduler().getQueue();
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
}
