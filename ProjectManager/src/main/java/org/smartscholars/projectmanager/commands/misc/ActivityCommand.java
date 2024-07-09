package org.smartscholars.projectmanager.commands.misc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.text.TextInput;
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle;
import net.dv8tion.jda.api.interactions.modals.Modal;
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
public class ActivityCommand extends ListenerAdapter implements ICommand{

    @Override
    public void execute(SlashCommandInteractionEvent event) {

        String activity = Objects.requireNonNull(event.getOption("activity")).getAsString();

        TextInput dateInput = TextInput.create("date", "What date for " + activity + "?", TextInputStyle.SHORT)
                                       .setPlaceholder("MM/DD/YYYY")
                                       .setRequiredRange(10, 10)
                                       .build();

        TextInput timeInput = TextInput.create("time", "What time for (military time) " + activity + "?", TextInputStyle.SHORT)
                                       .setPlaceholder("HH:MM")
                                       .setRequiredRange(5, 5)
                                       .build();


        Modal modal = Modal.create("activityDateModal:" + activity, "Activity Date") // Append activity to the modal ID
                .setTitle("Activity Date")
                .addComponents(ActionRow.of(dateInput), ActionRow.of(timeInput))
                .build();

        event.replyModal(modal).queue();
    }
}
