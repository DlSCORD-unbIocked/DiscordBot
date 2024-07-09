package org.smartscholars.projectmanager.eventlisteners;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
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
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;


public class ReactionListener extends ListenerAdapter implements IEvent {
    public EmbedBuilder embed;
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
        channel.editMessageById("1259903701271974002", ",").queue();

        if(emojicode.equals("⭐"))
        {
            String channelUrl = channel.getId() + "/" +  messageid;
            channel.retrieveMessageById("1259903701271974002").queue((message -> {
                String content = message.getContentDisplay();
                content +=  channelUrl + ",";
                message.editMessage(content).queue();
            }));
            getStarredComments(guild);
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
            getStarredComments(guild);
        }
    }


    public void updateStars(HashMap<String, Integer> starboard, Guild guild)
    {
        guild.getTextChannelById("1259869260927340614").retrieveMessageById("1259903444920176690").queue((message -> {
            embed = new EmbedBuilder();
            embed.setTitle("Star Leaderboard");
            embed.setColor(Color.BLUE);
            Set<String> keys = starboard.keySet();

            // in progress
            String filePath = "ProjectManager/src/main/resources/activities.json";
            FileReader reader = null;
            try {
                reader = new FileReader(filePath);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }
            Type type = new TypeToken<JsonObject>() {}.getType();
            JsonObject jsonObject = new Gson().fromJson(reader, type);
            JsonArray starredMessages = jsonObject.getAsJsonArray("leaderboard");
            //

            for(JsonElement msg : starredMessages)
            {

                String mess = msg.getAsString();
                String channelid = mess.split("/")[0];
                String messageid = mess.split("/")[1];
                String stars = "temp";
                guild.getTextChannelById(channelid).retrieveMessageById(messageid).queue((m -> {
                    embed.addField("Stars: " + stars, "Author: "+ m.getAuthor().getName() +"\nMessage: " + m.getContentDisplay() + "\nOriginal: https://discord.com/channels/" + guild.getId()+"/"+channelid+"/"+messageid, false);

                    message.editMessageEmbeds(embed.build()).queue();
                }));


            }

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
                if(!element.equals("")) {
                    starboard.put(element, starboard.getOrDefault(element, 0) + 1);
                }
            }

            updateStars(starboard, guild);
        }));
    }
}