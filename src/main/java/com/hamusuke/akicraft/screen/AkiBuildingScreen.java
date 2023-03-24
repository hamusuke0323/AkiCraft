package com.hamusuke.akicraft.screen;

import com.github.markozajc.akiwrapper.core.entities.Server;
import com.hamusuke.akicraft.AkiCraft;
import com.hamusuke.akicraft.util.AvailableThemeSearcher;
import net.minecraft.client.gui.screen.LoadingDisplay;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AkiBuildingScreen extends Screen implements RelatedToAkiScreen {
    private static final Logger LOGGER = LogManager.getLogger();
    private static final Map<Server.Language, Set<Server.GuessType>> AVAILABLE_GUESS_TYPE_MAP = new HashMap<>();
    private static final int RESEARCH_INTERVAL = 300;
    private final AtomicBoolean locked = new AtomicBoolean();
    private final ErrorDisplay display = new ErrorDisplay(200);
    private Server.Language language;
    private Server.GuessType type;
    @Nullable
    private Screen parent;
    private int waitTick;
    private long tickCount;
    private ButtonWidget research;
    private ButtonWidget theme;
    private ButtonWidget play;

    public AkiBuildingScreen() {
        super(NarratorManager.EMPTY);
    }

    @Override
    public void tick() {
        super.tick();

        this.display.tick();

        this.tickCount += 150L;

        if (this.waitTick > 0) {
            this.waitTick--;
        }

        if (this.language != null && this.waitTick <= 0 && !this.research.active) {
            this.research.active = true;
        } else if ((this.language == null || this.waitTick > 0) && this.research.active) {
            this.research.active = false;
        }

        this.theme.active = this.language != null;
        this.play.active = this.language != null && this.type != null;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(LANG, button -> this.client.setScreen(new EnumSelectionScreen<>(e -> {
            if (e != null) {
                this.language = (Server.Language) e;
                if (!AVAILABLE_GUESS_TYPE_MAP.containsKey(this.language)) {
                    this.search();
                }
            }
            this.client.setScreen(this);
        }, Arrays.stream(Server.Language.values()).collect(Collectors.toUnmodifiableSet()), this.language))).dimensions(this.width / 4, this.height / 2 - 40, this.width / 2, 20).build());
        this.research = this.addDrawableChild(ButtonWidget.builder(RESEARCH, button -> this.search()).dimensions(this.width / 4, this.height / 2 - 20, this.width / 2, 20).build());
        this.research.active = this.language != null && this.waitTick <= 0;
        this.theme = this.addDrawableChild(ButtonWidget.builder(GUESS, button -> this.client.setScreen(new EnumSelectionScreen<>(e -> {
            if (e != null) {
                this.type = (Server.GuessType) e;
            }
            this.client.setScreen(this);
        }, AVAILABLE_GUESS_TYPE_MAP.computeIfAbsent(this.language == null ? this.language = Server.Language.ENGLISH : this.language, lang -> Set.of()), this.type))).dimensions(this.width / 4, this.height / 2, this.width / 2, 20).build());
        this.theme.active = this.language != null;
        this.play = this.addDrawableChild(ButtonWidget.builder(PLAY, button -> {
            if (this.language != null && this.type != null) {
                var screen = new AkiScreen(this.language, this.type).setParent(this.parent);
                AkiCraft.getInstance().setAkiScreen(screen);
                this.client.setScreen(screen);
            }
        }).dimensions(this.width / 4, this.height / 2 + 20, this.width / 2, 20).build());
        this.play.active = this.language != null && this.type != null;
        this.addDrawableChild(ButtonWidget.builder(BACK, p_93751_ -> this.close()).dimensions(this.width / 4, this.height - 20, this.width / 2, 20).build());

        if (this.checkLocked()) {
            this.lock();
        }
    }

    private void search() {
        this.type = null;
        this.research.active = false;
        this.play.active = false;
        this.theme.active = false;
        var alreadyLockedWidgets = this.lock();
        this.waitTick = RESEARCH_INTERVAL;
        AvailableThemeSearcher.searchAvailableThemeAsync(this.language, (guessTypes, throwable) -> {
            AVAILABLE_GUESS_TYPE_MAP.put(this.language, guessTypes);
            this.unlock(alreadyLockedWidgets);

            if (throwable != null) {
                LOGGER.warn("Error occurred while searching theme", throwable);
                this.display.sendError(throwable.getMessage());
            }
        });
    }

    private synchronized List<ClickableWidget> lock() {
        this.locked.set(true);
        var widgets = this.children().stream()
                .filter(element -> element instanceof ClickableWidget)
                .map(element -> (ClickableWidget) element)
                .filter(clickableWidget -> clickableWidget.getMessage() != BACK)
                .toList();
        var result = widgets.stream().filter(clickableWidget -> !clickableWidget.active).toList();

        widgets.forEach(clickableWidget -> clickableWidget.active = false);
        return result;
    }

    private synchronized void unlock(List<ClickableWidget> exclude) {
        this.locked.set(false);
        this.children().stream()
                .filter(element -> element instanceof ClickableWidget)
                .map(element -> (ClickableWidget) element)
                .filter(clickableWidget -> {
                    for (ClickableWidget widget : exclude) {
                        if (widget.getMessage() == clickableWidget.getMessage()) {
                            return false;
                        }
                    }

                    return true;
                })
                .forEach(clickableWidget -> clickableWidget.active = true);
    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        if (this.checkLocked()) {
            this.renderBackground(matrices);
            drawCenteredTextWithShadow(matrices, this.textRenderer, Text.translatable(AkiCraft.MOD_ID + ".building.searching", LoadingDisplay.get(this.tickCount)), this.width / 2, this.height / 2 - 5, 16777215);
            this.children().stream()
                    .filter(element -> element instanceof ClickableWidget clickableWidget && clickableWidget.getMessage() == BACK)
                    .forEach(element -> ((ClickableWidget) element).render(matrices, mouseX, mouseY, delta));
        } else {
            this.renderBackground(matrices);
            super.render(matrices, mouseX, mouseY, delta);
        }

        if (this.display.errorDisplayable()) {
            this.renderOrderedTooltip(matrices, this.textRenderer.wrapLines(Text.translatable(AkiCraft.MOD_ID + ".error", this.display.getError()), this.width / 2), mouseX, mouseY);
        }
    }

    private boolean checkLocked() {
        return this.locked.get();
    }

    public AkiBuildingScreen setParent(@Nullable Screen parent) {
        this.parent = parent;
        return this;
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
