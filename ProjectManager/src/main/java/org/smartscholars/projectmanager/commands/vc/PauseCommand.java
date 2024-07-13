package org.smartscholars.projectmanager.commands.vc;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.GuildMusicManager;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.PlayerManager;
import org.smartscholars.projectmanager.util.VcUtil;

import java.util.Objects;

@CommandInfo(
    name = "pause",
    description = "The bot will pause the current song"
)
public class PauseCommand implements ICommand {
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

        GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
        guildMusicManager.getTrackScheduler().getPlayer().setPaused(true);
        event.reply("Paused").queue();

    }
}
