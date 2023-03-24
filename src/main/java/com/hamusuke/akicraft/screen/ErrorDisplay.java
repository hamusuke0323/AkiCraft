package com.hamusuke.akicraft.screen;

import javax.annotation.Nullable;

public class ErrorDisplay {
    private final int displayTick;
    @Nullable
    private String error;
    private int tickCount;

    public ErrorDisplay(int displayTick) {
        this.displayTick = displayTick;
    }

    public void tick() {
        if (this.tickCount > 0) {
            this.tickCount--;
            if (this.tickCount <= 0) {
                this.clearError();
            }
        }
    }

    public void sendError(String error) {
        this.error = error;
        this.tickCount = this.displayTick;
    }

    public void clearError() {
        this.error = null;
    }

    public boolean errorDisplayable() {
        return this.error != null;
    }

    @Nullable
    public String getError() {
        return this.error;
    }
}
