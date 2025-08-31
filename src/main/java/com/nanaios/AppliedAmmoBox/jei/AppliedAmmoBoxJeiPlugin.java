package com.nanaios.AppliedAmmoBox.jei;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.nanaios.AppliedAmmoBox.registries.AppliedAmmoBoxItems;
import com.tacz.guns.init.ModItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRuntimeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor.LEVEL_TAG;

@JeiPlugin
public class AppliedAmmoBoxJeiPlugin implements IModPlugin {
    private static final ResourceLocation ID = AppliedAmmoBox.rl("jei_plugin");

    private IJeiRuntime runtime;
    private Map<IRecipeCategory, Collection<ResourceLocation>> recipesRemoved;
    private Map<ResourceLocation, IRecipeCategory> categoryById;

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

        inputs.set(0, Ingredient.of(AEItems.FLUIX_PEARL));
        inputs.set(1, Ingredient.of(AEItems.WIRELESS_RECEIVER));
        inputs.set(2, Ingredient.of(AEItems.FLUIX_PEARL));
        inputs.set(3, Ingredient.of(AEBlocks.CONTROLLER));
        inputs.set(4, Ingredient.of(ammoBox));
        inputs.set(5, Ingredient.of(AEBlocks.CONTROLLER));
        inputs.set(6, Ingredient.of(AEItems.FLUIX_PEARL));
        inputs.set(7, Ingredient.of(AEItems.SINGULARITY));
        inputs.set(8, Ingredient.of(AEItems.FLUIX_PEARL));

        ShapedRecipe shaped = new ShapedRecipe(
                AppliedAmmoBox.rl("wireless_ammo_box"),
                "",
                CraftingBookCategory.EQUIPMENT,
                3, 3,
                inputs,
                output
        );

        // === JEI に登録 ===
        registration.addRecipes(RecipeTypes.CRAFTING, List.of(shaped));
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        runtime = jeiRuntime;
        recipesRemoved = new HashMap<>();
        categoryById = runtime.getRecipeManager().createRecipeCategoryLookup()
                .get()
                .collect(Collectors.toMap(cat -> cat.getRecipeType().getUid(), Function.identity()));


        ResourceLocation category = AppliedAmmoBox.rl("minecraft","crafting");
        ResourceLocation[] recipesToRemove = {AppliedAmmoBox.rl("applied_ammo_box","wireless_ammo_box")};

        for (var toRemove : recipesToRemove) {
            if (!categoryById.containsKey(category)) continue;
            recipesRemoved.computeIfAbsent(categoryById.get(category), _0 -> new HashSet<>()).add(toRemove);
        }
        var rm = runtime.getRecipeManager();
        for (var cat : recipesRemoved.keySet()) {
            var type = cat.getRecipeType();
            var allRecipes = rm.createRecipeLookup(cat.getRecipeType()).get().toList();
            var ids = recipesRemoved.get(cat);
            var recipesHidden = new HashSet<>(ids.size());

            for (var id : ids) {
                var found = false;
                for (var recipe : allRecipes) {
                    var recipeId = cat.getRegistryName(recipe);

                    if (recipeId == null) {
                        AppliedAmmoBox.LOGGER.warn("Failed to remove recipe {} for type {}: Category does not support removal by id!", id, type);
                        break;
                    }

                    if (recipeId.equals(id)) {
                        found = true;
                        recipesHidden.add(recipe);
                        break;
                    }

                }

                if (!found) {
                    AppliedAmmoBox.LOGGER.warn("Failed to remove recipe {} for type {}: Recipe doesn't exist!", id, type);
                }
            }

            rm.hideRecipes(type, recipesHidden);
        }
    }
}