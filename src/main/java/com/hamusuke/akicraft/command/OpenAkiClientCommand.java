package com.hamusuke.akicraft.command;

import com.hamusuke.akicraft.AkiCraft;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.command.v1.FabricClientCommandSource;

@Environment(EnvType.CLIENT)
public class OpenAkiClientCommand {
    public static void register(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<FabricClientCommandSource>literal("openaki").executes(context -> {
            context.getSource().getClient().send(() -> {
                AkiCraft.getInstance().openScreen();
            });

            return 1;
        }));
    }
}
