package ru.metall.voidTeleport;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class VoidTabCompleter implements TabCompleter {

    private final VoidTeleport plugin;
    private final List<String> colorCodes = Arrays.asList("&0", "&1", "&2", "&3", "&4", "&5", "&6", "&7", "&8", "&9", "&a", "&b", "&c", "&d", "&e", "&f", "&k", "&l", "&m", "&n", "&o", "&r");

    public VoidTabCompleter(VoidTeleport plugin) {
        this.plugin = plugin;
    }

    private boolean shouldRegisterCommands(CommandSender sender) {
        FileConfiguration cfg = plugin.getConfig();
        if (!cfg.contains("groups")) return true;

        ConfigurationSection sec = cfg.getConfigurationSection("groups");
        assert sec != null;
        for (String key : sec.getKeys(false)) {
            String groupPerm = sec.getString(key + ".permission", "");
            if (!groupPerm.isEmpty() && sender.hasPermission(groupPerm)) {
                return sec.getBoolean(key + ".register-commands", true);
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NonNull CommandSender sender, @NonNull Command command, @NonNull String alias, String @NonNull [] args) {
        // Bug fix 6: Intercept execution to check group permission registries
        if (!shouldRegisterCommands(sender)) {
            return Collections.emptyList();
        }

        List<String> completions = new ArrayList<>();
        String currentArg = args[args.length - 1];

        String baseCmd = command.getName().toLowerCase();
        String subCommand = alias.toLowerCase(); // Use alias to correctly recognize standalone execution frames
        int flagStartIndex;

        if (baseCmd.equals("voidteleport") || baseCmd.equals("vt")) {
            if (args.length == 1) {
                // Bug fix 3 & 4: Restored complete sub-command collection listings
                return StringUtil.copyPartialMatches(currentArg, Arrays.asList("info", "spawn", "spawninfo", "setheight", "setspawn", "editspawn", "reload", "reloadspawn", "delspawn"), new ArrayList<>());
            }
            subCommand = args[0].toLowerCase();
            flagStartIndex = 1;
        } else {
            flagStartIndex = 0;
        }

        // Standardize base subCommand trackers for alternate labels
        if (subCommand.equals("spawnreload")) subCommand = "reloadspawn";

        if (subCommand.equals("spawn") || subCommand.equals("spawninfo") || subCommand.equals("reloadspawn") || subCommand.equals("delspawn")) {
            if (args.length - flagStartIndex == 1) {
                for (World w : Bukkit.getWorlds()) completions.add(w.getName());
            }
            return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());
        }

        if (subCommand.equals("reload")) {
            if (args.length - flagStartIndex == 1) {
                completions.addAll(Arrays.asList("all", "messages"));
                for (World w : Bukkit.getWorlds()) completions.add(w.getName());
            }
            return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());
        }

        if (subCommand.equals("setheight")) {
            List<String> activeArgs = Arrays.asList(args).subList(flagStartIndex, args.length - 1);
            boolean hasWorldFlag = activeArgs.contains("-world");
            boolean hasHeightFlag = activeArgs.contains("-height");
            String lastArg = args.length - flagStartIndex > 1 ? args[args.length - 2] : "";

            if (lastArg.equalsIgnoreCase("-world")) {
                for (World w : Bukkit.getWorlds()) completions.add(w.getName());
                return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());
            }
            if (lastArg.equalsIgnoreCase("-height")) {
                return Collections.emptyList();
            }

            if (!hasWorldFlag) completions.add("-world");
            if (!hasHeightFlag) completions.add("-height");
            return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());
        }

        if (subCommand.equals("setspawn") || subCommand.equals("editspawn")) {
            List<String> usedFlags = new ArrayList<>();
            String activeFlag = "";
            int wordsInFlag = 0;
            boolean insideQuotes = false;
            String particleSelected = "NONE";

            for (int i = flagStartIndex; i < args.length - 1; i++) {
                String arg = args[i];

                if (arg.startsWith("-") && !insideQuotes) {
                    activeFlag = arg.toLowerCase();
                    usedFlags.add(activeFlag);
                    wordsInFlag = 0;
                } else {
                    wordsInFlag++;
                    if (arg.startsWith("\"") && !arg.endsWith("\"")) insideQuotes = true;
                    else if (arg.endsWith("\"") && insideQuotes) insideQuotes = false;

                    if (activeFlag.equals("-particle") && wordsInFlag == 1) {
                        particleSelected = arg.toUpperCase();
                    }
                }

                if (!activeFlag.isEmpty() && !insideQuotes) {
                    if (activeFlag.equals("-world") && wordsInFlag >= 1) activeFlag = "";
                    else if (activeFlag.equals("-height") && wordsInFlag >= 1) activeFlag = "";
                    else if (activeFlag.equals("-position") && (wordsInFlag >= 3 || arg.equalsIgnoreCase("current"))) activeFlag = "";
                    else if (activeFlag.equals("-rotation") && (wordsInFlag >= 2 || arg.equalsIgnoreCase("current") || arg.equalsIgnoreCase("none"))) activeFlag = "";
                    else if (activeFlag.equals("-sound") && (wordsInFlag >= 1 || arg.equalsIgnoreCase("none"))) {
                        if (args[args.length - 1].startsWith("-")) activeFlag = "";
                    }
                    else if (activeFlag.equals("-particle")) {
                        if (args[args.length - 1].startsWith("-")) activeFlag = "";
                    }
                    else if (activeFlag.equals("-message") && (arg.equalsIgnoreCase("none") || arg.endsWith("\""))) {
                        activeFlag = "";
                    }
                }
            }

            List<String> structuralFlags = getStructuralFlags(usedFlags, subCommand);

            if (!activeFlag.isEmpty()) {
                switch (activeFlag) {
                    case "-world":
                        for (World w : Bukkit.getWorlds()) completions.add(w.getName());
                        return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());

                    case "-height":
                        return Collections.emptyList();

                    case "-position":
                        if (wordsInFlag == 0 && sender instanceof Player p) {
                            Location loc = p.getLocation();
                            completions.add("current");
                            completions.add(String.format("%d", (int)loc.getX()));
                            completions.add(String.format("%d %d", (int)loc.getX(), (int)loc.getY()));
                            completions.add(String.format("%d %d %d", (int)loc.getX(), (int)loc.getY(), (int)loc.getZ()));
                        }
                        return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());

                    case "-rotation":
                        if (wordsInFlag == 0 && sender instanceof Player p) {
                            Location loc = p.getLocation();
                            completions.add("current");
                            completions.add("none");
                            completions.add(String.format("%d", (int)loc.getPitch()));
                            completions.add(String.format("%d %d", (int)loc.getPitch(), (int)loc.getYaw()));
                        }
                        return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());

                    case "-sound":
                        if (wordsInFlag == 0) {
                            completions.add("NONE");
                            for (Sound s : Sound.values()) completions.add(s.name().toLowerCase());
                            return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());
                        }
                        return StringUtil.copyPartialMatches(currentArg, structuralFlags, new ArrayList<>());

                    case "-particle":
                        if (wordsInFlag == 0) {
                            completions.add("NONE");
                            // Custom implementation tracking both standard Enums and custom variant tokens
                            completions.add("dust_plume");
                            completions.add("dust_pillar");
                            for (Particle p : Particle.values()) completions.add(p.name().toLowerCase());
                            return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());
                        }
                        if (wordsInFlag == 1) {
                            completions.addAll(Arrays.asList("10", "20", "50"));
                            return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());
                        }
                        if (wordsInFlag == 2) {
                            completions.addAll(Arrays.asList("0.0", "0.1", "1.0"));
                            return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());
                        }
                        if (wordsInFlag == 3) {
                            // Bug Fix 1: Verify and return data layouts for both valid datatypes and custom variant strings
                            boolean isDustPlumeOrPillar = particleSelected.equals("DUST_PLUME") || particleSelected.equals("DUST_PILLAR");
                            Class<?> dataType = null;
                            try { dataType = Particle.valueOf(particleSelected).getDataType(); } catch (Exception ignored) {}

                            if (dataType == org.bukkit.block.data.BlockData.class || dataType == ItemStack.class) {
                                for (Material mat : Material.values()) {
                                    if (dataType == ItemStack.class || mat.isBlock()) {
                                        completions.add(mat.name().toLowerCase());
                                    }
                                }
                            } else if (dataType == Particle.DustOptions.class || isDustPlumeOrPillar) {
                                completions.add("255 0 0 1.0");
                            } else if (dataType == Particle.DustTransition.class) {
                                completions.add("0 0 255 255 0 0 1.0");
                            } else {
                                completions.add("NONE");
                            }
                            return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());
                        }
                        return StringUtil.copyPartialMatches(currentArg, structuralFlags, new ArrayList<>());

                    case "-message":
                        if (wordsInFlag == 0) {
                            completions.add("NONE");
                            completions.add("\"");
                            return StringUtil.copyPartialMatches(currentArg, completions, new ArrayList<>());
                        }

                        boolean parsingQuote = false;
                        for (int i = flagStartIndex; i < args.length; i++) {
                            if (args[i].equalsIgnoreCase("-message") && i + 1 < args.length) {
                                if (args[i + 1].startsWith("\"")) {
                                    parsingQuote = true;
                                    break;
                                }
                            }
                        }

                        if (parsingQuote) {
                            if (currentArg.endsWith("\"") && currentArg.length() > 1) {
                                return StringUtil.copyPartialMatches(currentArg, structuralFlags, new ArrayList<>());
                            }

                            List<String> colorInjections = new ArrayList<>();
                            String baseText = currentArg;

                            if (baseText.contains("&")) {
                                baseText = baseText.substring(0, baseText.lastIndexOf("&"));
                            }
                            for (String code : colorCodes) {
                                colorInjections.add(baseText + code);
                            }
                            return StringUtil.copyPartialMatches(currentArg, colorInjections, new ArrayList<>());
                        }
                        break;
                }
            }

            return StringUtil.copyPartialMatches(currentArg, structuralFlags, new ArrayList<>());
        }

        return Collections.emptyList();
    }

    private static @NonNull List<String> getStructuralFlags(List<String> usedFlags, String subCommand) {
        List<String> structuralFlags = new ArrayList<>();
        if (!usedFlags.contains("-world")) structuralFlags.add("-world");
        if (subCommand.equals("editspawn") && !usedFlags.contains("-height")) structuralFlags.add("-height");
        if (!usedFlags.contains("-position")) structuralFlags.add("-position");
        if (!usedFlags.contains("-rotation")) structuralFlags.add("-rotation");
        if (!usedFlags.contains("-sound")) structuralFlags.add("-sound");
        if (!usedFlags.contains("-particle")) structuralFlags.add("-particle");
        if (!usedFlags.contains("-message")) structuralFlags.add("-message");
        return structuralFlags;
    }
}