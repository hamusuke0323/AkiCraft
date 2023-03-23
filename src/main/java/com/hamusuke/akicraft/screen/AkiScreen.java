package com.hamusuke.akicraft.screen;

import com.github.markozajc.akiwrapper.Akiwrapper;
import com.github.markozajc.akiwrapper.AkiwrapperBuilder;
import com.github.markozajc.akiwrapper.core.entities.Question;
import com.github.markozajc.akiwrapper.core.entities.Server;
import com.github.markozajc.akiwrapper.core.exceptions.ServerNotFoundException;
import com.hamusuke.akicraft.AkiCraft;
import com.hamusuke.akicraft.util.AkiEmotions;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AkiScreen extends UseTextureManagerScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    @Nullable
    private Screen parent;
    private final Server.Language language;
    private final Server.GuessType guessType;
    private Akiwrapper akinator;
    @Nullable
    private Question question;
    private AkiEmotions.AkiEmotion curEmotion = AkiEmotions.DEFI;
    private double curProgress;
    public double curProbabilityToStopGuessing = 0.85D;
    private final AtomicBoolean locked = new AtomicBoolean();

    public AkiScreen(Server.Language language, Server.GuessType guessType) {
        super(NarratorManager.EMPTY);
        this.language = language;
        this.guessType = guessType;
    }

    public AkiScreen setParent(@Nullable Screen parent) {
        this.parent = parent;
        return this;
    }

    @Override
    protected void init() {
        this.addDrawableChild(new ButtonWidget(0, this.height - 80, 20, 20, PREVIOUS_QUESTION, button -> this.undo(), (button, matrices, mouseX, mouseY) -> {
            if (button.active) {
                AkiScreen.this.renderOrderedTooltip(matrices, AkiScreen.this.client.textRenderer.wrapLines(PREVIOUS_QUESTION_TOOLTIP, Math.max(AkiScreen.this.width / 2 - 43, 170)), mouseX, mouseY);
            }
        }));
        this.addDrawableChild(new ButtonWidget(0, this.height - 60, this.width / 2, 20, PROBABLY, p_93751_ -> this.answer(Akiwrapper.Answer.PROBABLY)));
        this.addDrawableChild(new ButtonWidget(this.width / 2, this.height - 60, this.width / 2, 20, PROBABLY_NOT, p_93751_ -> this.answer(Akiwrapper.Answer.PROBABLY_NOT)));
        this.addDrawableChild(new ButtonWidget(0, this.height - 40, this.width / 3, 20, YES, p_93751_ -> this.answer(Akiwrapper.Answer.YES)));
        this.addDrawableChild(new ButtonWidget(this.width / 3, this.height - 40, this.width / 3, 20, DONT_KNOW, p_93751_ -> this.answer(Akiwrapper.Answer.DONT_KNOW)));
        this.addDrawableChild(new ButtonWidget(this.width * 2 / 3, this.height - 40, this.width / 3, 20, NO, p_93751_ -> this.answer(Akiwrapper.Answer.NO)));
        this.addDrawableChild(new ButtonWidget(0, this.height - 20, this.width / 2, 20, EXIT, p_93751_ -> this.exit()));
        this.addDrawableChild(new ButtonWidget(this.width / 2, this.height - 20, this.width / 2, 20, BACK, p_93751_ -> this.close()));

        if (this.akinator == null) {
            this.lock();
            CompletableFuture.supplyAsync(() -> {
                try {
                    return new AkiwrapperBuilder().setLanguage(this.language).setGuessType(this.guessType).build();
                } catch (ServerNotFoundException e) {
                    throw new Error(e);
                }
            }, AkiCraft.getAkiThread()).whenComplete((akiwrapper, throwable) -> {
                this.akinator = akiwrapper;
                this.question = this.akinator.getQuestion();
                this.onQuestionChanged();
                this.curEmotion = AkiEmotions.DEFI;
                this.unlock();

                if (throwable != null) {
                    LOGGER.warn("Error occurred while initializing akinator", throwable);
                    this.exit();
                }
            });
        }
    }

    @Override
    public void render(MatrixStack p_96562_, int p_96563_, int p_96564_, float p_96565_) {
        this.renderBackground(p_96562_);

        if (this.curEmotion.isRenderable()) {
            RenderSystem.setShaderTexture(0, this.textureManager.bindTexture(this.curEmotion.getImg()).getGlId());
            var d = wrapImageSizeToMin(AkiEmotions.SIZE, new Dimension(this.width * 2 / 3, this.height * 2 / 3));
            drawTexture(p_96562_, 0, 0, 0, 0, d.width, d.height, d.width, d.height);
        }

        if (this.question != null) {
            drawCenteredText(p_96562_, this.textRenderer, new TranslatableText(AkiCraft.MOD_ID + ".qnum", this.question.getStep() + 1), this.width / 2, (this.height - 60) / 2 - 20, 16777215);
            drawCenteredText(p_96562_, this.textRenderer, this.question.getQuestion(), this.width / 2, (this.height - 60) / 2, 16777215);
        } else {
            drawCenteredText(p_96562_, this.textRenderer, LOADING, this.width / 2, (this.height - 60) / 2, 16777215);
        }

        this.renderProgressBar(p_96562_);
        super.render(p_96562_, p_96563_, p_96564_, p_96565_);
    }

    private void renderProgressBar(MatrixStack p_96562_) {
        drawCenteredText(p_96562_, this.textRenderer, String.format("%.2f%%", this.curProgress), (this.width + 20) / 2, this.height - 90, 16777215);
        int minY = this.height - 80;
        int maxY = minY + 20;
        int i = MathHelper.ceil((float) (this.width - 20 - 2 - 2) * this.curProgress * 0.01);
        int color = ColorHelper.Argb.getArgb(255, 255, 255, 255);
        fill(p_96562_, 20 + 2, minY + 2, 20 + 2 + i, maxY - 2, color);
        fill(p_96562_, 20 + 1, minY, this.width - 1, minY + 1, color);
        fill(p_96562_, 20 + 1, maxY, this.width - 1, maxY - 1, color);
        fill(p_96562_, 20, minY, 20 + 1, maxY, color);
        fill(p_96562_, this.width, minY, this.width - 1, maxY, color);
    }

    private synchronized void answer(Akiwrapper.Answer answer) {
        if (this.checkLocked()) {
            return;
        }

        this.lock();
        this.question = null;
        CompletableFuture.supplyAsync(() -> {
                    var q = this.akinator.answer(answer);
                    this.akinator.getGuesses();
                    return q;
                }, AkiCraft.getAkiThread())
                .whenComplete((question1, throwable) -> {
                    this.question = question1;
                    this.onQuestionChanged();
                    this.unlock();

                    if (throwable != null) {
                        this.reset();
                        LOGGER.warn("Error occurred while answering question", throwable);
                    } else if (this.question == null) {
                        var screen = new AkiGameResultScreen(this, null);
                        AkiCraft.getInstance().setResultScreen(screen);
                        this.client.send(() -> this.client.setScreen(screen));
                        LOGGER.debug("Akinator has lost!");
                    }

                    if (this.question != null && this.question.getProgression() >= this.curProbabilityToStopGuessing) {
                        this.stopGuessing();
                    }
                });
    }

    private synchronized void undo() {
        if (this.checkLocked()) {
            return;
        }

        this.lock();
        this.question = null;
        CompletableFuture.supplyAsync(this.akinator::undoAnswer, AkiCraft.getAkiThread())
                .whenComplete((question1, throwable) -> {
                    this.question = question1;
                    this.onQuestionChanged();
                    this.unlock();

                    if (throwable != null || this.question == null) {
                        this.reset();
                        LOGGER.warn("Error occurred while undoing the answer", throwable);
                    }
                });
    }

    public synchronized void reset() {
        if (this.checkLocked()) {
            return;
        }

        this.lock();
        this.question = null;
        this.curProgress = 0.0D;
        this.curProbabilityToStopGuessing = 0.85D;
        this.curEmotion = AkiEmotions.DEFI;
        CompletableFuture.supplyAsync(() -> {
            try {
                return new AkiwrapperBuilder().setLanguage(this.language).setGuessType(this.guessType).build();
            } catch (ServerNotFoundException e) {
                throw new Error(e);
            }
        }, AkiCraft.getAkiThread()).whenComplete((akiwrapper, throwable) -> {
            this.akinator = akiwrapper;
            this.question = this.akinator.getQuestion();
            this.onQuestionChanged();
            this.curEmotion = AkiEmotions.DEFI;
            this.unlock();

            if (throwable != null) {
                LOGGER.warn("Failed to restart Akinator", throwable);
                this.exit();
            }
        });
    }

    private synchronized void stopGuessing() {
        if (this.checkLocked()) {
            return;
        }

        this.lock();
        CompletableFuture.supplyAsync(() -> this.akinator.getGuessesAboveProbability(this.curProbabilityToStopGuessing), AkiCraft.getAkiThread())
                .whenComplete((guesses, throwable) -> {
                    if (guesses.size() > 0) {
                        GuessResultScreen guessResultScreen = new GuessResultScreen(this, guesses.get(0));
                        AkiCraft.getInstance().setResultScreen(guessResultScreen);
                        if (this.client.currentScreen instanceof AkiScreen) {
                            this.client.send(() -> this.client.setScreen(guessResultScreen));
                        }
                    }

                    this.unlock();
                });
    }

    private void onQuestionChanged() {
        if (this.question != null) {
            this.curProgress = this.question.getProgression();
            double gain = this.question.getInfogain();
            if (gain >= 0.6D) {
                this.curEmotion = AkiEmotions.MOBILE;
            } else if (gain >= 0.58D) {
                this.curEmotion = AkiEmotions.INSPIRATION_FORTE;
            } else if (gain >= 0.5D) {
                this.curEmotion = AkiEmotions.SEREIN;
            } else if (gain > 0.0D) {
                this.curEmotion = AkiEmotions.INSPIRATION_LEGERE;
            } else {
                this.curEmotion = AkiEmotions.DISCOURAGEMENT;
            }
        }
    }

    private synchronized void lock() {
        this.locked.set(true);
        this.children().stream()
                .filter(element -> element instanceof ClickableWidget)
                .map(element -> (ClickableWidget) element)
                .filter(clickableWidget -> clickableWidget.getMessage() != BACK)
                .forEach(clickableWidget -> clickableWidget.active = false);
    }

    private synchronized void unlock() {
        this.locked.set(false);
        this.children().stream()
                .filter(element -> element instanceof ClickableWidget)
                .map(element -> (ClickableWidget) element)
                .forEach(clickableWidget -> clickableWidget.active = true);
    }

    private boolean checkLocked() {
        return this.locked.get();
    }

    @Override
    public void close() {
        this.client.send(() -> this.client.setScreen(this.parent));
    }

    void exit() {
        AkiCraft.getInstance().setAkiScreen(null);
        this.close();
    }
}
