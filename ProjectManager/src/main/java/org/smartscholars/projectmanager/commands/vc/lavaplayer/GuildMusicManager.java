package org.smartscholars.projectmanager.commands.vc.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.Guild;

public class GuildMusicManager {
    private final TrackScheduler trackScheduler;
    private final LavaPlayerAudioProvider lavaPlayerAudioProvider;
    private static AudioPlayer player;

    public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
        player = manager.createPlayer();
        trackScheduler = new TrackScheduler(player);
        lavaPlayerAudioProvider = new LavaPlayerAudioProvider(player, guild);
        player.addListener(trackScheduler);
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public LavaPlayerAudioProvider getLavaPlayerAudioProvider() {
        return lavaPlayerAudioProvider;
    }

    public void setVolume(int volume) {
        player.setVolume(volume);
    }
}
