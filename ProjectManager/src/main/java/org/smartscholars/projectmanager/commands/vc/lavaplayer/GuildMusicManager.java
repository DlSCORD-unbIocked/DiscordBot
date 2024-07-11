package org.smartscholars.projectmanager.commands.vc.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;

public class GuildMusicManager {
    public final AudioPlayer player;
    public final TrackScheduler scheduler;

    public GuildMusicManager(AudioPlayer player) {
        this.player = player;
        this.scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }

    public LavaPlayerAudioProvider getSendHandler() {
        return new LavaPlayerAudioProvider(player);
    }
}
