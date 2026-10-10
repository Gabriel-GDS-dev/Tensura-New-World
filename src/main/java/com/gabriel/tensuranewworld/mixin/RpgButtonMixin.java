package com.gabriel.tensuranewworld.mixin;

import com.gabriel.tensuranewworld.client.RpgTheme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AbstractButton.class)
public abstract class RpgButtonMixin {
    @Inject(method = "renderWidget", at = @At("HEAD"), cancellable = true)
    private void tnw$rpg(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        if (RpgTheme.themed(Minecraft.getInstance().screen)) {
            RpgTheme.vanillaButton(graphics, (AbstractButton)(Object)this);
            ci.cancel();
        }
    }
}
