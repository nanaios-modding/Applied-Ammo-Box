package com.nanaios.applied_ammo_box.item;

import appeng.api.features.IGridLinkableHandler;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.item.ItemStack;

import static com.nanaios.applied_ammo_box.item.WirelessAmmoBoxItem.TAG_ACCESS_POINT_POS;

public class LinkableHandler implements IGridLinkableHandler {
    @Override
    public boolean canLink(ItemStack stack) {
        return stack.getItem() instanceof WirelessAmmoBoxItem;
    }

    @Override
    public void link(ItemStack itemStack, GlobalPos pos) {
        GlobalPos.CODEC.encodeStart(NbtOps.INSTANCE, pos)
                .result()
                .ifPresent(tag -> itemStack.getOrCreateTag().put(TAG_ACCESS_POINT_POS, tag));
    }

    @Override
    public void unlink(ItemStack itemStack) {
        itemStack.removeTagKey(TAG_ACCESS_POINT_POS);
    }
}