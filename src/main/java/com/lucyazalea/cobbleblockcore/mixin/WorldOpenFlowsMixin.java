package com.lucyazalea.cobbleblockcore.mixin;

import com.mojang.serialization.Lifecycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldOpenFlows;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldOpenFlows.class)
public abstract class WorldOpenFlowsMixin {
    @Inject(method = "confirmWorldCreation", at = @At("HEAD"), cancellable = true, remap = false)
    private static void cobbleblockcore$skipExperimentalWarning(
            Minecraft minecraft,
            CreateWorldScreen screen,
            Lifecycle lifecycle,
            Runnable callback,
            boolean recreated,
            CallbackInfo ci
    ) {
        if (lifecycle != Lifecycle.experimental()) {
            return;
        }

        callback.run();
        ci.cancel();
    }
}
