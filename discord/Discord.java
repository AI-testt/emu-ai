package ains.discord;

import ains.discord.commands.CommandManager;
import ains.discord.listeners.AutoCompleteListener;
import ains.discord.listeners.SlashListener;
import ains.world.World;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.concurrent.CompletableFuture;

public class Discord {

    private static final Logger log = LoggerFactory.getLogger(Discord.class);

    public World world;
    public JDA jda;
    public CommandManager commandManager;

    public Discord(String token, World world) {
        this.world = world;

        commandManager = CommandManager.commandManager();

        jda = JDABuilder.createLight(token, Collections.emptyList())
                .addEventListeners(new SlashListener(commandManager, world), new AutoCompleteListener(world))
                .build();

        CompletableFuture.runAsync(() -> {
            try {
                jda.awaitReady();
                commandManager.updateCommands(jda);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                log.error("Interrupted while waiting for Discord to be ready.", ex);
            } catch (Exception ex) {
                log.error("Failed to initialize Discord commands.", ex);
            }
        });

        log.info("Discord Bot Initialized.");
    }
}
