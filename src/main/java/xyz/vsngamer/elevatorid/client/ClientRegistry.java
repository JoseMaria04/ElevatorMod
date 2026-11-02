package xyz.vsngamer.elevatorid.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.blockrenderlayer.v1.BlockRenderLayerMap;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;
import net.fabricmc.fabric.api.client.rendering.v1.ColorProviderRegistry;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import xyz.vsngamer.elevatorid.ElevatorHandler;
import xyz.vsngamer.elevatorid.ElevatorMod;
import xyz.vsngamer.elevatorid.client.gui.ElevatorScreen;
import xyz.vsngamer.elevatorid.client.render.ColorCamoElevator;
import xyz.vsngamer.elevatorid.client.render.ElevatorBakedModel;
import xyz.vsngamer.elevatorid.init.Registry;
import xyz.vsngamer.elevatorid.init.RegistryObject;

/** Antes eran eventos de cliente de Forge (FMLClientSetup, ModelEvent, RegisterColorHandlers, InputEvent). */
public class ClientRegistry implements ClientModInitializer {
    public static final ResourceLocation ARROW = new ResourceLocation(ElevatorMod.ID, "arrow");

    @Override
    public void onInitializeClient() {
        MenuScreens.register(Registry.ELEVATOR_CONTAINER.get(), ElevatorScreen::new);

        Block[] blocks = Registry.ELEVATOR_BLOCKS.values().stream().map(RegistryObject::get).toArray(Block[]::new);
        ColorProviderRegistry.BLOCK.register(new ColorCamoElevator(), blocks);
        BlockRenderLayerMap.INSTANCE.putBlocks(RenderType.cutoutMipped(), blocks);

        ClientTickEvents.END_CLIENT_TICK.register(client -> ElevatorHandler.onInput());

        ModelLoadingPlugin.register(ctx -> {
            ctx.addModels(ARROW);
            ctx.modifyModelAfterBake().register((model, context) -> {
                ResourceLocation id = context.id();
                if (model == null || !(id instanceof ModelResourceLocation mrl) || "inventory".equals(mrl.getVariant())) {
                    return model;
                }
                if (ElevatorMod.ID.equals(id.getNamespace()) && id.getPath().startsWith("elevator_")) {
                    return new ElevatorBakedModel(model);
                }
                return model;
            });
        });
    }
}
