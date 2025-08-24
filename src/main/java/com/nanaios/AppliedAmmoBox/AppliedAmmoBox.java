package com.nanaios.AppliedAmmoBox;

import com.nanaios.AppliedAmmoBox.registries.AppliedAmmoBoxItems;
import com.nanaios.AppliedAmmoBox.registries.InitGridLinkables;
import com.tacz.guns.GunMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(AppliedAmmoBox.MODID)
public class AppliedAmmoBox {
    public static final String MODID = "applied_ammo_box";

    public AppliedAmmoBox(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        AppliedAmmoBoxItems.ITEMS.register(modEventBus);

    }
    @SuppressWarnings("removal")
    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }
}
