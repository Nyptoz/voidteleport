package ru.metall.voidTeleport;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class EffectManager {

    public static void playSound(Player player, String configPath, FileConfiguration config) {
        String baseKey = configPath + ".sound";
        String id;
        double volume, pitch;

        if (config.isConfigurationSection(baseKey)) {
            id = config.getString(baseKey + ".id", "NONE");
            volume = config.getDouble(baseKey + ".volume", 1.0);
            pitch = config.getDouble(baseKey + ".pitch", 1.0);
        } else {
            id = config.getString(baseKey, "NONE");
            volume = config.getDouble(configPath + ".soundVolume", 1.0);
            pitch = config.getDouble(configPath + ".soundPitch", 1.0);
        }

        if (id.equalsIgnoreCase("NONE")) return;

        try {
            String localizedId = id.toUpperCase().replace('.', '_');
            player.playSound(player.getLocation(), Sound.valueOf(localizedId), (float) volume, (float) pitch);
        } catch (Exception ignored) {}
    }

    public static void playParticles(Player player, World world, Location targetLoc, String configPath, FileConfiguration config, JavaPlugin plugin, boolean applyDelay) {
        String baseKey = configPath + ".particle";
        String id;
        int count;
        double speed;
        String dataStr;

        if (config.isConfigurationSection(baseKey)) {
            id = config.getString(baseKey + ".id", "NONE");
            count = config.getInt(baseKey + ".count", 10);
            speed = config.getDouble(baseKey + ".speed", 0.0);
            dataStr = config.getString(baseKey + ".data", "NONE");
        } else {
            id = config.getString(baseKey, "NONE");
            count = config.getInt(configPath + ".particleCount", 10);
            speed = config.getDouble(configPath + ".particleSpeed", 0.0);
            dataStr = config.getString(configPath + ".particleData", "NONE");
        }

        if (id.equalsIgnoreCase("NONE")) return;

        long ticks = applyDelay ? config.getLong("particle-delay", 1L) : 0L;
        final Location loc = targetLoc.clone().add(0, 1, 0);

        Runnable spawnTask = () -> {
            if (applyDelay && !player.isOnline()) return;
            try {
                String normalizedId = id.toUpperCase().replace('.', '_');
                Particle particle = Particle.valueOf(normalizedId);
                Object dataObject = parseParticleData(particle, normalizedId, dataStr);

                world.spawnParticle(particle, loc, count, 0.5, 0.5, 0.5, speed, dataObject, true);
            } catch (Exception ignored) {}
        };

        if (ticks > 0) {
            Bukkit.getScheduler().runTaskLater(plugin, spawnTask, ticks);
        } else {
            spawnTask.run();
        }
    }

    private static Object parseParticleData(Particle particle, String idStr, String dataStr) {
        if (dataStr == null || dataStr.equalsIgnoreCase("NONE") || dataStr.isEmpty()) return null;
        Class<?> dataType = particle.getDataType();

        if (dataType == org.bukkit.block.data.BlockData.class) {
            try {
                Material mat = Material.valueOf(dataStr.toUpperCase());
                if (mat.isBlock()) return Bukkit.createBlockData(mat);
            } catch (Exception e) {
                return Bukkit.createBlockData(Material.STONE);
            }
        }

        if (dataType == ItemStack.class) {
            try {
                Material mat = Material.valueOf(dataStr.toUpperCase());
                return new ItemStack(mat);
            } catch (Exception e) {
                return new ItemStack(Material.STONE);
            }
        }

        // Catch both native DustOptions data classes AND custom plume/pillar string allocations
        if (dataType == Particle.DustOptions.class || idStr.equals("DUST_PLUME") || idStr.equals("DUST_PILLAR")) {
            try {
                String[] split = dataStr.split(" ");
                int r = Integer.parseInt(split[0]);
                int g = Integer.parseInt(split[1]);
                int b = Integer.parseInt(split[2]);
                float size = split.length >= 4 ? Float.parseFloat(split[3]) : 1.0f;
                return new Particle.DustOptions(Color.fromRGB(r, g, b), size);
            } catch (Exception e) {
                return new Particle.DustOptions(Color.RED, 1.0f);
            }
        }

        if (dataType == Particle.DustTransition.class) {
            try {
                String[] split = dataStr.split(" ");
                int r1 = Integer.parseInt(split[0]);
                int g1 = Integer.parseInt(split[1]);
                int b1 = Integer.parseInt(split[2]);
                int r2 = Integer.parseInt(split[3]);
                int g2 = Integer.parseInt(split[4]);
                int b2 = Integer.parseInt(split[5]);
                float size = split.length >= 7 ? Float.parseFloat(split[6]) : 1.0f;
                return new Particle.DustTransition(Color.fromRGB(r1, g1, b1), Color.fromRGB(r2, g2, b2), size);
            } catch (Exception e) {
                return new Particle.DustTransition(Color.RED, Color.BLUE, 1.0f);
            }
        }

        return null;
    }
}