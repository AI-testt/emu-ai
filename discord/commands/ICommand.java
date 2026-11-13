package ains.discord.commands;

import ains.world.World;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public interface ICommand {

    SlashCommandData getData();

    void execute(SlashCommandInteractionEvent event, World world);

    default boolean isStaffOnly() {
        return false;
    }
}
