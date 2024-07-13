package org.smartscholars.projectmanager.util;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class VcUtil {
    public static boolean isMemberInVoiceChannel(Member member) {
        GuildVoiceState memberVoiceState = member.getVoiceState();
        return memberVoiceState != null && memberVoiceState.inAudioChannel();
    }
    public static boolean isSelfInVoiceChannel(Member self) {
        GuildVoiceState selfVoiceState = self.getVoiceState();
        return selfVoiceState != null && selfVoiceState.inAudioChannel();
    }
    public static boolean isMemberInSameVoiceChannel(Member member, Member self) {
        GuildVoiceState memberVoiceState = member.getVoiceState();
        GuildVoiceState selfVoiceState = self.getVoiceState();
        return selfVoiceState != null && memberVoiceState != null && selfVoiceState.getChannel().equals(memberVoiceState.getChannel());
    }

}
