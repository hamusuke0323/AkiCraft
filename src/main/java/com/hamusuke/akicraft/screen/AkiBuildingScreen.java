package com.hamusuke.akicraft.screen;

import com.github.markozajc.akiwrapper.core.entities.Server;
import com.hamusuke.akicraft.AkiCraft;
import com.hamusuke.akicraft.util.SupportedGuessTypeFinder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.screen.ScreenTexts;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.stream.Collectors;

public class AkiBuildingScreen extends Screen implements RelatedToAkiScreen {
    @Nullable
    private final Screen parent;
    private Server.Language language;
    private Server.GuessType type;

    public AkiBuildingScreen(@Nullable Screen parent) {
        super(NarratorManager.EMPTY);
        this.parent = parent;
    }

    @Override
    protected void init() {
        this.addDrawableChild(ButtonWidget.builder(LANG, button -> this.client.setScreen(new EnumSelectionScreen<>(e -> {
            if (e != null) {
                this.language = (Server.Language) e;
            }
            this.client.setScreen(this);
        }, Arrays.stream(Server.Language.values()).collect(Collectors.toUnmodifiableSet()), this.language))).dimensions(this.width / 4, this.height / 2 - 30, this.width / 2, 20).build());
        this.addDrawableChild(ButtonWidget.builder(GUESS, button -> this.client.setScreen(new EnumSelectionScreen<>(e -> {
            if (e != null) {
                this.type = (Server.GuessType) e;
            }
            this.client.setScreen(this);
        }, SupportedGuessTypeFinder.SUPPORT_MAP.get(this.language), this.type))).dimensions(this.width / 4, this.height / 2 - 10, this.width / 2, 20).build()).active = this.language != null;
        this.addDrawableChild(ButtonWidget.builder(PLAY, button -> {
            if (this.language != null && this.type != null) {
                var screen = new AkiScreen(this.language, this.type).setParent(this.parent);
                AkiCraft.getInstance().setAkiScreen(screen);
                this.client.setScreen(screen);
            }
        }).dimensions(this.width / 4, this.height / 2 + 10, this.width / 2, 20).build()).active = this.language != null && this.type != null;
        this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, p_93751_ -> this.close()).dimensions(this.width / 4, this.height - 20, this.width / 2, 20).build());
    }

    @Override
    public void render(MatrixStack p_96562_, int p_96563_, int p_96564_, float p_96565_) {
        this.renderBackground(p_96562_);
        super.render(p_96562_, p_96563_, p_96564_, p_96565_);
    }

    @Override
    public void close() {
        this.client.setScreen(this.parent);
    }
}
