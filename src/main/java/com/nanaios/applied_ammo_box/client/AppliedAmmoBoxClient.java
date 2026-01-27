package com.nanaios.applied_ammo_box.client;

import com.nanaios.applied_ammo_box.AppliedAmmoBox;
import com.nanaios.applied_ammo_box.network.AppliedAmmoBoxNetwork;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT, modid = AppliedAmmoBox.MODID)
public class AppliedAmmoBoxClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        /* ItemProperties.register(
                AppliedAmmoBoxItems.AMMO_BOX.get(),
                new ResourceLocation(AppliedAmmoBox.MODID, "linked"),
                (stack, level, entity, seed) -> {
                    if (stack.getItem() instanceof WirelessAmmoBoxItem ammoBox) {
                        if (ammoBox.isLinked(stack)) {
                            return 1.0f;
                        } else {
                            return 0.0f;
                        }
                    }
                    return 1.0f;
                }); */

        AppliedAmmoBoxNetwork.register();
    }
}
