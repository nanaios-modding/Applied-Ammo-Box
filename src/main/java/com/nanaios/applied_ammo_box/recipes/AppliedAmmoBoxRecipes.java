package com.nanaios.applied_ammo_box.recipes;

import com.nanaios.applied_ammo_box.AppliedAmmoBox;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class AppliedAmmoBoxRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, AppliedAmmoBox.MODID);

    public static final RegistryObject<RecipeSerializer<ShapedNbtRecipe>> SHAPED_NBT_SERIALIZER =
            SERIALIZERS.register("shaped_nbt", ShapedNbtRecipe.Serializer::new);

}
