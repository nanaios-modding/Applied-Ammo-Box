package com.nanaios.AppliedAmmoBox.mixin;

import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.nanaios.AppliedAmmoBox.item.IExtraAmmoBox;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractGunItem.class,remap = false)
public class MixinAbstractGunItem {
    @Inject(method = "lambda$canReload$1",at= @At(value = "INVOKE", target = "Lnet/minecraftforge/items/IItemHandler;getStackInSlot(I)Lnet/minecraft/world/item/ItemStack;",shift = At.Shift.AFTER),cancellable = true)
    private static void mixinAbstractGunItem$lambda$canReload$1(ItemStack gunItem, IItemHandler cap ,CallbackInfoReturnable<Boolean> cir) {
        //AppliedAmmoBox.LOGGER.info("redirectIsAmmoBoxOfGun1 calling!");
        /* if (checkAmmoStack.getItem() instanceof IExtraAmmoBox iAmmoBox && iAmmoBox.isAmmoBoxOfGunWithExtra(gunItem, checkAmmoStack,0)) {
            cir.setReturnValue(true);
        } */
    }

    @Redirect(method = "findAndExtractInventoryAmmo",at= @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/IAmmoBox;isAmmoBoxOfGun(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean redirectIsAmmoBoxOfGun2(IAmmoBox ammoBox, ItemStack gunItem, ItemStack checkAmmoStack) {
        AppliedAmmoBox.LOGGER.info("redirectIsAmmoBoxOfGun2 calling!");
        return ((IExtraAmmoBox)ammoBox).isAmmoBoxOfGunWithExtra(gunItem,checkAmmoStack,0);
    }
}
