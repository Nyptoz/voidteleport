package ru.metall.voidTeleport;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlagContext {

    private final List<String> rawArgsWithoutFlags = new ArrayList<>();
    private final Map<String, List<String>> flagsMap = new HashMap<>();
    private final List<String> skippedInvalidFlags = new ArrayList<>();

    public FlagContext(String[] args) {
        List<String> validIdentifiers = Arrays.asList("-world", "-position", "-rotation", "-sound", "-particle", "-message", "-height");
        int i = 0;

        while (i < args.length) {
            String token = args[i];

            if (token.startsWith("-") && validIdentifiers.contains(token.toLowerCase())) {
                String currentFlag = token.toLowerCase();
                List<String> collectedArgs = new ArrayList<>();
                i++;

                // Special capture rule loop block optimized for multi-word message structures
                if (currentFlag.equals("-message")) {
                    if (i < args.length && args[i].equalsIgnoreCase("NONE")) {
                        collectedArgs.add("NONE");
                        i++;
                    } else if (i < args.length && args[i].startsWith("\"")) {
                        StringBuilder sb = new StringBuilder();
                        while (i < args.length) {
                            sb.append(args[i]).append(" ");
                            if (args[i].endsWith("\"") && sb.length() > 2) {
                                i++;
                                break;
                            }
                            i++;
                        }
                        String parsedStr = sb.toString().trim();
                        // Strip wrapping quotes cleanly
                        if (parsedStr.startsWith("\"") && parsedStr.endsWith("\"")) {
                            parsedStr = parsedStr.substring(1, parsedStr.length() - 1);
                        }
                        collectedArgs.add(parsedStr);
                    } else {
                        // Flag structure layout error fallback: skip only this flag block
                        skippedInvalidFlags.add(currentFlag);
                        continue;
                    }

                    // Optional capture lookahead for true/false trailing parameter
                    if (i < args.length && (args[i].equalsIgnoreCase("true") || args[i].equalsIgnoreCase("false"))) {
                        collectedArgs.add(args[i].toLowerCase());
                        i++;
                    }

                    flagsMap.put(currentFlag, collectedArgs);
                    continue;
                }

                // General parameter collection loop for standard spacing properties
                while (i < args.length && !args[i].startsWith("-")) {
                    collectedArgs.add(args[i]);
                    i++;
                }

                // Apply safety validation rules per individual feature blocks
                if (validateFlagParameters(currentFlag, collectedArgs)) {
                    flagsMap.put(currentFlag, collectedArgs);
                } else {
                    skippedInvalidFlags.add(currentFlag);
                }

            } else {
                rawArgsWithoutFlags.add(token);
                i++;
            }
        }
    }

    private boolean validateFlagParameters(String flag, List<String> arguments) {
        switch (flag) {
            case "-world":
            case "-height", "-sound", "-particle":
                return !arguments.isEmpty();
            case "-position":
                if (arguments.size() == 1 && arguments.get(0).equalsIgnoreCase("current")) return true;
                if (arguments.size() >= 3) {
                    try {
                        Double.parseDouble(arguments.get(0));
                        Double.parseDouble(arguments.get(1));
                        Double.parseDouble(arguments.get(2));
                        return true;
                    } catch (NumberFormatException e) { return false; }
                }
                return false;
            case "-rotation":
                if (arguments.size() == 1 && (arguments.get(0).equalsIgnoreCase("current") || arguments.get(0).equalsIgnoreCase("none"))) return true;
                if (arguments.size() >= 2) {
                    try {
                        if (!arguments.get(0).equalsIgnoreCase("none")) Double.parseDouble(arguments.get(0));
                        if (!arguments.get(1).equalsIgnoreCase("none")) Double.parseDouble(arguments.get(1));
                        return true;
                    } catch (NumberFormatException e) { return false; }
                }
                return false;
            default:
                return false;
        }
    }

    public boolean hasFlag(String flag) { return flagsMap.containsKey(flag.toLowerCase()); }
    public List<String> getFlagArgs(String flag) { return flagsMap.getOrDefault(flag.toLowerCase(), new ArrayList<>()); }
    public List<String> getRawArgsWithoutFlags() { return rawArgsWithoutFlags; }
    public List<String> getSkippedInvalidFlags() { return skippedInvalidFlags; }
}