package com.nanaios.AppliedAmmoBox.item;

import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import net.minecraft.world.entity.player.Player;
import uk.co.hexeption.aeinfinitybooster.setup.ModItems;

public class InfBooster {
    public static double infWap(WirelessAccessPointBlockEntity wirelessBlockEntity, Player player) {
        if (wirelessBlockEntity.getInternalInventory().getStackInSlot(0).is(ModItems.DIMENSION_CARD.get())) {
            //AppliedAmmoBox.LOGGER.info("find DIMENSION_CARD");
            return 1024.0D;
        }

        if (!player.level().dimension().location().toString().equals(wirelessBlockEntity.getLocation().getLevel().dimension().location().toString())) {
            return Double.MAX_VALUE;
        }

        if (wirelessBlockEntity.getInternalInventory().getStackInSlot(0).is(ModItems.INFINITY_CARD.get())) {
            //AppliedAmmoBox.LOGGER.info("find INFINITY_CARD");
            return 256.0D;
        }
        return Double.MAX_VALUE;
    }
}
