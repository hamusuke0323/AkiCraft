package com.hamusuke.akicraft.screen;

import com.hamusuke.akicraft.AkiCraft;
import com.hamusuke.akicraft.texture.TextureManager;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import java.awt.*;

public abstract class UseTextureManagerScreen extends Screen implements RelatedToAkiScreen {
    protected final TextureManager textureManager;

    protected UseTextureManagerScreen(Text title) {
        super(title);
        this.textureManager = AkiCraft.getInstance().textureManager;
    }

    protected static Dimension wrapImageSizeToMin(Dimension imageSize, Dimension boundary) {
        double ratio = Math.min(boundary.getWidth() / imageSize.getWidth(), boundary.getHeight() / imageSize.getHeight());
        return new Dimension((int) (imageSize.width * ratio), (int) (imageSize.height * ratio));
    }
}
