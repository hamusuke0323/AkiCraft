package com.hamusuke.akicraft.util;

import com.hamusuke.akicraft.AkiCraft;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.zajc.akiwrapper.core.entities.Server;
import org.eu.zajc.akiwrapper.core.utils.Servers;
import org.eu.zajc.akiwrapper.core.utils.UnirestUtils;

import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class AvailableThemeSearcher {
    private static final Logger LOGGER = LogManager.getLogger();

    public static void searchAvailableThemeAsync(Server.Language language, BiConsumer<Set<Server.GuessType>, ? super Throwable> action) {
        CompletableFuture.supplyAsync(() -> {
            LOGGER.info("Start searching available theme in language: {}", language);
            long time = System.currentTimeMillis();

            var result = Servers.getServers(UnirestUtils.getInstance())
                    .filter(server1 -> server1.getLanguage() == language)
                    .map(Server::getGuessType)
                    .collect(Collectors.toUnmodifiableSet());

            LOGGER.info("Done! {} milli secs elapsed", System.currentTimeMillis() - time);
            UnirestUtils.shutdownInstance();
            return result;
        }, AkiCraft.getAkiThread()).whenComplete(action);
    }
}
