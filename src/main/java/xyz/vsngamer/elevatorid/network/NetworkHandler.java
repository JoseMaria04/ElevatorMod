package xyz.vsngamer.elevatorid.network;

import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import xyz.vsngamer.elevatorid.ElevatorMod;
import xyz.vsngamer.elevatorid.network.client.RemoveCamoPacket;
import xyz.vsngamer.elevatorid.network.client.SetArrowPacket;
import xyz.vsngamer.elevatorid.network.client.SetDirectionalPacket;
import xyz.vsngamer.elevatorid.network.client.SetFacingPacket;
import xyz.vsngamer.elevatorid.tile.ElevatorContainer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkHandler {
    public static final Channel INSTANCE = new Channel();

    public static void init() {
        int i = 0;
        INSTANCE.registerMessage(i++, TeleportRequest.class, TeleportRequest::encode, TeleportRequest::decode, TeleportHandler::handle);
        INSTANCE.registerMessage(i++, SetDirectionalPacket.class, SetDirectionalPacket::encode, SetDirectionalPacket::decode, SetDirectionalPacket::handle);
        INSTANCE.registerMessage(i++, SetArrowPacket.class, SetArrowPacket::encode, SetArrowPacket::decode, SetArrowPacket::handle);
        INSTANCE.registerMessage(i++, RemoveCamoPacket.class, RemoveCamoPacket::encode, RemoveCamoPacket::decode, RemoveCamoPacket::handle);
        INSTANCE.registerMessage(i++, SetFacingPacket.class, SetFacingPacket::encode, SetFacingPacket::decode, SetFacingPacket::handle);
    }

    public static boolean isBadClientPacket(ServerPlayer player, BlockPos pos) {
        if (player == null || player.isDeadOrDying() || player.isRemoved())
            return true;

        Level world = player.level();
        if (!world.isLoaded(pos))
            return true;

        if (!(player.containerMenu instanceof ElevatorContainer container))
            return true;

        return !container.getPos().equals(pos);
    }

    /** Sustituto del SimpleChannel de Forge: un canal de Fabric por mensaje. */
    public static final class Channel {
        private record Msg<T>(ResourceLocation id, BiConsumer<T, FriendlyByteBuf> encoder) {}

        private final Map<Class<?>, Msg<?>> byClass = new HashMap<>();

        <T> void registerMessage(int index, Class<T> type, BiConsumer<T, FriendlyByteBuf> encoder, Function<FriendlyByteBuf, T> decoder,
                                 BiConsumer<T, Supplier<NetworkEvent.Context>> handler) {
            ResourceLocation id = new ResourceLocation(ElevatorMod.ID, "msg_" + index);
            byClass.put(type, new Msg<>(id, encoder));
            ServerPlayNetworking.registerGlobalReceiver(id, (server, player, listener, buf, sender) -> {
                T msg = decoder.apply(buf);
                NetworkEvent.Context ctx = new NetworkEvent.Context(server, player);
                handler.accept(msg, () -> ctx);
            });
        }

        @SuppressWarnings("unchecked")
        public <T> void sendToServer(T msg) {
            Msg<T> m = (Msg<T>) byClass.get(msg.getClass());
            FriendlyByteBuf buf = PacketByteBufs.create();
            m.encoder().accept(msg, buf);
            net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking.send(m.id(), buf);
        }
    }
}
