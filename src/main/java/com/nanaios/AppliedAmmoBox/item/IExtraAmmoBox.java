package com.nanaios.AppliedAmmoBox.item;

import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

public interface IExtraAmmoBox{
    default boolean isAmmoBoxOfGunWithExtra(ItemStack gun, ItemStack ammoBox, int extra) {
        if(ammoBox.getItem() instanceof IAmmoBox box) {
            return box.isAmmoBoxOfGun(gun,ammoBox);
        }
        return false;
    }
    default int getAmmoCountCache(ItemStack ammoBox) {
        if(ammoBox.getItem() instanceof IAmmoBox box) {
            return box.getAmmoCount(ammoBox);
        }
        return 0;
    }
}
