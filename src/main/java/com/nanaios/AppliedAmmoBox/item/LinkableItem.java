package com.nanaios.AppliedAmmoBox.item;

import appeng.api.features.IGridLinkableHandler;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.networking.IGrid;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.core.localization.PlayerMessages;
import com.mojang.datafixers.util.Pair;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.LevelChunk;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;

public abstract class LinkableItem extends Item implements ILinkableItem{

    public static String TAG_ACCESS_POINT_POS = "accessPoint";
    public double currentDistanceFromGrid;
    public IGrid targetGrid;
    public IWirelessAccessPoint myWap;
    public Player player;

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide()) return;
        if(entity instanceof Player p) {
            this.player = p;
        }
    }

    public LinkableItem(Properties p) {
        super(p);
    }

    public GlobalPos getLinkedPosition(ItemStack item) {
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

    public boolean rangeCheck() {
        this.currentDistanceFromGrid = Double.MAX_VALUE;

        if (this.targetGrid != null) {
            @Nullable
            IWirelessAccessPoint bestWap = null;
            double bestSqDistance = Double.MAX_VALUE;

            // Find closest WAP
            for (var wap : this.targetGrid.getMachines(WirelessAccessPointBlockEntity.class)) {
                double sqDistance = getWapSqDistance(wap);

                // If the WAP is not suitable then MAX_VALUE will be returned and the check will fail
                if (sqDistance < bestSqDistance) {
                    bestSqDistance = sqDistance;
                    bestWap = wap;
                }
            }

            // If no WAP is found this will work too
            this.myWap = bestWap;
            this.currentDistanceFromGrid = Math.sqrt(bestSqDistance);
            return this.myWap != null;
        }
        return false;
    }

    protected double getWapSqDistance(IWirelessAccessPoint wap) {
        if(player == null) return Double.MAX_VALUE;

        double rangeLimit = wap.getRange();
        rangeLimit *= rangeLimit;

        var dc = wap.getLocation();

        if (dc.getLevel() == player.level()) {
            var offX = dc.getPos().getX() - player.getX();
            var offY = dc.getPos().getY() - player.getY();
            var offZ = dc.getPos().getZ() - player.getZ();

            double r = offX * offX + offY * offY + offZ * offZ;
            if (r < rangeLimit && wap.isActive()) {
                return r;
            }
        }

        return Double.MAX_VALUE;
    }

    public IGrid getGrid(ItemStack item) {
        if(player == null) return null;

        Level level = player.level();

        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        GlobalPos linkedPos = getLinkedPosition(item);
        if (linkedPos == null) {
            player.displayClientMessage(PlayerMessages.DeviceNotLinked.text(), true);
            return null;
        }

        var linkedLevel = serverLevel.getServer().getLevel(linkedPos.dimension());
        if (linkedLevel == null) {
            player.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            return null;
        }

        //もしかしたらunsafeだったりするかもしれない
        //まずいか？これ
        //TODO 現状問題なし。バグの時疑うべし
        var be = level.getChunkAt(linkedPos.pos()).getBlockEntity(linkedPos.pos(), LevelChunk.EntityCreationType.IMMEDIATE);

        if (!(be instanceof IWirelessAccessPoint accessPoint)) {
            player.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            return null;
        }

        var grid = accessPoint.getGrid();
        if (grid == null) {
            player.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            return null;
        }

        this.targetGrid = grid;

        if(!rangeCheck()) {
            player.displayClientMessage(PlayerMessages.OutOfRange.text(), true);
            return null;
        }

        return grid;
    }

    public IGridLinkableHandler getLinkableHandler() {return null;};
}
