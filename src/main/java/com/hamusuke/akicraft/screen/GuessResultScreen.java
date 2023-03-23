package com.hamusuke.akicraft.screen;

import com.github.markozajc.akiwrapper.core.entities.Guess;
import com.hamusuke.akicraft.AkiCraft;
import com.hamusuke.akicraft.util.AkiEmotions;
import com.hamusuke.akicraft.util.ImageDataDeliverer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.awt.*;

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
        this.addDrawableChild(new ButtonWidget(0, this.height - 20, this.width / 3, 20, YES, button -> this.showGameResult(true)));
        this.addDrawableChild(new ButtonWidget(this.width / 3, this.height - 20, this.width / 3, 20, NO, button -> this.no()));
        this.addDrawableChild(new ButtonWidget(this.width * 2 / 3, this.height - 20, this.width / 3, 20, BACK, button -> this.close()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        if (AkiEmotions.CONFIDENT.isRenderable()) {
            RenderSystem.setShaderTexture(0, this.textureManager.bindTexture(AkiEmotions.CONFIDENT.getImg()).getGlId());
            Dimension d0 = wrapImageSizeToMin(AkiEmotions.SIZE, new Dimension(this.width * 2 / 3, this.height * 2 / 3));
            drawTexture(matrices, 0, this.height / 8, 0, 0, d0.width, d0.height, d0.width, d0.height);
        }

        drawCenteredText(matrices, this.textRenderer, I_THINK_OF, this.width / 2, (this.height - 20) / 10, 16777215);

        if (this.deliverer.readyToRender()) {
            AbstractTexture texture = this.textureManager.bindTexture(this.deliverer.deliver());
            RenderSystem.setShaderTexture(0, texture.getGlId());
            Dimension d = wrapImageSizeToMin(this.deliverer.getDim(), new Dimension(this.width / 2, this.height / 2));
            drawTexture(matrices, this.width / 2 - d.width / 2, (this.height - 20) / 2 - d.height / 2, 0, 0, d.width, d.height, d.width, d.height);
        }

        drawCenteredText(matrices, this.textRenderer, this.guess.getName(), this.width / 2, this.height - 55, 16777215);
        drawCenteredText(matrices, this.textRenderer, this.guess.getDescription(), this.width / 2, this.height - 45, 16777215);

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
            } else {
                this.showGameResult(false);
            }
        }, LiteralText.EMPTY, CONTINUE));
    }
}
