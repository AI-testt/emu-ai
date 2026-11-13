package ains.discord.commands;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CommandManager {

    private static final Logger log = LoggerFactory.getLogger(CommandManager.class);

    private static final CommandManager _instance = new CommandManager();

    private final Map<String, ICommand> commands = new ConcurrentHashMap<>();
    private JDA jda;

    public CommandManager() {
        scan();
    }

    public static CommandManager commandManager() {
        return _instance;
    }

    private void scan() {
        Reflections reflections = new Reflections("ains.discord.commands");
        Set<Class<? extends ICommand>> classes = reflections.getSubTypesOf(ICommand.class);

        for (Class<? extends ICommand> clazz : classes) {
            try {
                ICommand command = clazz.getDeclaredConstructor().newInstance();
                String name = command.getData().getName();
                if (commands.containsKey(name)) {
                    log.warn("Duplicate Discord command '{}' from {} ignored.", name, clazz.getName());
                } else {
                    commands.put(name, command);
                }
            } catch (Exception ex) {
                log.error("Unable to register Discord command {}: {}", clazz.getName(), ex);
            }
        }

        log.info("Loaded {} Discord commands.", commands.size());
    }

    public List<SlashCommandData> getCommandData() {
        List<SlashCommandData> data = new ArrayList<>();
        for (ICommand command : commands.values()) {
            data.add(command.getData());
        }
        return data;
    }

    public Collection<ICommand> getCommands() {
        return commands.values();
    }

    public ICommand find(String name) {
        return commands.get(name);
    }

    public void updateCommands(JDA jda) {
        this.jda = jda;
        jda.updateCommands().addCommands(getCommandData())
                .queue(
                        success -> log.info("Updated {} Discord slash commands.", commands.size()),
                        failure -> log.error("Unable to update Discord slash commands.", failure)
                );
    }

    public void reload() {
        Map<String, ICommand> old = new ConcurrentHashMap<>(commands);
        commands.clear();
        scan();

        for (String name : commands.keySet()) {
            if (!old.containsKey(name)) {
                log.info("Added Discord command '{}'.", name);
            }
        }
        for (String name : old.keySet()) {
            if (!commands.containsKey(name)) {
                log.info("Removed Discord command '{}'.", name);
            }
        }

        if (jda != null) {
            updateCommands(jda);
        }
    }
}
