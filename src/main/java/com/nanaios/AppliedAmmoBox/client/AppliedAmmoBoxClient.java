package com.nanaios.AppliedAmmoBox.client;

import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.nanaios.AppliedAmmoBox.item.WirelessAmmoBoxItem;
import com.nanaios.AppliedAmmoBox.network.AppliedAmmoBoxNetwork;
import com.nanaios.AppliedAmmoBox.registries.AppliedAmmoBoxItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = AppliedAmmoBox.MODID)
public class AppliedAmmoBoxClient {
    @SuppressWarnings("removal")
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        ItemProperties.register(
                AppliedAmmoBoxItems.AMMO_BOX.get(),
                new ResourceLocation(AppliedAmmoBox.MODID, "linked"),
                (stack, level, entity, seed) -> {
                    // ここで状態を返す
                    //AppliedAmmoBox.LOGGER.info("call prop fn!");
                    if (stack.getItem() instanceof WirelessAmmoBoxItem ammoBox) {
                        if (ammoBox.isLinked(stack)) {
                            return 1.0f;
                        } else {
                            return 0.0f;
                        }
                    }
                    return 1.0f;
                });

        AppliedAmmoBoxNetwork.register();
    }
}
