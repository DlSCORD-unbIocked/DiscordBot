package org.smartscholars.projectmanager.commands.vc;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.util.ListUtils;

import java.awt.*;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.List;


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
    public static List<Map.Entry<String, String>> voiceEntries;
    public static List<List<Map.Entry<String, String>>> partitions;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Dotenv dotenv = Dotenv.load();
        String authToken = dotenv.get("SPEECH_TOKEN");

        int currentPage = event.getOption("page") != null ? Objects.requireNonNull(event.getOption("page")).getAsInt() : 1;

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

            //clear existing data before fetching new data
            voiceEntries = new ArrayList<>();
            partitions = new ArrayList<>();

            for (int i = 0; i < voicesArray.size(); i++) {
                JsonObject voice = voicesArray.get(i).getAsJsonObject();
                if (voice.has("voice_id") && !voice.get("voice_id").isJsonNull() && voice.has("name") && !voice.get("name").isJsonNull()) {
                    voiceEntries.add(new AbstractMap.SimpleEntry<>(voice.get("voice_id").getAsString(), voice.get("name").getAsString()));
                }
            }

            int totalSize = voiceEntries.size();
            //change to whatever
            int ITEMS_PER_PAGE = 7;

            boolean isFirstPage = currentPage == 1;
            boolean isLastPage = currentPage >= (totalSize + ITEMS_PER_PAGE - 1) / ITEMS_PER_PAGE;

            partitions = ListUtils.partition(voiceEntries, ITEMS_PER_PAGE);

            if (currentPage > partitions.size()) currentPage = partitions.size();
            if (currentPage < 1) currentPage = 1;

            EmbedBuilder embed = buildPageEmbed(partitions.get(currentPage - 1), currentPage, partitions.size());

            Button linkButton = Button.of(ButtonStyle.LINK, "https://www.elevenlabs.io", "Eleven Labs");
            Button prevButton = Button.of(ButtonStyle.PRIMARY,"prev_voice_page", Emoji.fromUnicode("◀"));
            Button nextButton = Button.of(ButtonStyle.PRIMARY,"next_voice_page", Emoji.fromUnicode("▶"));

            prevButton = isFirstPage ? prevButton.asDisabled() : prevButton;
            nextButton = isLastPage ? nextButton.asDisabled() : nextButton;

            event.getHook().sendMessageEmbeds(embed.build()).addActionRow(linkButton).addActionRow(prevButton, nextButton).queue();
        }
        catch (IOException | InterruptedException e) {
            logger.error("Failed to fetch voice ID options", e);
            event.getHook().sendMessage("An error occurred").queue();
        }
    }

    public static EmbedBuilder buildPageEmbed(List<Map.Entry<String, String>> commands, int current, int totalPages) {
        EmbedBuilder embed = new EmbedBuilder();
        embed.setTitle("Voice ID Options");
        embed.setDescription("Page " + current + " of " + totalPages);
        embed.setColor(new Color(0x1F8B4C));
        commands.forEach(entry -> embed.addField(entry.getValue(), entry.getKey(), false));
        return embed;
    }
}


