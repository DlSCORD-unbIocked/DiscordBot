package org.smartscholars.projectmanager.commands.vc.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PlayerManager {

    private static PlayerManager INSTANCE;
    private final Map<Long, GuildMusicManager> guildMusicManagers = new HashMap<>();
    private final AudioPlayerManager audioPlayerManager;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(PlayerManager.class);
    public static boolean isPlaying = false;

    private PlayerManager() {
        audioPlayerManager = new DefaultAudioPlayerManager();
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

    public synchronized boolean tryPlaying() {
        if (!isPlaying) {
            isPlaying = true;
            return true;
        }
        return false;
    }

    public static synchronized void setPlaying(boolean playing) {
        isPlaying = playing;
    }

    public synchronized void finishPlaying() {
        isPlaying = false;
    }

    public GuildMusicManager getGuildMusicManager(Guild guild) {
        return guildMusicManagers.computeIfAbsent(guild.getIdLong(), (guildId) -> {
            GuildMusicManager musicManager = new GuildMusicManager(audioPlayerManager, guild);
            guild.getAudioManager().setSendingHandler(musicManager.getLavaPlayerAudioProvider());
            return musicManager;
        });
    }

   public void loadAndPlay(Guild guild, String trackURL, TextChannel channel, boolean addPlaylist, boolean isLocal) {
        GuildMusicManager guildMusicManager = getGuildMusicManager(guild);
        logger.info("Loading track: {}", trackURL);

        audioPlayerManager.loadItemOrdered(guildMusicManager, trackURL, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                if (isLocal) {
                    logger.info("Playing TTS");
                    guildMusicManager.getTrackScheduler().queue(track);
                    return;
                }
                try {
                    logger.info("Track loaded successfully: {}", track.getInfo().title);
                    guildMusicManager.getTrackScheduler().queue(track);
                    channel.sendMessage("`Track loaded successfully: {}`" + track.getInfo().title).queue();
                }
                catch (Exception e) {
                    logger.error("Error sending track loaded message", e);
                }
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                List<AudioTrack> tracks = playlist.getTracks();
                if (addPlaylist) {
                    logger.info("Playlist loaded successfully: {}", playlist.getName());
                    channel.sendMessage("`Adding` **`" + playlist.getTracks().size() +"`** `tracks to queue from playlist: " + playlist.getName() + "`").queue();
                    tracks.forEach(guildMusicManager.getTrackScheduler()::queue);
                }
                else {
                    logger.info("Track loaded successfully: {}", playlist.getTracks().getFirst().getInfo().title);
                    channel.sendMessage("**`Track loaded successfully:`** `" + playlist.getTracks().getFirst().getInfo().title + "`").queue();
                    guildMusicManager.getTrackScheduler().queue(playlist.getTracks().getFirst());
                }
            }

            @Override
            public void noMatches() {
                logger.warn("No matches found for: {}", trackURL);
                channel.sendMessage("No matches found for: " + trackURL).queue();
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                logger.error("Could not load track: {}", trackURL, exception);
                channel.sendMessage("Could not load track: " + trackURL).queue();
            }
        });
    }
}