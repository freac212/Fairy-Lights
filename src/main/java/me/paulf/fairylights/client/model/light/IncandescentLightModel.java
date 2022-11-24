package me.paulf.fairylights.client.model.light;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.paulf.fairylights.server.feature.light.BrightnessLightBehavior;
import me.paulf.fairylights.server.feature.light.Light;
import net.minecraft.client.model.geom.ModelPart;

public class IncandescentLightModel extends LightModel<BrightnessLightBehavior> {
    final ModelPart bulb;

    final ModelPart filament;

    public IncandescentLightModel() {
        this.unlit.setTextureOffset(90, 10);
        this.unlit.addBox(-1.0F, -0.01F, -1.0F, 2.0F, 1.0F, 2.0F);
        this.bulb = new ModelRenderer(this, 98, 10);
        this.bulb.addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F);
        this.filament = new ModelRenderer(this, 90, 13);
        this.filament.addBox(-1.0F, -3.0F, 0.0F, 2.0F, 3.0F, 0.0F);
    }

    @Override
    public void animate(final Light<?> light, final BrightnessLightBehavior behavior, final float delta) {
        super.animate(light, behavior, delta);
        this.brightness = behavior.getBrightness(delta);
    }

    @Override
    protected int getLight(final int packedLight) {
        return (int) Math.max((this.brightness * 15.0F * 16.0F), packedLight & 255) | packedLight & (255 << 16);
    }

    @Override
    public void render(final MatrixStack matrix, final IVertexBuilder builder, final int light, final int overlay, final float r, final float g, final float b, final float a) {
        super.render(matrix, builder, light, overlay, r, g, b, a);
        final int emissiveLight = this.getLight(light);
        final float cr = 0.23F, cg = 0.18F, cb = 0.14F;
        final float br = this.brightness;
        this.filament.render(matrix, builder, emissiveLight, overlay, r * (cr * (1.0F - br) + br), g * (cg * (1.0F - br) + br), b * (cb * (1.0F - br) + br), a);
    }

    @Override
    public void renderTranslucent(final MatrixStack matrix, final IVertexBuilder builder, final int light, final int overlay, final float r, final float g, final float b, final float a) {
        final float bi = this.brightness;
        final int emissiveLight = this.getLight(light);
        final float br = 1.0F, bg = 0.73F, bb = 0.3F;
        this.bulb.render(matrix, builder, emissiveLight, overlay, r * (br * bi + (1.0F - bi)), g * (bg * bi + (1.0F - bi)), b * (bb * bi + (1.0F - bi)), bi * 0.4F + 0.25F);
        super.renderTranslucent(matrix, builder, light, overlay, r, g, b, a);
    }
}
