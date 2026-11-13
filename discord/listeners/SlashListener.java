package ains.discord.listeners;

import ains.discord.commands.CommandManager;
import ains.discord.commands.ICommand;
import ains.world.World;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SlashListener extends ListenerAdapter {

    private static final Logger log = LoggerFactory.getLogger(SlashListener.class);

    private final CommandManager commandManager;
    private final World world;
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public SlashListener(CommandManager commandManager, World world) {
        this.commandManager = commandManager;
        this.world = world;
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        ICommand command = commandManager.find(event.getName());
        if (command == null) {
            return;
        }

        executor.submit(() -> {
            try {
                command.execute(event, world);
            } catch (Exception ex) {
                log.error("Error executing Discord command '/{}'.", event.getName(), ex);
                if (!event.isAcknowledged()) {
                    event.reply("Terjadi error saat menjalankan command.").setEphemeral(true).queue();
                }
            }
        });
    }

}
