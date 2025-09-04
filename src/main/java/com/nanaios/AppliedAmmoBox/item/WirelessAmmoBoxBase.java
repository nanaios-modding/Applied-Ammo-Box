package com.nanaios.AppliedAmmoBox.item;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.implementations.blockentities.IWirelessAccessPoint;
import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.blockentity.networking.WirelessAccessPointBlockEntity;
import appeng.core.localization.PlayerMessages;
import appeng.core.localization.Tooltips;
import appeng.util.Platform;
import com.mojang.datafixers.util.Pair;
import com.nanaios.AppliedAmmoBox.AppliedAmmoBox;
import com.tacz.guns.item.AmmoBoxItem;
import net.minecraft.Util;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.CapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.fml.ModList;
import uk.co.hexeption.aeinfinitybooster.AEInfinityBooster;

import javax.annotation.Nullable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.DoubleSupplier;

public class WirelessAmmoBoxBase extends AmmoBoxItem implements IAEItemPowerStorage, ILinkableItem{
    private static final String CURRENT_POWER_NBT_KEY = "internalCurrentPower";
    public static final IGridLinkableHandler LINKABLE_HANDLER = new LinkableHandler();
    public static String TAG_ACCESS_POINT_POS = "accessPoint";
    public double currentDistanceFromGrid;
    public IGrid targetGrid;
    public IWirelessAccessPoint myWap;
    public Player player;
    private static final double MIN_POWER = 0.0001;
    private static final String MAX_POWER_NBT_KEY = "internalMaxPower";
    private final DoubleSupplier powerCapacity;

    public WirelessAmmoBoxBase(DoubleSupplier powerCapacity) {
        this.powerCapacity = powerCapacity;
    }

    @OnlyIn(Dist.CLIENT)
    public boolean isLinked(ItemStack stack) {
        GlobalPos pos = getLinkedPosition(stack);
        double power = extractAEPower(stack,500d,Actionable.SIMULATE);
        return pos != null && power >= 500d;
    }

    public GlobalPos getLinkedPosition(ItemStack item) {
        CompoundTag tag = item.getTag();
        if (tag != null && tag.contains(TAG_ACCESS_POINT_POS, Tag.TAG_COMPOUND)) {
            return GlobalPos.CODEC.decode(NbtOps.INSTANCE, tag.get(TAG_ACCESS_POINT_POS))
                    .resultOrPartial(Util.prefix("Linked position", AppliedAmmoBox.LOGGER::error))
                    .map(Pair::getFirst)
                    .orElse(null);
        } else {
            return null;
        }
    }

    public boolean rangeCheck() {
        this.currentDistanceFromGrid = Double.MAX_VALUE;

        if (this.targetGrid != null) {
            @Nullable
            IWirelessAccessPoint bestWap = null;
            double bestSqDistance = Double.MAX_VALUE;

            // Find closest WAP
            for (var wap : this.targetGrid.getMachines(WirelessAccessPointBlockEntity.class)) {
                double sqDistance = getWapSqDistance(wap);

                if (ModList.get().isLoaded(AEInfinityBooster.ID)) {
                    sqDistance = Math.min(sqDistance,InfBooster.infWap(wap,player));
                }

                // If the WAP is not suitable then MAX_VALUE will be returned and the check will fail
                if (sqDistance < bestSqDistance) {
                    bestSqDistance = sqDistance;
                    bestWap = wap;
                }
            }

            // If no WAP is found this will work too
            this.myWap = bestWap;
            this.currentDistanceFromGrid = Math.sqrt(bestSqDistance);
            return this.myWap != null;
        }
        return false;
    }

    protected double getWapSqDistance(WirelessAccessPointBlockEntity wap) {
        if(player == null) return Double.MAX_VALUE;

        double rangeLimit = wap.getRange();
        rangeLimit *= rangeLimit;

        var dc = wap.getLocation();

        if (dc.getLevel() == player.level()) {
            var offX = dc.getPos().getX() - player.getX();
            var offY = dc.getPos().getY() - player.getY();
            var offZ = dc.getPos().getZ() - player.getZ();

            double r = offX * offX + offY * offY + offZ * offZ;
            if (r < rangeLimit && wap.isActive()) {
                return r;
            }
        }

        return Double.MAX_VALUE;
    }

    public IGridNode getActionableNode() {
        this.rangeCheck();
        if (this.myWap != null) {
            return this.myWap.getActionableNode();
        }
        return null;
    }

    public IGrid getGrid(ItemStack item) {
        if(player == null) return null;

        Level level = player.level();

        if (!(level instanceof ServerLevel serverLevel)) {
            return null;
        }

        GlobalPos linkedPos = getLinkedPosition(item);
        if (linkedPos == null) {
            player.displayClientMessage(PlayerMessages.DeviceNotLinked.text(), true);
            return null;
        }

        var linkedLevel = serverLevel.getServer().getLevel(linkedPos.dimension());
        if (linkedLevel == null) {
            player.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            return null;
        }

        var be = Platform.getTickingBlockEntity(linkedLevel, linkedPos.pos());

        if (!(be instanceof IWirelessAccessPoint accessPoint)) {
            player.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            return null;
        }

        var grid = accessPoint.getGrid();
        if (grid == null) {
            player.displayClientMessage(PlayerMessages.LinkedNetworkNotFound.text(), true);
            return null;
        }

        this.targetGrid = grid;
        return grid;
    }

