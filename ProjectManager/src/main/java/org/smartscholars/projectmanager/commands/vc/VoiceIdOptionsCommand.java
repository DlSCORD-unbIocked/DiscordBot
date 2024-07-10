package org.smartscholars.projectmanager.commands.vc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;



@CommandInfo(
        name = "voice-id-options",
        description = "Get the list of available voice ids",
        options = {
                @CommandOption(
                    name = "page",
                    description = "The page number",
                    type = OptionType.INTEGER,
                    required = false
                )
        }
)

public class VoiceIdOptionsCommand implements ICommand {

    private final Logger logger = LoggerFactory.getLogger(VoiceIdOptionsCommand.class);
    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Dotenv dotenv = Dotenv.load();
        String authToken = dotenv.get("SPEECH_TOKEN");

        String apiURL = "https://api.elevenlabs.io/v1/voices";

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiURL))
                .headers("Accept", "application/json", "xi-api-key", authToken, "Content-Type", "application/json")
                .GET()
                .build();

        HttpClient client = HttpClient.newHttpClient();

        try {
            logger.info("Sending request to API");
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            logger.info("Received response from API");

            String responseBody = response.body();
            JsonElement jsonElement = JsonParser.parseString(responseBody);
            JsonObject jsonObj = jsonElement.getAsJsonObject();
            JsonArray voicesArray = jsonObj.getAsJsonArray("voices");
            StringBuilder builder = new StringBuilder();

            for (int i = 0; i < voicesArray.size(); i++) {
                JsonObject voice = voicesArray.get(i).getAsJsonObject();
                String id = "";
                String name = "";

                if (voice.has("voice_id") && !voice.get("voice_id").isJsonNull()) {
                    id = voice.get("voice_id").getAsString();
                }

                if (voice.has("name") && !voice.get("name").isJsonNull()) {
                    name = voice.get("name").getAsString();
                }

                builder.append("ID: ").append(id).append(", Name: ").append(name).append("\n");
            }
            event.getHook().sendMessage(builder.toString()).queue();
        }
        catch (IOException | InterruptedException e) {
            logger.error("Failed to fetch voice ID options", e);
            event.getHook().sendMessage("An error occurred").queue();
        }
    }
}
