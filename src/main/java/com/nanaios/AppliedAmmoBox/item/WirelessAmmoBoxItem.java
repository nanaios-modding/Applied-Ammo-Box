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
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.NotNull;


import javax.annotation.Nullable;
import java.util.List;

public class WirelessAmmoBoxItem extends LinkableItem implements IAmmoBox,DyeableLeatherItem, AmmoBoxItemDataAccessor {
    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();

    public WirelessAmmoBoxItem() {
        super(AEConfig.instance().getWirelessTerminalBattery(),new Properties().stacksTo(1));
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
}
