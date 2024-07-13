package org.smartscholars.projectmanager.eventlisteners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import org.smartscholars.projectmanager.commands.api.ImageOptionsCommand;
import org.smartscholars.projectmanager.commands.api.VoiceIdOptionsCommand;
import org.smartscholars.projectmanager.commands.misc.RockPaperScissorsCommand;
import org.smartscholars.projectmanager.commands.vc.ListQueueCommand;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ButtonListener extends ListenerAdapter implements IEvent {

    private final Map<String, String> messageIdToUserIdMap = new HashMap<>();
    private static ButtonListener instance;
    public ButtonListener() {}

    @Override
    public void execute(GenericEvent event) {
        onButtonInteraction((ButtonInteractionEvent) event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        String userId = messageIdToUserIdMap.get(event.getMessageId());
        if (userId == null || !userId.equals(event.getUser().getId())) {
            event.reply("You are not allowed to interact with this button.").setEphemeral(true).queue();
            return;
        }
        //button handling for ImageOptionsCommand
        if (event.getComponentId().equals("left_image") || event.getComponentId().equals("right_image")) {
            event.deferEdit().queue();
            int currentPage = Integer.parseInt(Objects.requireNonNull(event.getMessage().getEmbeds().getFirst().getDescription()).split("\\s+")[1]);
            boolean isFirstPage, isLastPage;

            if (event.getComponentId().equals("left_image")) {
                currentPage = Math.max(currentPage - 1, 1); // Ensure currentPage does not go below 1
            }
            else if (event.getComponentId().equals("right_image")) {
                currentPage = Math.min(currentPage + 1, ImageOptionsCommand.pages.size()); // Ensure currentPage does not exceed total pages
            }

            isFirstPage = currentPage <= 1;
            isLastPage = currentPage >= ImageOptionsCommand.pages.size();

            EmbedBuilder embed = ImageOptionsCommand.buildPageEmbed(ImageOptionsCommand.pages.get(currentPage - 1), currentPage, ImageOptionsCommand.pages.size());

            Button galleryButton = Button.of(ButtonStyle.LINK,"https://jeyy.xyz/gallery", "View Options Gallery");
            Button prev = Button.of(ButtonStyle.PRIMARY, "left_image", Emoji.fromUnicode("◀")).withDisabled(isFirstPage);
            Button next = Button.of(ButtonStyle.PRIMARY, "right_image", Emoji.fromUnicode("▶")).withDisabled(isLastPage);

            event.getHook().editOriginalEmbeds(embed.build()).setComponents(ActionRow.of(galleryButton), ActionRow.of(prev, next)).queue();
        }

        if (event.getComponentId().equals("prev_voice_page") || event.getComponentId().equals("next_voice_page")) {
            event.deferEdit().queue();
            int currentPage = Integer.parseInt(Objects.requireNonNull(event.getMessage().getEmbeds().getFirst().getDescription()).split("\\s+")[1]);
            EmbedBuilder embed = null;

            boolean isFirstPage = currentPage <= 1;
            boolean isLastPage = currentPage >= VoiceIdOptionsCommand.partitions.size();

            if ("prev_voice_page".equals(event.getComponentId())) {
                if (!isFirstPage) {
                    currentPage--;
                }
            }
            else if ("next_voice_page".equals(event.getComponentId())) {
                if (!isLastPage) {
                    currentPage++;
                }
            }

            if (currentPage < 1) {
                currentPage = 1;
            }
            else if (currentPage > VoiceIdOptionsCommand.partitions.size()) {
                currentPage = VoiceIdOptionsCommand.partitions.size();
            }

            isFirstPage = currentPage <= 1;
            isLastPage = currentPage >= VoiceIdOptionsCommand.partitions.size();

            if (currentPage >= 1 && currentPage <= VoiceIdOptionsCommand.partitions.size()) {
                embed = VoiceIdOptionsCommand.buildPageEmbed(VoiceIdOptionsCommand.partitions.get(currentPage - 1), currentPage, VoiceIdOptionsCommand.partitions.size());
            }

            Button linkButton = Button.of(ButtonStyle.LINK, "https://www.elevenlabs.io", "Eleven Labs");
            Button prevButton = Button.of(ButtonStyle.PRIMARY, "prev_voice_page", Emoji.fromUnicode("◀")).withDisabled(isFirstPage);
            Button nextButton = Button.of(ButtonStyle.PRIMARY, "next_voice_page", Emoji.fromUnicode("▶")).withDisabled(isLastPage);

            if (embed != null) {
                event.getHook().editOriginalEmbeds(embed.build()).setComponents(ActionRow.of(linkButton), ActionRow.of(prevButton, nextButton)).queue();
            }
        }

        if (event.getComponentId().equals("prev_queue_page") || event.getComponentId().equals("next_queue_page")) {
            event.deferEdit().queue();
            int currentPage = Integer.parseInt(Objects.requireNonNull(event.getMessage().getEmbeds().getFirst().getDescription()).split("\\s+")[1]);
            EmbedBuilder embed = null;

            boolean isFirstPage = currentPage <= 1;
            boolean isLastPage = currentPage >= ListQueueCommand.pages.size();

            if ("prev_queue_page".equals(event.getComponentId())) {
                if (!isFirstPage) {
                    currentPage--;
                }
            }
            else if ("next_queue_page".equals(event.getComponentId())) {
                if (!isLastPage) {
                    currentPage++;
                }
            }

            if (currentPage < 1) {
                currentPage = 1;
            }
            else if (currentPage > ListQueueCommand.pages.size()) {
                currentPage = ListQueueCommand.pages.size();
            }

            isFirstPage = currentPage <= 1;
            isLastPage = currentPage >= ListQueueCommand.pages.size();

            if (currentPage >= 1 && currentPage <= ListQueueCommand.pages.size()) {
                embed = ListQueueCommand.buildPageEmbed(ListQueueCommand.pages.get(currentPage - 1), currentPage, ListQueueCommand.pages.size());
            }

            Button prev = Button.of(ButtonStyle.PRIMARY, "prev_queue_page", Emoji.fromUnicode("◀")).withDisabled(isFirstPage);
            Button next = Button.of(ButtonStyle.PRIMARY, "next_queue_page", Emoji.fromUnicode("▶")).withDisabled(isLastPage);

            if (embed != null) {
                event.getHook().editOriginalEmbeds(embed.build()).setComponents(ActionRow.of(prev, next)).queue();
            }
        }

        if (event.getComponentId().equals("rock") || event.getComponentId().equals("paper") || event.getComponentId().equals("scissors")) {
            RockPaperScissorsCommand command = new RockPaperScissorsCommand();
            command.handleButtonInteraction(event.getComponentId(), event);
            event.deferEdit().queue();
        }
    }

    public Map<String, String> getMessageIdToUserIdMap() {
        return messageIdToUserIdMap;
    }

    public static synchronized ButtonListener getInstance() {
        if (instance == null) {
            instance = new ButtonListener();
        }
        return instance;
    }

}