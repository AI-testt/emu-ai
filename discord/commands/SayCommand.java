package ains.discord.commands;

import ains.world.World;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

import static net.dv8tion.jda.api.interactions.commands.OptionType.STRING;

public class SayCommand implements ICommand {

    @Override
    public SlashCommandData getData() {
        return Commands.slash("say", "Makes the bot say what you tell it to")
                .addOption(STRING, "content", "What the bot should say", true)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE));
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, World world) {
        String content = event.getOption("content", OptionMapping::getAsString);
        event.reply(content).queue();
    }
}
