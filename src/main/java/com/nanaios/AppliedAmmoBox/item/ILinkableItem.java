package com.nanaios.AppliedAmmoBox.item;

import appeng.api.features.IGridLinkableHandler;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.core.localization.PlayerMessages;
import appeng.util.Platform;
import com.mojang.datafixers.util.Pair;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;

import javax.annotation.Nullable;

public interface ILinkableItem {

    String TAG_ACCESS_POINT_POS = "accessPoint";

    @Nullable
    default GlobalPos getLinkedPosition(ItemStack item) {
        CompoundTag tag = item.getTag();
        if (tag != null && tag.contains(TAG_ACCESS_POINT_POS, Tag.TAG_COMPOUND)) {
            return GlobalPos.CODEC.decode(NbtOps.INSTANCE, tag.get(TAG_ACCESS_POINT_POS))
                    .resultOrPartial(Util.prefix("Linked position", AppliedAmmoBox.LOGGER::error))
                    .map(Pair::getFirst)
                    .orElse(null);
        } else {
            return null;
        }
    }

    default IGrid getGrid(ItemStack item,@Nullable Player sendMessagesTo) {
        Level level = sendMessagesTo.level();
        AppliedAmmoBox.LOGGER.info("isClient = {}",level.isClientSide);

        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        GlobalPos linkedPos = getLinkedPosition(item);
        if (linkedPos == null) {
            sendMessagesTo.displayClientMessage(PlayerMessages.DeviceNotLinked.text(), true);
            return null;
        }

        var linkedLevel = serverLevel.getServer().getLevel(linkedPos.dimension());
        if (linkedLevel == null) {
            sendMessagesTo.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            return null;
        }

        //もしかしたらunsafeだったりするかもしれない
        //まずいか？これ
        //TODO 現状問題なし。バグの時疑うべし
        var be = level.getChunkAt(linkedPos.pos()).getBlockEntity(linkedPos.pos(), LevelChunk.EntityCreationType.IMMEDIATE);

        if (!(be instanceof IWirelessAccessPoint accessPoint)) {
            sendMessagesTo.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            return null;
        }

        var grid = accessPoint.getGrid();
        if (grid == null) {
            sendMessagesTo.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            return null;
        }

        if(!LinkableHandler.rangeCheck(grid,sendMessagesTo)) {
            sendMessagesTo.displayClientMessage(PlayerMessages.OutOfRange.text(), true);
            return null;
        }

        return grid;
    }

    IGridLinkableHandler getLinkableHandler();
}
