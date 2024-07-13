package org.smartscholars.projectmanager.commands.vc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.GuildMusicManager;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.PlayerManager;

import java.util.Objects;

@CommandInfo(
        name = "volume",
        description = "Set the volume of the bot in the voice channel",
        options = {
                @CommandOption(
                    name = "volume",
                    description = "The volume you want to set",
                    type = OptionType.INTEGER,
                    required = true
                )
        }
)
public class VolumeCommand implements ICommand {
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        int volume = Objects.requireNonNull(event.getOption("volume")).getAsInt();
        if (volume < 0 || volume > 100) {
            event.reply("Volume must be between 0 and 100").queue();
            return;
        }

        GuildMusicManager guildMusicManager = PlayerManager.get().getGuildMusicManager(Objects.requireNonNull(event.getGuild()));
        guildMusicManager.setVolume(volume);

        event.reply("Volume set to " + volume).queue();
    }
}
