package com.hamusuke.akicraft.util;

import com.github.markozajc.akiwrapper.core.entities.Server;
import com.github.markozajc.akiwrapper.core.exceptions.ServerNotFoundException;
import com.github.markozajc.akiwrapper.core.utils.Servers;
import com.github.markozajc.akiwrapper.core.utils.UnirestUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SupportedGuessTypeFinder {
    public static final Map<Server.Language, Set<Server.GuessType>> SUPPORT_MAP;
    private static final Logger LOGGER = LogManager.getLogger();

    static {
        Map<Server.Language, Set<Server.GuessType>> map = new HashMap<>();
        LOGGER.info("Start finding supported guess type for every server");
        long time = System.currentTimeMillis();
        for (var lang : Server.Language.values()) {
            Set<Server.GuessType> guessTypes = new HashSet<>();
            for (var g : Server.GuessType.values()) {
                try {
                    Servers.findServers(UnirestUtils.getInstance(), lang, g);
                    guessTypes.add(g);
                } catch (ServerNotFoundException ignored) {
                }
            }
            map.put(lang, guessTypes);
        }

        LOGGER.info("Done! {} milli secs elapsed", System.currentTimeMillis() - time);

        UnirestUtils.shutdownInstance();
        SUPPORT_MAP = Map.copyOf(map);
    }

    public static void find() {
    }
}
