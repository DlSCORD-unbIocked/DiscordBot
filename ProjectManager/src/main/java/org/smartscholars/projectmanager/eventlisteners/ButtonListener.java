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

import java.util.Objects;

public class ButtonListener extends ListenerAdapter implements IEvent {

    @Override
    public void execute(GenericEvent event) {
        onButtonInteraction((ButtonInteractionEvent) event);
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
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
    }
}