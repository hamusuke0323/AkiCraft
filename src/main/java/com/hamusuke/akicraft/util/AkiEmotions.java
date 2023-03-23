package com.hamusuke.akicraft.util;

import com.hamusuke.akicraft.texture.TextureManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.awt.*;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static com.hamusuke.akicraft.screen.UseTextureManagerScreen.wrapImageSize;
import static net.minecraft.client.gui.DrawableHelper.drawTexture;

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
        private static final File CACHE_DIR = new File("akicache");
        private final File cachedFile;
        private final ImageDataDeliverer cachedImage;
        private final ImageDataDeliverer deliverer;

        public AkiEmotion(String s) {
            try {
                var url = new URL(s);
                var split = url.getPath().split("/");
                this.cachedFile = new File(CACHE_DIR, split[split.length - 1]);
                this.cachedImage = new ImageDataDeliverer(this.cachedFile.toURI().toURL());
                this.deliverer = new ImageDataDeliverer(url, ImageDataDeliverer.Type.WEBP);
                this.cacheAndLoadImage(() -> this.cachedImage.prepareAsync(e -> {
                    LOGGER.warn("Error occurred while loading image", e);
                }, imageDataDeliverer -> {
                }));
            } catch (MalformedURLException e) {
                LOGGER.fatal("Error occurred while constructing URL instance! Minecraft will crash", e);
                throw new RuntimeException(e);
            }
        }

        private void cacheAndLoadImage(Runnable loadImageFunc) {
            if (this.cachedFile.exists()) {
                loadImageFunc.run();
                return;
            }

            if (!CACHE_DIR.exists() || !CACHE_DIR.isDirectory()) {
                CACHE_DIR.mkdir();
            }

            this.deliverer.prepareAsync(e -> {
                LOGGER.warn("Error occurred while retrieving image data", e);
            }, deliverer -> {
                try {
                    FileUtils.writeByteArrayToFile(this.cachedFile, deliverer.deliver().readAllBytes());
                } catch (Exception e) {
                    LOGGER.warn("Error occurred while saving file", e);
                }

                loadImageFunc.run();
            });
        }

        public void renderEmotion(TextureManager textureManager, MatrixStack matrices, int width, int height, int x, int y) {
            if (this.isRenderable()) {
                RenderSystem.setShaderTexture(0, textureManager.bindTexture(this.getImg()).getGlId());
                var d = wrapImageSize(AkiEmotions.SIZE, new Dimension(width, (int) (height * 0.9D)));
                drawTexture(matrices, x, (int) (y - height * 0.1D), 0, 0, d.width, d.height, d.width, d.height);
            }
        }

        public boolean isRenderable() {
            return this.cachedImage.readyToRender();
        }

        @Nullable
        public InputStream getImg() {
            return this.cachedImage.deliver();
        }
    }
}
