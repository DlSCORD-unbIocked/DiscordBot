package org.smartscholars.projectmanager.commands.vc;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.Permission;

import java.util.Objects;

@CommandInfo(name = "leave", description = "The bot will leave the voice channel", permissions = {Permission.MEMBER})
public class LeaveCommand implements ICommand {
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Guild guild = event.getGuild();
        assert guild != null;
        if (Objects.requireNonNull(event.getMember()).getVoiceState() == null || event.getMember().getVoiceState().getChannel() == null) {
            event.reply("You need to be in a voice channel to use this command").queue();
            return;
        }
        else if (guild.getAudioManager().isConnected()) {
            guild.getAudioManager().closeAudioConnection();
            event.reply("Leaving voice channel").queue();
        }
        else {
            event.reply("I'm not in a voice channel").queue();
        }
    }
}
