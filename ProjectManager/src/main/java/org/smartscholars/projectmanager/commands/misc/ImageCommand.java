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

        event.getGuild().upsertCommand("edit", "edit image").addOption(OptionType.STRING, "image url", "select the image url to edit").queue();
        event.reply("test reply");
    }
}
