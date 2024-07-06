package org.smartscholars.projectmanager.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.commands.ICommand;

import java.awt.*;

@CommandInfo(name = "edit", description = "Edits an image in many different ways")
public class EditCommand implements ICommand {

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Define a command with options
        String url = event.getOption("image_url").getAsString();
        String effect = event.getOption("effect").getAsString();
        event.reply("Command with options has been set up " + url + effect).queue();
    }
}