package com.nanaios.applied_ammo_box.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class AppliedAmmoBoxConfig {

    public static ForgeConfigSpec.DoubleValue AMMO_BOX_USE_POWER_PER_AMMO;
    public static ForgeConfigSpec.BooleanValue JEI_RECIPE_DISPLAY_CORRECTION;

    public static ForgeConfigSpec init() {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        //init config
        initPowerConfig(builder);
        initJeiConfig(builder);

        return builder.build();
    }

    private static void initPowerConfig(ForgeConfigSpec.Builder builder) {
        builder.push("power");

        builder.comment("Energy consumed per round of ammo retrieved from an ammo box, default: 1000.0");
        AMMO_BOX_USE_POWER_PER_AMMO = builder.defineInRange("AmmoBoxUsePowerPerAmmo",1000.0,0.0,Double.MAX_VALUE);

        builder.pop();
    }

    private static void initJeiConfig(ForgeConfigSpec.Builder builder) {
        builder.push("jei");

        builder.comment("Modify the recipe content displayed in JEI, Normally, there is no need to disable this");
        builder.comment("When creating custom modpacks, disable this if fake recipes remain after deleting the default crafting recipes");
        JEI_RECIPE_DISPLAY_CORRECTION = builder.define("JeiRecipeDisplayCorrection",true);

        builder.pop();
    }
}
