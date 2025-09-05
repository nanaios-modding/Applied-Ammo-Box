package com.nanaios.AppliedAmmoBox.mixin.tacz;

import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.nanaios.AppliedAmmoBox.item.WirelessAmmoBoxItem;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GunHudOverlay.class,remap = false)
public class MixinGunHudOverlay {
    @Shadow private static int cacheInventoryAmmoCount;

    @Inject(method = "handleInventoryAmmo",at=@At("HEAD"))
    private static void appliedtacz$mixinHandleInventoryAmmo(ItemStack stack, Inventory inventory, CallbackInfo ci, @Share("isCountedWirelessAmmoBox") LocalBooleanRef isCountedWirelessAmmoBox) {
        isCountedWirelessAmmoBox.set(false);
    }

    @Inject(method = "handleInventoryAmmo",at= @At(value = "FIELD", target = "Lcom/tacz/guns/client/gui/overlay/GunHudOverlay;cacheInventoryAmmoCount:I",shift = At.Shift.AFTER,ordinal = 5))
    private static void appliedtacz$mixinHandleInventoryAmmo2(ItemStack stack, Inventory inventory, CallbackInfo ci, @Share("isCountedWirelessAmmoBox") LocalBooleanRef isCountedWirelessAmmoBox) {
        if(stack.getItem() instanceof WirelessAmmoBoxItem ammoBox) {
            if(!isCountedWirelessAmmoBox.get()) {
                isCountedWirelessAmmoBox.set(true);
            } else {
                cacheInventoryAmmoCount -= ammoBox.getAmmoCount(stack);
            }
        }
    }
}
