package com.hamusuke.akicraft.screen;

import com.hamusuke.akicraft.AkiCraft;
import net.minecraft.text.Text;

public interface RelatedToAkiScreen {
    Text LANG = Text.translatable(AkiCraft.MOD_ID + ".building.lang");
    Text GUESS = Text.translatable(AkiCraft.MOD_ID + ".building.guess");
    Text RESEARCH = Text.translatable(AkiCraft.MOD_ID + ".building.research");
    Text PROFANITY_FILTER = Text.translatable(AkiCraft.MOD_ID + ".building.profanity.filter");
    Text PLAY = Text.translatable(AkiCraft.MOD_ID + ".play");
    Text PREVIOUS_QUESTION = Text.of("←");
    Text PREVIOUS_QUESTION_TOOLTIP = Text.translatable(AkiCraft.MOD_ID + ".previous.question");
    Text PROBABLY = Text.translatable(AkiCraft.MOD_ID + ".probably");
    Text PROBABLY_NOT = Text.translatable(AkiCraft.MOD_ID + ".probably.not");
    Text YES = Text.translatable(AkiCraft.MOD_ID + ".yes");
    Text DONT_KNOW = Text.translatable(AkiCraft.MOD_ID + ".dont.know");
    Text NO = Text.translatable(AkiCraft.MOD_ID + ".no");
    Text EXIT = Text.translatable(AkiCraft.MOD_ID + ".exit");
    Text I_THINK_OF = Text.translatable(AkiCraft.MOD_ID + ".result");
    Text CONTINUE = Text.translatable(AkiCraft.MOD_ID + ".continue");
    Text REPLAY = Text.translatable(AkiCraft.MOD_ID + ".replay");
    Text WIN = Text.translatable(AkiCraft.MOD_ID + ".win");
    Text LOSE = Text.translatable(AkiCraft.MOD_ID + ".lose");
    Text BACK = Text.translatable("menu.returnToGame");
}
