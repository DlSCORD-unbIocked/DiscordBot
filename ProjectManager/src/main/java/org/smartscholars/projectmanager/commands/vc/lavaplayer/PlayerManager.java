package org.smartscholars.projectmanager.commands.vc.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(PlayerManager.class);

    private PlayerManager() {
        audioPlayerManager.registerSourceManager(new dev.lavalink.youtube.YoutubeAudioSourceManager());
        AudioSourceManagers.registerRemoteSources(audioPlayerManager, com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager.class);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
    }

    public static PlayerManager get() {
        if (INSTANCE == null) {
            INSTANCE = new PlayerManager();
        }
        return INSTANCE;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return guildMusicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            GuildMusicManager musicManager = new GuildMusicManager(audioPlayerManager, guild);

            guild.getAudioManager().setSendingHandler(musicManager.getAudioForwarder());

            return musicManager;
        });
    }

    public void play(Guild guild, String trackURL, SlashCommandInteractionEvent event) {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        audioPlayerManager.loadItemOrdered(guildMusicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                logger.info("Track loaded successfully: {}", track.getInfo().title);
                event.reply("Track loaded successfully: " + track.getInfo().title).queue();
                guildMusicManager.getTrackScheduler().queue(track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                guildMusicManager.getTrackScheduler().queue(playlist.getTracks().get(0));
            }

            @Override
            public void noMatches() {
                logger.warn("No matches found for: {}", trackURL);
                event.reply("No matches found for: " + trackURL).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                logger.error("Could not load track: {}", trackURL, exception);
                event.reply("Could not load track: " + trackURL).queue();
            }
        });
    }
}