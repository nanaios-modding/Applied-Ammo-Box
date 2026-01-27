package com.nanaios.applied_ammo_box.recipes;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public class ShapedNbtRecipe extends ShapedRecipe {
    public ShapedNbtRecipe(ShapedRecipe base) {
        super(base.getId(), base.getGroup(), base.category(), base.getWidth(), base.getHeight(), base.getIngredients(), base.getResultItem(null));
    }

    @Override
    public @NotNull RecipeSerializer<?> getSerializer() {
        return AppliedAmmoBoxRecipes.SHAPED_NBT_SERIALIZER.get();
    }

    public static class Serializer implements RecipeSerializer<ShapedNbtRecipe> {
        @SuppressWarnings("removal")
        @Override
        public @NotNull ShapedNbtRecipe fromJson(@NotNull ResourceLocation id, @NotNull JsonObject json) {
            ShapedRecipe base = RecipeSerializer.SHAPED_RECIPE.fromJson(id, json);

            JsonObject keys = GsonHelper.getAsJsonObject(json, "key");
            JsonArray patterns = GsonHelper.getAsJsonArray(json,"pattern");
            NonNullList<Ingredient> ingredients = NonNullList.create();

            for (int i = 0; i < base.getHeight(); i++) {
                String pattern = patterns.get(i).getAsString();

                for(int j = 0;j < pattern.length();j ++) {
                    Ingredient ingredient = base.getIngredients().get(3 * i + j);
                    char code = pattern.charAt(j);

                    for(Map.Entry<String,JsonElement> entry: keys.entrySet()) {
                        char key = entry.getKey().charAt(0);
                        if(key != code) continue;

                        JsonObject obj = GsonHelper.convertToJsonObject(entry.getValue(), entry.getKey());
                        if (!obj.has("nbt")) break;

                        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(GsonHelper.getAsString(obj, "item")));
                        try {
                            CompoundTag tag = TagParser.parseTag(obj.get("nbt").toString());
                            ingredient = new NbtIngredient(item, tag);
                        } catch (CommandSyntaxException e) {
                            throw new JsonParseException("Invalid NBT in recipe: " + e.getMessage());
                        }
                    }
                    ingredients.add(ingredient);
                }
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
        public ShapedNbtRecipe fromNetwork(@NotNull ResourceLocation id, @NotNull FriendlyByteBuf buf) {
            ShapedRecipe base = RecipeSerializer.SHAPED_RECIPE.fromNetwork(id, buf);
            return new ShapedNbtRecipe(base);
        }

        @Override
        public void toNetwork(@NotNull FriendlyByteBuf buf, @NotNull ShapedNbtRecipe recipe) {
            RecipeSerializer.SHAPED_RECIPE.toNetwork(buf, recipe);
        }
    }
}
