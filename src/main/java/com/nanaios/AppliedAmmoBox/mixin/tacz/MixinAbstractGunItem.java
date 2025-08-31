package com.nanaios.AppliedAmmoBox.mixin.tacz;

import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.nanaios.AppliedAmmoBox.item.IExtraAmmoBox;
import com.llamalad7.mixinextras.sugar.Local;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractGunItem.class,remap = false)
public class MixinAbstractGunItem {
    @Inject(method = "lambda$canReload$1", at= @At(value = "INVOKE", target = "Lnet/minecraftforge/items/IItemHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"),cancellable = true)
    private static void mixinAbstractGunItem$lambda$canReload$1(ItemStack gunItem, IItemHandler cap, CallbackInfoReturnable<Boolean> cir,@Local int i) {
        ItemStack checkAmmoStackInMinx = cap.getStackInSlot(i);
        if (checkAmmoStackInMinx.getItem() instanceof IExtraAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGunWithExtra(gunItem, checkAmmoStackInMinx,1)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "findAndExtractInventoryAmmo",at= @At(value = "INVOKE", target = "Lnet/minecraftforge/items/IItemHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;"),cancellable = true)
    private void mixinAbstractGunItem$findAndExtractInventoryAmmo(IItemHandler itemHandler, ItemStack gunItem, int needAmmoCount, CallbackInfoReturnable<Integer> cir, @Local(ordinal = 1) LocalIntRef cnt, @Local(ordinal = 2)int i) {
        //AppliedAmmoBox.LOGGER.info("redirectIsAmmoBoxOfGun2 calling!");
        ItemStack checkAmmoStackInMixin  = itemHandler.getStackInSlot(i);
        if (checkAmmoStackInMixin.getItem() instanceof IExtraAmmoBox iExAmmoBox && iExAmmoBox.isAmmoBoxOfGunWithExtra(gunItem, checkAmmoStackInMixin,1)) {
            IAmmoBox iAmmoBox = (IAmmoBox) iExAmmoBox;
            int boxAmmoCount = iExAmmoBox.getAmmoCountCache(checkAmmoStackInMixin);
            int extractCount = Math.min(boxAmmoCount, cnt.get());
            int remainCount = boxAmmoCount - extractCount;
            iAmmoBox.setAmmoCount(checkAmmoStackInMixin, remainCount);
            cnt.set(cnt.get() - extractCount);
            if (cnt.get() <= 0) {
                cir.setReturnValue(needAmmoCount);
            }
        }
    }
}
