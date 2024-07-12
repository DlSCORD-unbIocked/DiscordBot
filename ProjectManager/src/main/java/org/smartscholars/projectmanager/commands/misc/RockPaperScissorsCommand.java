package org.smartscholars.projectmanager.commands.misc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;

import java.util.Random;

@CommandInfo(
        name = "rps",
        description = "Play Rock-Paper-Scissors game"
)
public class RockPaperScissorsCommand implements ICommand {

    private static final String ROCK_EMOJI = "\u270A";
    private static final String PAPER_EMOJI = "\u270B";
    private static final String SCISSORS_EMOJI = "\u270C";
    private boolean choiceMade;

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        this.choiceMade = false;
        event.reply("Choose your weapon!")
            .addActionRow(
                Button.primary("rock", ROCK_EMOJI),
                Button.primary("paper", PAPER_EMOJI),
                Button.primary("scissors", SCISSORS_EMOJI)
            ).queue();
    }

    public void handleButtonInteraction(String userChoice, @NotNull ButtonInteractionEvent event) {
        if (this.choiceMade) {
            return;
        }
        this.choiceMade = true;

        String[] choices = {"rock", "paper", "scissors"};
        String botChoice = choices[new Random().nextInt(choices.length)];

        String result = determineWinner(userChoice, botChoice);

        event.getHook().editOriginal(String.format("You chose %s. Bot chose %s. %s", userChoice, botChoice, result)).setComponents().queue();
    }

    private String determineWinner(String userChoice, String botChoice) {
        if (userChoice.equals(botChoice)) {
            return "It's a draw!";
        }

        return switch (userChoice) {
            case "rock" -> (botChoice.equals("scissors")) ? "You win!" : "You lose! LLL";
            case "paper" -> (botChoice.equals("rock")) ? "You win!" : "You lose! LLL";
            case "scissors" -> (botChoice.equals("paper")) ? "You win!" : "You lose! LLL";
            default -> "Invalid choice. Try better next time!";
        };
    }
}