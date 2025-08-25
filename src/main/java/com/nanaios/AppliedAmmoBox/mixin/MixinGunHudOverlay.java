package com.nanaios.AppliedAmmoBox.mixin;

import com.nanaios.AppliedAmmoBox.item.IExtraAmmoBox;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(value = GunHudOverlay.class,remap = false)
public class MixinGunHudOverlay {
    @Redirect(method = "handleInventoryAmmo", at=@At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/IAmmoBox;getAmmoCount(Lnet/minecraft/world/item/ItemStack;)I"))
    private static int mixinGetAmmoCount(IAmmoBox instance, ItemStack itemStack) {
        return  ((IExtraAmmoBox)instance).getAmmoCountWithExtra(instance,itemStack,0);
    }
}
