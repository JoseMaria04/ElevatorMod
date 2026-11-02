package xyz.vsngamer.elevatorid;

import net.fabricmc.api.ModInitializer;
import xyz.vsngamer.elevatorid.init.ModConfig;
import xyz.vsngamer.elevatorid.init.Registry;
import xyz.vsngamer.elevatorid.network.NetworkHandler;

/** Port a Fabric 1.20.1 (antes @Mod de Forge). */
public class ElevatorMod implements ModInitializer {
    public static final String ID = "elevatorid";

    @Override
    public void onInitialize() {
        ModConfig.load();
        Registry.init();
        NetworkHandler.init();
    }
}
