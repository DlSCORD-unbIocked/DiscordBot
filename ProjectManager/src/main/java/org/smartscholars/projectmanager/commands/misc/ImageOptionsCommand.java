package org.smartscholars.projectmanager.commands.misc;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.util.ListUtils;

import java.awt.*;
import java.util.*;
import java.util.List;

@CommandInfo(
        name = "image-options",
        description = "Displays a list of available image options",
        options = {
                @CommandOption(name = "page", description = "The page number of the command list", type = OptionType.INTEGER, required = false)
        }
)
public class ImageOptionsCommand implements ICommand {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(HelpCommand.class);

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        int pageRequested = event.getOption("page") != null ? Objects.requireNonNull(event.getOption("page")).getAsInt() : 1;

        int COMMANDS_PER_PAGE = 15;
        List<List<String>> pages = paginateCommands(List.of(ImageCommand.endpoints), COMMANDS_PER_PAGE);

        if (pageRequested > pages.size()) pageRequested = pages.size();
        if (pageRequested < 1) pageRequested = 1;

        EmbedBuilder embed = buildPageEmbed(pages.get(pageRequested - 1), pageRequested, pages.size());
        event.replyEmbeds(embed.build()).queue();


    }

    private java.util.List<java.util.List<String>> paginateCommands(java.util.List<String> commands, int pageSize) {
        return new ArrayList<>(ListUtils.partition(commands, pageSize));
    }

    private EmbedBuilder buildPageEmbed(List<String> commandsOnPage, int currentPage, int totalPages) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Image edit options");
        embed.setDescription("Page " + currentPage + " of " + totalPages);
        embed.setColor(new Color(0x1F8B4C));
        commandsOnPage.forEach(command -> embed.addField("", command, true));
        embed.addField("", "View the API Gallery [here](https://jeyy.xyz/gallery)", false);
        return embed;
    }

}
