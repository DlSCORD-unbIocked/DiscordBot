package org.smartscholars.projectmanager.commands.misc;
import java.io.*;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import io.github.cdimascio.dotenv.Dotenv;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandManager;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.CommandOption;


@CommandInfo(
        name = "image",
        description = "Edits an image in many different ways",
        options = {
                @CommandOption(name = "image_url", description = "select the image url to edit", type = OptionType.STRING, required = true),
                @CommandOption(name = "effect", description = "the effect to apply", type = OptionType.STRING, required = true)
        }
)
public class ImageCommand implements ICommand {

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        // Define a command with options
        Dotenv dotenv = Dotenv.load();
        String authToken = dotenv.get("IMAGE_TOKEN");

        String url = event.getOption("image_url").getAsString();
        String effect = event.getOption("effect").getAsString();
        String apiUrl = "https://api.jeyy.xyz/v2/image/" + effect + "?url=" + url;


        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + authToken)
                .build();

        try {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            // Process the response body here
            String responseBody = response.body();


//            byte[] imageBytes = Base64.getDecoder().decode(responseBody);
            byte[] imageBytes = responseBody.getBytes();
            InputStream is = new ByteArrayInputStream(imageBytes);
            BufferedImage bufferedImage = ImageIO.read(is);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "png", baos); // Use appropriate format, e.g., "png"
            byte[] bytes = baos.toByteArray();


            event.reply("Command with options has been set up. URL: " + url + ", Effect: " + effect + ". Response: " ).addFiles().queue();
        } catch (IOException | InterruptedException e) {
            event.reply("An error occurred while fetching the URL.").queue();
        }

        event.reply("Command with options has been set up " + url + effect).queue();

    }
}