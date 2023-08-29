package com.hamusuke.akicraft.screen;

import net.minecraft.text.Text;

import javax.annotation.Nullable;

public class MessageDisplay {
    private final int displayTick;
    @Nullable
    private Text message;
    private int tickCount;

    public MessageDisplay(int displayTick) {
        this.displayTick = displayTick;
    }

    public void tick() {
        if (this.tickCount > 0) {
            this.tickCount--;
            if (this.tickCount <= 0) {
                this.clearMessage();
            }
        }
    }

    public void sendMessage(String message) {
        this.sendMessage(Text.of(message));
    }

    public void sendMessage(Text message) {
        this.message = message;
        this.tickCount = this.displayTick;
    }

    public void sendMessage(Throwable error) {
        this.sendMessage(Text.of(error.getLocalizedMessage()));
    }

    public void clearMessage() {
        this.message = null;
    }

    public boolean messageDisplayable() {
        return this.message != null;
    }

    @Nullable
    public Text getMessage() {
        return this.message;
    }
}
