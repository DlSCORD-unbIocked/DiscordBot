package org.smartscholars.projectmanager.commands.vc;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.Permission;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.LavaPlayerAudioProvider;

import java.util.Objects;

@CommandInfo(name = "join", description = "The bot will join your current voice channel", permissions = {Permission.MEMBER})
public class JoinVoiceChannelCommand implements ICommand {

    private final AudioPlayer player;

    public JoinVoiceChannelCommand(AudioPlayer player) {
        this.player = player;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        assert guild != null;
        AudioManager audioManager = guild.getAudioManager();

        if (Objects.requireNonNull(event.getMember()).getVoiceState() == null || event.getMember().getVoiceState().getChannel() == null) {
            event.reply("You need to be in a voice channel to use this command").queue();
            return;
        }
        audioManager.setSendingHandler(new LavaPlayerAudioProvider(player));
        audioManager.openAudioConnection(event.getMember().getVoiceState().getChannel());
        event.reply("Joining voice channel").queue();
    }
}