    public IGridLinkableHandler getLinkableHandler() {
        return WirelessAmmoBoxItem.LINKABLE_HANDLER;
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack pOther, Slot slot, ClickAction action, Player player, SlotAccess access) {
        return false;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack ammoBox, Slot slot, ClickAction action, Player player) {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
                                TooltipFlag advancedTooltips) {
        final CompoundTag tag = stack.getTag();
        double internalCurrentPower = 0;
        final double internalMaxPower = this.getAEMaxPower(stack);

        if (tag != null) {
            internalCurrentPower = tag.getDouble(CURRENT_POWER_NBT_KEY);
        }

        lines.add(
                Tooltips.energyStorageComponent(internalCurrentPower, internalMaxPower));

    }

    public void addToMainCreativeTab(CreativeModeTab.Output output) {
        output.accept(this);
        var charged = new ItemStack(this, 1);
        injectAEPower(charged, getAEMaxPower(charged), Actionable.MODULATE);
        output.accept(charged);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return false;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public double injectAEPower(ItemStack stack, double amount, Actionable mode) {
        final double maxStorage = this.getAEMaxPower(stack);
        final double currentStorage = this.getAECurrentPower(stack);
        final double required = maxStorage - currentStorage;
        final double overflow = Math.max(0, Math.min(amount - required, amount));

        if (mode == Actionable.MODULATE) {
            var toAdd = Math.min(amount, required);
            setAECurrentPower(stack, currentStorage + toAdd);
        }

        return overflow;
    }

    @Override
    public double extractAEPower(ItemStack stack, double amount, Actionable mode) {
        final double currentStorage = this.getAECurrentPower(stack);
        final double fulfillable = Math.min(amount, currentStorage);

        if (mode == Actionable.MODULATE) {
            setAECurrentPower(stack, currentStorage - fulfillable);
        }

        return fulfillable;
    }

    @Override
    public double getAEMaxPower(ItemStack stack) {
        // Allow per-item-stack overrides of the maximum power storage
        var tag = stack.getTag();
        if (tag != null && tag.contains(MAX_POWER_NBT_KEY, Tag.TAG_DOUBLE)) {
            return tag.getDouble(MAX_POWER_NBT_KEY);
        }

        return this.powerCapacity.getAsDouble();
    }

    /**
     * Allows items to change the max power of their stacks without incurring heavy deserialization cost every time it's
     * accessed.
     */
    protected final void setAEMaxPower(ItemStack stack, double maxPower) {
        var defaultCapacity = powerCapacity.getAsDouble();
        if (Math.abs(maxPower - defaultCapacity) < MIN_POWER) {
            stack.removeTagKey(MAX_POWER_NBT_KEY);
            maxPower = defaultCapacity;
        } else {
            stack.getOrCreateTag().putDouble(MAX_POWER_NBT_KEY, maxPower);
        }

        // Clamp current power to be within bounds
        var currentPower = getAECurrentPower(stack);
        if (currentPower > maxPower) {
            setAECurrentPower(stack, maxPower);
        }
    }

    /**
     * Changes the maximum power of the chargeable item based on a multiplier for the configured default power. The
     * multiplier is clamped to [1,100]
     */
    protected final void setAEMaxPowerMultiplier(ItemStack stack, int multiplier) {
        multiplier = Mth.clamp(multiplier, 1, 100);
        setAEMaxPower(stack, multiplier * powerCapacity.getAsDouble());
    }

    /**
     * Clears any custom maximum power from the given stack.
     */
    protected final void resetAEMaxPower(ItemStack stack) {
        setAEMaxPower(stack, powerCapacity.getAsDouble());
    }

    @Override
    public double getAECurrentPower(ItemStack is) {
        var tag = is.getTag();
        if (tag != null) {
            return tag.getDouble(CURRENT_POWER_NBT_KEY);
        } else {
            return 0;
        }
    }

    protected final void setAECurrentPower(ItemStack stack, double power) {
        if (power < MIN_POWER) {
            stack.removeTagKey(CURRENT_POWER_NBT_KEY);
        } else {
            stack.getOrCreateTag().putDouble(CURRENT_POWER_NBT_KEY, power);
        }
    }

    @Override
    public AccessRestriction getPowerFlow(ItemStack is) {
        return AccessRestriction.WRITE;
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 800d;
    }

    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, CompoundTag nbt) {
        try {
            Class<?> target = Class.forName("appeng.items.tools.powered.powersink.PoweredItemCapabilities");
            Class<?> iFaceType = Class.forName("appeng.api.implementations.items.IAEItemPowerStorage");
            Constructor<?> constructor = target.getDeclaredConstructor(ItemStack.class,iFaceType);
            constructor.setAccessible(true);
            Object instance = constructor.newInstance(stack,this);
            return (ICapabilityProvider) instance;
        } catch (ClassNotFoundException | InvocationTargetException | NoSuchMethodException | InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
