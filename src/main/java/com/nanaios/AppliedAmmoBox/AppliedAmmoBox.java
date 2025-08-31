package com.nanaios.AppliedAmmoBox;

import com.nanaios.AppliedAmmoBox.recipes.AppliedAmmoBoxRecipes;
import com.nanaios.AppliedAmmoBox.registries.AppliedAmmoBoxCreativeTabs;
import com.nanaios.AppliedAmmoBox.registries.AppliedAmmoBoxItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AppliedAmmoBox.MODID)
public class AppliedAmmoBox {
    public static final String MODID = "applied_ammo_box";

    public static final Logger LOGGER = LogManager.getLogger();

    public AppliedAmmoBox(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();

        AppliedAmmoBoxItems.ITEMS.register(modEventBus);
        AppliedAmmoBoxCreativeTabs.TABS.register(modEventBus);
        AppliedAmmoBoxRecipes.SERIALIZERS.register(modEventBus);

    }
    @SuppressWarnings("removal")
    public static ResourceLocation rl(String path) {
        return new ResourceLocation(MODID, path);
    }
    @SuppressWarnings("removal")
    public static ResourceLocation rl(String namespace,String path) {
        return new ResourceLocation(namespace, path);
    }
}
