package com.hamusuke.akicraft.screen;

import com.github.markozajc.akiwrapper.core.entities.Server;
import com.hamusuke.akicraft.AkiCraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ScreenTexts;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.NarratorManager;
import net.minecraft.client.util.math.MatrixStack;

import javax.annotation.Nullable;

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
        this.addDrawableChild(new ButtonWidget(this.width / 4, this.height / 2 - 30, this.width / 2, 20, LANG, p_93751_ -> {
            this.client.setScreen(new EnumSelectionScreen<>(e -> {
                if (e != null) {
                    this.language = (Server.Language) e;
                }
                this.client.setScreen(this);
            }, Server.Language.class, this.language));
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 4, this.height / 2 - 10, this.width / 2, 20, GUESS, p_93751_ -> {
            this.client.setScreen(new EnumSelectionScreen<>(e -> {
                if (e != null) {
                    this.type = (Server.GuessType) e;
                }
                this.client.setScreen(this);
            }, Server.GuessType.class, this.type));
        }));
        this.addDrawableChild(new ButtonWidget(this.width / 4, this.height / 2 + 10, this.width / 2, 20, PLAY, p_93751_ -> {
            this.children().forEach(element -> {
                if (element instanceof ClickableWidget clickableWidget) {
                    clickableWidget.active = false;
                }
            });

            if (this.language != null && this.type != null) {
                var screen = new AkiScreen(this.language, this.type).setParent(this.parent);
                AkiCraft.getInstance().setAkiScreen(screen);
                this.client.setScreen(screen);
            }
        })).active = this.language != null && this.type != null;
        this.addDrawableChild(new ButtonWidget(this.width / 4, this.height - 20, this.width / 2, 20, ScreenTexts.BACK, p_93751_ -> this.close()));
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
