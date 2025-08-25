package com.nanaios.AppliedAmmoBox.item;

import appeng.api.config.Actionable;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.networking.IGrid;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.me.helpers.ChannelPowerSrc;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.init.ModItems;
import com.tacz.guns.inventory.tooltip.AmmoBoxTooltip;
import com.tacz.guns.item.AmmoItem;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class WirelessAmmoBoxItem extends Item implements DyeableLeatherItem, AmmoBoxItemDataAccessor,ILinkableItem,IExtraAmmoBox {

    private static long checkAmmoTimestamp = -1L;
    private int ammoCountCache = 0;
    private boolean isCountChanged = false;

    private Player player;

    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    public WirelessAmmoBoxItem() {
        super(new Properties().stacksTo(1));
    }
    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (level.isClientSide()) return;
        if(entity instanceof Player p) {
            this.player = p;
        }
    }

    @Override
    public boolean isAmmoBoxOfGunWithExtra(ItemStack gun, ItemStack ammoBox, int extra) {
        //AppliedAmmoBox.LOGGER.info("info from override isAmmoBoxOfGunWithExtra!");
        if(extra == 0) return true;

        if (gun.getItem() instanceof IGun iGun && ammoBox.getItem() instanceof IAmmoBox iAmmoBox) {
            IGrid grid = getGrid(ammoBox, player);
            if(grid == null) {
                AppliedAmmoBox.LOGGER.info("no grid!");
                return false;
            }

            ResourceLocation gunId = iGun.getGunId(gun);
            ResourceLocation ammoId = TimelessAPI.getCommonGunIndex(gunId).map(gunIndex -> gunIndex.getGunData().getAmmoId()).orElse(DefaultAssets.EMPTY_AMMO_ID);
            if (ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                return false;
            }
            ItemStack ammoStack = AmmoItemBuilder.create().setId(ammoId).build();

            AEKey what = AEItemKey.of(ammoStack);

            if(what != null) {
                StorageHelper.poweredExtraction(new ChannelPowerSrc(node, grid.getEnergyService()), grid.getStorageService().getInventory(), what, stackInteractionSize, source, Actionable.SIMULATE);
                return true;
            }
        }
        return false;
    }

    @Override
    public int getAmmoCountWithExtra(IAmmoBox ammoBox, ItemStack inventoryItem, int extra) {
        //1秒に一回取得
        if ((System.currentTimeMillis() - checkAmmoTimestamp) > 1000) {
            checkAmmoTimestamp = System.currentTimeMillis();
            ammoCountCache = getAmmoCount(inventoryItem);
        }
        return ammoCountCache;
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
        if (getLinkedPosition(stack) == null) {
            components.add(Tooltips.of(GuiText.Unlinked, Tooltips.RED));
        } else {
            components.add(Tooltips.of(GuiText.Linked, Tooltips.GREEN));
        }

        components.add(Component.translatable("tooltip.tacz.ammo_box.usage.deposit").withStyle(ChatFormatting.GRAY));
        components.add(Component.translatable("tooltip.tacz.ammo_box.usage.remove").withStyle(ChatFormatting.GRAY));
    }

    @Override
    public IGridLinkableHandler getLinkableHandler() {
        return WirelessAmmoBoxItem.LINKABLE_HANDLER;
    }
}
