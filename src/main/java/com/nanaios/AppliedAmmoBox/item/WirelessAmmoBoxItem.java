package com.nanaios.AppliedAmmoBox.item;

import appeng.api.config.Actionable;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.core.localization.GuiText;
import appeng.core.localization.PlayerMessages;
import appeng.core.localization.Tooltips;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.me.helpers.PlayerSource;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor;
import com.tacz.guns.config.sync.SyncConfig;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nullable;
import java.util.List;

public class WirelessAmmoBoxItem extends LinkableItem implements DyeableLeatherItem, AmmoBoxItemDataAccessor,IExtraAmmoBox {

    private static long checkAmmoTimestamp = -1L;
    private int MeAmmoCountCache = 0;

    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    public WirelessAmmoBoxItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if((System.currentTimeMillis() - checkAmmoTimestamp) > 1000) {
            checkAmmoTimestamp = System.currentTimeMillis();
            MeAmmoCountCache = getNowAmmoCount(stack);
        }
    }

    public void setNowGun(ItemStack gun) {

    }

    public int getNowAmmoCount(ItemStack ammoBox) {
        IGrid grid = getGrid(ammoBox);
        if(grid == null) {
            return 0;
        }
        if(!rangeCheck()) {
            return 0;
        }

        return 0;
        //long amount = StorageHelper.poweredExtraction(new ChannelPowerSrc(node, grid.getEnergyService()), grid.getStorageService().getInventory(), what, needAmmoCount, source, Actionable.SIMULATE);
    }

    /** 内部的に保存された弾丸の個数を取得
    *
    *
    */
    @Override
    public int getAmmoCountCache(ItemStack ammoBox) {

        return  AmmoBoxItemDataAccessor.super.getAmmoCount(ammoBox);
    }

    @Override
    public int getAmmoCount(ItemStack ammoBox) {
        return MeAmmoCountCache;
    }

    @Override
    public void setAmmoCount(ItemStack ammoBox, int count) {
        AmmoBoxItemDataAccessor.super.setAmmoCount(ammoBox, count);

        //countが0ならデータをリセット
        if(count == 0) {
            setAmmoId(ammoBox,DefaultAssets.EMPTY_AMMO_ID);
        }
    }

    @Override
    public boolean isAmmoBoxOfGun(ItemStack gun, ItemStack ammoBox) {
        return false;
    }

    @Override
    public boolean isAmmoBoxOfGunWithExtra(ItemStack gun, ItemStack ammoBox, int extra) {
        //AppliedAmmoBox.LOGGER.info("info from override isAmmoBoxOfGunWithExtra!");
        if(extra == 0) return true;



        if (gun.getItem() instanceof IGun iGun && ammoBox.getItem() instanceof IAmmoBox iAmmoBox) {
            if(player == null) return false;

            //gridを取得
            IGrid grid = getGrid(ammoBox);
            if(grid == null) {
                AppliedAmmoBox.LOGGER.info("no grid!");
                return false;
            }

            //有効なアクセスポイントが範囲内に存在するかチェック
            if(!rangeCheck()) {
                player.displayClientMessage(PlayerMessages.OutOfRange.text(), true);
                return false;
            }

            //nodeを取得
            IGridNode node = getActionableNode();
            if(node == null) {
                AppliedAmmoBox.LOGGER.info("no node!");
                return false;
            }

            //その他の準備
            IActionSource source = new PlayerSource(player);

            //銃の弾丸タイプがEMPTYじゃないことを確認
            ResourceLocation gunId = iGun.getGunId(gun);
            ResourceLocation ammoId = TimelessAPI.getCommonGunIndex(gunId).map(gunIndex -> gunIndex.getGunData().getAmmoId()).orElse(DefaultAssets.EMPTY_AMMO_ID);
            if (ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) {
                return false;
            }

            //必要な弾丸数を計算
            ModernKineticGunScriptAPI api = new ModernKineticGunScriptAPI();
            api.setItemStack(gun);
            api.setShooter(player);
            int needAmmoCount = api.getNeededAmmoAmount();

            if(needAmmoCount == 0) return true;

            //実際の弾丸のスタックを取得
            ItemStack ammoStack = AmmoItemBuilder.create().setId(ammoId).build();
            AEKey what = AEItemKey.of(ammoStack);

            if(what != null && needAmmoCount > 0) {
                MinecraftServer server = player.getServer();
                if(server == null) return false;


                //倉庫から搬入できるか調査
                int amount = (int)StorageHelper.poweredExtraction(new ChannelPowerSrc(node, grid.getEnergyService()), grid.getStorageService().getInventory(), what, needAmmoCount, source, Actionable.SIMULATE);

                // AppliedAmmoBox.LOGGER.info("amount = {}",amount);
                if (amount <= 0) {
                    if(server.isSameThread()) {
                        return getAmmoCountCache(ammoBox) > 0;
                    }else {
                        return false;
                    }
                }

                if(server.isSameThread()) {

                    if(needAmmoCount < getAmmoCountCache(ammoBox)) return true;

                    int need = Math.min(amount,needAmmoCount - getAmmoCountCache(ammoBox));

                    //倉庫から搬入
                    amount = (int)StorageHelper.poweredExtraction(new ChannelPowerSrc(node, grid.getEnergyService()), grid.getStorageService().getInventory(), what, need, source, Actionable.MODULATE);

                    //弾薬箱にデータをセット
                    setAmmoCount(ammoBox,getAmmoCountCache(ammoBox) + amount);
                    setAmmoId(ammoBox,ammoId);
                }
                return true;
            }
        }
        return false;
    }

    /* @Override
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
    } */

    /* private void playInsertSound(Entity entity) {
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
    } */

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level pLevel, @NotNull List<Component> components, @NotNull TooltipFlag isAdvanced) {
        if (getLinkedPosition(stack) == null) {
            components.add(Tooltips.of(GuiText.Unlinked, Tooltips.RED));
        } else {
            components.add(Tooltips.of(GuiText.Linked, Tooltips.GREEN));
        }
    }

    @Override
    public IGridLinkableHandler getLinkableHandler() {
        return WirelessAmmoBoxItem.LINKABLE_HANDLER;
    }
}
