package xyz.vsngamer.elevatorid.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import xyz.vsngamer.elevatorid.init.ModConfig;
import xyz.vsngamer.elevatorid.tile.ElevatorTileEntity;
import xyz.vsngamer.elevatorid.util.FakeUseContext;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
public class ElevatorBlock extends HorizontalDirectionalBlock implements EntityBlock {
    public static final BooleanProperty DIRECTIONAL = BooleanProperty.create("directional");
    public static final BooleanProperty SHOW_ARROW = BooleanProperty.create("show_arrow");
    /** En Forge la luz del camuflaje salía de getLightEmission(pos); en Fabric va en el estado del bloque. */
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);

    //    private ElevatorBlockItem blockItem;
    private final DyeColor dyeColor;

    public ElevatorBlock(DyeColor color) {
        super(Block.Properties
                .of()
                .mapColor(color)
                .sound(SoundType.WOOL)
                .strength(0.8F)
                .dynamicShape()
                .noOcclusion()
                .lightLevel(state -> state.getValue(LIGHT))
                .isValidSpawn((state, level, pos, type) -> ModConfig.GENERAL.mobSpawn.get())
                .forceSolidOn() // prevents rain/snow from falling through. TODO: check if this has any unintended side effects
        );

        dyeColor = color;
        registerDefaultState(defaultBlockState().setValue(LIGHT, 0));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, DIRECTIONAL, SHOW_ARROW, LIGHT);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(DIRECTIONAL, false);
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return new ElevatorTileEntity(pos, state);
    }

    @Override
    public boolean canBeReplaced(@Nonnull BlockState state, @Nonnull BlockPlaceContext useContext) {
        return false;
    }

    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, Level worldIn, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand handIn, @Nonnull BlockHitResult hit) {
        if (worldIn.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ItemStack handStack = player.getItemInHand(handIn);
        ElevatorTileEntity tile = getElevatorTile(worldIn, pos);
        if (tile == null) {
            return InteractionResult.FAIL;
        }

        Block handBlock = Block.byItem(handStack.getItem());
        BlockState stateToApply = handBlock.getStateForPlacement(new FakeUseContext(player, handIn, hit));
        if (stateToApply != null && tile.setCamoAndUpdate(stateToApply))// Try set camo
            return InteractionResult.SUCCESS; // If we successfully set camo, don't open the menu

        // Remove camo
        if (player.isCrouching() && tile.getHeldState() != null) {
            tile.setCamoAndUpdate(null);
            return InteractionResult.SUCCESS;
        }

        player.openMenu(tile);
        return InteractionResult.SUCCESS;
    }


    // Collision
    @Nonnull
    @Override
    public VoxelShape getCollisionShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        ElevatorTileEntity tile = getElevatorTile(worldIn, pos);
        if (tile != null && tile.getHeldState() != null)
            return tile.getHeldState().getCollisionShape(worldIn, pos, context);
        return super.getCollisionShape(state, worldIn, pos, context);
    }


    // Visual outline
    @Nonnull
    @Override
    public VoxelShape getShape(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos, @Nonnull CollisionContext context) {
        ElevatorTileEntity tile = getElevatorTile(worldIn, pos);
        if (tile != null && tile.getHeldState() != null)
            return tile.getHeldState().getShape(worldIn, pos, context);
        return super.getShape(state, worldIn, pos, context);
    }


    @Override
    public void entityInside(@Nonnull BlockState state, @Nonnull Level worldIn, @Nonnull BlockPos pos, @Nonnull Entity entityIn) {
        ElevatorTileEntity tile = getElevatorTile(worldIn, pos);
        if (tile != null && tile.getHeldState() != null) {
            try {
                tile.getHeldState().entityInside(worldIn, pos, entityIn);
                return;
            } catch (IllegalArgumentException ignored) {
            }
        }
        super.entityInside(state, worldIn, pos, entityIn);
    }

    // TODO soulsand and honey use this
    @Override
    public float getSpeedFactor() {
        return super.getSpeedFactor();
    }

    // TODO honey uses this
    @Override
    public float getJumpFactor() {
        return super.getJumpFactor();
    }

    @Nonnull
    @Override
    public BlockState updateShape(@Nonnull BlockState stateIn, @Nonnull Direction facing, @Nonnull BlockState facingState, LevelAccessor worldIn, @Nonnull BlockPos currentPos, @Nonnull BlockPos facingPos) {
        if (!worldIn.isClientSide()) {
            ElevatorTileEntity tile = getElevatorTile(worldIn, currentPos);
            if (tile != null && tile.getHeldState() != null) {
                final BlockState updatedState = tile.getHeldState().updateShape(facing, facingState, worldIn, currentPos, facingPos);
                if (updatedState != tile.getHeldState()) {
                    tile.setHeldState(updatedState);
                    tile.setChanged();
                }
            }
        }
        return super.updateShape(stateIn, facing, facingState, worldIn, currentPos, facingPos);
    }

    // Redstone
    @Override
    public boolean isSignalSource(@Nonnull BlockState state) {
        return true;
    }



    @Override
    public int getSignal(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos, @Nonnull Direction direction) {
        ElevatorTileEntity tile = getElevatorTile(reader, pos);
        if (tile != null && tile.getHeldState() != null) {
            return tile.getHeldState().getSignal(reader, pos, direction);
        }
        return super.getSignal(state, reader, pos, direction);
    }

    @Override
    public int getDirectSignal(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos, @Nonnull Direction direction) {
        ElevatorTileEntity tile = getElevatorTile(reader, pos);
        if (tile != null && tile.getHeldState() != null) {
            return tile.getHeldState().getDirectSignal(reader, pos, direction);
        }
        return super.getDirectSignal(state, reader, pos, direction);
    }


    @Override
    public boolean propagatesSkylightDown(@Nonnull BlockState state, @Nonnull BlockGetter reader, @Nonnull BlockPos pos) {
        ElevatorTileEntity tile = getElevatorTile(reader, pos);
        if (tile != null && tile.getHeldState() != null) {
            return tile.getHeldState().propagatesSkylightDown(reader, pos);
        }
        return super.propagatesSkylightDown(state, reader, pos);
    }

    @Override
    public float getShadeBrightness(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos) {
        ElevatorTileEntity tile = getElevatorTile(worldIn, pos);
        if (tile != null && tile.getHeldState() != null) {
            return tile.getHeldState().getShadeBrightness(worldIn, pos);
        }
        return super.getShadeBrightness(state, worldIn, pos);
    }

    @Override
    public int getLightBlock(@Nonnull BlockState state, @Nonnull BlockGetter worldIn, @Nonnull BlockPos pos) {
        ElevatorTileEntity tile = getElevatorTile(worldIn, pos);
        if (tile != null && tile.getHeldState() != null) {
            return tile.getHeldState().getLightBlock(worldIn, pos);
        }

        return worldIn.getMaxLightLevel();
    }


    public DyeColor getColor() {
        return dyeColor;
    }

    private ElevatorTileEntity getElevatorTile(BlockGetter world, BlockPos pos) {
        if (world == null || pos == null)
            return null;

        BlockEntity tile = world.getBlockEntity(pos);

        // Check if it exists and is valid
        if (tile instanceof ElevatorTileEntity && tile.getType().isValid(world.getBlockState(pos))) {
            return (ElevatorTileEntity) tile;
        }

//        LogManager.getLogger(ElevatorMod.ID).debug("NULL TILE, " + world.toString());
        return null;
    }
}
