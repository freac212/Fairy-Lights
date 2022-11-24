package me.paulf.fairylights.client.renderer.block.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import me.paulf.fairylights.server.block.entity.FastenerBlockEntity;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.fastener.BlockView;
import me.paulf.fairylights.util.matrix.Matrix;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.util.math.vector.Vector3d;

public final class FastenerBlockEntityRenderer extends BlockEntityRenderer<FastenerBlockEntity> {
    private final BlockView view;

    public FastenerBlockEntityRenderer(final BlockEntityRenderDispatcher dispatcher, final BlockView view) {
        super(dispatcher);
        this.view = view;
    }

    private final FastenerRenderer renderer = new FastenerRenderer();

    @Override
    public boolean isGlobalRenderer(final FastenerBlockEntity fastener) {
        return true;
    }

    @Override
    public void render(final FastenerBlockEntity fastener, final float delta, final PoseStack matrix, final MultiBufferSource bufferSource, final int packedLight, final int packedOverlay) {
        fastener.getCapability(CapabilityHandler.FASTENER_CAP).ifPresent(f -> {
            //this.bindTexture(FastenerRenderer.TEXTURE);
            matrix.push();
            final Vector3d offset = fastener.getOffset();
            matrix.translate(offset.x, offset.y, offset.z);
            //this.view.unrotate(this.getWorld(), f.getPos(), FastenerBlockEntityRenderer.GlMatrix.INSTANCE, delta);
            this.renderer.render(f, delta, matrix, bufferSource, packedLight, packedOverlay);
            matrix.pop();
        });
    }

    static class GlMatrix implements Matrix {
        static final FastenerBlockEntityRenderer.GlMatrix INSTANCE = new FastenerBlockEntityRenderer.GlMatrix();

        @Override
        public void translate(final float x, final float y, final float z) {
            RenderSystem.translatef(x, y, z);
        }

        @Override
        public void rotate(final float angle, final float x, final float y, final float z) {
            RenderSystem.rotatef(angle, x, y, z);
        }
    }
}
