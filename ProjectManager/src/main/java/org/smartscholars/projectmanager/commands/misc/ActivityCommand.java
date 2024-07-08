package org.smartscholars.projectmanager.commands.misc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;

import java.util.Objects;

@CommandInfo(
        name = "activity",
        description = "Set a reminder for a specific time",
        options = {
            @CommandOption(
                    name = "activity",
                    description = "The activity to set the reminder for",
                    type = OptionType.STRING,
                    required = true
            ),
        })
public class ActivityCommand implements ICommand{

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String activity = Objects.requireNonNull(event.getOption("activity")).getAsString();
        event.reply(activity).queue();

    }
}
