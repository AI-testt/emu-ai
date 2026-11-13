package ains.discord.listeners;

import ains.world.World;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.Command;

import java.util.List;
import java.util.stream.Collectors;

public class AutoCompleteListener extends ListenerAdapter {

    private final World world;

    public AutoCompleteListener(World world) {
        this.world = world;
    }

    @Override
    public void onCommandAutoCompleteInteraction(CommandAutoCompleteInteractionEvent event) {
        if (!event.getName().equals("wipe") || !event.getFocusedOption().getName().equals("map")) {
            return;
        }

        String typed = event.getFocusedOption().getValue().toLowerCase();

        List<Command.Choice> choices = world.areas.keySet().stream()
                .filter(name -> name.contains(typed))
                .sorted()
                .limit(25)
                .map(name -> new Command.Choice(name, name))
                .collect(Collectors.toList());

        event.replyChoices(choices).queue();
    }
}
