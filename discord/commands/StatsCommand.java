package ains.discord.commands;

import ains.network.manager.UserManager;
import ains.world.World;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import static ains.network.manager.UserManager.userManager;

public class StatsCommand implements ICommand {

    @Override
    public SlashCommandData getData() {
        return Commands.slash("stats", "Shows current server statistics.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, World world) {
        UserManager userManager = userManager();
        String message = "Online players: " + userManager.getUserCount()
                + "\nHighest online record: " + userManager.highestCount;
        event.reply(message).queue();
    }
}
