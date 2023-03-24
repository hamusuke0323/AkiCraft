package com.hamusuke.akicraft.screen;

import com.github.markozajc.akiwrapper.Akiwrapper;
import com.github.markozajc.akiwrapper.AkiwrapperBuilder;
import com.github.markozajc.akiwrapper.core.entities.Question;
import com.github.markozajc.akiwrapper.core.entities.Server;
import com.github.markozajc.akiwrapper.core.exceptions.ServerNotFoundException;
import com.github.markozajc.akiwrapper.core.utils.UnirestUtils;
import com.hamusuke.akicraft.AkiCraft;
import com.hamusuke.akicraft.util.AkiEmotions;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.tooltip.Tooltip;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.math.ColorHelper;
import net.minecraft.util.math.MathHelper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

public class AkiScreen extends UseTextureManagerScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    @Nullable
    private Screen parent;
    private final Server.Language language;
    private final Server.GuessType guessType;
    private Akiwrapper akiwrapper;
    @Nullable
    private Question question;
    private AkiEmotions.AkiEmotion curEmotion = AkiEmotions.DEFI;
    private double curProgress;
    public double curProbabilityToStopGuessing = 0.85D;
    private final AtomicBoolean locked = new AtomicBoolean();
    private final ErrorDisplay display = new ErrorDisplay(200);
    private ButtonWidget correctButton;
    private long tickCount;

    public AkiScreen(Server.Language language, Server.GuessType guessType) {
        super(NarratorManager.EMPTY);
        this.language = language;
        this.guessType = guessType;
    }

    @Override
    public void tick() {
        super.tick();
        this.tickCount += 150L;
        this.display.tick();
    }

    public AkiScreen setParent(@Nullable Screen parent) {
        this.parent = parent;
        return this;
    }

    @Override
    protected void init() {
        this.correctButton = this.addDrawableChild(ButtonWidget.builder(PREVIOUS_QUESTION, button -> this.undo()).dimensions(0, this.height - 80, 20, 20).tooltip(Tooltip.of(PREVIOUS_QUESTION_TOOLTIP)).build());
        this.addDrawableChild(ButtonWidget.builder(PROBABLY, p_93751_ -> this.answer(Akiwrapper.Answer.PROBABLY)).dimensions(0, this.height - 60, this.width / 2, 20).build());
        this.addDrawableChild(ButtonWidget.builder(PROBABLY_NOT, p_93751_ -> this.answer(Akiwrapper.Answer.PROBABLY_NOT)).dimensions(this.width / 2, this.height - 60, this.width / 2, 20).build());
        this.addDrawableChild(ButtonWidget.builder(YES, p_93751_ -> this.answer(Akiwrapper.Answer.YES)).dimensions(0, this.height - 40, this.width / 3, 20).build());
        this.addDrawableChild(ButtonWidget.builder(DONT_KNOW, p_93751_ -> this.answer(Akiwrapper.Answer.DONT_KNOW)).dimensions(this.width / 3, this.height - 40, this.width / 3, 20).build());
        this.addDrawableChild(ButtonWidget.builder(NO, p_93751_ -> this.answer(Akiwrapper.Answer.NO)).dimensions(this.width * 2 / 3, this.height - 40, this.width / 3, 20).build());
        this.addDrawableChild(ButtonWidget.builder(EXIT, p_93751_ -> this.exit()).dimensions(0, this.height - 20, this.width / 2, 20).build());
        this.addDrawableChild(ButtonWidget.builder(BACK, p_93751_ -> this.close()).dimensions(this.width / 2, this.height - 20, this.width / 2, 20).build());

        if (this.checkLocked()) {
            this.lock();
        }

        this.toggleCorrectButton$active();

        if (this.akiwrapper == null) {
            if (this.checkLocked()) {
                return;
            }

            this.lock();
            CompletableFuture.supplyAsync(() -> {
                try {
                    return new AkiwrapperBuilder().setLanguage(this.language).setGuessType(this.guessType).build();
                } catch (ServerNotFoundException e) {
                    throw new Error(e);
                }
            }, AkiCraft.getAkiThread()).whenComplete((akiwrapper, throwable) -> {
                this.akiwrapper = akiwrapper;
                this.question = this.akiwrapper.getQuestion();
                this.curEmotion = AkiEmotions.DEFI;
                this.unlock();
                this.onQuestionChanged();

                if (throwable != null) {
                    LOGGER.warn("Error occurred while initializing akiwrapper", throwable);
                    this.display.sendError(throwable.getMessage());
                    this.exit();
                }
            });
        }
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        this.renderBackground(matrices);

        this.curEmotion.renderEmotion(this.textureManager, matrices, this.width, this.height, 0, 0);

        if (this.question != null) {
            drawCenteredTextWithShadow(matrices, this.textRenderer, Text.translatable(AkiCraft.MOD_ID + ".qnum", this.question.getStep() + 1), this.width / 2, (this.height - 60) / 2 - 20, 16777215);
            drawCenteredTextWithShadow(matrices, this.textRenderer, this.question.getQuestion(), this.width / 2, (this.height - 60) / 2, 16777215);
        } else {
            drawCenteredTextWithShadow(matrices, this.textRenderer, Text.translatable(AkiCraft.MOD_ID + ".loading", LoadingDisplay.get(this.tickCount)), this.width / 2, (this.height - 60) / 2, 16777215);
        }

        this.renderProgressBar(matrices);

        if (this.display.errorDisplayable()) {
            this.renderOrderedTooltip(matrices, this.textRenderer.wrapLines(Text.translatable(AkiCraft.MOD_ID + ".error", this.display.getError()), this.width / 2), mouseX, mouseY);
        }

        super.render(matrices, mouseX, mouseY, delta);
    }

    private void renderProgressBar(MatrixStack p_96562_) {
        drawCenteredTextWithShadow(p_96562_, this.textRenderer, String.format("%.2f%%", this.curProgress), (this.width + 20) / 2, this.height - 90, 16777215);
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
                    var q = this.akiwrapper.answer(answer);
                    this.akiwrapper.getGuesses();
                    return q;
                }, AkiCraft.getAkiThread())
                .whenComplete((question1, throwable) -> {
                    this.question = question1;
                    this.unlock();
                    this.onQuestionChanged();

                    if (throwable != null) {
                        this.reset();
                        this.display.sendError(throwable.getMessage());
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
        CompletableFuture.supplyAsync(this.akiwrapper::undoAnswer, AkiCraft.getAkiThread())
                .whenComplete((question1, throwable) -> {
                    this.question = question1;
                    this.unlock();
                    this.onQuestionChanged();

                    if (throwable != null) {
                        this.reset();
                        LOGGER.warn("Error occurred while undoing the answer", throwable);
                        this.display.sendError(throwable.getMessage());
                    }
                });
    }

    public synchronized void reset() {
        if (this.checkLocked()) {
            return;
        }

        this.lock();
        UnirestUtils.shutdownInstance();
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
            this.akiwrapper = akiwrapper;
            this.question = this.akiwrapper.getQuestion();
            this.curEmotion = AkiEmotions.DEFI;
            this.unlock();
            this.onQuestionChanged();

            if (throwable != null) {
                LOGGER.warn("Failed to restart Akiwrapper", throwable);
                this.exit();
            }
        });
    }

    private synchronized void stopGuessing() {
        if (this.checkLocked()) {
            return;
        }

        this.lock();
        CompletableFuture.supplyAsync(() -> this.akiwrapper.getGuessesAboveProbability(this.curProbabilityToStopGuessing), AkiCraft.getAkiThread())
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
            this.toggleCorrectButton$active();
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

    private void toggleCorrectButton$active() {
        if (this.question != null) {
            this.correctButton.active = this.question.getStep() > 0;
        }
    }

    private synchronized void lock() {
        this.locked.set(true);
        this.children().stream()
                .filter(element -> element instanceof ClickableWidget)
                .map(element -> (ClickableWidget) element)
                .filter(clickableWidget -> clickableWidget.getMessage() != EXIT && clickableWidget.getMessage() != BACK)
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
        UnirestUtils.shutdownInstance();
        this.close();
    }
}
