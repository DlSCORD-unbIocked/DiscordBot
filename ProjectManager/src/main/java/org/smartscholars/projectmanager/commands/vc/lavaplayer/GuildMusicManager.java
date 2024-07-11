package org.smartscholars.projectmanager.commands.vc.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import net.dv8tion.jda.api.entities.Guild;

public class GuildMusicManager {
    private final TrackScheduler trackScheduler;
    private final LavaPlayerAudioProvider lavaPlayerAudioProvider;

    public GuildMusicManager(AudioPlayerManager manager, Guild guild) {
        AudioPlayer player = manager.createPlayer();
        trackScheduler = new TrackScheduler(player);
        player.addListener(trackScheduler);
        lavaPlayerAudioProvider = new LavaPlayerAudioProvider(player, guild);
    }

    public TrackScheduler getTrackScheduler() {
        return trackScheduler;
    }

    public LavaPlayerAudioProvider getAudioForwarder() {
        return lavaPlayerAudioProvider;
    }
}
