package ains.discord.commands;

import ains.world.World;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.InteractionContextType;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ClearCommand implements ICommand {

    @Override
    public SlashCommandData getData() {
        OptionData option = new OptionData(OptionType.STRING, "option", "Which data to clear", true)
                .addChoice("all", "all")
                .addChoice("item", "item")
                .addChoice("title", "title")
                .addChoice("map", "map")
                .addChoice("quest", "quest")
                .addChoice("shop", "shop")
                .addChoice("enhshop", "enhshop")
                .addChoice("setting", "setting");

        return Commands.slash("clear", "Clear/reload server data from the database (staff only).")
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
                .addOptions(option);
    }

    @Override
    public boolean isStaffOnly() {
        return true;
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, World world) {
        event.deferReply().queue();

        String option = event.getOption("option", OptionMapping::getAsString);

        try {
            world.retrieveDatabaseObject(option);
            event.getHook().sendMessage("Cache `" + option + "` berhasil di-reload dari database.").queue();
        } catch (IllegalArgumentException ex) {
            event.getHook().sendMessage("Option `" + option + "` tidak valid: " + ex.getMessage()).queue();
        } catch (Exception ex) {
            event.getHook().sendMessage("Gagal me-reload `" + option + "`: " + ex.getMessage()).queue();
        }
    }
}
