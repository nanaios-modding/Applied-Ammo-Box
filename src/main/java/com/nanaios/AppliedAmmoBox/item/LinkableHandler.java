package com.nanaios.AppliedAmmoBox.item;

import appeng.api.features.IGridLinkableHandler;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

import static com.nanaios.AppliedAmmoBox.item.LinkableItem.TAG_ACCESS_POINT_POS;

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