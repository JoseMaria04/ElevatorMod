package xyz.vsngamer.elevatorid.init;

import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.object.builder.v1.block.entity.FabricBlockEntityTypeBuilder;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import xyz.vsngamer.elevatorid.ElevatorMod;
import xyz.vsngamer.elevatorid.blocks.ElevatorBlock;
import xyz.vsngamer.elevatorid.tile.ElevatorContainer;
import xyz.vsngamer.elevatorid.tile.ElevatorTileEntity;

import java.util.EnumMap;

public class Registry {
    public static final EnumMap<DyeColor, RegistryObject<ElevatorBlock>> ELEVATOR_BLOCKS = new EnumMap<>(DyeColor.class);
    public static final EnumMap<DyeColor, RegistryObject<BlockItem>> ELEVATOR_ITEMS = new EnumMap<>(DyeColor.class);
    public static RegistryObject<BlockEntityType<ElevatorTileEntity>> ELEVATOR_TILE_ENTITY;
    public static RegistryObject<MenuType<ElevatorContainer>> ELEVATOR_CONTAINER;
    public static RegistryObject<SoundEvent> TELEPORT_SOUND;
    public static RegistryObject<SoundEvent> CAMOUFLAGE_SOUND;

    private static ResourceLocation id(String path) {
        return new ResourceLocation(ElevatorMod.ID, path);
    }

    private static <V, T extends V> RegistryObject<T> register(net.minecraft.core.Registry<V> registry, String path, T value) {
        return new RegistryObject<>(net.minecraft.core.Registry.register(registry, id(path), value));
    }

    public static void init() {
        for (DyeColor color : DyeColor.values()) {
            ELEVATOR_BLOCKS.put(color, register(BuiltInRegistries.BLOCK, "elevator_" + color.getName(), new ElevatorBlock(color)));
        }
        ELEVATOR_BLOCKS.forEach((color, block) ->
                ELEVATOR_ITEMS.put(color, register(BuiltInRegistries.ITEM, "elevator_" + color.getName(), new BlockItem(block.get(), new Item.Properties()))));

        ELEVATOR_TILE_ENTITY = register(BuiltInRegistries.BLOCK_ENTITY_TYPE, "elevator_tile",
                FabricBlockEntityTypeBuilder.create(ElevatorTileEntity::new,
                        ELEVATOR_BLOCKS.values().stream().map(RegistryObject::get).toArray(Block[]::new)).build());

        ELEVATOR_CONTAINER = register(BuiltInRegistries.MENU, "elevator_container",
                new ExtendedScreenHandlerType<>((windowId, inv, data) -> new ElevatorContainer(windowId, data.readBlockPos(), inv.player)));

        TELEPORT_SOUND = register(BuiltInRegistries.SOUND_EVENT, "teleport", SoundEvent.createVariableRangeEvent(id("teleport")));
        CAMOUFLAGE_SOUND = register(BuiltInRegistries.SOUND_EVENT, "camouflage", SoundEvent.createVariableRangeEvent(id("camouflage")));

        CreativeModeTab tab = FabricItemGroup.builder()
                .icon(() -> ELEVATOR_ITEMS.get(DyeColor.WHITE).get().getDefaultInstance())
                .displayItems((params, output) -> ELEVATOR_ITEMS.values().forEach(item -> output.accept(item.get())))
                .title(Component.translatable("itemGroup.elevators_tab"))
                .build();
        net.minecraft.core.Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB, id("elevators_tab"), tab);
    }
}
