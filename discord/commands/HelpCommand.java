package ains.discord.commands;

import ains.discord.DiscordRole;
import ains.world.World;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import java.util.ArrayList;
import java.util.List;

import static ains.discord.commands.CommandManager.commandManager;

public class HelpCommand implements ICommand {

    @Override
    public SlashCommandData getData() {
        return Commands.slash("help", "Shows available commands.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, World world) {
        DiscordRole role = DiscordRole.of(event.getMember());

        List<String> lines = new ArrayList<>();
        for (ICommand command : commandManager().getCommands()) {
            boolean staffOnly = command.isStaffOnly();
            if (staffOnly && role == DiscordRole.PLAYER) continue;

            SlashCommandData data = command.getData();
            lines.add("/" + data.getName() + " - " + data.getDescription()
                    + (staffOnly ? " *(staff only)*" : ""));
        }

        event.reply("**Available commands:**\n" + String.join("\n", lines)).queue();
    }
}
