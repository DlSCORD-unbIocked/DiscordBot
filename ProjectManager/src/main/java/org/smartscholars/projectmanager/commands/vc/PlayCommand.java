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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;
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
        ),
        @CommandOption(
                name = "addplaylist",
                description = "If true add the playlist to the queue",
                type = OptionType.BOOLEAN,
                required = false
        )
    }
)
public class PlayCommand implements ICommand {

    private final Logger logger = LoggerFactory.getLogger(PlayCommand.class);

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();

        Member member = event.getMember();
        assert member != null;
        GuildVoiceState memberVoiceState = member.getVoiceState();

        assert memberVoiceState != null;
        if(!memberVoiceState.inAudioChannel()) {
            event.getHook().sendMessage("`You need to be in a voice channel`").queue();
            return;
        }

        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();
        GuildVoiceState selfVoiceState = self.getVoiceState();

        assert selfVoiceState != null;
        if(!selfVoiceState.inAudioChannel()) {
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
        } else {
            if(selfVoiceState.getChannel() != memberVoiceState.getChannel()) {
                event.getHook().sendMessage("`You need to be in the same channel as me`8u").queue();
                return;
            }
        }

        String song = Objects.requireNonNull(event.getOption("song"), "Song option cannot be null").getAsString();
        boolean addPlaylist;
        if (event.getOption("addplaylist") == null) {
            addPlaylist = false;
        }
        else {
            addPlaylist = Objects.requireNonNull(event.getOption("addplaylist")).getAsBoolean();
        }
        logger.info(addPlaylist ? "Adding playlist" : "Not adding playlist");
        try {
            new URL(song);
        }
        catch (MalformedURLException e) {
           song = "ytsearch:" + song;
        }

        PlayerManager playerManager = PlayerManager.get();
        try {
            event.getHook().sendMessage("`Loading track...`").queue();
            playerManager.loadAndPlay(event.getGuild(), song, event.getChannel().asTextChannel(), addPlaylist);
        }
        catch (Exception e) {
            logger.error("Error playing song", e);
            event.getHook().sendMessage("Error playing song").queue();
        }
    }
}