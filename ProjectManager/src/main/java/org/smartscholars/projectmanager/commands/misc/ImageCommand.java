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
import java.nio.channels.Channel;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Objects;

import javax.imageio.ImageIO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import net.dv8tion.jda.api.utils.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger logger = LoggerFactory.getLogger(ImageCommand.class);

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        Dotenv dotenv = Dotenv.load();
        String authToken = dotenv.get("IMAGE_TOKEN");

        String url = Objects.requireNonNull(event.getOption("image_url")).getAsString();
        // Validate URL format (basic validation)
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            event.reply("Invalid image URL. Please ensure it starts with http:// or https://").setEphemeral(true).queue();
            return;
        }

        String effect = Objects.requireNonNull(event.getOption("effect")).getAsString();
//        // Example validation for effect, assuming a predefined list of effects
//        if (!effect.matches("effect1|effect2|effect3")) {
//            event.reply("Invalid effect. Please choose from effect1, effect2, or effect3.").setEphemeral(true).queue();
//            return;
//        }

        String apiUrl = "https://api.jeyy.xyz/v2/image/" + effect + "/?image_url=" + url;

        logger.info("Fetching image from API: {}", apiUrl);

        HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .build();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + authToken)
                .build();

        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            // Check for 307 Temporary Redirect
            if (response.statusCode() == 307) {
                String newUrl = response.headers().firstValue("Location").orElse(null);
                if (newUrl != null) {
                    HttpRequest newRequest = HttpRequest.newBuilder()
                            .uri(URI.create(newUrl))
                            .header("Authorization", "Bearer " + authToken)
                            .build();
                    response = client.send(newRequest, HttpResponse.BodyHandlers.ofByteArray());
                }
            }

            if (response.statusCode() != 200) {
                event.reply("Failed to fetch image from API. Status: " + response.statusCode()).setEphemeral(true).queue();
                return;
            }
            byte[] imageBytes = response.body();

            if (imageBytes.length == 0) {
                event.reply("No image was returned from the API.").setEphemeral(true).queue();
                return;
            }

            BufferedImage image = null;
            try (ByteArrayInputStream bis = new ByteArrayInputStream(imageBytes)) {
                image = ImageIO.read(bis);
            } catch (IOException e) {
                logger.error("An error occurred while converting bytes to an image", e);
            }

            if (image == null) {
                event.reply("Failed to process the image data.").setEphemeral(true).queue();
                return;
            }
            String fileExtension = response.headers().firstValue("Content-Type").orElse("image/png").split("/")[1];

            EmbedBuilder embed = new EmbedBuilder();
            embed.setTitle("Edited Image");
            embed.setImage("attachment://image." + fileExtension);

            event.replyEmbeds(embed.build())
                    .addFiles(FileUpload.fromData(imageBytes, "image." + fileExtension))
                    .queue();
        }
        catch (IOException | InterruptedException e) {
            event.reply("An error occurred while fetching the image.").setEphemeral(true).queue();
            logger.error("An error occurred while fetching the image", e);
        }
    }
}