package com.hamusuke.akicraft.texture;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.util.crash.CrashException;
import net.minecraft.util.crash.CrashReport;
import net.minecraft.util.crash.CrashReportSection;
import org.apache.commons.lang3.mutable.MutableObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

public class TextureManager implements AutoCloseable {
    private static final Logger LOGGER = LogManager.getLogger();
    private final Map<InputStream, AbstractTexture> textureMap = Maps.newHashMap();

    public AbstractTexture bindTexture(InputStream is) {
        if (!RenderSystem.isOnRenderThread()) {
            MutableObject<AbstractTexture> mutableObject = new MutableObject<>();
            RenderSystem.recordRenderCall(() -> mutableObject.setValue(this.bindTextureRaw(is)));
            return mutableObject.getValue();
        } else {
            return this.bindTextureRaw(is);
        }
    }

    private AbstractTexture bindTextureRaw(InputStream inputStream) {
        AbstractTexture texture = this.textureMap.get(inputStream);
        if (texture == null) {
            texture = new InputStreamTexture();
            this.registerTexture(inputStream, texture);
        }

        texture.bindTexture();
        return texture;
    }

    public void registerTexture(InputStream inputStream, AbstractTexture texture) {
        texture = this.loadTexture(inputStream, texture);
        AbstractTexture texture1 = this.textureMap.put(inputStream, texture);
        if (texture1 != texture) {
            if (texture1 != null && texture1 != MissingSprite.getMissingSpriteTexture()) {
                texture1.clearGlId();
            }
        }
    }

    private AbstractTexture loadTexture(InputStream inputStream, AbstractTexture texture) {
        try {
            ((InputStreamTexture) texture).load(inputStream);
            return texture;
        } catch (IOException var7) {
            LOGGER.warn("Failed to load InputStream texture", var7);
            return MissingSprite.getMissingSpriteTexture();
        } catch (Throwable var8) {
            CrashReport crashReport = CrashReport.create(var8, "Registering texture");
            CrashReportSection crashReportSection = crashReport.addElement("Resource location being registered");
            crashReportSection.add("Texture object class", () -> texture.getClass().getName());
            throw new CrashException(crashReport);
        }
    }

    private void closeTexture(InputStream inputStream, AbstractTexture texture) {
        if (texture != MissingSprite.getMissingSpriteTexture()) {
            try {
                inputStream.close();
                texture.close();
            } catch (Exception e) {
                LOGGER.warn("Failed to close InputStream texture", e);
            }
        }

        texture.clearGlId();
    }

    public void close() {
        this.textureMap.forEach(this::closeTexture);
    }
}
