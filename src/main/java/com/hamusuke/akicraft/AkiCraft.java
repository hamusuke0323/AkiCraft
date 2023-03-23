package com.hamusuke.akicraft;

import com.hamusuke.akicraft.command.OpenAkiClientCommand;
import com.hamusuke.akicraft.screen.AkiBuildingScreen;
import com.hamusuke.akicraft.screen.AkiScreen;
import com.hamusuke.akicraft.screen.RelatedToAkiScreen;
import com.hamusuke.akicraft.texture.TextureManager;
import com.hamusuke.akicraft.util.AkiEmotions;
import com.hamusuke.akicraft.util.ImageDataDeliverer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class AkiCraft implements ClientModInitializer {
    public static final String MOD_ID = "akicraft";
    private static final MinecraftClient mc = MinecraftClient.getInstance();
    private static AkiCraft INSTANCE;
    private static final ExecutorService AKI_THREAD = Executors.newCachedThreadPool(r -> new Thread(r, "Aki Thread"));
    @Nullable
    private AkiScreen akiScreen;
    @Nullable
    private Screen resultScreen;
    public final TextureManager textureManager = new TextureManager();
    private final KeyBinding keyMapping = new KeyBinding(MOD_ID + ".playAkinator", GLFW.GLFW_KEY_B, "key.categories.misc");

    public AkiCraft() {
        INSTANCE = this;
        AkiEmotions.registerEmotions();
    }

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(this.keyMapping);
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> OpenAkiClientCommand.register(dispatcher));
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (this.keyMapping.wasPressed()) {
                this.openScreen();
            }
        });
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            ImageDataDeliverer.shutdown();
            AKI_THREAD.shutdown();
            this.textureManager.close();
        });
    }

    public synchronized void openScreen() {
        if (mc.currentScreen instanceof RelatedToAkiScreen) {
            return;
        }

        if (this.resultScreen != null) {
            mc.setScreen(this.resultScreen);
        } else if (this.akiScreen == null) {
            mc.setScreen(new AkiBuildingScreen(mc.currentScreen));
        } else {
            mc.setScreen(this.akiScreen.setParent(mc.currentScreen));
        }
    }

    public void setAkiScreen(@Nullable AkiScreen screen) {
        this.akiScreen = screen;
    }

    public void setResultScreen(@Nullable Screen resultScreen) {
        this.resultScreen = resultScreen;
    }

    public static AkiCraft getInstance() {
        return INSTANCE;
    }

    public static ExecutorService getAkiThread() {
        return AKI_THREAD;
    }
}
