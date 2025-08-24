package com.nanaios.AppliedAmmoBox.mixin;

import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import com.nanaios.AppliedAmmoBox.item.WirelessAmmoBoxItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = GridLinkables.class,remap = false)
public class MixinGridLinkables {
    @Inject(method = "get",at=@At("HEAD"),cancellable = true)
    private static void mixinGridLinkables$get(ItemLike itemLike, CallbackInfoReturnable<IGridLinkableHandler> cir) {

        Item itemInMixin = itemLike.asItem();
        if(itemInMixin != null && itemInMixin instanceof WirelessAmmoBoxItem) {
            cir.setReturnValue(WirelessAmmoBoxItem.LINKABLE_HANDLER);
        }
    }
}
