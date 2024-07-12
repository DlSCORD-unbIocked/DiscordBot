package org.smartscholars.projectmanager.commands.misc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;

@CommandInfo(
        name = "ping",
        description = "Get your ping"
)
public class PingCommand implements ICommand {
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long time = System.currentTimeMillis();
        event.reply("Pong!").queue(response -> {
            long latency = System.currentTimeMillis() - time;
            response.editOriginalFormat("Pong! %d ms", latency).queue();
        });
    }
}
