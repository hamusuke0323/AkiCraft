package com.hamusuke.akicraft.util;

import com.github.markozajc.akiwrapper.core.entities.Server;
import com.github.markozajc.akiwrapper.core.exceptions.ServerNotFoundException;
import com.github.markozajc.akiwrapper.core.utils.Servers;
import com.github.markozajc.akiwrapper.core.utils.UnirestUtils;
import com.hamusuke.akicraft.AkiCraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class AvailableThemeSearcher {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void searchAvailableThemeAsync(Server.Language language, BiConsumer<Set<Server.GuessType>, ? super Throwable> action) {
        CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Start searching available theme in language: {}", language);
            long time = System.currentTimeMillis();
            Set<Server.GuessType> guessTypes = new HashSet<>();
            for (var g : Server.GuessType.values()) {
                try {
                    Servers.findServers(UnirestUtils.getInstance(), language, g);
                    guessTypes.add(g);
                } catch (ServerNotFoundException ignored) {
                }
            }

            LOGGER.info("Done! {} milli secs elapsed", System.currentTimeMillis() - time);
            UnirestUtils.shutdownInstance();
            return Set.copyOf(guessTypes);
        }, AkiCraft.getAkiThread()).whenComplete(action);
    }
}
