package xyz.vsngamer.elevatorid.client.render;

import net.fabricmc.fabric.api.client.model.BakedModelManagerHelper;
import net.fabricmc.fabric.api.renderer.v1.RendererAccess;
import net.fabricmc.fabric.api.renderer.v1.material.BlendMode;
import net.fabricmc.fabric.api.renderer.v1.material.RenderMaterial;
import net.fabricmc.fabric.api.renderer.v1.model.FabricBakedModel;
import net.fabricmc.fabric.api.renderer.v1.model.ForwardingBakedModel;
import net.fabricmc.fabric.api.renderer.v1.render.RenderContext;
import net.fabricmc.fabric.api.rendering.data.v1.RenderAttachedBlockView;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;
import xyz.vsngamer.elevatorid.blocks.ElevatorBlock;
import xyz.vsngamer.elevatorid.client.ClientRegistry;

import java.util.function.Supplier;

/**
 * Antes un BakedModelWrapper de Forge con ModelData; ahora un modelo de Fabric Rendering API:
 * dibuja el bloque de camuflaje (dato del block entity) y la flecha girada si es direccional.
 */
public class ElevatorBakedModel extends ForwardingBakedModel {

    public ElevatorBakedModel(BakedModel originalModel) {
        this.wrapped = originalModel;
    }

    @Override
    public boolean isVanillaAdapter() {
        return false;
    }

    private static RenderMaterial material(BlendMode mode) {
        return RendererAccess.INSTANCE.getRenderer().materialFinder().blendMode(0, mode).find();
    }

    @Override
    public void emitBlockQuads(BlockAndTintGetter blockView, BlockState state, BlockPos pos, Supplier<RandomSource> randomSupplier, RenderContext context) {
        Minecraft mc = Minecraft.getInstance();

        // Flecha de los ascensores direccionales
        if (state.getValue(ElevatorBlock.DIRECTIONAL) && state.getValue(ElevatorBlock.SHOW_ARROW)) {
            BakedModel arrow = BakedModelManagerHelper.getModel(mc.getModelManager(), ClientRegistry.ARROW);
            if (arrow != null) {
                float angle = (float) Math.toRadians(-state.getValue(ElevatorBlock.FACING).toYRot());
                RenderMaterial cutout = material(BlendMode.CUTOUT_MIPPED);
                context.pushTransform(quad -> {
                    Vector3f v = new Vector3f();
                    for (int i = 0; i < 4; i++) {
                        quad.copyPos(i, v);
                        v.sub(.5f, 0, .5f).rotateY(angle).add(.5f, 0, .5f);
                        quad.pos(i, v);
                    }
                    quad.material(cutout);
                    return true;
                });
                ((FabricBakedModel) arrow).emitBlockQuads(blockView, state, pos, randomSupplier, context);
                context.popTransform();
            }
        }

        // Camuflaje
        Object data = ((RenderAttachedBlockView) blockView).getBlockEntityRenderAttachment(pos);
        if (data instanceof BlockState heldState) {
            BakedModel model = mc.getBlockRenderer().getBlockModel(heldState);
            RenderMaterial mat = material(BlendMode.fromRenderLayer(ItemBlockRenderTypes.getChunkRenderType(heldState)));
            context.pushTransform(quad -> {
                quad.material(mat);
                return true;
            });
            ((FabricBakedModel) model).emitBlockQuads(blockView, heldState, pos, randomSupplier, context);
            context.popTransform();
            return;
        }

        super.emitBlockQuads(blockView, state, pos, randomSupplier, context);
    }
}
