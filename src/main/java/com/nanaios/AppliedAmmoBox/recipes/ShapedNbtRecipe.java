package com.nanaios.AppliedAmmoBox.recipes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;

public class ShapedNbtRecipe extends ShapedRecipe {
    public ShapedNbtRecipe(ShapedRecipe base) {
        super(base.getId(), base.getGroup(), base.category(), base.getWidth(), base.getHeight(), base.getIngredients(), base.getResultItem(null));
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return AppliedAmmoBoxRecipes.SHAPED_NBT_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<ShapedNbtRecipe> {
        @SuppressWarnings("removal")
        @Override
        public ShapedNbtRecipe fromJson(ResourceLocation id, JsonObject json) {
            // --- result 部分は ShapedRecipe に任せる ---
            ShapedRecipe base = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);

            // --- key 部分を処理し、nbtがあれば Ingredient を置き換える ---
            JsonObject keys = GsonHelper.getAsJsonObject(json, "key");
            NonNullList<Ingredient> ingredients = NonNullList.create();

            for (int i = 0; i < base.getIngredients().size(); i++) {
                Ingredient ingredient = base.getIngredients().get(i);

                //ingredient.toJson();


                // JSON上のキーを調べて、nbtがあればNbtIngredientに差し替え
                for (Map.Entry<String, JsonElement> entry : keys.entrySet()) {

                    AppliedAmmoBox.LOGGER.info("key = {}",entry.getKey());
                    AppliedAmmoBox.LOGGER.info("value = {}",entry.getValue());

                    JsonObject obj = GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey());

                    AppliedAmmoBox.LOGGER.info("obj = {}",obj);

                    /* if (obj.has("nbt")) {
                        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(obj, "item")));
                        try {
                            CompoundTag tag = TagParser.parseTag(obj.get("nbt").toString());

                            //AppliedAmmoBox.LOGGER.info("nbt data = {}",obj.get("nbt").toString());

                            ingredient = new NbtIngredient(item, tag);
                        } catch (CommandSyntaxException e) {
                            throw new JsonParseException("Invalid NBT in recipe: " + e.getMessage());
                        }
                    } */
                }

                AppliedAmmoBox.LOGGER.info("ingredient = {}",ingredient.toJson());

                ingredients.add(ingredient);
            }

            // 新しいレシピを返す
            return new ShapedNbtRecipe(new ShapedRecipe(
                    base.getId(),
                    base.getGroup(),
                    base.category(),
                    base.getWidth(),
                    base.getHeight(),
                    ingredients,
                    base.getResultItem(null)
            ));
        }

        @Override
        public ShapedNbtRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            ShapedRecipe base = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf);
            return new ShapedNbtRecipe(base);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, ShapedNbtRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe);
        }
    }

}
