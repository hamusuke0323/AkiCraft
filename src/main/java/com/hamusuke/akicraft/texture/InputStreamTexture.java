package com.hamusuke.akicraft.texture;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.ResourceManager;
import org.apache.commons.io.IOUtils;

import javax.annotation.Nullable;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;

public class InputStreamTexture extends AbstractTexture {
    @Override
    public void load(ResourceManager manager) {
    }

    public void load(InputStream inputStream) throws IOException {
        InputStreamTexture.TextureData textureData = InputStreamTexture.TextureData.load(inputStream);
        textureData.checkException();
        NativeImage nativeImage = textureData.getImage();
        if (!RenderSystem.isOnRenderThreadOrInit()) {
            RenderSystem.recordRenderCall(() -> this.uploadTexture(nativeImage));
        } else {
            this.uploadTexture(nativeImage);
        }
    }

    private void uploadTexture(NativeImage nativeImage) {
        TextureUtil.prepareImage(this.getGlId(), 0, nativeImage.getWidth(), nativeImage.getHeight());
        nativeImage.upload(0, 0, 0, 0, 0, nativeImage.getWidth(), nativeImage.getHeight(), false, false, false, true);
    }

    public static class TextureData implements Closeable {
        @Nullable
        private final NativeImage image;
        @Nullable
        private final IOException exception;

        public TextureData(@Nullable IOException exception) {
            this.exception = exception;
            this.image = null;
        }

        public TextureData(@Nullable NativeImage image) {
            this.exception = null;
            this.image = image;
        }

        public static InputStreamTexture.TextureData load(InputStream inputStream) {
            try {
                return new TextureData(NativeImage.read(inputStream));
            } catch (IOException e) {
                return new TextureData(e);
            } finally {
                IOUtils.closeQuietly(inputStream);
            }
        }

        public NativeImage getImage() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            } else {
                return this.image;
            }
        }

        @Override
        public void close() {
            if (this.image != null) {
                this.image.close();
            }
        }

        public void checkException() throws IOException {
            if (this.exception != null) {
                throw this.exception;
            }
        }
    }
}
