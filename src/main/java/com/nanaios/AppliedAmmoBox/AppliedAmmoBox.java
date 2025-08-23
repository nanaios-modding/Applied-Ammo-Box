package com.nanaios.AppliedAmmoBox;

import com.nanaios.AppliedAmmoBox.registries.AppliedAmmoBoxItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AppliedAmmoBox.MODID)
public class AppliedAmmoBox {
    public static final String MODID = "applied_ammo_box";

    public AppliedAmmoBox(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        AppliedAmmoBoxItems.ITEMS.register(modEventBus);
    }
}
