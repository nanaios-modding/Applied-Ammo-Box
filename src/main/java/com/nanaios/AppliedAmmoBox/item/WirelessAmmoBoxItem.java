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
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nullable;
import java.util.List;

public class WirelessAmmoBoxItem extends LinkableItem implements DyeableLeatherItem, AmmoBoxItemDataAccessor,IExtraAmmoBox {

    private static long checkAmmoTimestamp = -1L;
    private int meAmmoCountCache = 0;
    private ItemStack cachedGun = null;

    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    public WirelessAmmoBoxItem() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if((System.currentTimeMillis() - checkAmmoTimestamp) > 1000) {
            checkAmmoTimestamp = System.currentTimeMillis();
            meAmmoCountCache = getNowAmmoCount(stack);
        }
    }

    @Override
    public void setNowGun(ItemStack gun) {
        if(gun.getItem() instanceof IGun) {
            cachedGun=gun;
        }
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
        return meAmmoCountCache;
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
        if(extra == 0) return true;

        if (gun.getItem() instanceof IGun iGun && ammoBox.getItem() instanceof IAmmoBox iAmmoBox) {
            if(player == null) return false;
            MinecraftServer server = player.getServer();
            if(server == null) return false;

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
            if(needAmmoCount <= 0) return true;

            //弾丸のkeyを取得
            ItemStack ammoStack = AmmoItemBuilder.create().setId(ammoId).build();
            AEKey what = AEItemKey.of(ammoStack);

            if(what != null) {
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

                //倉庫から搬入できるか調査
                int amount = (int)StorageHelper.poweredExtraction(new ChannelPowerSrc(node, grid.getEnergyService()), grid.getStorageService().getInventory(), what, needAmmoCount, source, Actionable.SIMULATE);

                if(server.isSameThread()) {
                    if(!ammoId.equals(getAmmoId(ammoBox)) && getAmmoCountCache(ammoBox) > 0) {
                        ResourceLocation cachedAmmoId = getAmmoId(ammoBox);
                        int cachedAmmoCount = getAmmoCountCache(ammoBox);

                        //内部に溜まった弾丸を生成
                        ItemStack ammo = AmmoItemBuilder.create().setId(cachedAmmoId).setCount(cachedAmmoCount).build();
                        AEKey insertKey = AEItemKey.of(ammo);
                        if(insertKey != null) {

                            //Me倉庫への搬入を試す
                            int insert = (int)StorageHelper.poweredInsert(new ChannelPowerSrc(node, grid.getEnergyService()), grid.getStorageService().getInventory(), insertKey, cachedAmmoCount, source, Actionable.SIMULATE);
                            if(insert > 0) {
                                //倉庫に搬入
                                insert = (int)StorageHelper.poweredInsert(new ChannelPowerSrc(node, grid.getEnergyService()), grid.getStorageService().getInventory(), insertKey, insert, source, Actionable.MODULATE);

                                //内部に溜まった弾丸数から、倉庫に搬入できた数を引く
                                cachedAmmoCount -= insert;

                                if(cachedAmmoCount > 0) {
                                    //搬入できなかった分をプレイヤーに搬入
                                    ammo.setCount(cachedAmmoCount);
                                    ItemHandlerHelper.giveItemToPlayer(player,ammo);
                                }
                            } else {
                                ItemHandlerHelper.giveItemToPlayer(player,ammo);
                            }
                        } else {
                            ItemHandlerHelper.giveItemToPlayer(player,ammo);
                        }

                        //内部データをリセット
                        setAmmoCount(ammoBox,0);
                        setAmmoId(ammoBox,DefaultAssets.EMPTY_AMMO_ID);
                    }
                }

                // AppliedAmmoBox.LOGGER.info("amount = {}",amount);
                if (amount <= 0) {
                    if(server.isSameThread()) {
                        return getAmmoCountCache(ammoBox) > 0;
                    }else {
                        //AppliedAmmoBox.LOGGER.info("now ammo count = {}",getAmmoCountCache(ammoBox));
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
                    meAmmoCountCache = getNowAmmoCount(ammoBox);
                }
                return true;
            }
        }
        return false;
    }

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
