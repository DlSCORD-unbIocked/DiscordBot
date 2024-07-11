package org.smartscholars.projectmanager.commands.vc.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

public class PlayerManager {
    private final AudioPlayerManager audioPlayerManager;
    private final AudioPlayer audioPlayer;

    public PlayerManager() {
        audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerLocalSource(audioPlayerManager);
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        audioPlayer = audioPlayerManager.createPlayer();
    }

    public AudioPlayerManager getManager() {
        return audioPlayerManager;
    }

    public AudioPlayer getPlayer() {
        return audioPlayer;
    }
}