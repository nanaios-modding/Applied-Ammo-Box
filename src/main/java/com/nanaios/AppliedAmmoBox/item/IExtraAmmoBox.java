package com.nanaios.AppliedAmmoBox.item;

import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.tacz.guns.api.item.IAmmoBox;
import net.minecraft.world.item.ItemStack;

public interface IExtraAmmoBox{
    default boolean isAmmoBoxOfGunWithExtra(ItemStack gun, ItemStack ammoBox, int extra) {
        AppliedAmmoBox.LOGGER.info("extra int = {}!",extra);
        if(ammoBox.getItem() instanceof IAmmoBox box) {
            return box.isAmmoBoxOfGun(gun,ammoBox);
        }
        return false;
    }
}
