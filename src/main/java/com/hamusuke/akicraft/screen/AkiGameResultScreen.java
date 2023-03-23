package com.hamusuke.akicraft.screen;

import com.github.markozajc.akiwrapper.core.entities.Guess;
import com.hamusuke.akicraft.AkiCraft;
import com.hamusuke.akicraft.util.AkiEmotions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import javax.annotation.Nullable;
import java.awt.*;

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
        this.addDrawableChild(new ButtonWidget(0, this.height - 20, this.width / 3, 20, REPLAY, button -> this.restart()));
        this.addDrawableChild(new ButtonWidget(this.width / 3, this.height - 20, this.width / 3, 20, EXIT, p_93751_ -> this.exit()));
        this.addDrawableChild(new ButtonWidget(this.width * 2 / 3, this.height - 20, this.width / 3, 20, BACK, button -> this.close()));
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        if (this.emotion.isRenderable()) {
            RenderSystem.setShaderTexture(0, this.textureManager.bindTexture(this.emotion.getImg()).getGlId());
            Dimension d0 = wrapImageSizeToMin(AkiEmotions.SIZE, new Dimension(this.width * 2 / 3, this.height * 2 / 3));
            drawTexture(matrices, 0, this.height / 8, 0, 0, d0.width, d0.height, d0.width, d0.height);
        }

        if (this.guess != null) {
            drawCenteredText(matrices, this.textRenderer, WIN, this.width / 2, this.height / 2 - 20, 16777215);
            drawCenteredText(matrices, this.textRenderer, this.guess.getName(), this.width / 2, this.height / 2 - 7, 16777215);
            drawCenteredText(matrices, this.textRenderer, this.guess.getDescription(), this.width / 2, this.height / 2 + 7, 16777215);
        } else {
            drawCenteredText(matrices, this.textRenderer, LOSE, this.width / 2, this.height / 2 - 5, 16777215);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void restart() {
        AkiCraft.getInstance().setResultScreen(null);
        this.client.setScreen(this.parent);
        this.parent.reset();
    }

    private void exit() {
        AkiCraft.getInstance().setResultScreen(null);
        this.parent.exit();
    }
}
