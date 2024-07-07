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
        name = "help",
        description = "Displays a list of available commands",
        options = {
                @CommandOption(name = "category", description = "The category of commands to display", type = OptionType.STRING, required = false),
                @CommandOption(name = "page", description = "The page number of the command list", type = OptionType.INTEGER, required = false)
        }
)
public class HelpCommand implements ICommand {

    private final Logger logger = org.slf4j.LoggerFactory.getLogger(HelpCommand.class);

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String categoryRequested = event.getOption("category") != null ? Objects.requireNonNull(event.getOption("category")).getAsString() : null;
        int pageRequested = event.getOption("page") != null ? Objects.requireNonNull(event.getOption("page")).getAsInt() : 1;

        Map<String, List<String>> categorizedCommands = organizeCommandsByCategory();

        if (categoryRequested != null && categorizedCommands.containsKey(categoryRequested)) {
            List<String> commands = categorizedCommands.get(categoryRequested);
            int COMMANDS_PER_PAGE = 5;
            List<List<String>> pages = paginateCommands(commands, COMMANDS_PER_PAGE);

            if (pageRequested > pages.size()) pageRequested = pages.size();
            if (pageRequested < 1) pageRequested = 1;

            EmbedBuilder embed = buildPageEmbed(categoryRequested, pages.get(pageRequested - 1), pageRequested, pages.size());
            event.replyEmbeds(embed.build()).queue();
            logger.info("test");
            logger.info("test");
        }
        else {
            event.reply("Category not found or no category specified. Please specify a valid category.").setEphemeral(true).queue();
        }
    }

    private Map<String, java.util.List<String>> organizeCommandsByCategory() {
        Map<String, java.util.List<String>> categorizedCommands = new TreeMap<>();
        CommandManager.getCommandClasses().forEach((name, clazz) -> {
            CommandInfo info = clazz.getAnnotation(CommandInfo.class);
            String category = clazz.getPackage().getName().substring(clazz.getPackage().getName().lastIndexOf('.') + 1);
            categorizedCommands.computeIfAbsent(category, k -> new ArrayList<>()).add(name + ": " + info.description());
        });
        return categorizedCommands;
    }

    private java.util.List<java.util.List<String>> paginateCommands(java.util.List<String> commands, int pageSize) {
        return new ArrayList<>(ListUtils.partition(commands, pageSize));
    }

    private EmbedBuilder buildPageEmbed(String category, List<String> commandsOnPage, int currentPage, int totalPages) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Help - " + category + " Commands");
        embed.setDescription("Page " + currentPage + " of " + totalPages);
        embed.setColor(new Color(0x1F8B4C));
        commandsOnPage.forEach(command -> embed.addField("", command, false));
        return embed;
    }
}
