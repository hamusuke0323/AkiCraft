package com.hamusuke.akicraft.screen;

import com.hamusuke.akicraft.AkiCraft;
import com.hamusuke.akicraft.util.AkiEmotions;
import com.hamusuke.akicraft.util.ImageDataDeliverer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.zajc.akiwrapper.core.entities.Guess;

import java.awt.*;
import java.util.concurrent.CompletableFuture;

public class GuessResultScreen extends UseTextureManagerScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private final AkiScreen parent;
    private final Guess guess;
    private final ImageDataDeliverer deliverer;

    public GuessResultScreen(AkiScreen parent, Guess guess) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
        this.guess = guess;
        this.deliverer = new ImageDataDeliverer(this.guess.getImage()).prepareAsync(e -> {
            LOGGER.warn("Failed to load image", e);
        }, imageDataDeliverer -> {
        });
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(YES, button -> this.showGameResult(true)).dimensions(0, this.height - 20, this.width / 3, 20).build());
        this.addDrawableChild(ButtonWidget.builder(NO, button -> this.no()).dimensions(this.width / 3, this.height - 20, this.width / 3, 20).build());
        this.addDrawableChild(ButtonWidget.builder(BACK, button -> this.close()).dimensions(this.width * 2 / 3, this.height - 20, this.width / 3, 20).build());
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        AkiEmotions.CONFIDENT.renderEmotion(this.textureManager, matrices, this.width, this.height, 0, this.height / 8);

        drawCenteredTextWithShadow(matrices, this.textRenderer, I_THINK_OF, this.width / 2, (this.height - 20) / 10, 16777215);

        if (this.deliverer.readyToRender()) {
            var texture = this.textureManager.bindTexture(this.deliverer.deliver());
            RenderSystem.setShaderTexture(0, texture.getGlId());
            var d = wrapImageSize(this.deliverer.getDim(), new Dimension(this.width / 2, this.height / 2));
            drawTexture(matrices, this.width / 2 - d.width / 2, (this.height - 20) / 2 - d.height / 2, 0, 0, d.width, d.height, d.width, d.height);
        }

        drawCenteredTextWithShadow(matrices, this.textRenderer, this.guess.getName(), this.width / 2, this.height - 55, 16777215);
        drawCenteredTextWithShadow(matrices, this.textRenderer, this.guess.getDescription(), this.width / 2, this.height - 45, 16777215);

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void showGameResult(boolean yes) {
        var screen = new AkiGameResultScreen(this.parent, yes ? this.guess : null);
        AkiCraft.getInstance().setResultScreen(screen);
        this.client.setScreen(screen);
    }

    private void no() {
        this.client.setScreen(new ConfirmScreen(t -> {
            if (t) {
                this.parent.curProbabilityToStopGuessing += 0.1D;
                if (this.parent.curProbabilityToStopGuessing >= 1.0D) {
                    this.showGameResult(false);
                    return;
                }

                this.client.setScreen(this.parent);
                this.rejectGuess();
            } else {
                this.showGameResult(false);
            }
        }, Text.empty(), CONTINUE));
    }

    private void rejectGuess() {
        CompletableFuture.supplyAsync(this.parent.getAkiwrapper()::rejectLastGuess, AkiCraft.getAkiThread())
                .whenComplete((question1, throwable) -> {
                    if (question1 == null) {
                        this.showGameResult(false);
                        return;
                    }

                    this.parent.onLastQRejected(question1, throwable);
                });
    }
}
