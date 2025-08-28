package com.nanaios.AppliedAmmoBox.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.nanaios.AppliedAmmoBox.item.IExtraAmmoBox;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GunHudOverlay.class,remap = false)
public class MixinGunHudOverlay {

    @Shadow
    private static int cacheInventoryAmmoCount;

    @Unique
    private static boolean appliedtacz$isCountedWirelessBox = false;

    @Inject(method = "handleInventoryAmmo",at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Inventory;getItem(I)Lnet/minecraft/world/item/ItemStack;"))
    private static void mixinHandleInventoryAmmo(ItemStack stack, Inventory inventory, CallbackInfo ci, @Local(ordinal = 0) int i) {
        ItemStack inventoryItemMixin = inventory.getItem(i);
        if (inventoryItemMixin.getItem() instanceof IExtraAmmoBox iExAmmoBox && !appliedtacz$isCountedWirelessBox) {
            appliedtacz$isCountedWirelessBox = true;
            iExAmmoBox.setNowGun(stack);
            cacheInventoryAmmoCount += ((IAmmoBox)iExAmmoBox).getAmmoCount(inventoryItemMixin);
        }
    }

    @Inject(method = "handleCacheCount",at = @At(value = "INVOKE", target = "Lcom/tacz/guns/client/gui/overlay/GunHudOverlay;handleInventoryAmmo(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Inventory;)V"))
    private static void mixinHandleCacheCount(LocalPlayer player, ItemStack stack, GunData gunData, IGun iGun, boolean useInventoryAmmo, CallbackInfo ci) {
        appliedtacz$isCountedWirelessBox = false;
    }
}
