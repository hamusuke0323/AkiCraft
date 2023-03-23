package com.hamusuke.akicraft.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class AkiEmotions {
    private static final Logger LOGGER = LogManager.getLogger();
    public static final Dimension SIZE = new Dimension(670, 1096);
    public static final AkiEmotion DEFI = new AkiEmotion("https://jp.akinator.com/bundles/elokencesite/images/akitudes_670x1096/defi.png?v95");
    public static final AkiEmotion INSPIRATION_LEGERE = new AkiEmotion("https://jp.akinator.com/bundles/elokencesite/images/akitudes_670x1096/inspiration_legere.png");
    public static final AkiEmotion SEREIN = new AkiEmotion("https://jp.akinator.com/bundles/elokencesite/images/akitudes_670x1096/serein.png");
    public static final AkiEmotion INSPIRATION_FORTE = new AkiEmotion("https://jp.akinator.com/bundles/elokencesite/images/akitudes_670x1096/inspiration_forte.png");
    public static final AkiEmotion DISCOURAGEMENT = new AkiEmotion("https://jp.akinator.com/bundles/elokencesite/images/akitudes_670x1096/vrai_decouragement.png");
    public static final AkiEmotion MOBILE = new AkiEmotion("https://jp.akinator.com/bundles/elokencesite/images/akitudes_670x1096/mobile.png");
    public static final AkiEmotion CONFIDENT = new AkiEmotion("https://jp.akinator.com/bundles/elokencesite/images/akitudes_670x1096/confiant.png?v95");
    public static final AkiEmotion WIN = new AkiEmotion("https://jp.akinator.com/bundles/elokencesite/images/akitudes_670x1096/triomphe.png?v95");
    public static final AkiEmotion LOSE = new AkiEmotion("https://jp.akinator.com/bundles/elokencesite/images/akitudes_670x1096/deception.png?v95");

    public static void registerEmotions() {
    }

    public static final class AkiEmotion {
        private final ImageDataDeliverer deliverer;

        public AkiEmotion(String url) {
            URL url1 = null;
            try {
                url1 = new URL(url);
            } catch (MalformedURLException ignored) {
            }

            this.deliverer = new ImageDataDeliverer(url1, ImageDataDeliverer.Type.WEBP).prepareAsync(e -> {
                LOGGER.warn("Error occurred while retrieving image data", e);
            }, imageDataDeliverer -> {
            });
        }

        public boolean isRenderable() {
            return this.deliverer.readyToRender();
        }

        @Nullable
        public InputStream getImg() {
            return this.deliverer.deliver();
        }
    }
}
