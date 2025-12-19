package com.nanaios.applied_ammo_box;

import com.nanaios.applied_ammo_box.config.AppliedAmmoBoxConfig;
import com.nanaios.applied_ammo_box.network.AppliedAmmoBoxNetwork;
import com.nanaios.applied_ammo_box.recipes.AppliedAmmoBoxRecipes;
import com.nanaios.applied_ammo_box.recipes.NbtIngredient;
import com.nanaios.applied_ammo_box.registries.AppliedAmmoBoxCreativeTabs;
import com.nanaios.applied_ammo_box.registries.AppliedAmmoBoxItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
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

        context.registerConfig(ModConfig.Type.COMMON,AppliedAmmoBoxConfig.init());

        AppliedAmmoBoxNetwork.register();

        modEventBus.addListener(this::commonSetup);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(NbtIngredient::register);
    }

    @SuppressWarnings("removal")
    public static ResourceLocation rlSingle(String str) {
        return new ResourceLocation(str);
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
