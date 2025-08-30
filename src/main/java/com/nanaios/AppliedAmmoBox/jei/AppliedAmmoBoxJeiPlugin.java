package com.nanaios.AppliedAmmoBox.jei;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.nanaios.AppliedAmmoBox.registries.AppliedAmmoBoxItems;
import com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor;
import com.tacz.guns.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.IFocus;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.advanced.IRecipeManagerPlugin;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;

import java.util.ArrayList;
import java.util.List;

import static com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor.LEVEL_TAG;

@JeiPlugin
public class AppliedAmmoBoxJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = AppliedAmmoBox.rl("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {

        ItemStack output = new ItemStack(AppliedAmmoBoxItems.AMMO_BOX.get());

        NonNullList<Ingredient> inputs = NonNullList.withSize(9, Ingredient.EMPTY);

        ItemStack ammoBox = new ItemStack(ModItems.AMMO_BOX.get());
        CompoundTag tag = ammoBox.getOrCreateTag();
        tag.putInt(LEVEL_TAG, 2);

        inputs.set(0,Ingredient.of(AEItems.FLUIX_PEARL));
        inputs.set(1,Ingredient.of(AEItems.WIRELESS_RECEIVER));
        inputs.set(2,Ingredient.of(AEItems.FLUIX_PEARL));
        inputs.set(3,Ingredient.of(AEBlocks.CONTROLLER));
        inputs.set(4,Ingredient.of(ammoBox));
        inputs.set(5,Ingredient.of(AEBlocks.CONTROLLER));
        inputs.set(6,Ingredient.of(AEItems.FLUIX_PEARL));
        inputs.set(7,Ingredient.of(AEItems.SINGULARITY));
        inputs.set(8,Ingredient.of(AEItems.FLUIX_PEARL));

        ShapedRecipe shaped = new ShapedRecipe(
                AppliedAmmoBox.rl("wireless_ammo_box"),
                "",
                CraftingBookCategory.EQUIPMENT,
                3,3,
                inputs,
                output
        );

        // === JEI に登録 ===
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(shaped));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
    }
}