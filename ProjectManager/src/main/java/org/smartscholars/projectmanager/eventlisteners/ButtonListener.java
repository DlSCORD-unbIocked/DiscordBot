package org.smartscholars.projectmanager.eventlisteners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import org.smartscholars.projectmanager.commands.misc.ImageOptionsCommand;
import org.smartscholars.projectmanager.commands.vc.VoiceIdOptionsCommand;


import java.awt.*;
import java.util.Objects;

import static org.smartscholars.projectmanager.commands.vc.VoiceIdOptionsCommand.voiceEntries;

public class ButtonListener extends ListenerAdapter implements IEvent {

    @Override
    public void execute(GenericEvent event) {
        onButtonInteraction((ButtonInteractionEvent) event);
    }
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        //to add more buttons, add more else if statements
        if (event.getComponentId().equals("left_image")) {
            event.deferEdit().queue();
            int page = Integer.parseInt(Objects.requireNonNull(event.getMessage().getEmbeds().getFirst().getDescription()).split("\\s+")[1]);
            EmbedBuilder embed = ImageOptionsCommand.buildPageEmbed(ImageOptionsCommand.pages.get(page - 2), page - 1, ImageOptionsCommand.pages.size());
            event.getMessage().editMessageEmbeds(embed.build()).queue();

        }
        if (event.getComponentId().equals("right_image")) {
            //defer
            event.deferEdit().queue();
            int page = Integer.parseInt(Objects.requireNonNull(event.getMessage().getEmbeds().getFirst().getDescription()).split("\\s+")[1]);
            EmbedBuilder embed = ImageOptionsCommand.buildPageEmbed(ImageOptionsCommand.pages.get(page), page + 1, ImageOptionsCommand.pages.size());
            event.getMessage().editMessageEmbeds(embed.build()).queue();
        }

        if (event.getComponentId().equals("prev_voice_page")) {
            event.deferEdit().queue();
            int currentPage = Integer.parseInt(Objects.requireNonNull(event.getMessage().getEmbeds().getFirst().getDescription()).split("\\s+")[1]) + 1;
            if (currentPage > 1) {
                EmbedBuilder embed = VoiceIdOptionsCommand.buildPageEmbed(VoiceIdOptionsCommand.partitions.get(currentPage - 2), currentPage - 1, VoiceIdOptionsCommand.partitions.size());
                Button prevButton = Button.of(ButtonStyle.PRIMARY, "prev_voice_page", "Previous").withDisabled(currentPage - 1 <= 1);
                Button nextButton = Button.of(ButtonStyle.PRIMARY, "next_voice_page", "Next").withDisabled(currentPage - 1 >= VoiceIdOptionsCommand.partitions.size());
                event.getHook().editOriginalEmbeds(embed.build()).setActionRow(prevButton, nextButton).queue();
            }
        }
        if (event.getComponentId().equals("next_voice_page")) {
            event.deferEdit().queue();
            int currentPage = Integer.parseInt(Objects.requireNonNull(event.getMessage().getEmbeds().getFirst().getDescription()).split("\\s+")[1]) - 1;
            if (currentPage <= VoiceIdOptionsCommand.partitions.size()) {
                EmbedBuilder embed = VoiceIdOptionsCommand.buildPageEmbed(VoiceIdOptionsCommand.partitions.get(currentPage - 1), currentPage, VoiceIdOptionsCommand.partitions.size());
                Button prevButton = Button.of(ButtonStyle.PRIMARY, "prev_voice_page", "Previous").withDisabled(currentPage <= 1);
                Button nextButton = Button.of(ButtonStyle.PRIMARY, "next_voice_page", "Next").withDisabled(currentPage >= VoiceIdOptionsCommand.partitions.size());
                event.getHook().editOriginalEmbeds(embed.build()).setActionRow(prevButton, nextButton).queue();
            }
        }
    }
}