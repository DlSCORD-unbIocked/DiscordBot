package org.smartscholars.projectmanager.commands.api;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;
import org.smartscholars.projectmanager.commands.vc.lavaplayer.PlayerManager;
import org.smartscholars.projectmanager.util.VcUtil;

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

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        event.deferReply().queue();
        Member member = event.getMember();
        assert member != null;

        if(!VcUtil.isMemberInVoiceChannel(member)) {
            event.getHook().sendMessage("`You need to be in a voice channel`").queue();
            return;
        }

        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();

        if(!VcUtil.isSelfInVoiceChannel(self)) {
            event.getGuild().getAudioManager().openAudioConnection(member.getVoiceState().getChannel());
        }
        else if(!VcUtil.isMemberInSameVoiceChannel(member, self)) {
            event.getHook().sendMessage("`You need to be in the same channel as me`").queue();
            return;
        }

        //generateTTS(Objects.requireNonNull(event.getOption("message")).getAsString(), event.getOption("voice_id") != null ? Objects.requireNonNull(event.getOption("voice_id")).getAsString() : "29vD33N1CtxCmqQRPOHJ");

        File file = new File(outputPath.replace("\\", "/"));
        if (!file.exists()) {
            logger.error("TTS file does not exist: {}", file.getAbsolutePath());
            event.getHook().sendMessage("An error occurred: TTS file does not exist.").queue();
        }
        //load file
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
