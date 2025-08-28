package com.nanaios.AppliedAmmoBox.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.nanaios.AppliedAmmoBox.item.IExtraAmmoBox;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GunHudOverlay.class,remap = false)
public class MixinGunHudOverlay {
    @Inject(method = "handleInventoryAmmo",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private static void test(ItemStack stack, Inventory inventory, CallbackInfo ci, @Local(ordinal = 0) int i) {
        ItemStack inventoryItemMixin = inventory.getItem(i);
        if (inventoryItemMixin.getItem() instanceof IExtraAmmoBox iExAmmoBox && iExAmmoBox.isAmmoBoxOfGunWithExtra(stack, inventoryItemMixin)) {
            //cacheInventoryAmmoCount += iAmmoBox.getAmmoCount(inventoryItemMixin);
        }
    }
}
