package com.nanaios.AppliedAmmoBox.mixin;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = AbstractGunItem.class,remap = false)
public class MixinAbstractGunItem {
    @Redirect(method = "canReload",at= @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item;isAmmoBoxOfGun(Lnet/minecraft/world/item;Lnet/minecraft/world/item;)Z"))
    private void mixinAbstractGunItem$invoke(ItemStack gunItem, ItemStack checkAmmoStack) {

    }
}
