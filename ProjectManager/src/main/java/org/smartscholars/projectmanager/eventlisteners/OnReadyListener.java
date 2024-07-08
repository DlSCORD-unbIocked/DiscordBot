package org.smartscholars.projectmanager.eventlisteners;


import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent;



import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.commands.misc.ImageOptionsCommand;
import org.slf4j.Logger;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class OnReadyListener extends ListenerAdapter implements IEvent {
    private final CommandManager commandManager;
    private Message starredMessages;

    public OnReadyListener(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    @Override
    public void execute(GenericEvent event) {
        if (event instanceof ReadyEvent) {
            onReady((ReadyEvent) event);
        } else if (event instanceof GuildReadyEvent) {
            onGuildReady((GuildReadyEvent) event);
        }
    }

    public void onReady(@NotNull ReadyEvent event) {
        commandManager.setJda(event.getJDA());
        commandManager.registerCommands(event.getJDA());
    }

    public void onGuildReady(@NotNull GuildReadyEvent event) {
        commandManager.registerCommands(event.getGuild());
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        //to add more buttons, add more else if statements
        if (event.getComponentId().equals("left_image")) {
            event.deferEdit().queue();
            int page = Integer.parseInt(event.getMessage().getEmbeds().get(0).getDescription().split("\\s+")[1]);
            EmbedBuilder embed = ImageOptionsCommand.buildPageEmbed(ImageOptionsCommand.pages.get(page - 2), page - 1, ImageOptionsCommand.pages.size());
            event.getMessage().editMessageEmbeds(embed.build()).queue();

        }
        if (event.getComponentId().equals("right_image")) {
            //defer
            event.deferEdit().queue();
            int page = Integer.parseInt(event.getMessage().getEmbeds().get(0).getDescription().split("\\s+")[1]);
            EmbedBuilder embed = ImageOptionsCommand.buildPageEmbed(ImageOptionsCommand.pages.get(page), page + 1, ImageOptionsCommand.pages.size());
            event.getMessage().editMessageEmbeds(embed.build()).queue();

        }
    }
    @Override
    public void onMessageReactionAdd(@NotNull MessageReactionAddEvent event)
    {
        String emojicode = event.getReaction().getEmoji().getAsReactionCode();
        String messageid = event.getMessageId();
        Guild guild = event.getGuild();
        MessageChannelUnion channel = event.getChannel();
//        TextChannel name = guild.getTextChannelById("1259897391214231583");
//        channel.editMessageById("1259903701271974002", "bot:6,test:7,saa:4").queue();
        getStarredComments(guild);
        if(emojicode.equals("⭐"))
        {
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


    public void updateStars(Map<Integer, String> starboard, Guild guild)
    {
        guild.getTextChannelById("1259869260927340614").retrieveMessageById("1259903444920176690").queue((message -> {
            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Star Leaderboard");
            embed.setColor(Color.RED);
            for(Map.Entry<Integer, String> entry : starboard.entrySet()) {
                embed.addField("Stars: " + entry.getKey(), "Message: " + entry.getValue(), false);
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
            Map<Integer, String> starboard = new HashMap<>();
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