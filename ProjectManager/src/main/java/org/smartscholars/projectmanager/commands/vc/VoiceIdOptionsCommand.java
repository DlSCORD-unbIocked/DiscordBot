package org.smartscholars.projectmanager.commands.vc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;

@CommandInfo(
        name = "voice-id-options",
        description = "Get the list of available voice ids",
        options = {
                @CommandOption(
                    name = "language",
                    description = "The language you want to get the voice ids for",
                    type = OptionType.STRING,
                    required = true
                ),
                @CommandOption(
                        name = "page",
                        description = "The page number",
                        type = OptionType.INTEGER,
                        required = false
                )
        }
)

public class VoiceIdOptionsCommand implements ICommand {

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.reply("test").queue();
    }
}
