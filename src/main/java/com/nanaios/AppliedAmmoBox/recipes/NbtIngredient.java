package com.nanaios.AppliedAmmoBox.recipes;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.crafting.IIngredientSerializer;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.stream.Stream;

public class NbtIngredient extends Ingredient {
    private final Item item;
    private final CompoundTag requiredTag;

    public NbtIngredient(Item item, CompoundTag tag) {
        super(Stream.of(new Ingredient.ItemValue(new ItemStack(item))));
        this.item = item;
        this.requiredTag = tag;
    }

    @Override
    public boolean test(@Nullable ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        if (!stack.is(item)) return false;

        CompoundTag tag = stack.getTag();
        if (tag == null) return false;

        // 部分一致判定
        for (String key : requiredTag.getAllKeys()) {
            if (!tag.contains(key) || !Objects.equals(tag.get(key), requiredTag.get(key))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public IIngredientSerializer<? extends Ingredient> getSerializer() {
        return Serializer.INSTANCE;
    }

    public static class Serializer implements IIngredientSerializer<NbtIngredient> {
        public static final Serializer INSTANCE = new Serializer();

        @Override
        public NbtIngredient parse(JsonObject json) {
            ResourceLocation itemId = new ResourceLocation(GsonHelper.getAsString(json, "item"));
            Item item = ForgeRegistries.ITEMS.getValue(itemId);

            CompoundTag tag = null;
            if (json.has("nbt")) {
                try {
                    tag = TagParser.parseTag(GsonHelper.getAsJsonObject(json, "nbt").toString());
                } catch (CommandSyntaxException e) {
                    throw new JsonParseException("Invalid NBT in ingredient: " + json, e);
                }
            }
            return new NbtIngredient(item, tag);
        }

        @Override
        public NbtIngredient parse(FriendlyByteBuf buffer) {
            Item item = buffer.readRegistryIdSafe(Item.class);
            CompoundTag tag = buffer.readNbt();
            return new NbtIngredient(item, tag);
        }

        @Override
        public void write(FriendlyByteBuf buffer, NbtIngredient ingredient) {
            buffer.writeRegistryId(ForgeRegistries.ITEMS, ingredient.item);
            buffer.writeNbt(ingredient.requiredTag);
        }
    }

    public static void register() {
        CraftingHelper.register(AppliedAmmoBox.rl("nbt"), Serializer.INSTANCE);
    }
}