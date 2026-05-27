package ru.metall.voidTeleport;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.jspecify.annotations.NonNull;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class VoidCommand implements CommandExecutor {

    private final VoidTeleport plugin;

    public VoidCommand(VoidTeleport plugin) {
        this.plugin = plugin;
    }

    private void sendMessage(CommandSender sender, String key, String world, String height, String flag) {
        FileConfiguration config = plugin.getMessagesConfig();
        String path = "messages." + key;

        if (!config.contains(path)) return;

        if (config.isList(path)) {
            List<String> lines = config.getStringList(path);
            for (String line : lines) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', line));
            }
            return;
        }

        String msg = config.getString(path, "");
        if (msg.isEmpty()) return;

        if (world != null) msg = msg.replace("%world%", world);
        if (height != null) msg = msg.replace("%height%", height);
        if (flag != null) msg = msg.replace("%flag%", flag);

        String out = ChatColor.translateAlternateColorCodes('&', "&r" + msg);
        if (config.getBoolean("use-prefix", true)) {
            out = ChatColor.translateAlternateColorCodes('&', "&r" + config.getString("prefix", "")) + out;
        }
        sender.sendMessage(out);
    }

    private void printCustomSpawnInfo(CommandSender sender, String wName) {
        FileConfiguration config = plugin.getMessagesConfig();
        FileConfiguration dataCfg = plugin.getConfig();
        String path = "messages.spawn-info";

        if (!config.contains(path) || !config.isList(path)) return;

        String worldPath = "worlds." + wName;
        String height = String.valueOf(dataCfg.getDouble(worldPath + ".teleportHeight", -20.0));
        String x = String.valueOf(dataCfg.getDouble(worldPath + ".spawnLocation.x", 0.5));
        String y = String.valueOf(dataCfg.getDouble(worldPath + ".spawnLocation.y", 100.0));
        String z = String.valueOf(dataCfg.getDouble(worldPath + ".spawnLocation.z", 0.5));

        String sound = dataCfg.getString(worldPath + ".sound.id", "NONE");
        String volume = String.valueOf((int)(dataCfg.getDouble(worldPath + ".sound.volume", 1.0) * 100));
        String pitch = String.valueOf((int)(dataCfg.getDouble(worldPath + ".sound.pitch", 1.0) * 100));

        String particle = dataCfg.getString(worldPath + ".particle.id", "NONE").toUpperCase();
        String pCount = String.valueOf(dataCfg.getInt(worldPath + ".particle.count", 10));
        String pSpeed = String.valueOf(dataCfg.getDouble(worldPath + ".particle.speed", 0.0));
        String pData = dataCfg.getString(worldPath + ".particle.data", "NONE");

        String formattedData = getFormattedData(pData, particle);

        for (String line : config.getStringList(path)) {
            String processedLine = line;
            if (processedLine.startsWith("<req:")) {
                int closingIndex = processedLine.indexOf(">");
                if (closingIndex != -1) {
                    String conditionTag = processedLine.substring(5, closingIndex);
                    processedLine = processedLine.substring(closingIndex + 1);

                    String evaluationTarget = "";
                    if (conditionTag.equalsIgnoreCase("%sound%")) evaluationTarget = sound;
                    else if (conditionTag.equalsIgnoreCase("%particle%")) evaluationTarget = particle;

                    if (evaluationTarget.equalsIgnoreCase("NONE") || evaluationTarget.isEmpty()) continue;
                }
            }

            processedLine = processedLine
                    .replace("%world%", wName)
                    .replace("%height%", height)
                    .replace("%x%", x)
                    .replace("%y%", y)
                    .replace("%z%", z)
                    .replace("%sound%", sound)
                    .replace("%volume%", volume)
                    .replace("%pitch%", pitch)
                    .replace("%particle%", particle)
                    .replace("%particleCount%", pCount)
                    .replace("%particleSpeed%", pSpeed)
                    .replace("%particleData%", formattedData)
                    .replace("%%", "%");

            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', processedLine));
        }
    }

    private static @NonNull String getFormattedData(String pData, String particle) {
        String formattedData = pData;
        if (!pData.equalsIgnoreCase("NONE") && !pData.isEmpty()) {
            String[] split = pData.split(" ");
            if (particle.equals("DUST") || particle.equals("DUST_PLUME") || particle.equals("DUST_PILLAR")) {
                if (split.length >= 3) {
                    String r = ChatColor.RED + split[0];
                    String g = ChatColor.GREEN + split[1];
                    String b = ChatColor.BLUE + split[2];
                    String size = split.length >= 4 ? ChatColor.GOLD + split[3] : ChatColor.GOLD + "1.0";
                    formattedData = String.format("%s %s %s %s", r, g, b, size);
                }
            } else if (particle.equals("DUST_COLOR_TRANSITION")) {
                if (split.length >= 6) {
                    String r1 = ChatColor.RED + split[0];
                    String g1 = ChatColor.GREEN + split[1];
                    String b1 = ChatColor.BLUE + split[2];
                    String r2 = ChatColor.DARK_RED + split[3];
                    String g2 = ChatColor.DARK_GREEN + split[4];
                    String b2 = ChatColor.DARK_BLUE + split[5];
                    String size = split.length >= 7 ? ChatColor.GOLD + split[6] : ChatColor.GOLD + "1.0";
                    formattedData = String.format("%s %s %s %s %s %s %s", r1, g1, b1, r2, g2, b2, size);
                }
            } else {
                formattedData = ChatColor.WHITE + pData;
            }
        }
        return formattedData;
    }

    private boolean hasPerms(CommandSender s, String node) {
        if (s.hasPermission(node)) return false;

        FileConfiguration cfg = plugin.getConfig();
        boolean showMsg = false;
        if (cfg.contains("groups")) {
            ConfigurationSection sec = cfg.getConfigurationSection("groups");
            assert sec != null;
            for (String key : sec.getKeys(false)) {
                String groupPerm = sec.getString(key + ".permission", "");
                if (!groupPerm.isEmpty() && s.hasPermission(groupPerm)) {
                    showMsg = sec.getBoolean(key + ".show-no-permissions", false);
                    break;
                }
            }
        } else {
            showMsg = cfg.getBoolean("show-no-permissions", false);
        }

        if (showMsg) {
            sendMessage(s, "no-permission", null, null, null);
        }
        return true;
    }

    private void runFailSound(CommandSender s) {
        if (!(s instanceof Player p)) return;
        FileConfiguration c = plugin.getMessagesConfig();
        if (!c.getBoolean("use-sounds", true)) return;
        try {
            p.playSound(p.getLocation(), Sound.valueOf(Objects.requireNonNull(c.getString("fail-sound"))), 1f, 1f);
        } catch (Exception ignored) {}
    }

    private void runSuccessSound(CommandSender s) {
        if (!(s instanceof Player p)) return;
        FileConfiguration c = plugin.getMessagesConfig();
        if (!c.getBoolean("use-sounds", true)) return;
        try {
            p.playSound(p.getLocation(), Sound.valueOf(Objects.requireNonNull(c.getString("success-sound"))), 1f, 1f);
        } catch (Exception ignored) {}
    }

    @Override
    public boolean onCommand(@NonNull CommandSender sender, Command command, @NonNull String label, String @NonNull [] args) {
        String action = command.getName().toLowerCase();
        String[] actionArgs = args;

        if (action.equals("voidteleport") || action.equals("vt")) {
            if (args.length == 0 || args[0].equalsIgnoreCase("info")) {
                if (hasPerms(sender, "voidteleport.player")) return true;
                FileConfiguration config = plugin.getMessagesConfig();
                if (config.contains("messages.plugin-info") && config.isList("messages.plugin-info")) {
                    for (String line : config.getStringList("messages.plugin-info")) {
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&',
                                line.replace("%version%", plugin.getDescription().getVersion())));
                    }
                }
                return true;
            }
            action = args[0].toLowerCase();
            actionArgs = Arrays.copyOfRange(args, 1, args.length);
        }

        if (action.equals("reloadspawn") || action.equals("spawnreload")) action = "reloadspawn";

        String permissionNode = action.equals("spawn") ? "voidteleport.player" : "voidteleport.admin";
        if (hasPerms(sender, permissionNode)) return true;

        FlagContext context = new FlagContext(actionArgs);

        if (!context.getSkippedInvalidFlags().isEmpty()) {
            for (String badFlag : context.getSkippedInvalidFlags()) {
                sendMessage(sender, "flag-incomplete", null, null, badFlag);
            }
            runFailSound(sender);
        }

        World targetWorld = null;
        if (context.hasFlag("-world")) {
            List<String> wArgs = context.getFlagArgs("-world");
            if (!wArgs.isEmpty()) targetWorld = Bukkit.getWorld(wArgs.getFirst());
        } else if (actionArgs.length > 0 && !actionArgs[0].startsWith("-") && Bukkit.getWorld(actionArgs[0]) != null) {
            targetWorld = Bukkit.getWorld(actionArgs[0]);
        } else if (sender instanceof Player) {
            targetWorld = ((Player) sender).getWorld();
        }

        if (context.hasFlag("-world") && targetWorld == null) {
            List<String> wArgs = context.getFlagArgs("-world");
            sendMessage(sender, "world-not-found", wArgs.isEmpty() ? "Unknown" : wArgs.getFirst(), null, null);
            runFailSound(sender);
            return true;
        }

        if (targetWorld == null && !Arrays.asList("reload", "reloadspawn", "delspawn").contains(action)) {
            sendMessage(sender, "only-players", null, null, null);
            return true;
        }

        String wName = targetWorld != null ? targetWorld.getName() : "Unknown";

        switch (action) {
            case "reload":
                String choice = (actionArgs.length > 0) ? actionArgs[0].toLowerCase() : "all";
                if (choice.equals("all")) {
                    plugin.reloadConfig();
                    plugin.reloadMessagesConfig();
                    sendMessage(sender, "all-reloaded", null, null, null);
                } else if (choice.equals("messages")) {
                    plugin.reloadMessagesConfig();
                    sendMessage(sender, "messages-reloaded", null, null, null);
                } else {
                    plugin.reloadConfig();
                    sendMessage(sender, "world-reloaded", choice, null, null);
                }
                if (sender instanceof Player) ((Player) sender).updateCommands();
                runSuccessSound(sender);
                return true;

            case "reloadspawn":
                plugin.reloadConfig();
                sendMessage(sender, "world-reloaded", wName, null, null);
                runSuccessSound(sender);
                return true;

            case "setheight":
                Double heightValue = null;
                if (context.hasFlag("-height")) {
                    List<String> hArgs = context.getFlagArgs("-height");
                    if (!hArgs.isEmpty()) {
                        try { heightValue = Double.parseDouble(hArgs.getFirst()); } catch (NumberFormatException ignored) {}
                    }
                }
                if (heightValue == null && !context.getRawArgsWithoutFlags().isEmpty()) {
                    try { heightValue = Double.parseDouble(context.getRawArgsWithoutFlags().getFirst()); } catch (NumberFormatException ignored) {}
                }
                if (heightValue == null) {
                    if (sender instanceof Player) {
                        heightValue = ((Player) sender).getLocation().getY();
                    } else {
                        sender.sendMessage(ChatColor.RED + "Usage: /setheight [<height>] [-world <world>] [-height <height>]");
                        return true;
                    }
                }

                try {
                    String path = "worlds." + wName;
                    if (plugin.getConfig().contains(path + ".spawnLocation.y")) {
                        double spawnY = plugin.getConfig().getDouble(path + ".spawnLocation.y");
                        if (heightValue >= spawnY) {
                            sendMessage(sender, "height-below-spawn", wName, String.valueOf(heightValue), null);
                            runFailSound(sender);
                            return true;
                        }
                    }
                    plugin.getConfig().set(path + ".teleportHeight", heightValue);
                    plugin.saveConfig();
                    sendMessage(sender, "height-set", wName, String.valueOf(heightValue), null);
                    runSuccessSound(sender);
                } catch (Exception e) {
                    sendMessage(sender, "parse-error", wName, null, null);
                    runFailSound(sender);
                }
                return true;

            case "delspawn":
                if (!plugin.getConfig().contains("worlds." + wName)) {
                    sendMessage(sender, "spawn-not-found", wName, null, null);
                    runFailSound(sender);
                    return true;
                }
                plugin.getConfig().set("worlds." + wName, null);
                plugin.saveConfig();
                sendMessage(sender, "spawn-deleted", wName, null, null);
                runSuccessSound(sender);
                return true;

            case "spawninfo":
                String profilePath = "worlds." + wName;
                if (!plugin.getConfig().contains(profilePath)) {
                    sendMessage(sender, "spawn-not-found", wName, null, null);
                    return true;
                }
                printCustomSpawnInfo(sender, wName);
                return true;

            case "spawn":
                String spawnPath = "worlds." + wName;
                if (!plugin.getConfig().contains(spawnPath + ".spawnLocation")) {
                    sendMessage(sender, "spawn-not-found", wName, null, null);
                    runFailSound(sender);
                    return true;
                }
                if (!(sender instanceof Player p)) {
                    sendMessage(sender, "only-players", null, null, null);
                    return true;
                }
                Location targetLoc = new Location(targetWorld,
                        plugin.getConfig().getDouble(spawnPath + ".spawnLocation.x"),
                        plugin.getConfig().getDouble(spawnPath + ".spawnLocation.y"),
                        plugin.getConfig().getDouble(spawnPath + ".spawnLocation.z"));

                float pValue = (float) plugin.getConfig().getDouble(spawnPath + ".spawnLocation.pitch");
                float yValue = (float) plugin.getConfig().getDouble(spawnPath + ".spawnLocation.yaw");
                targetLoc.setPitch(pValue != -1.0f ? pValue : p.getLocation().getPitch());
                targetLoc.setYaw(yValue != -1.0f ? yValue : p.getLocation().getYaw());

                p.setFallDistance(0);
                p.teleport(targetLoc);

                VoidListener.playEffects(p, targetWorld, spawnPath, plugin.getConfig(), plugin);

                String forceMsg = plugin.getMessagesConfig().getString("messages.force-teleport", "");
                if (!forceMsg.isEmpty()) {
                    String out = ChatColor.translateAlternateColorCodes('&', "&r" + forceMsg.replace("%world%", wName));
                    if (plugin.getMessagesConfig().getBoolean("use-prefix", true)) {
                        out = ChatColor.translateAlternateColorCodes('&', "&r" + plugin.getMessagesConfig().getString("prefix", "")) + out;
                    }
                    p.sendMessage(out);
                }
                return true;

            case "setspawn":
            case "editspawn":
                String path = "worlds." + wName;
                FileConfiguration cfg = plugin.getConfig();
                boolean isEdit = action.equals("editspawn") && cfg.contains(path);

                if (!cfg.contains(path)) {
                    cfg.set(path + ".teleportHeight", -20.0);
                    cfg.set(path + ".spawnLocation.x", 0.5);
                    cfg.set(path + ".spawnLocation.y", 100.0);
                    cfg.set(path + ".spawnLocation.z", 0.5);
                    cfg.set(path + ".spawnLocation.pitch", -1.0);
                    cfg.set(path + ".spawnLocation.yaw", -1.0);
                    cfg.set(path + ".message", "NONE");
                    cfg.set(path + ".messagePrefix", true);

                    cfg.set(path + ".sound.id", "NONE");
                    cfg.set(path + ".sound.volume", 1.0);
                    cfg.set(path + ".sound.pitch", 1.0);
                    cfg.set(path + ".particle.id", "NONE");
                    cfg.set(path + ".particle.count", 10);
                    cfg.set(path + ".particle.speed", 0.0);
                    cfg.set(path + ".particle.data", "NONE");
                }

                try {
                    if (context.hasFlag("-position")) {
                        List<String> posArgs = context.getFlagArgs("-position");
                        if (posArgs.get(0).equalsIgnoreCase("current") && sender instanceof Player) {
                            Location l = ((Player) sender).getLocation();
                            cfg.set(path + ".spawnLocation.x", Math.floor(l.getX()) + 0.5);
                            cfg.set(path + ".spawnLocation.y", l.getY());
                            cfg.set(path + ".spawnLocation.z", Math.floor(l.getZ()) + 0.5);
                        } else {
                            cfg.set(path + ".spawnLocation.x", Math.floor(Double.parseDouble(posArgs.get(0))) + 0.5);
                            if (posArgs.size() > 1) cfg.set(path + ".spawnLocation.y", Double.parseDouble(posArgs.get(1)));
                            if (posArgs.size() > 2) cfg.set(path + ".spawnLocation.z", Math.floor(Double.parseDouble(posArgs.get(2))) + 0.5);
                        }
                    }

                    if (context.hasFlag("-rotation")) {
                        List<String> rotArgs = context.getFlagArgs("-rotation");
                        if (rotArgs.get(0).equalsIgnoreCase("current") && sender instanceof Player) {
                            Location l = ((Player) sender).getLocation();
                            cfg.set(path + ".spawnLocation.pitch", (double) l.getPitch());
                            cfg.set(path + ".spawnLocation.yaw", (double) l.getYaw());
                        } else if (rotArgs.get(0).equalsIgnoreCase("none")) {
                            cfg.set(path + ".spawnLocation.pitch", -1.0);
                            cfg.set(path + ".spawnLocation.yaw", -1.0);
                        } else {
                            cfg.set(path + ".spawnLocation.pitch", Double.parseDouble(rotArgs.get(0)));
                            if (rotArgs.size() > 1) cfg.set(path + ".spawnLocation.yaw", Double.parseDouble(rotArgs.get(1)));
                        }
                    }

                    if (context.hasFlag("-height")) {
                        List<String> hArgs = context.getFlagArgs("-height");
                        if (!hArgs.isEmpty()) {
                            cfg.set(path + ".teleportHeight", Double.parseDouble(hArgs.getFirst()));
                        }
                    }

                    if (context.hasFlag("-message")) {
                        List<String> mArgs = context.getFlagArgs("-message");
                        cfg.set(path + ".message", mArgs.get(0));
                        boolean usePrefix = mArgs.size() < 2 || Boolean.parseBoolean(mArgs.get(1));
                        cfg.set(path + ".messagePrefix", usePrefix);
                    }

                    boolean soundModified = false;
                    if (context.hasFlag("-sound")) {
                        List<String> sArgs = context.getFlagArgs("-sound");
                        if (sArgs.get(0).equalsIgnoreCase("none")) {
                            cfg.set(path + ".sound.id", "NONE");
                        } else {
                            cfg.set(path + ".sound.id", sArgs.get(0).toUpperCase());
                            double v = sArgs.size() >= 2 ? Double.parseDouble(sArgs.get(1)) : 1.0;
                            double pr = sArgs.size() >= 3 ? Double.parseDouble(sArgs.get(2)) : 1.0;
                            cfg.set(path + ".sound.volume", v);
                            cfg.set(path + ".sound.pitch", pr);
                            soundModified = true;
                        }
                    }

                    boolean particleModified = false;
                    if (context.hasFlag("-particle")) {
                        List<String> pArgs = context.getFlagArgs("-particle");

                        if (pArgs.isEmpty() || pArgs.get(0).equalsIgnoreCase("none")) {
                            cfg.set(path + ".particle.id", "NONE");
                            cfg.set(path + ".particle.count", 10);
                            cfg.set(path + ".particle.speed", 0.0);
                            cfg.set(path + ".particle.data", "NONE");
                        } else {
                            cfg.set(path + ".particle.id", pArgs.getFirst().toUpperCase());
                            particleModified = true;

                            int count = 10;
                            double speed = 0.0;
                            StringBuilder dataBuilder = new StringBuilder();

                            // If the second argument is a raw Material/Data string instead of a count
                            if (pArgs.size() == 2 && !isInteger(pArgs.get(1))) {
                                cfg.set(path + ".particle.count", 10);
                                cfg.set(path + ".particle.speed", 0.0);
                                cfg.set(path + ".particle.data", pArgs.get(1));
                            } else {
                                // Standard progressive extraction format: [Name] [Count] [Speed] [Data...]
                                if (pArgs.size() > 1) {
                                    try { count = Integer.parseInt(pArgs.get(1)); } catch (NumberFormatException ignored) {}
                                }
                                if (pArgs.size() > 2) {
                                    try { speed = Double.parseDouble(pArgs.get(2)); } catch (NumberFormatException ignored) {}
                                }
                                if (pArgs.size() > 3) {
                                    for (int i = 3; i < pArgs.size(); i++) {
                                        dataBuilder.append(pArgs.get(i)).append(" ");
                                    }
                                    cfg.set(path + ".particle.data", dataBuilder.toString().trim());
                                } else {
                                    cfg.set(path + ".particle.data", "NONE");
                                }

                                cfg.set(path + ".particle.count", count);
                                cfg.set(path + ".particle.speed", speed);
                            }
                        }
                    }

                    if (actionArgs.length == 0 && sender instanceof Player) {
                        Location core = ((Player) sender).getLocation();
                        cfg.set(path + ".spawnLocation.x", Math.floor(core.getX()) + 0.5);
                        cfg.set(path + ".spawnLocation.y", core.getY());
                        cfg.set(path + ".spawnLocation.z", Math.floor(core.getZ()) + 0.5);
                        cfg.set(path + ".spawnLocation.pitch", (double) core.getPitch());
                        cfg.set(path + ".spawnLocation.yaw", (double) core.getYaw());
                    }

                    plugin.saveConfig();
                    sendMessage(sender, isEdit ? "spawn-edited" : "spawn-set", wName, null, null);

                    if (sender instanceof Player && (soundModified || particleModified)) {
                        VoidListener.playEffects((Player) sender, targetWorld, path, cfg, plugin);
                    }

                    if (!soundModified) {
                        runSuccessSound(sender);
                    }

                } catch (Exception e) {
                    sendMessage(sender, "parse-error", wName, null, null);
                    runFailSound(sender);
                }
                return true;
        }

        return false;
    }

    private boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}