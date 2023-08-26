package com.hamusuke.akicraft.screen;

import com.hamusuke.akicraft.AkiCraft;
import com.hamusuke.akicraft.download.FileDownload;
import com.hamusuke.akicraft.util.AkiEmotions;
import com.hamusuke.akicraft.util.ImageDataDeliverer;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.ConfirmScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Language;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.eu.zajc.akiwrapper.core.entities.Guess;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuessResultScreen extends UseTextureManagerScreen {
    private static final File DL_DIR = new File("akiimages");
    private static final Logger LOGGER = LogManager.getLogger();
    private final AkiScreen parent;
    private final Guess guess;
    private final ImageDataDeliverer deliverer;
    private final File out;
    private final AtomicBoolean dlStarted = new AtomicBoolean();
    private final ErrorDisplay display = new ErrorDisplay(200);
    private int x, y, w, h;
    @Nullable
    private FileDownload fileDownload;

    public GuessResultScreen(AkiScreen parent, Guess guess) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
        this.guess = guess;
        this.deliverer = new ImageDataDeliverer(this.guess.getImage()).prepareAsync(e -> {
            LOGGER.warn("Failed to load image", e);
        }, imageDataDeliverer -> {
        });
        this.out = new File(DL_DIR, getFileName(this.guess.getImage()));
        this.out.getParentFile().mkdirs();
    }

    private static String getFileName(@Nullable URL url) {
        if (url == null) {
            return "image_is_null";
        }

        var split = url.getFile().split("/");
        return split[split.length - 1];
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(YES, button -> this.showGameResult(true)).dimensions(0, this.height - 20, this.width / 3, 20).build());
        this.addDrawableChild(ButtonWidget.builder(NO, button -> this.no()).dimensions(this.width / 3, this.height - 20, this.width / 3, 20).build());
        this.addDrawableChild(ButtonWidget.builder(BACK, button -> this.close()).dimensions(this.width * 2 / 3, this.height - 20, this.width / 3, 20).build());
    }

    @Override
    public void tick() {
        super.tick();
        this.display.tick();
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
            this.x = this.width / 2 - d.width / 2;
            this.y = (this.height - 20) / 2 - d.height / 2;
            this.w = d.width;
            this.h = d.height;
            drawTexture(matrices, this.x, this.y, 0, 0, this.w, this.h, d.width, d.height);

            this.renderProgress(matrices);

            if (this.isOnImage(mouseX, mouseY) && !this.dlStarted.get() && !this.display.errorDisplayable()) {
                this.renderTooltip(matrices, DOWNLOAD_IMAGE, mouseX, mouseY);
            }
        }

        drawCenteredTextWithShadow(matrices, this.textRenderer, this.guess.getName(), this.width / 2, this.height - 55, 16777215);
        drawCenteredTextWithShadow(matrices, this.textRenderer, this.guess.getDescription(), this.width / 2, this.height - 45, 16777215);

        if (this.display.errorDisplayable()) {
            this.renderOrderedTooltip(matrices, this.textRenderer.wrapLines(Text.literal(this.display.getError()), this.width / 2), mouseX, mouseY);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private boolean isOnImage(int mouseX, int mouseY) {
        return this.w > 0 && this.h > 0 && this.deliverer.readyToRender() && this.x <= mouseX && this.x + this.w >= mouseX && this.y <= mouseY && this.y + this.h >= mouseY;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && !this.dlStarted.get() && this.isOnImage((int) mouseX, (int) mouseY)) {
            this.client.setScreen(new ConfirmScreen(this::startDownload, DOWNLOAD_IMAGE, DOWNLOAD_IMAGE_MSG));
            return true;
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    private synchronized void startDownload(boolean b) {
        this.client.setScreen(this);

        if (!b || this.dlStarted.get() || !this.deliverer.readyToRender()) {
            return;
        }

        this.dlStarted.set(true);
        this.fileDownload = new FileDownload(this.deliverer.getURL().toString());
        this.fileDownload.download(this.out, throwable -> {
            this.display.sendError(throwable);
            this.dlStarted.set(false);
        });
    }

    private void renderProgress(MatrixStack matrices) {
        if (this.fileDownload != null && this.dlStarted.get() && this.fileDownload.isStarted()) {
            double p = this.fileDownload.bytesWritten() / (double) this.fileDownload.totalBytes();
            int dec = (int) (this.w * p);
            fillGradient(matrices, this.x + dec, this.y, this.x + this.w, this.y + this.h, -1072689136, -804253680);
            drawCenteredTextWithShadow(matrices, this.textRenderer, "%.1f%%".formatted(p * 100.0D), this.x + (this.w / 2), this.y + (this.h / 2), 16777215);

            if (this.fileDownload.finished()) {
                this.onDLFinished();
            }
        }
    }

    private void onDLFinished() {
        this.dlStarted.set(false);
        this.display.sendError(Language.getInstance().get(AkiCraft.MOD_ID + ".dl.fin"));
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
