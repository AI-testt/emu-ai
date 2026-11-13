package ains.discord.commands;

import ains.world.World;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PingCommand implements ICommand {

    @Override
    public SlashCommandData getData() {
        return Commands.slash("ping", "Check the bot's latency.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, World world) {
        long ping = event.getJDA().getGatewayPing();
        event.reply("Pong! Gateway ping: " + ping + "ms").queue();
    }
}
