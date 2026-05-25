package ru.metall.voidTeleport;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.Objects;

public class VoidTeleport extends JavaPlugin {

    private File messagesFile;
    private FileConfiguration messagesConfig;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        createMessagesConfig();

        getServer().getPluginManager().registerEvents(new VoidListener(this), this);

        VoidCommand commandExecutor = new VoidCommand(this);
        VoidTabCompleter tabCompleter = new VoidTabCompleter(this);

        // Register every primary route explicitly so they don't return "unknown command"
        String[] commandNodes = {"voidteleport", "vt", "spawn", "setheight", "setspawn", "editspawn", "delspawn", "spawninfo", "relaodspawn", "setheight"};

        for (String cmd : commandNodes) {
            if (getCommand(cmd) != null) {
                Objects.requireNonNull(getCommand(cmd)).setExecutor(commandExecutor);
                Objects.requireNonNull(getCommand(cmd)).setTabCompleter(tabCompleter);
            }
        }

        getLogger().info("VoidTeleport v" + getDescription().getVersion() + " initialized cleanly.");
    }

    @Override
    public void onDisable() {
        getLogger().info("VoidTeleport disabled.");
    }

    public FileConfiguration getMessagesConfig() {
        if (this.messagesConfig == null) reloadMessagesConfig();
        return this.messagesConfig;
    }

    public void reloadMessagesConfig() {
        if (this.messagesFile == null) {
            this.messagesFile = new File(getDataFolder(), "messages.yml");
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration(this.messagesFile);
    }

    private void createMessagesConfig() {
        this.messagesFile = new File(getDataFolder(), "messages.yml");
        if (!this.messagesFile.exists()) {
            this.messagesFile.getParentFile().mkdirs();
            saveResource("messages.yml", false);
        }
        this.messagesConfig = YamlConfiguration.loadConfiguration(this.messagesFile);
    }
}