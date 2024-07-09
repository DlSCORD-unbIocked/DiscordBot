package org.smartscholars.projectmanager.commands.vc;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;

import java.util.Objects;

@CommandInfo(name = "text-to-speech",
        description = "Converts text to speech in vc (make sure you are in vc)",
        options = {
                @CommandOption(
                    name = "message",
                    description = "The message you want to convert to speech",
                    type = OptionType.STRING,
                    required = true
                ),
                @CommandOption(
                    name = "voice",
                    description = "The voice you want to use",
                    type = OptionType.STRING,
                    required = false
                )
        }
)
public class TextToSpeechCommand implements ICommand {

    private final int CHUNK_SIZE = 1024;
    private final String EXPORT_PATH = "ProjectManager/src/main/resources/audio/output.mp3";
    private final Logger logger = LoggerFactory.getLogger(TextToSpeechCommand.class);

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Dotenv dotenv = Dotenv.load();
        String authToken = dotenv.get("SPEECH_TOKEN");

        String message = Objects.requireNonNull(event.getOption("message")).getAsString();
        String voice = Objects.requireNonNull(event.getOption("voice")).getAsString();

        String apiURL = "https://api.elevenlabs.io/v1/voices";
        event.reply("test").queue();
    }
}
