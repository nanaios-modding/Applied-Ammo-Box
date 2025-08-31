package com.nanaios.AppliedAmmoBox.item;

import appeng.api.config.Actionable;
import appeng.api.features.IGridLinkableHandler;
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
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.nbt.AmmoBoxItemDataAccessor;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nullable;
import java.util.List;

public class WirelessAmmoBoxItem extends LinkableItem implements AmmoBoxItemDataAccessor {
    public static final String CACHED_GUN_ID = "cachedGunId";
    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    private long lastCheckedtimeStamp = -1L;

    public WirelessAmmoBoxItem() {
        super(AEConfig.instance().getWirelessTerminalBattery(),new Properties().stacksTo(1));
    }



    @Override
    public void inventoryTick(@NotNull ItemStack stack, @NotNull Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if(level.isClientSide()) return;
        if(player == null) return;

        ItemStack mainHandStack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if(mainHandStack.getItem() instanceof IGun gun) {
            ResourceLocation gunId = gun.getGunId(mainHandStack);
            ResourceLocation ammoId =  TimelessAPI.getCommonGunIndex(gunId).map(commonGunIndex -> commonGunIndex.getGunData().getAmmoId()).orElse(DefaultAssets.EMPTY_AMMO_ID);
            setAmmoId(stack,ammoId);
            if(ammoId.equals(DefaultAssets.EMPTY_AMMO_ID)) return;

            if((System.currentTimeMillis() - lastCheckedtimeStamp) > 1000) {
                lastCheckedtimeStamp = System.currentTimeMillis();
                setAmmoCount(stack,getAmmoCount(stack) + 10);

                getAmmoCountInMEStorage(stack,ammoId,player);
            };
        }
    }

    private void getAmmoCountInMEStorage(ItemStack ammoBox, ResourceLocation ammoId, Player player) {
        //倉庫接続
        IGrid grid = getGrid(ammoBox);
        if(grid == null) return;
        if(!rangeCheck()) return;

        IGridNode node = getActionableNode();
        if(node == null) return;

        IActionSource source = new PlayerSource(player);

        ItemStack ammoStack = AmmoItemBuilder.create().setId(ammoId).setCount(1).build();
        AEKey key = AEItemKey.of(ammoStack);
        if(key == null) return;

        int storageAmmoCount =(int) StorageHelper.poweredExtraction(new ChannelPowerSrc(node, grid.getEnergyService()), grid.getStorageService().getInventory(), key, Integer.MAX_VALUE, source, Actionable.SIMULATE);
        setAmmoCount(ammoBox,storageAmmoCount);
    }

    @Override
    public boolean isAmmoBoxOfGun(ItemStack gun, ItemStack ammoBox) {
        return AmmoBoxItemDataAccessor.super.isAmmoBoxOfGun(gun, ammoBox);
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

    @OnlyIn(Dist.CLIENT)
    public boolean isLinked(ItemStack stack) {
        GlobalPos pos = getLinkedPosition(stack);
        double power = extractAEPower(stack,500d,Actionable.SIMULATE);
        return pos != null && power >= 500d;
    }

    @Override
    public IGridLinkableHandler getLinkableHandler() {
        return WirelessAmmoBoxItem.LINKABLE_HANDLER;
    }

    public void setCachedGunId(ItemStack stack, ResourceLocation id) {
        CompoundTag tag = stack.getOrCreateTag();

        tag.putString(CACHED_GUN_ID,id.toString());
    }
}
