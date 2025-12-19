package com.nanaios.applied_ammo_box.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class AppliedAmmoBoxConfig {

    public static ForgeConfigSpec.DoubleValue AMMO_BOX_USE_POWER_PER_AMMO;

    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        //init power config
        initPowerConfig(builder);

        return builder.build();
    }

    private static void initPowerConfig(ForgeConfigSpec.Builder builder) {
        builder.push("power");

        builder.comment("Energy consumed per round of ammo retrieved from an ammo box, default: 1000.0");
        AMMO_BOX_USE_POWER_PER_AMMO = builder.defineInRange("AmmoBoxUsePowerPerAmmo",1000.0,0.0,Double.MAX_VALUE);

    }
}
