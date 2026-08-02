package com.ghostipedia.nebulaeae2.config;

import com.mojang.logging.LogUtils;
import net.neoforged.fml.loading.FMLPaths;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;

public final class AE2ChannelConfigPolicy {

    public static final String CHANNEL_COMMENT =
            "Channels are drastically changed for Cosmic Frontiers : This setting does nothing as device capacity is based on CWU";

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String CHANNEL_VALUE = "X2";
    private static final String ALLOWED_VALUES_COMMENT = "#Allowed Values: INFINITE, DEFAULT, X2, X3, X4";

    private AE2ChannelConfigPolicy() {}

    public static void enforce() {
        enforce(FMLPaths.CONFIGDIR.get().resolve("ae2-common.toml"));
    }

    static void enforce(Path configFile) {
        if (!Files.isRegularFile(configFile)) {
            return;
        }

        try {
            String original = Files.readString(configFile, StandardCharsets.UTF_8);
            String lineSeparator = original.contains("\r\n") ? "\r\n" : "\n";
            List<String> lines = new ArrayList<>(Arrays.asList(original.split("\\R", -1)));

            if (!applyPolicy(lines)) {
                return;
            }

            String updated = String.join(lineSeparator, lines);
            if (updated.equals(original)) {
                return;
            }

            writeAtomically(configFile, updated);
            LOGGER.info("Locked AE2's channel configuration to Nebulae's CWU-based channel policy");
        } catch (IOException | RuntimeException exception) {
            LOGGER.warn("Unable to enforce Nebulae's AE2 channel configuration policy at {}", configFile, exception);
        }
    }

    private static boolean applyPolicy(List<String> lines) {
        int generalStart = findSection(lines, "general");
        if (generalStart < 0) {
            return false;
        }

        int generalEnd = findNextSection(lines, generalStart + 1);
        int channelLine = findSetting(lines, generalStart + 1, generalEnd, "channels");
        String indentation = "\t";

        if (channelLine >= 0) {
            String existingLine = lines.get(channelLine);
            indentation = existingLine.substring(0, existingLine.length() - existingLine.stripLeading().length());

            int commentStart = channelLine;
            while (commentStart > generalStart + 1 && lines.get(commentStart - 1).stripLeading().startsWith("#")) {
                commentStart--;
            }

            lines.subList(commentStart, channelLine).clear();
            channelLine = commentStart;
            lines.set(channelLine, indentation + "channels = \"" + CHANNEL_VALUE + "\"");
        } else {
            channelLine = generalEnd;
            while (channelLine > generalStart + 1 && lines.get(channelLine - 1).isBlank()) {
                channelLine--;
            }
            lines.add(channelLine, indentation + "channels = \"" + CHANNEL_VALUE + "\"");
        }

        lines.add(channelLine, indentation + ALLOWED_VALUES_COMMENT);
        lines.add(channelLine, indentation + "#" + CHANNEL_COMMENT);
        return true;
    }

    private static int findSection(List<String> lines, String name) {
        String section = "[" + name + "]";
        for (int index = 0; index < lines.size(); index++) {
            if (lines.get(index).trim().equals(section)) {
                return index;
            }
        }
        return -1;
    }

    private static int findNextSection(List<String> lines, int start) {
        for (int index = start; index < lines.size(); index++) {
            String line = lines.get(index).trim();
            if (line.startsWith("[") && line.endsWith("]")) {
                return index;
            }
        }
        return lines.size();
    }

    private static int findSetting(List<String> lines, int start, int end, String name) {
        for (int index = start; index < end; index++) {
            String line = lines.get(index).stripLeading();
            if (line.startsWith(name) && line.substring(name.length()).stripLeading().startsWith("=")) {
                return index;
            }
        }
        return -1;
    }

    private static void writeAtomically(Path target, String content) throws IOException {
        Path parent = target.toAbsolutePath().getParent();
        if (parent == null) {
            throw new IOException("AE2 config has no parent directory: " + target);
        }

        Path temporary = Files.createTempFile(parent, "ae2-common-nebulae-", ".tmp");
        try {
            Files.writeString(temporary, content, StandardCharsets.UTF_8);
            try {
                Files.move(
                        temporary,
                        target,
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporary);
        }
    }
}
