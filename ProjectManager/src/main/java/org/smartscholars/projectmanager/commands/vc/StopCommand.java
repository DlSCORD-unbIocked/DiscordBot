package org.smartscholars.projectmanager.commands.vc;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.PlayerManager;
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
        if (VoiceChannelUtil.ensureMemberAndBotInSameChannel(event, logger)) {
            return;
        }
        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();
        assert selfVoiceState != null;
        if (!selfVoiceState.inAudioChannel()) {
            event.reply("I am not in a voice channel").queue();
            return;
        }

        PlayerManager playerManager = PlayerManager.get();
        playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().clearQueue();
        playerManager.getGuildMusicManager(Objects.requireNonNull(event.getGuild())).getTrackScheduler().getPlayer().stopTrack();

        event.reply("Stopped and cleared queue").queue();
    }
}
