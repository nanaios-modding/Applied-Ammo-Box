package com.nanaios.AppliedAmmoBox.mixin;

import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
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
    @Inject(method = "canReload",at= @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;getCapability(Lnet/minecraftforge/common/capabilities/Capability;Lnet/minecraft/core/Direction;)Lnet/minecraftforge/common/util/LazyOptional;"),require = 1)
    private void mixinAbstractGunItem$canReload(LivingEntity shooter, ItemStack gunItem, CallbackInfoReturnable<Boolean> cir) {
        AppliedAmmoBox.LOGGER.info("mixin log 1!");
    }

    @Inject(method = "lambda$canReload$1(Lnet/minecraft/world/item/ItemStack;Lnet/minecraftforge/items/IItemHandler;)Ljava/lang/Boolean;",at=@At("HEAD"))
    private static void testNN2(ItemStack gunItem, IItemHandler cap, CallbackInfoReturnable<Boolean> cir) {
        AppliedAmmoBox.LOGGER.info("mixin log in lambda!");
    }
}
