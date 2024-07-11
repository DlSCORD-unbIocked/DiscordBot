package org.smartscholars.projectmanager.commands.vc.lavaplayer;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.MutableAudioFrame;
import net.dv8tion.jda.api.audio.AudioSendHandler;
import net.dv8tion.jda.api.entities.Guild;

import javax.annotation.Nullable;
import java.nio.ByteBuffer;

public class LavaPlayerAudioProvider implements AudioSendHandler {

    private final AudioPlayer audioPlayer;
    private final ByteBuffer buffer;
    private final MutableAudioFrame frame;
    private final Guild guild;
    private int time;

    public LavaPlayerAudioProvider(AudioPlayer audioPlayer, Guild guild) {
        this.audioPlayer = audioPlayer;
        this.guild = guild;
        this.buffer = ByteBuffer.allocate(1024);
        this.frame = new MutableAudioFrame();
        this.frame.setBuffer(buffer);
    }

    @Override
    public boolean canProvide() {
        boolean canProvide = audioPlayer.provide(frame);
        if(!canProvide) {
            time += 20;
            if(time >= 300000) {
                time = 0;
                guild.getAudioManager().closeAudioConnection();
            }
        } else {
            time = 0;
        }
        return canProvide;
    }

    @Nullable
    @Override
    public ByteBuffer provide20MsAudio() {
        return buffer.flip();
    }

    @Override
    public boolean isOpus() {
        return true;
    }
}
