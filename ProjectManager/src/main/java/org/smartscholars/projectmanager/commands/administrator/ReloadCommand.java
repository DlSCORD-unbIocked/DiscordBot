package org.smartscholars.projectmanager.commands.administrator;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.Permission;

@CommandInfo(name = "reload", description = "Reloads the commands", permissions = {Permission.ADMINISTRATOR})
public class ReloadCommand implements ICommand {

    private final CommandManager commandManager;

    public ReloadCommand(CommandManager commandManager)
    {
        this.commandManager = commandManager;
    }

    //testing command to register commands without having to rerun the bot
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        commandManager.reloadCommands();
        //Only register commands in dev server for testing
        commandManager.registerNewCommandsForGuild(CommandManager.getJda(), "1086425022245118033");
        event.reply("Reloaded commands").queue();
    }
}
