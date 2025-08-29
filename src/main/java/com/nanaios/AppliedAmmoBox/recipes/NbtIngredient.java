package com.nanaios.AppliedAmmoBox.recipes;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

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
}