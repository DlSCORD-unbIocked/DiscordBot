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

import javax.imageio.ImageIO;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;

import net.dv8tion.jda.api.utils.FileUpload;
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
        String apiUpload = "https://api.jeyy.xyz/v2/general/image_upload";

        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(apiUrl))
                .header("Authorization", "Bearer " + authToken)
                .build();


        try {
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            byte[] imageBytes = response.body();

            if (imageBytes.length > 0) {
                EmbedBuilder embed = new EmbedBuilder();
                embed.setTitle("Edited Image");
                embed.setImage("attachment://image.png"); // This will be replaced by the actual image in the next step

                event.replyEmbeds(embed.build())
                        .addFiles(FileUpload.fromData(imageBytes, "image.png"))
                        .queue();
            }
            else {
                event.reply("No image was returned from the API.").setEphemeral(true).queue();
            }
        }
        catch (IOException | InterruptedException e) {
            event.reply("An error occurred while fetching the image.").setEphemeral(true).queue();
        }

//        event.reply("Command with options has been set up " + url + effect).queue();

    }
}