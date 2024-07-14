package org.smartscholars.projectmanager.eventlisteners;

import net.dv8tion.jda.api.events.GenericEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.selections.SelectMenuInteraction;
import org.jetbrains.annotations.NotNull;
import org.smartscholars.projectmanager.commands.vc.ListQueueCommand;

public class SelectionMenuListener extends ListenerAdapter implements IEvent{
    @Override
    public void execute(GenericEvent event) {
        onSelectMenuInteraction((SelectMenuInteraction) event);
    }
    @Override
    public void onSelectMenuInteraction(@NotNull SelectMenuInteraction event) {
        if (event.getComponentId().equals("remove-song")) {

            String selectedValue = (String) event.getValues().getFirst();

            boolean success = ListQueueCommand.removeSong(index, queue);

            if (success) {
                event.reply("Song removed successfully").setEphemeral(true).queue();
            } else {
                event.reply("Failed to remove the song").setEphemeral(true).queue();
            }
        }
    }
    private boolean removeSongFromQueue(String songIdentifier, long guildId) {
        return true;
    }
}
