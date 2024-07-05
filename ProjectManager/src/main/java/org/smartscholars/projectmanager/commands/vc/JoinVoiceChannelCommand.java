package org.smartscholars.projectmanager.commands.vc;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;

import java.util.Objects;

@CommandInfo(name = "join", description = "The bot will join your current voice channel")
public class JoinVoiceChannelCommand implements ICommand {

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        assert guild != null;
        AudioManager audioManager = guild.getAudioManager();

        if (Objects.requireNonNull(event.getMember()).getVoiceState() == null || event.getMember().getVoiceState().getChannel() == null) {
            event.reply("You need to be in a voice channel to use this command").queue();
            return;
        }
        audioManager.openAudioConnection(event.getMember().getVoiceState().getChannel());
        event.reply("Joining voice channel").queue();
    }
}
