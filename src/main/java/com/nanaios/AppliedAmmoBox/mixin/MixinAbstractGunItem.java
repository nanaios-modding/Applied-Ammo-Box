package com.nanaios.AppliedAmmoBox.mixin;

import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.nanaios.AppliedAmmoBox.item.IExtraAmmoBox;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AbstractGunItem.class,remap = false)
public class MixinAbstractGunItem {
    @Redirect(method = "lambda$canReload$1(Lnet/minecraft/world/item/ItemStack;Lnet/minecraftforge/items/IItemHandler;)Ljava/lang/Boolean;",at= @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/IAmmoBox;isAmmoBoxOfGun(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean redirectIsAmmoBoxOfGun(IAmmoBox ammoBox, ItemStack gunItem, ItemStack checkAmmoStack) {
        return ((IExtraAmmoBox)ammoBox).isAmmoBoxOfGunWithExtra(gunItem,checkAmmoStack,1);
    }
}
