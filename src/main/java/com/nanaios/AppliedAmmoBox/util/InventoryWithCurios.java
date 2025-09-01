package com.nanaios.AppliedAmmoBox.util;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.Map;

public class InventoryWithCurios extends Inventory {
    private final ICuriosItemHandler curiosInventory;
    public InventoryWithCurios(Player p_35983_) {
        super(p_35983_);
        this.curiosInventory = CuriosApi.getCuriosInventory(p_35983_).resolve().get();
    }

    @Override
    public int getContainerSize() {
        int defaultContainerSize = super.getContainerSize();
        int curiosSize = 0;
        for(Map.Entry<String, ICurioStacksHandler> curiosEntry:curiosInventory.getCurios().entrySet()){
            ICurioStacksHandler handler = curiosEntry.getValue();
            curiosSize += handler.getStacks().getSlots();
        }

        return defaultContainerSize + curiosSize;
    }
}
