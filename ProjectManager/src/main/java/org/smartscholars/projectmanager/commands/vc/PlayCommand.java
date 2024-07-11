package org.smartscholars.projectmanager.commands.vc;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.PlayerManager;
import org.smartscholars.projectmanager.util.VoiceChannelUtil;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

@CommandInfo(
    name = "play",
    description = "The bot will play the song",
    options = {
        @CommandOption(
            name = "song",
            description = "The song you want to play",
            type = OptionType.STRING,
            required = true
        )
    }
)
public class PlayCommand implements ICommand {

    private final Logger logger = LoggerFactory.getLogger(PlayCommand.class);

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!VoiceChannelUtil.ensureMemberAndBotInSameChannel(event, logger)) {
            return;
        }

        String song = Objects.requireNonNull(event.getOption("song"), "Song option cannot be null").getAsString();
        try {
            new URL(song);
        }
        catch (MalformedURLException e) {
           song = "ytsearch:" + song;
        }

        PlayerManager playerManager = PlayerManager.get();
        try {
            playerManager.play(event.getGuild(), song, event);
        } catch (Exception e) {
            logger.error("Error playing song", e);
            event.reply("Error playing song").queue();
        }
    }
}