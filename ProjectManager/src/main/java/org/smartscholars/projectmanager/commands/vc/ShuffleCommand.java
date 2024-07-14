package org.smartscholars.projectmanager.commands.vc;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.GuildMusicManager;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.PlayerManager;
import org.smartscholars.projectmanager.util.VcUtil;

import java.util.Objects;
import java.util.concurrent.BlockingQueue;

@CommandInfo(
        name = "shuffle",
        description = "Shuffles the queue"
)
public class ShuffleCommand implements ICommand {
    @Override
    public void execute(SlashCommandInteractionEvent event) {
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

        GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(Objects.requireNonNull(event.getGuild()));
        BlockingQueue<AudioTrack> queue = guildMusicManager.getTrackScheduler().getQueue();
        if (queue.isEmpty()) {
            event.getHook().sendMessage("**`The queue is empty`**").queue();
            return;
        }
        guildMusicManager.getTrackScheduler().shuffle();
        event.reply("**`Shuffled the queue`**").queue();
    }
}
