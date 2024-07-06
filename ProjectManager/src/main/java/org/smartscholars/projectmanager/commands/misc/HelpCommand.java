package org.smartscholars.projectmanager.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.commands.ICommand;

import java.awt.*;

@CommandInfo(name = "help", description = "Displays a list of available commands")
public class HelpCommand implements ICommand {

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Help - List of Commands");
        embed.setDescription("Here are the commands you can use:");
        embed.setColor(new Color(0x1F8B4C)); // Set a color for the embed

        CommandManager.getCommandClasses().forEach((name, clazz) -> {
            CommandInfo info = clazz.getAnnotation(CommandInfo.class);
            embed.addField(name, info.description(), false);
        });

        event.replyEmbeds(embed.build()).queue();
    }
}
