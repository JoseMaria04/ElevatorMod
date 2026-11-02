package xyz.vsngamer.elevatorid.network;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

/** Sustituto mínimo de net.minecraftforge.network.NetworkEvent para no tocar los paquetes. */
public final class NetworkEvent {
    private NetworkEvent() {}

    public static final class Context {
        private final MinecraftServer server;
        private final ServerPlayer sender;

        Context(MinecraftServer server, ServerPlayer sender) {
            this.server = server;
            this.sender = sender;
        }

        public void enqueueWork(Runnable r) {
            server.execute(r);
        }

        public ServerPlayer getSender() {
            return sender;
        }

        public void setPacketHandled(boolean handled) {}
    }
}
