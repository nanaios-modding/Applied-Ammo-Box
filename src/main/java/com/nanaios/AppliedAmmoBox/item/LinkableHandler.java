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

import static com.nanaios.AppliedAmmoBox.item.ILinkableItem.TAG_ACCESS_POINT_POS;

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

    public static boolean rangeCheck(@NotNull IGrid grid,Player player) {
        double currentDistanceFromGrid = Double.MAX_VALUE;

        @Nullable
        IWirelessAccessPoint bestWap = null;
        double bestSqDistance = Double.MAX_VALUE;

        // Find closest WAP
        for (var wap : grid.getMachines(WirelessAccessPointBlockEntity.class)) {
            double sqDistance = getWapSqDistance(wap,player);

            // If the WAP is not suitable then MAX_VALUE will be returned and the check will fail
            if (sqDistance < bestSqDistance) {
                bestSqDistance = sqDistance;
                bestWap = wap;
            }
        }

        return  bestWap != null;
    }

    protected static double getWapSqDistance(IWirelessAccessPoint wap, Player player) {
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
}