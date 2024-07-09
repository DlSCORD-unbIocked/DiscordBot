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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


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
//        channel.editMessageById("1259903701271974002", ",").queue();

        if(emojicode.equals("⭐"))
        {

            event.retrieveMessage().queue((message -> {
                for(MessageReaction r : message.getReactions())
                {System.out.println("Starred message: " + r.getCount());}
            }));
            String channelUrl = channel.getId() + "/" +  messageid;
//            channel.sendMessage("You clicked the star button, the redirect to the message is https://discord.com/channels/" + channelUrl).queue();
            channel.retrieveMessageById("1259903701271974002").queue((message -> {
                String content = message.getContentDisplay();
                content +=  channelUrl + ",";
                message.editMessage(content).queue();
            }));
            getStarredComments(guild);
//            channel.editMessageEmbedsById(messageid, new EmbedBuilder().setTitle("Star Leaderboard").setColor(Color.RED).build()).queue();
        }
    }

    @Override
    public void onMessageReactionRemove(@NotNull MessageReactionRemoveEvent event)
    {
        String emojicode = event.getReaction().getEmoji().getAsReactionCode();
        String messageid = event.getMessageId();
        Guild guild = event.getGuild();
        String channelid = event.getChannel().getId();

        MessageChannelUnion channel = event.getChannel();
        String channelUrl = channel.getId() + "/" +  messageid;
        if(emojicode.equals("⭐"))
        {
            channel.retrieveMessageById("1259903701271974002").queue((message -> {
                String content = message.getContentDisplay();
                content = content.replace(channelUrl + ",", "");
                message.editMessage(content).queue();
            }));
//            event.getChannel().sendMessage("You removed the star button, the redirect to the message is https://discord.com/channels/" + guildid + "/" + channelid + "/" +  messageid).queue();
        }
    }

    public void changeStars(String messageId, int change) {
//        guild.getTextChannelById("1259897391214231583").retrieveMessageById("1259903701271974002").queue((m -> {}));
    }
    public void updateStars(HashMap<String, Integer> starboard, Guild guild)
    {
        guild.getTextChannelById("1259869260927340614").retrieveMessageById("1259903444920176690").queue((message -> {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Star Leaderboard");
            embed.setColor(Color.RED);
            Iterator<String> starredMessages = starboard.keySet().iterator();
            //almost working
//            guild.getTextChannelById(starredMessages.next().split("/")[1]).retrieveMessageById(starredMessages[0].split("/")[2]).queue((m -> {
//                embed.addField("Stars: " + starboard.get(starredMessages[0]), "Author: "+ m.getAuthor().getName() +"\nMessage: " + m.getContentDisplay(), false);
//            }));
//            for(Map.Entry<String, String> entry : starboard.descendingMap().entrySet()) {
//                String val = entry.getValue();
//                System.out.println(val);
//                embed.addField("Stars: " + entry.getKey(), "Author: "+ val.split("\\.")[0] +"\nMessage: " + val.split("\\.")[1], false);
//            }

            message.editMessageEmbeds(embed.build()).queue();
        }));
    }
    public void getStarredComments(Guild guild)
    {
        int r;
        guild.getTextChannelById("1259897391214231583").retrieveMessageById("1259903701271974002").queue((m -> {
//            starredMessages = m;


            String[] parts = m.getContentDisplay().split(",");

            HashMap<String, Integer> starboard = new HashMap<>();
            for (String element : parts) {
                starboard.put(element, starboard.getOrDefault(element, 0) + 1);
            }
            updateStars(starboard, guild);
        }));
    }
}