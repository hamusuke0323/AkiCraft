package com.hamusuke.akicraft.screen;

import com.hamusuke.akicraft.AkiCraft;
import com.hamusuke.akicraft.util.AkiEmotions;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import org.eu.zajc.akiwrapper.core.entities.Guess;

import javax.annotation.Nullable;

public class AkiGameResultScreen extends UseTextureManagerScreen {
    private final AkiScreen parent;
    @Nullable
    private final Guess guess;
    private final AkiEmotions.AkiEmotion emotion;

    public AkiGameResultScreen(AkiScreen parent, @Nullable Guess guess) {
        super(NarratorManager.EMPTY);

        this.parent = parent;
        this.guess = guess;
        this.emotion = this.guess != null ? AkiEmotions.WIN : AkiEmotions.LOSE;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(REPLAY, button -> this.restart()).dimensions(0, this.height - 20, this.width / 3, 20).build());
        this.addDrawableChild(ButtonWidget.builder(EXIT, p_93751_ -> this.exit()).dimensions(this.width / 3, this.height - 20, this.width / 3, 20).build());
        this.addDrawableChild(ButtonWidget.builder(BACK, button -> this.close()).dimensions(this.width * 2 / 3, this.height - 20, this.width / 3, 20).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        this.emotion.renderEmotion(this.textureManager, matrices, this.width, this.height, 0, this.height / 8);

        if (this.guess != null) {
            drawCenteredTextWithShadow(matrices, this.textRenderer, WIN, this.width / 2, this.height / 2 - 20, 16777215);
            drawCenteredTextWithShadow(matrices, this.textRenderer, this.guess.getName(), this.width / 2, this.height / 2 - 7, 16777215);
            drawCenteredTextWithShadow(matrices, this.textRenderer, this.guess.getDescription(), this.width / 2, this.height / 2 + 7, 16777215);
        } else {
            drawCenteredTextWithShadow(matrices, this.textRenderer, LOSE, this.width / 2, this.height / 2 - 5, 16777215);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void restart() {
        AkiCraft.getInstance().setResultScreen(null);
        this.client.setScreen(this.parent);
        this.parent.unlock();
        this.parent.reset();
    }

    private void exit() {
        AkiCraft.getInstance().setResultScreen(null);
        this.parent.exit();
    }
}
