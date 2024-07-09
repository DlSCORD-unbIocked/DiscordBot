package org.smartscholars.projectmanager.eventlisteners;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.smartscholars.projectmanager.commands.misc.ImageOptionsCommand;
import org.smartscholars.projectmanager.eventlisteners.IEvent;

import java.awt.*;

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
            int page = Integer.parseInt(event.getMessage().getEmbeds().get(0).getDescription().split("\\s+")[1]);
            EmbedBuilder embed = ImageOptionsCommand.buildPageEmbed(ImageOptionsCommand.pages.get(page - 2), page - 1, ImageOptionsCommand.pages.size());
            event.getMessage().editMessageEmbeds(embed.build()).queue();

        }
        if (event.getComponentId().equals("right_image")) {
            //defer
            event.deferEdit().queue();
            int page = Integer.parseInt(event.getMessage().getEmbeds().get(0).getDescription().split("\\s+")[1]);
            EmbedBuilder embed = ImageOptionsCommand.buildPageEmbed(ImageOptionsCommand.pages.get(page), page + 1, ImageOptionsCommand.pages.size());
            event.getMessage().editMessageEmbeds(embed.build()).queue();

        }
}}