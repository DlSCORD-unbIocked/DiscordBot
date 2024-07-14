package org.smartscholars.projectmanager.commands.api;

import io.github.cdimascio.dotenv.Dotenv;
import net.dv8tion.jda.api.entities.Member;
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
import java.util.HashMap;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

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
    private final String outputPath = "ProjectManager/src/main/resources/tts/";

    private static final long COOLDOWN_DURATION = TimeUnit.SECONDS.toMillis(5);
    private static final HashMap<Long, Long> cooldowns = new HashMap<>();

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        long userId = event.getUser().getIdLong();
        long currentTime = System.currentTimeMillis();

        if (cooldowns.containsKey(userId) && (currentTime - cooldowns.get(userId) < COOLDOWN_DURATION)) {
            long timeLeft = COOLDOWN_DURATION - (currentTime - cooldowns.get(userId));
            event.reply(String.format("Please wait %d seconds before using TTS again.", TimeUnit.MILLISECONDS.toSeconds(timeLeft))).queue();
            return;
        }

        event.deferReply().queue();
        Member member = event.getMember();
        assert member != null;

        if(!VcUtil.isMemberInVoiceChannel(member)) {
            event.getHook().sendMessage("`You need to be in a voice channel`").queue();
            return;
        }

        Member self = Objects.requireNonNull(event.getGuild()).getSelfMember();

        if(!VcUtil.isSelfInVoiceChannel(self)) {
            event.getGuild().getAudioManager().openAudioConnection(Objects.requireNonNull(member.getVoiceState()).getChannel());
        }
        else if(!VcUtil.isMemberInSameVoiceChannel(member, self)) {
            event.getHook().sendMessage("`You need to be in the same channel as me`").queue();
            return;
        }
        if (PlayerManager.get().tryPlaying()) {
            long time = generateTTS(Objects.requireNonNull(event.getOption("message")).getAsString(), event.getOption("voice_id") != null ? Objects.requireNonNull(event.getOption("voice_id")).getAsString() : "29vD33N1CtxCmqQRPOHJ");

            File file = new File(outputPath.replace("\\", "/") + time + ".mp3");
            if (!file.exists()) {
                event.getHook().sendMessage("An error occurred: TTS file does not exist.").queue();
                return;
            }

            PlayerManager playerManager = PlayerManager.get();
            playerManager.loadAndPlay(event.getGuild(), file.getPath().replace("\\", "/"), event.getChannel().asTextChannel(), false, true);
            event.getHook().sendMessage("`Playing TTS`").setEphemeral(true).queue();
            PlayerManager.get().finishPlaying();
            cooldowns.remove(userId);
            cooldowns.put(userId, currentTime);
        }
        else {
            event.getHook().sendMessage("The bot is currently busy.").setEphemeral(true).queue();
        }
    }

    private long generateTTS(String message, String voiceId) {
        Dotenv dotenv = Dotenv.load();
        String authToken = dotenv.get("SPEECH_TOKEN");
        long time = System.currentTimeMillis();
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

            File outputFile = new File(outputPath + time + ".mp3");
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
        return time;
    }
}
