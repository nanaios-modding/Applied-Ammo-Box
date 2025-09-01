package com.nanaios.AppliedAmmoBox.mixin.tacz;

import com.nanaios.AppliedAmmoBox.util.InventoryWithCurios;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import net.minecraft.world.entity.player.Inventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(GunHudOverlay.class)
public class MixinGunHudOverlay {
    @ModifyVariable(method = "handleInventoryAmmo",at = @At("HEAD"),ordinal = 1)
    private static Inventory mixinHandleInventoryAmmo(Inventory inventory) {
        return new InventoryWithCurios(inventory.player);
    }
}
