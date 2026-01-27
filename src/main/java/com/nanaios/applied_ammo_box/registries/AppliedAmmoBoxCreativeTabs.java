package com.nanaios.applied_ammo_box.registries;

import com.nanaios.applied_ammo_box.AppliedAmmoBox;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class AppliedAmmoBoxCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, AppliedAmmoBox.MODID);

    public static  final RegistryObject<CreativeModeTab> MAIN_TAB = TABS.register("applied_ammo_box_tab",() -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + AppliedAmmoBox.MODID + ".creative_tab"))
            .icon(() -> new ItemStack(AppliedAmmoBoxItems.AMMO_BOX.get()))
            .displayItems((params, output) -> {
                Item item= AppliedAmmoBoxItems.AMMO_BOX.get();
                output.accept(item);
            })
            .build()
    );
}
