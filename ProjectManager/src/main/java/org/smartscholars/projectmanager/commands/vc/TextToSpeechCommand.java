package org.smartscholars.projectmanager.commands.vc;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.MusicPlayer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
                    name = "voice_id",
                    description = "The voice you want to use",
                    type = OptionType.STRING,
                    required = false
                )
        }
)
public class TextToSpeechCommand implements ICommand {

    private final Logger logger = LoggerFactory.getLogger(TextToSpeechCommand.class);
    private final String outputPath = "ProjectManager/src/main/resources/tts.mp3";
    private final MusicPlayer musicPlayer;

    public TextToSpeechCommand(MusicPlayer musicPlayer) {
        this.musicPlayer = musicPlayer;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        if (!Objects.requireNonNull(event.getGuild()).getAudioManager().isConnected()) {
            event.reply("You need to be in a voice channel to use this command").queue();
            return;
        }
        VoiceChannel userVoiceChannel = (VoiceChannel) Objects.requireNonNull(Objects.requireNonNull(event.getMember()).getVoiceState()).getChannel();
        VoiceChannel botVoiceChannel = (VoiceChannel) event.getGuild().getAudioManager().getConnectedChannel();

        if (userVoiceChannel == null || !userVoiceChannel.equals(botVoiceChannel)) {
            event.reply("You need to be in the same voice channel as the bot to use this command").queue();
            return;
        }

        event.deferReply().queue();
        String message = Objects.requireNonNull(event.getOption("message")).getAsString();
        String voiceId = event.getOption("voice_id") != null ? Objects.requireNonNull(event.getOption("voice_id")).getAsString() : "zcAOhNBS3c14rBihAFp1";

        //generateTTS(message, voiceId);
        File file = new File(outputPath.replace("\\", "/"));
        musicPlayer.play("file://" + file.getAbsolutePath());
        //musicPlayer.play("https://www.youtube.com/watch?v=AzqTd73CCrA");

    }

    private void generateTTS(String message, String voiceId) {
        Dotenv dotenv = Dotenv.load();
        String authToken = dotenv.get("SPEECH_TOKEN");

        String ttsUrl = "https://api.elevenlabs.io/v1/text-to-speech/" + voiceId + "/stream";

        String requestBody = String.format("{\"text\":\"%s\", \"model_id\": \"eleven_multilingual_v2\", \"voice_settings\": {\"stability\": 0.5, \"similarity_boost\": 0.8, \"style\": 0.0, \"use_speaker_boost\": true}}", message);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(java.net.URI.create(ttsUrl))
                .header("Accept", "application/json")
                .header("xi-api-key", authToken)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpClient client = HttpClient.newHttpClient();
        try {
            HttpResponse<InputStream> response = client.send(request, HttpResponse.BodyHandlers.ofInputStream());

            File outputFile = new File(outputPath);
            try (InputStream responseBody = response.body(); OutputStream outputStream = new FileOutputStream(outputFile)) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = responseBody.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
            }
            catch (Exception e) {
                logger.error("Error while saving the text-to-speech response to file", e);
            }
        }
        catch (Exception e) {
            logger.error("Error while sending request to text-to-speech API", e);
        }
    }
}
