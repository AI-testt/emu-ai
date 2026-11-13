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

public class WipeCommand implements ICommand {

    @Override
    public SlashCommandData getData() {
        OptionData map = new OptionData(OptionType.STRING, "map", "Which map to wipe", true)
                .setAutoComplete(true);

        return Commands.slash("wipe", "Wipe a map (staff only).")
                .setContexts(InteractionContextType.GUILD)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_MANAGE))
                .addOptions(map);
    }

    @Override
    public void execute(SlashCommandInteractionEvent event, World world) {
        event.deferReply().queue();

        String map = event.getOption("map", OptionMapping::getAsString);

        if (map == null || !world.areas.containsKey(map)) {
            event.getHook().sendMessage("Map `" + map + "` tidak ditemukan.").queue();
            return;
        }

        world.sendServerMessage("Teleporting users in room: " + map);

        event.getHook().sendMessage("Pesan terkirim: Teleporting users in room: " + map).queue();
    }

    @Override
    public boolean isStaffOnly() {
        return true;
    }
}
