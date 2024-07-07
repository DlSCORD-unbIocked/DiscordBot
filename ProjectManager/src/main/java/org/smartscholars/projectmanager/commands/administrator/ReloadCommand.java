package org.smartscholars.projectmanager.commands.administrator;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.slf4j.Logger;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.Permission;

import java.util.Objects;

@CommandInfo(name = "reload", description = "Reloads the commands", permissions = {Permission.ADMINISTRATOR})
public class ReloadCommand implements ICommand {

    private final CommandManager commandManager;
    private final Logger logger = org.slf4j.LoggerFactory.getLogger(ReloadCommand.class);

    public ReloadCommand(CommandManager commandManager) {
        this.commandManager = commandManager;
    }

    //testing command to register commands without having to rerun the bot
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        //Only register commands in dev server for testing
        logger.info(Objects.requireNonNull(event.getGuild()).getId());
        if (Objects.requireNonNull(event.getGuild()).getId().equals("1086425022245118033")) {
            commandManager.reloadCommands();
            commandManager.registerGuildCommands(event.getGuild());
            event.reply("Reloaded commands").queue();
        }
    }
}
