package org.smartscholars.projectmanager.commands.vc;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.GuildMusicManager;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.PlayerManager;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.TrackScheduler;
import org.smartscholars.projectmanager.util.VoiceChannelUtil;

import java.util.Objects;

@CommandInfo(
    name = "stop",
    description = "The bot will stop playing the song"
)
public class StopCommand implements ICommand {

    private final Logger logger = LoggerFactory.getLogger(StopCommand.class);

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();
        if (VoiceChannelUtil.verifySelfInVoiceChannel(self, event)) return;
        if (VoiceChannelUtil.ensureMemberAndBotInSameChannel(event, logger)) return;

        GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(event.getGuild());
        TrackScheduler trackScheduler = guildMusicManager.getTrackScheduler();
        trackScheduler.getQueue().clear();
        trackScheduler.getPlayer().stopTrack();

        event.getHook().sendMessage("Stopped and cleared queue").queue();
    }
}
