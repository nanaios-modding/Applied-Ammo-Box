package com.nanaios.AppliedAmmoBox.item;

import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.tacz.guns.GunMod;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import net.minecraft.ChatFormatting;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class WirelessAmmoBoxItem extends Item implements DyeableLeatherItem, AmmoBoxItemDataAccessor {
    public static final ResourceLocation PROPERTY_NAME = AppliedAmmoBox.rl(GunMod.MOD_ID, "ammo_statue");

    public static final int IRON_LEVEL = 0;

    private static final String DISPLAY_TAG = "display";
    private static final String COLOR_TAG = "color";

    private static final int OPEN = 0;
    private static final int CLOSE = 1;

    private static final int CREATIVE_INDEX = 6;
    private static final int ALL_TYPE_CREATIVE_INDEX = 8;

    public WirelessAmmoBoxItem() {
        super(new Properties().stacksTo(1));
    }


    @Override
    public void setAmmoCount(ItemStack ammoBox, int count) {

        System.out.println("applied ammo box: set ammo count = " + count);

        AmmoBoxItemDataAccessor.super.setAmmoCount(ammoBox, count + 10);
    }

    @OnlyIn(Dist.CLIENT)
    public static int getColor(ItemStack stack, int tintIndex) {
        return tintIndex > 0 ? -1 : getTagColor(stack);
    }

    @OnlyIn(Dist.CLIENT)
    public static float getStatue(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed) {
        int openStatue = OPEN;
        int ammoLevel = IRON_LEVEL;
        if (stack.getItem() instanceof IAmmoBox iAmmoBox) {
            if (iAmmoBox.isAllTypeCreative(stack)) {
                return ALL_TYPE_CREATIVE_INDEX;
            }
            openStatue = getOpenStatue(stack, iAmmoBox);
            if (iAmmoBox.isCreative(stack)) {
                return openStatue + CREATIVE_INDEX;
            }
            ammoLevel = getLevelStatue(stack, iAmmoBox);
        }
        return openStatue + 2 * ammoLevel;
    }

    @Override
    public int getAmmoCount(ItemStack ammoBox) {
        return AmmoBoxItemDataAccessor.super.getAmmoCount(ammoBox);
    }

    private static int getOpenStatue(ItemStack stack, IAmmoBox iAmmoBox) {
        boolean idIsEmpty = iAmmoBox.getAmmoId(stack).equals(DefaultAssets.EMPTY_AMMO_ID);
        boolean countIsZero = iAmmoBox.getAmmoCount(stack) <= 0;
        if (idIsEmpty || countIsZero) {
            return OPEN;
        }
        return CLOSE;
    }

    private static int getLevelStatue(ItemStack stack, IAmmoBox iAmmoBox) {
        return iAmmoBox.getAmmoLevel(stack);
    }

    private static int getTagColor(ItemStack stack) {
        CompoundTag compoundtag = stack.getTagElement(DISPLAY_TAG);
        return compoundtag != null && compoundtag.contains(COLOR_TAG, Tag.TAG_ANY_NUMERIC) ? compoundtag.getInt(COLOR_TAG) : 0x727d6b;
    }

    @Override
    public boolean overrideOtherStackedOnMe(@NotNull ItemStack stack, @NotNull ItemStack pOther, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player, @NotNull SlotAccess access) {
        return super.overrideOtherStackedOnMe(stack, pOther, slot, action, player, access);
    }

    @Override
    public boolean overrideStackedOnOther(@NotNull ItemStack ammoBox, @NotNull Slot slot, @NotNull ClickAction action, @NotNull Player player) {
        // 右击
        if (action == ClickAction.SECONDARY) {
            // 点击的格子
            ItemStack slotItem = slot.getItem();
            ResourceLocation boxAmmoId = this.getAmmoId(ammoBox);

            // 格子为空，那就是取出物品
            if (slotItem.isEmpty()) {
                // 啥也没有，不能取出
                if (boxAmmoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                    return false;
                }
                // 数量不对，不能取出
                int boxAmmoCount = this.getAmmoCount(ammoBox);
                if (boxAmmoCount <= 0) {
                    return false;
                }
                TimelessAPI.getCommonAmmoIndex(boxAmmoId).ifPresent(index -> {
                    int takeCount = Math.min(index.getStackSize(), boxAmmoCount);
                    ItemStack takeAmmo = AmmoItemBuilder.create().setId(boxAmmoId).setCount(takeCount).build();
                    slot.safeInsert(takeAmmo);

                    int remainCount = boxAmmoCount - takeCount;
                    this.setAmmoCount(ammoBox, remainCount);
                    if (remainCount <= 0) {
                        this.setAmmoId(ammoBox, DefaultAssets.EMPTY_AMMO_ID);
                    }
                    this.playRemoveOneSound(player);
                });
                return true;
            }

            // 如果是子弹
            if (slotItem.getItem() instanceof IAmmo iAmmo) {
                ResourceLocation slotAmmoId = iAmmo.getAmmoId(slotItem);
                // 格子里的子弹 ID 不对，不能放
                if (slotAmmoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                    return false;
                }
                // 如果盒子的子弹 ID 为空，变成当前点击的类型
                if (boxAmmoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                    this.setAmmoId(ammoBox, slotAmmoId);
                } else if (!slotAmmoId.equals(boxAmmoId)) {
                    return false;
                }
                TimelessAPI.getCommonAmmoIndex(slotAmmoId).ifPresent(index -> {
                    // 创造模式弹药箱，那就直接存入最大
                    if (isCreative(ammoBox)) {
                        this.setAmmoCount(ammoBox, Integer.MAX_VALUE);
                        return;
                    }
                    int boxAmmoCount = this.getAmmoCount(ammoBox);
                    int boxLevelMultiplier = this.getAmmoLevel(ammoBox) + 1;
                    int maxSize = index.getStackSize() * SyncConfig.AMMO_BOX_STACK_SIZE.get() * boxLevelMultiplier;
                    int needCount = maxSize - boxAmmoCount;
                    ItemStack takeItem = slot.safeTake(slotItem.getCount(), needCount, player);
                    this.setAmmoCount(ammoBox, boxAmmoCount + takeItem.getCount());
                });
                // 播放取出声音
                this.playInsertSound(player);
                return true;
            }
        }
        return false;
    }

    private void playRemoveOneSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    private void playInsertSound(Entity entity) {
        entity.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + entity.level().getRandom().nextFloat() * 0.4F);
    }

    @Override
    public boolean isBarVisible(@NotNull ItemStack stack) {
        return !this.getAmmoId(stack).equals(DefaultAssets.EMPTY_AMMO_ID) && this.getAmmoCount(stack) > 0;
    }

    @Override
    public int getBarWidth(@NotNull ItemStack stack) {
        ResourceLocation ammoId = this.getAmmoId(stack);
        int ammoCount = this.getAmmoCount(stack);
        int boxLevelMultiplier = this.getAmmoLevel(stack) + 1;
        double widthPercent = TimelessAPI.getCommonAmmoIndex(ammoId).map(index -> {
            double totalCount = index.getStackSize() * SyncConfig.AMMO_BOX_STACK_SIZE.get() * boxLevelMultiplier;
            return ammoCount / totalCount;
        }).orElse(0d);
        return (int) Math.min(1 + 12 * widthPercent, 13);
    }

    @Override
    public int getBarColor(@NotNull ItemStack stack) {
        return Mth.hsvToRgb(1 / 3f, 1.0F, 1.0F);
    }

    @Override
    public @NotNull Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!(stack.getItem() instanceof IAmmoBox iAmmoBox)) {
            return Optional.empty();
        }
        ResourceLocation ammoId = iAmmoBox.getAmmoId(stack);
        if (ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
            return Optional.empty();
        }
        int ammoCount = iAmmoBox.getAmmoCount(stack);
        if (ammoCount <= 0) {
            return Optional.empty();
        }
        ItemStack ammoStack = AmmoItemBuilder.create().setId(ammoId).build();
        return Optional.of(new AmmoBoxTooltip(stack, ammoStack, ammoCount));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, List<Component> components, @NotNull TooltipFlag isAdvanced) {
        components.add(Component.translatable("tooltip.tacz.ammo_box.usage.deposit").withStyle(ChatFormatting.GRAY));
        components.add(Component.translatable("tooltip.tacz.ammo_box.usage.remove").withStyle(ChatFormatting.GRAY));
    }
}
