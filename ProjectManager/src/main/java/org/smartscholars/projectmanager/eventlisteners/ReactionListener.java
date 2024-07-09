package org.smartscholars.projectmanager.eventlisteners;


import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;


import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.service.SchedulerService;

import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.*;


public class ReactionListener extends ListenerAdapter implements IEvent {
    public EmbedBuilder embed;
    private static final Logger logger = LoggerFactory.getLogger(SchedulerService.class);
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

        if(emojicode.equals("⭐"))
        {
            String channelUrl = channel.getId() + "/" +  messageid;
            editStarboard(channelUrl, true);
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
            editStarboard(channelUrl, false);
            getStarredComments(guild);
        }
    }
    public void editStarboard(String path, boolean add)
    {
        try {
            String filePath = "ProjectManager/src/main/resources/starboard.json";
            FileReader reader = new FileReader(filePath);
            Type type = new TypeToken<JsonObject>() {}.getType();
            JsonObject jsonObject = new Gson().fromJson(reader, type);
            String leaderboard = jsonObject.get("leaderboard").getAsString();

            if(add)
            {
                leaderboard += path + ",";
            }
            else
            {
                leaderboard = leaderboard.replace(path + ",", "");
            }
            jsonObject.remove("leaderboard");
            jsonObject.addProperty("leaderboard", leaderboard);
            reader.close();
            Gson gson = new Gson();
            String json = gson.toJson(jsonObject);

            FileWriter writer = new FileWriter(filePath);
            writer.write(json);
            writer.close();
        }
        catch (Exception e) {
            logger.error("Error reading from file", e);
        }
    }

    public void updateStars(HashMap<String, Integer> starboard, String path, Guild guild)
    {
        String embedchannelid = path.split("/")[0];
        String embedmessageid = path.split("/")[1];
        guild.getTextChannelById(embedchannelid).retrieveMessageById(embedmessageid).queue((message -> {
            embed = new EmbedBuilder();
            embed.setTitle("Star Leaderboard");
            embed.setColor(Color.BLUE);

            Set<String> keys = starboard.keySet();

            String[] starredMessages = keys.toArray(new String[keys.size()]);
            for(String mess : starredMessages)
            {
                String channelid = mess.split("/")[0];
                String messageid = mess.split("/")[1];
                guild.getTextChannelById(channelid).retrieveMessageById(messageid).queue((m -> {
                    embed.addField("Stars: " + starboard.get(starredMessages[0]), "Author: "+ m.getAuthor().getName() +"\nMessage: " + m.getContentDisplay() + "\nOriginal: https://discord.com/channels/" + guild.getId()+"/"+channelid+"/"+messageid, false);

                    message.editMessageEmbeds(embed.build()).queue();
                }));

            }

        }));
    }
    public void getStarredComments(Guild guild) {

        String filePath = "ProjectManager/src/main/resources/starboard.json";
        FileReader reader;
        try {
            reader = new FileReader(filePath);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        Type type = new TypeToken<JsonObject>() {
        }.getType();
        JsonObject jsonObject = new Gson().fromJson(reader, type);
        String leader = jsonObject.get("leaderboard").getAsString();
        String path = jsonObject.get("starboardPath").getAsString();


        String[] parts = leader.split(",");

        HashMap<String, Integer> starboard = new HashMap<>();
        for (String element : parts) {
            if (!element.equals("")) {
                starboard.put(element, starboard.getOrDefault(element, 0) + 1);
            }
        }

        updateStars(starboard, path, guild);
    };
}
