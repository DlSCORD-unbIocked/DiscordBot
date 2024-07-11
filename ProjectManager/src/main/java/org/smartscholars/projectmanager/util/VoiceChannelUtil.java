package org.smartscholars.projectmanager.util;

import net.dv8tion.jda.api.entities.GuildVoiceState;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;

import java.util.Objects;

public class VoiceChannelUtil {

    public static boolean ensureMemberAndBotInSameChannel(SlashCommandInteractionEvent event, Logger logger) {
        Member member = Objects.requireNonNull(event.getMember(), "Member cannot be null");
        GuildVoiceState memberVoiceState = Objects.requireNonNull(member.getVoiceState(), "Member voice state cannot be null");

        if (!memberVoiceState.inAudioChannel()) {
            event.reply("You need to be in a voice channel").queue();
            return false;
        }

        Member self = Objects.requireNonNull(event.getGuild(), "Guild cannot be null").getSelfMember();
        GuildVoiceState selfVoiceState = Objects.requireNonNull(self.getVoiceState(), "Self voice state cannot be null");

        if (!selfVoiceState.inAudioChannel()) {
            event.getGuild().getAudioManager().openAudioConnection(memberVoiceState.getChannel());
            return true;
        }
        else {
            if (!Objects.equals(selfVoiceState.getChannel(), memberVoiceState.getChannel())) {
                event.reply("You need to be in the same channel as me").queue();
                return false;
            }
        }
        return true;
    }
}