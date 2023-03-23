package com.hamusuke.akicraft.screen;

import com.hamusuke.akicraft.AkiCraft;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

public interface RelatedToAkiScreen {
    Text LANG = new TranslatableText(AkiCraft.MOD_ID + ".building.lang");
    Text GUESS = new TranslatableText(AkiCraft.MOD_ID + ".building.guess");
    Text PLAY = new TranslatableText(AkiCraft.MOD_ID + ".play");
    Text PREVIOUS_QUESTION = new LiteralText("←");
    Text PREVIOUS_QUESTION_TOOLTIP = new TranslatableText(AkiCraft.MOD_ID + ".previous.question");
    Text PROBABLY = new TranslatableText(AkiCraft.MOD_ID + ".probably");
    Text PROBABLY_NOT = new TranslatableText(AkiCraft.MOD_ID + ".probably.not");
    Text YES = new TranslatableText(AkiCraft.MOD_ID + ".yes");
    Text DONT_KNOW = new TranslatableText(AkiCraft.MOD_ID + ".dont.know");
    Text NO = new TranslatableText(AkiCraft.MOD_ID + ".no");
    Text EXIT = new TranslatableText(AkiCraft.MOD_ID + ".exit");
    Text LOADING = new TranslatableText(AkiCraft.MOD_ID + ".loading");
    Text I_THINK_OF = new TranslatableText(AkiCraft.MOD_ID + ".result");
    Text CONTINUE = new TranslatableText(AkiCraft.MOD_ID + ".continue");
    Text REPLAY = new TranslatableText(AkiCraft.MOD_ID + ".replay");
    Text WIN = new TranslatableText(AkiCraft.MOD_ID + ".win");
    Text LOSE = new TranslatableText(AkiCraft.MOD_ID + ".lose");
    Text BACK = new TranslatableText("menu.returnToGame");
}
