package me.paulf.fairylights.client.model.light;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import me.paulf.fairylights.server.feature.light.Light;
import me.paulf.fairylights.server.feature.light.MultiLightBehavior;
import net.minecraft.client.model.geom.ModelPart;

import java.util.ArrayList;
import java.util.List;

import me.paulf.fairylights.client.model.light.LightModel.BulbBuilder;

public class IcicleLightsModel extends LightModel<MultiLightBehavior> {
    private final List<ColorLightModel> bulbs;

    public IcicleLightsModel(final int lights) {
        final ModelPart connector = new ModelPart(this, 77, 0);
        connector.setRotationPoint(0, 0, 0);
        connector.addBox(-1, -0.5F, -1, 2, 2, 2, -0.05F);
        this.unlit.addChild(connector);

        this.bulbs = new ArrayList<>();

        ModelRenderer wire1 = null;
        if (lights > 0) {
            wire1 = new ModelRenderer(this, 29, 76);
            wire1.setRotationPoint(0, -0.5F, 0);
            wire1.addBox(-0.5F, 0, -0.5F, 1, 4, 1, 0);
            wire1.rotateAngleX = -3.0543261909900767F;
            connector.addChild(wire1);
            final ModelRenderer lightBase1 = new ModelRenderer(this, 33, 76);
            lightBase1.setRotationPoint(0, 2, 0.5F);
            lightBase1.addBox(-0.5F, 0, 0, 1, 1, 1, 0);
            wire1.addChild(lightBase1);
            final ColorLightModel model = new ColorLightModel();
            final BulbBuilder light1 = model.createBulb().createChild(29, 72);
            light1.setPosition(0, -2.405233653435833F, -1.170506183587062F);
            light1.addBox(-1, -0.5F, 0, 2, 2, 2, 0);
            light1.setAngles(-3.0543261909900767F, 0.0F, 0.0F);
            this.bulbs.add(model);
        }

        ModelRenderer wire2 = null;
        if (lights > 1) {
            wire2 = new ModelRenderer(this, 29, 76);
            wire2.setRotationPoint(0, 4, 0);
            wire2.addBox(-0.5F, 0, -0.5F, 1, 4, 1, 0);
            wire2.rotateAngleX = -0.2617993877991494F;
            wire2.rotateAngleY = 0.5235987755982988F;
            wire1.addChild(wire2);
            final ModelRenderer lightBase2 = new ModelRenderer(this, 33, 76);
            lightBase2.setRotationPoint(0, 2, -1.5F);
            lightBase2.addBox(-0.5F, 0, 0, 1, 1, 1, 0);
            wire2.addChild(lightBase2);
            final ColorLightModel model = new ColorLightModel();
            final BulbBuilder light2 = model.createBulb().createChild(29, 72);
            light2.setPosition(-1.7077077845361228F, -5.893569134597652F, 2.4972589475492635F);
            light2.addBox(-1, -0.5F, 0, 2, 2, 2, 0);
            light2.setAngles(2.9804748914277273F, -0.5214031733599432F, -0.050276985685263745F);
            this.bulbs.add(model);
        }

        ModelRenderer wire3 = null;
        if (lights > 2) {
            wire3 = new ModelRenderer(this, 29, 76);
            wire3.setRotationPoint(0, 4, 0);
            wire3.addBox(-0.5F, 0, -0.5F, 1, 4, 1, 0);
            wire3.rotateAngleX = 0.4363323129985824F;
            wire3.rotateAngleY = 0.5235987755982988F;
            wire2.addChild(wire3);
            final ModelRenderer lightBase3 = new ModelRenderer(this, 33, 76);
            lightBase3.setRotationPoint(0, 2, 0.5F);
            lightBase3.addBox(-0.5F, 0, 0, 1, 1, 1, 0);
            wire3.addChild(lightBase3);
            final ColorLightModel model = new ColorLightModel();
            final BulbBuilder light3 = model.createBulb().createChild(29, 72);
            light3.setPosition(0.7935216418735993F, -10.095277243516536F, -0.4609129179470893F);
            light3.addBox(-1, -0.5F, 0, 2, 2, 2, 0);
            light3.setAngles(-2.9807093751793796F, -1.0339196228641108F, 0.10720187699072795F);
            this.bulbs.add(model);
        }

        if (lights > 3) {
            final ModelRenderer wire4 = new ModelRenderer(this, 29, 76);
            wire4.setRotationPoint(0.0F, 4.0F, 0.0F);
            wire4.addBox(-0.5F, 0.0F, -0.5F, 1, 4, 1, 0.0F);
            wire4.rotateAngleX = -0.4363323129985824F;
            wire4.rotateAngleY = 0.7853981633974483F;
            wire3.addChild(wire4);
            final ModelRenderer lightBase4 = new ModelRenderer(this, 33, 76);
            lightBase4.setRotationPoint(0.0F, 2.0F, -1.5F);
            lightBase4.addBox(-0.5F, 0.0F, 0.0F, 1, 1, 1, 0.0F);
            wire4.addChild(lightBase4);
            final ColorLightModel model = new ColorLightModel();
            final BulbBuilder light4 = model.createBulb().createChild(29, 72);
            light4.setPosition(-2.4652688758772636F, -12.95165172579971F, -1.9323986317282649F);
            light4.addBox(0.0F, 0.0F, 0.0F, 2, 2, 2, 0.0F);
            light4.setAngles(-0.7522647962292457F, -1.3039482923151255F, -2.5903859234812483F);
            this.bulbs.add(model);
        }
    }

    @Override
    public void animate(final Light<?> light, final MultiLightBehavior behavior, final float delta) {
        for (int i = 0; i < this.bulbs.size(); i++) {
            this.bulbs.get(i).animate(light, behavior.get(i), delta);
        }
    }

    @Override
    public void render(final MatrixStack matrix, final IVertexBuilder builder, final int light, final int overlay, final float r, final float g, final float b, final float a) {
        super.render(matrix, builder, light, overlay, r, g, b, a);
        for (final ColorLightModel bulb : this.bulbs) {
            bulb.render(matrix, builder, light, overlay, r, g, b, a);
        }
    }

    @Override
    public void renderTranslucent(final MatrixStack matrix, final IVertexBuilder builder, final int light, final int overlay, final float r, final float g, final float b, final float a) {
        super.renderTranslucent(matrix, builder, light, overlay, r, g, b, a);
        for (final ColorLightModel bulb : this.bulbs) {
            bulb.renderTranslucent(matrix, builder, light, overlay, r, g, b, a);
        }
    }
}
