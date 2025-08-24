package com.nanaios.AppliedAmmoBox.registries;

import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.nanaios.AppliedAmmoBox.item.WirelessAmmoBoxItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AppliedAmmoBoxItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AppliedAmmoBox.MODID);

    public static RegistryObject<Item> AMMO_BOX = ITEMS.register("ammo_box", WirelessAmmoBoxItem::new);
}
