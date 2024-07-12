package org.smartscholars.projectmanager.commands.misc;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.CommandOption;
import org.smartscholars.projectmanager.commands.ICommand;

import java.util.Objects;
import java.util.Random;

@CommandInfo(
        name = "rockpaperscissors",
        description = "Play Rock-Paper-Scissors game",
        options = {
            @CommandOption(
                name = "choice",
                description = "Your choice (rock, paper, or scissors)",
                type = OptionType.STRING,
                required = true
            )
        }
)
public class RockPaperScissorsCommand implements ICommand {

    @Override
    public void execute(SlashCommandInteractionEvent event) {
        String[] choices = {"rock", "paper", "scissors"};
        String userChoice = Objects.requireNonNull(event.getOption("choice")).getAsString().toLowerCase();
        String botChoice = choices[new Random().nextInt(choices.length)];

        String result = determineWinner(userChoice, botChoice);
        event.reply(String.format("You chose %s. Bot chose %s. %s", userChoice, botChoice, result)).queue();
    }

    private String determineWinner(String userChoice, String botChoice) {
        if (userChoice.equals(botChoice)) {
            return "It's a draw!";
        }

        switch (userChoice) {
            case "rock":
                return (botChoice.equals("scissors")) ? "You win!" : "You lose LLL!";
            case "paper":
                return (botChoice.equals("rock")) ? "You win!" : "You lose LLL!";
            case "scissors":
                return (botChoice.equals("paper")) ? "You win!" : "You lose LLL!";
            default:
                return "Invalid choice. Please choose rock, paper, or scissors. You lose LLLLL";
        }
    }
}