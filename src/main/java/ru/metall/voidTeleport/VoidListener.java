package ru.metall.voidTeleport;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class VoidListener implements Listener {

    private final VoidTeleport plugin;

    public VoidListener(VoidTeleport plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        World world = player.getWorld();
        String path = "worlds." + world.getName();

        FileConfiguration config = plugin.getConfig();
        if (!config.contains(path)) return;

        double limitHeight = config.getDouble(path + ".teleportHeight");
        if (player.getLocation().getY() <= limitHeight) {
            if (!player.hasPermission("voidteleport.player")) return;

            Location spawnLoc = new Location(world,
                    config.getDouble(path + ".spawnLocation.x"),
                    config.getDouble(path + ".spawnLocation.y"),
                    config.getDouble(path + ".spawnLocation.z"));

            float pitch = (float) config.getDouble(path + ".spawnLocation.pitch");
            float yaw = (float) config.getDouble(path + ".spawnLocation.yaw");
            spawnLoc.setPitch(pitch != -1.0f ? pitch : player.getLocation().getPitch());
            spawnLoc.setYaw(yaw != -1.0f ? yaw : player.getLocation().getYaw());

            player.setFallDistance(0);
            player.teleport(spawnLoc);

            // Trigger structural actions uniformly
            EffectManager.playSound(player, path, config);
            EffectManager.playParticles(player, world, spawnLoc, path, config, plugin, true);

            String customWorldMsg = config.getString(path + ".message", "NONE");
            if (!customWorldMsg.equalsIgnoreCase("NONE") && !customWorldMsg.isEmpty()) {
                String out = ChatColor.translateAlternateColorCodes('&', "&r" + customWorldMsg);
                if (config.getBoolean(path + ".messagePrefix", true) && plugin.getMessagesConfig().getBoolean("use-prefix", true)) {
                    out = ChatColor.translateAlternateColorCodes('&', "&r" + plugin.getMessagesConfig().getString("prefix", "")) + out;
                }
                player.sendMessage(out);
            }
        }
    }

    public static void playEffects(Player player, World world, String path, FileConfiguration config, VoidTeleport plugin) {
        if (config.getBoolean("preview-sounds", true)) {
            EffectManager.playSound(player, path, config);
        }
        if (config.getBoolean("preview-particles", true)) {
            EffectManager.playParticles(player, world, player.getLocation(), path, config, plugin, false);
        }
    }
}