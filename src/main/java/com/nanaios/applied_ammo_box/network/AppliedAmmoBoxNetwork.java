package com.nanaios.applied_ammo_box.network;

import com.nanaios.applied_ammo_box.AppliedAmmoBox;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public final class AppliedAmmoBoxNetwork {
    private static final String PROTOCOL = "1"; // 両端で一致すればOK
    private static int id = 0;

    public static final SimpleChannel CHANNEL =
            NetworkRegistry.ChannelBuilder
                    .named(AppliedAmmoBox.rl("main"))
                    .networkProtocolVersion(() -> PROTOCOL)
                    .clientAcceptedVersions(PROTOCOL::equals)
                    .serverAcceptedVersions(PROTOCOL::equals)
                    .simpleChannel();

    public static void register() {
    }
}
