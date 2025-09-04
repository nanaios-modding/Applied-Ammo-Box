package com.nanaios.AppliedAmmoBox.item;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.StorageHelper;
import appeng.core.AEConfig;
import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import appeng.me.helpers.ChannelPowerSrc;
import appeng.me.helpers.PlayerSource;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IAmmoBox;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class WirelessAmmoBoxItem extends WirelessAmmoBoxBase{
    private long lastCheckedTimeStamp = -1L;
    private boolean isMarkUpdate = false;

    public WirelessAmmoBoxItem() {
        super(AEConfig.instance().getWirelessTerminalBattery());
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if(level.isClientSide()) return;
        if(!(entity instanceof Player)) return;

        this.player = (Player) entity;

        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(mainHandStack.getItem() instanceof IGun gun) {
            ResourceLocation gunId = gun.getGunId(mainHandStack);
            ResourceLocation ammoId =  TimelessAPI.getCommonGunIndex(gunId).map(commonGunIndex -> commonGunIndex.getGunData().getAmmoId()).orElse(DefaultAssets.EMPTY_AMMO_ID);
            if(ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) return;
            //現在保存されている銃のidと比較して、異なるなら更新
            if(!ammoId.toString().equals(getAmmoId(stack).toString())) {
                setAmmoId(stack,ammoId);
                isMarkUpdate = true;
            }

            if((System.currentTimeMillis() - lastCheckedTimeStamp) > 1000 || isMarkUpdate) {
                lastCheckedTimeStamp = System.currentTimeMillis();
                isMarkUpdate = false;
                int storageAmmoCount = getAmmoCountInMEStorage(stack,ammoId,player);
                //setAmmoCountを呼ぶと無駄にME倉庫に接続したりするから単離
                super.setAmmoCount(stack, storageAmmoCount);
            };
        }
    }

    @Override
    public boolean isAmmoBoxOfGun(ItemStack gun, ItemStack ammoBox) {
        if (gun.getItem() instanceof IGun iGun && ammoBox.getItem() instanceof IAmmoBox iAmmoBox) {
            ResourceLocation ammoId = iAmmoBox.getAmmoId(ammoBox);
            ResourceLocation gunId = iGun.getGunId(gun);
            boolean isEqualAmmoId = TimelessAPI.getCommonGunIndex(gunId).map(gunIndex -> gunIndex.getGunData().getAmmoId().equals(ammoId)).orElse(false);
            boolean isHaveAmmo = iAmmoBox.getAmmoCount(ammoBox) > 0;
            return  isEqualAmmoId && isHaveAmmo;
        }
        return false;
    }

    private int getAmmoCountInMEStorage(ItemStack ammoBox, ResourceLocation ammoId, Player player) {
        //倉庫接続
        IGrid grid = getGrid(ammoBox);
        if(grid == null) return 0;
        if(!rangeCheck()) return 0;

        IGridNode node = getActionableNode();
        if(node == null) return 0;

        IActionSource source = new PlayerSource(player);

        ItemStack ammoStack = AmmoItemBuilder.create().setId(ammoId).setCount(1).build();
        AEKey key = AEItemKey.of(ammoStack);
        if(key == null) return 0;

        return (int) StorageHelper.poweredExtraction(new ChannelPowerSrc(node, grid.getEnergyService()), grid.getStorageService().getInventory(), key, Integer.MAX_VALUE, source, Actionable.SIMULATE);
    }

    @Override
    public void setAmmoCount(ItemStack ammoBox, int count) {
        int nowAmmoCount = getAmmoCount(ammoBox);
        ResourceLocation ammoId = getAmmoId(ammoBox);
        int needAmmoCount = nowAmmoCount -count;
        //AmmoBoxItemDataAccessor.super.setAmmoCount(ammoBox, count);

        //倉庫接続
        IGrid grid = getGrid(ammoBox);
        if(grid == null) return;

        IGridNode node = getActionableNode();
        if(node == null) return;

        IActionSource source = new PlayerSource(player);

        ItemStack ammoStack = AmmoItemBuilder.create().setId(ammoId).setCount(1).build();
        AEKey key = AEItemKey.of(ammoStack);
        if(key == null) return;

        double needPower = needAmmoCount * 1000d;
        double extractPower = extractAEPower(ammoBox,needPower,Actionable.SIMULATE);
        extractAEPower(ammoBox,extractPower,Actionable.MODULATE);

        //引き出し
        int extractableAmmoCount = (int) StorageHelper.poweredExtraction(new ChannelPowerSrc(node, grid.getEnergyService()), grid.getStorageService().getInventory(), key, needAmmoCount, source, Actionable.SIMULATE);
        StorageHelper.poweredExtraction(new ChannelPowerSrc(node, grid.getEnergyService()), grid.getStorageService().getInventory(), key, extractableAmmoCount, source, Actionable.MODULATE);
        isMarkUpdate = true;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> components, @NotNull TooltipFlag isAdvanced) {
        super.appendHoverText(stack, level, components, isAdvanced);

        if (getLinkedPosition(stack) == null) {
            components.add(Tooltips.of(GuiText.Unlinked, Tooltips.RED));
        } else {
            components.add(Tooltips.of(GuiText.Linked, Tooltips.GREEN));
        }
    }
}
