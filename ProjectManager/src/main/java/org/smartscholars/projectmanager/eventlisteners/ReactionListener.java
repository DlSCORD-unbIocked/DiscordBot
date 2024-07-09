package org.smartscholars.projectmanager.eventlisteners;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;


import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Map;
import java.util.TreeMap;


public class ReactionListener extends ListenerAdapter implements IEvent {

    @Override
    public void execute(GenericEvent event) {

        if(event instanceof MessageReactionAddEvent)
            onMessageReactionAdd((MessageReactionAddEvent) event);
        else if(event instanceof MessageReactionRemoveEvent)
            onMessageReactionRemove((MessageReactionRemoveEvent) event);

    }

    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event)
    {
        String emojicode = event.getReaction().getEmoji().getAsReactionCode();
        String messageid = event.getMessageId();
        Guild guild = event.getGuild();
        MessageChannelUnion channel = event.getChannel();
//        TextChannel name = guild.getTextChannelById("1259897391214231583");
//        channel.editMessageById("1259903701271974002", "bob.bot:6,bobby.test:7,joe.saa:4").queue();
        getStarredComments(guild);
        if(emojicode.equals("⭐"))
        {
            event.retrieveMessage().queue((message -> {
                for(MessageReaction r : message.getReactions())
                {System.out.println("Starred message: " + r.getCount());}
            }));
            channel.sendMessage("You clicked the star button, the redirect to the message is https://discord.com/channels/" + guild.getId() + "/" + channel.getId() + "/" +  messageid).queue();

//            channel.editMessageEmbedsById(messageid, new EmbedBuilder().setTitle("Star Leaderboard").setColor(Color.RED).build()).queue();
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event)
    {
        String emojicode = event.getReaction().getEmoji().getAsReactionCode();
        String messageid = event.getMessageId();
        String guildid = event.getGuild().getId();
        String channelid = event.getChannel().getId();

        if(emojicode.equals("⭐"))
        {
            event.getChannel().sendMessage("You removed the star button, the redirect to the message is https://discord.com/channels/" + guildid + "/" + channelid + "/" +  messageid).queue();
        }
    }

    public void changeStars(String messageId, int change) {
//        guild.getTextChannelById("1259897391214231583").retrieveMessageById("1259903701271974002").queue((m -> {}));
    }
    public void updateStars(TreeMap<Integer, String> starboard, Guild guild)
    {
        guild.getTextChannelById("1259869260927340614").retrieveMessageById("1259903444920176690").queue((message -> {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Star Leaderboard");
            embed.setColor(Color.RED);

            for(Map.Entry<Integer, String> entry : starboard.descendingMap().entrySet()) {
                String val = entry.getValue();
                System.out.println(val);
                embed.addField("Stars: " + entry.getKey(), "Author: "+ val.split("\\.")[0] +"\nMessage: " + val.split("\\.")[1], false);
            }
            message.editMessageEmbeds(embed.build()).queue();
        }));
    }
    public void getStarredComments(Guild guild)
    {
        int r;
        guild.getTextChannelById("1259897391214231583").retrieveMessageById("1259903701271974002").queue((m -> {
//            starredMessages = m;


            String[] parts = m.getContentDisplay().split(",");
            for(String p:parts)
            {
                System.out.println(p);
            }
            TreeMap<Integer, String> starboard = new TreeMap<>();
            for(String part : parts)
            {
                String[] parts2 = part.split(":");
                String messageid = parts2[0];
                String stars = parts2[1];
                starboard.put(Integer.parseInt(stars), messageid);
            }
            updateStars(starboard, guild);
        }));
    }
}