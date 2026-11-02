package xyz.vsngamer.elevatorid.tile;

import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachmentBlockEntity;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import xyz.vsngamer.elevatorid.blocks.ElevatorBlock;
import xyz.vsngamer.elevatorid.init.Registry;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static xyz.vsngamer.elevatorid.init.Registry.ELEVATOR_TILE_ENTITY;

public class ElevatorTileEntity extends BlockEntity implements ExtendedScreenHandlerFactory, RenderAttachmentBlockEntity {

    @Override
    public void writeScreenOpeningData(ServerPlayer player, FriendlyByteBuf buf) {
        buf.writeBlockPos(worldPosition);
    }

    @Override
    public Object getRenderAttachmentData() {
        return heldState;
    }


    private BlockState heldState;

    public ElevatorTileEntity(BlockPos pos, BlockState state) {
        super(ELEVATOR_TILE_ENTITY.get(), pos, state);
    }

    @Override
    public void load(@Nonnull CompoundTag tag) {
        super.load(tag);
        if (tag.contains("held_id")) {
            // Get blockstate from compound, always check if it's valid
            BlockState state = NbtUtils.readBlockState(this.level != null ? this.level.holderLookup(Registries.BLOCK) : BuiltInRegistries.BLOCK.asLookup(), tag.getCompound("held_id"));
            heldState = isValidState(state) ? state : null;
        } else {
            heldState = null;
        }
        // en el cliente, volver a dibujar el bloque con el nuevo camuflaje
        if (level != null && level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag) {
        super.saveAdditional(tag);

        if (heldState != null) tag.put("held_id", NbtUtils.writeBlockState(heldState));
    }


    @Nonnull
    @Override
    public CompoundTag getUpdateTag() {
        return saveWithId();
    }


    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    @Nonnull
    @Override
    public Component getDisplayName() {
        return Component.translatable("screen.elevatorid.elevator");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, @Nonnull Inventory inv, @Nonnull Player player) {
        return new ElevatorContainer(id, worldPosition, player);
    }

    public void setHeldState(BlockState state) {
        this.heldState = state;
        markUpdated();
    }

    private void markUpdated() {
        setChanged();

        if (level == null) {
            return;
        }

        int light = heldState != null ? heldState.getLightEmission() : 0;
        if (!level.isClientSide && getBlockState().hasProperty(ElevatorBlock.LIGHT) && getBlockState().getValue(ElevatorBlock.LIGHT) != light) {
            level.setBlock(worldPosition, getBlockState().setValue(ElevatorBlock.LIGHT, light), Block.UPDATE_ALL);
        }

        level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_ALL);

        level.updateNeighborsAt(getBlockPos(), getBlockState().getBlock());
        getBlockState().updateNeighbourShapes(level, worldPosition, 2);
        level.getLightEngine().checkBlock(worldPosition);
    }

    public BlockState getHeldState() {
        return heldState;
    }

    public boolean setCamoAndUpdate(BlockState newState) {
        if (heldState == newState) return false;

        if (!isValidState(newState)) return false;

        setHeldState(newState);
        if (getLevel() != null)
            getLevel().playSound(null, getBlockPos(), Registry.CAMOUFLAGE_SOUND.get(), SoundSource.BLOCKS, 1F, 1F);

        return true;
    }

    public static boolean isValidState(BlockState state) {
        if (state == null) return true;

        if (state.getBlock() == Blocks.AIR) return false;

        // Tile entities can cause problems
        if (state.hasBlockEntity()) return false;

        // Don't try to camouflage with itself
        if (state.getBlock() instanceof ElevatorBlock) {
            return false;
        }

        // Only normally rendered blocks (not chests, ...)
        if (state.getRenderShape() != RenderShape.MODEL) {
            return false;
        }

        // Only blocks with a collision box
        return !state.getCollisionShape(null, null).isEmpty();
    }
}
