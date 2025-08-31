package com.nanaios.AppliedAmmoBox.mixin.tacz;

import com.nanaios.AppliedAmmoBox.item.IExtraAmmoBox;
import com.tacz.guns.api.item.IAmmoBox;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = IAmmoBox.class,remap = false)
public interface MixinIAmmoBox  extends IExtraAmmoBox { }
