package org.smartscholars.projectmanager.commands.misc;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.slf4j.Logger;
import org.smartscholars.projectmanager.commands.CommandInfo;
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
    public static final int COMMANDS_PER_PAGE = 15;
    public static final List<List<String>> pages = paginateCommands(List.of(ImageCommand.endpoints), COMMANDS_PER_PAGE);

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        int currentPage = event.getOption("page") != null ? Objects.requireNonNull(event.getOption("page")).getAsInt() : 1;



        if (currentPage > pages.size()) currentPage = pages.size();
        if (currentPage < 1) currentPage = 1;

        boolean isFirstPage = currentPage == 1;
        boolean isLastPage = currentPage >= ImageOptionsCommand.pages.size();

        Button galleryButton = Button.of(ButtonStyle.LINK,"https://jeyy.xyz/gallery", "View Options Gallery");
        Button prev = Button.of(ButtonStyle.PRIMARY,"left_image", Emoji.fromUnicode("◀"));
        Button next = Button.of(ButtonStyle.PRIMARY,"right_image", Emoji.fromUnicode("▶"));

        prev = isFirstPage ? prev.asDisabled() : prev;
        next = isLastPage ? next.asDisabled() : next;

        EmbedBuilder embed = buildPageEmbed(pages.get(currentPage - 1), currentPage, pages.size());

        event.replyEmbeds(embed.build()).addActionRow(galleryButton).addActionRow(prev, next).queue();


    }

    private static java.util.List<java.util.List<String>> paginateCommands(java.util.List<String> commands, int pageSize) {
        return new ArrayList<>(ListUtils.partition(commands, pageSize));
    }

    public static EmbedBuilder buildPageEmbed(List<String> commandsOnPage, int current, int totalPages) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Image edit options");
        embed.setDescription("Page " + current + " of " + totalPages);
        embed.setColor(new Color(0x1F8B4C));
        commandsOnPage.forEach(command -> embed.addField("", command, true));
        return embed;
    }


}
