package org.smartscholars.projectmanager.commands.vc.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.slf4j.Logger;

public class MusicPlayer {
    private final PlayerManager manager;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(MusicPlayer.class);

    public MusicPlayer(PlayerManager manager) {
        this.manager = manager;
    }

    public void play(String trackUrl) {
    manager.getManager().loadItemOrdered(manager, trackUrl, new AudioLoadResultHandler() {

        @Override
        public void trackLoaded(AudioTrack track) {
            manager.getPlayer().playTrack(track);
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {

        }

        @Override
        public void noMatches() {
            logger.error("No matches found for the URL: {}", trackUrl);
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            logger.error("Could not load track: {}", trackUrl, exception);
        }
    });
}
}
