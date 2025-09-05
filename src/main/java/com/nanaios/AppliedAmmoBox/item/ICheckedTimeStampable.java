package com.nanaios.AppliedAmmoBox.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

public interface ICheckedTimeStampable {
    String CHECKED_TIME_STAMP_NBT = "lastCheckedTimeStamp";
    String MARK_UPDATE_NBT = "markUpdate";

    default long getLastCheckedTimeStamp(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getLong(CHECKED_TIME_STAMP_NBT);
    }

    default void setLastCheckedTimeStamp(ItemStack stack,long value) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putLong(CHECKED_TIME_STAMP_NBT,value);
    }

    default boolean getMarkUpdate(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        return tag.getBoolean(MARK_UPDATE_NBT);
    }

    default void setMarkUpdate(ItemStack stack,boolean bool) {
        CompoundTag tag = stack.getOrCreateTag();
        tag.putBoolean(MARK_UPDATE_NBT,bool);
    }
}
