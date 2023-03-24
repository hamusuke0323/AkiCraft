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

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class AkiBuildingScreen extends Screen implements RelatedToAkiScreen {
    private static final Map<Server.Language, Set<Server.GuessType>> AVAILABLE_GUESS_TYPE_MAP = new HashMap<>();
    private static final int RESEARCH_INTERVAL = 300;
    private final AtomicBoolean locked = new AtomicBoolean();
    private Server.Language language;
    private Server.GuessType type;
    @Nullable
    private Screen parent;
    private int waitTick;
    private long tickCount;
    private ButtonWidget research;
    private ButtonWidget play;

    public AkiBuildingScreen() {
        super(NarratorManager.EMPTY);
    }

    @Override
    public void tick() {
        super.tick();

        this.tickCount += 150L;

        if (this.waitTick > 0) {
            this.waitTick--;
        }

        if (this.language != null && this.waitTick <= 0 && !this.research.active) {
            this.research.active = true;
        } else if ((this.language == null || this.waitTick > 0) && this.research.active) {
            this.research.active = false;
        }

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
        this.addDrawableChild(ButtonWidget.builder(GUESS, button -> this.client.setScreen(new EnumSelectionScreen<>(e -> {
            if (e != null) {
                this.type = (Server.GuessType) e;
            }
            this.client.setScreen(this);
        }, AVAILABLE_GUESS_TYPE_MAP.computeIfAbsent(this.language == null ? this.language = Server.Language.ENGLISH : this.language, lang -> Set.of()), this.type))).dimensions(this.width / 4, this.height / 2, this.width / 2, 20).build()).active = this.language != null;
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
        if (this.waitTick > 0) {
            return;
        }

        this.lock();
        this.type = null;
        this.play.active = false;
        this.waitTick = RESEARCH_INTERVAL;
        AvailableThemeSearcher.searchAvailableThemeAsync(this.language, (guessTypes, throwable) -> {
            AVAILABLE_GUESS_TYPE_MAP.put(this.language, guessTypes);
            this.unlock();
        });
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
