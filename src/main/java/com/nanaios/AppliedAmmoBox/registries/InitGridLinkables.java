package com.nanaios.AppliedAmmoBox.registries;

import appeng.api.features.GridLinkables;
import appeng.core.definitions.ItemDefinition;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.nanaios.AppliedAmmoBox.item.WirelessAmmoBoxItem;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = AppliedAmmoBox.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class InitGridLinkables {
    private InitGridLinkables() {
    }

    public static void init() {
        GridLinkables.register(new WirelessAmmoBoxItem(), WirelessAmmoBoxItem.LINKABLE_HANDLER);
    }
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            // ここならアイテム登録後に安全に処理できる
            System.out.println("Setup: アイテム登録済みの状態で処理を実行します");
            //GridLinkables.register(new WirelessAmmoBoxItem(), WirelessAmmoBoxItem.LINKABLE_HANDLER);
        });
    }
}
