package org.smartscholars.projectmanager.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.commands.ICommand;

import java.awt.*;

@CommandInfo(name = "image", description = "Edits an image in many different ways")
public class ImageCommand implements ICommand {

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Define a command with options
        event.getGuild().upsertCommand("edit", "edit image")
                .addOption(OptionType.STRING, "image_url", "select the image url to edit", true) // Marked as required
                .addOption(OptionType.STRING, "effect", "the effect to apply", true) // Another option, also marked as required
                .queue();
        event.reply("Command with options has been set up").queue();
    }
}