package me.paulf.fairylights.client.model.light;

import me.paulf.fairylights.util.Mth;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3f;

import me.paulf.fairylights.client.model.light.LightModel.BulbBuilder;

public class FlowerLightModel extends ColorLightModel {
    public FlowerLightModel() {
        this.unlit.setTextureOffset(12, 0);
        this.unlit.addBox(-1.5F, -1.0F, -1.5F, 3.0F, 3.0F, 3.0F);
        final BulbBuilder bulb = this.createBulb();
        final Vector3f vec = new Vector3f(-1.0F, 0.0F, 1.0F);
        vec.normalize();
        final Quaternion droop = vec.rotation(-Mth.PI / 6.0F);
        final int peddleCount = 5;
        for (int p = 0; p < peddleCount; p++) {
            final Quaternion q = Vector3f.YP.rotation(p * Mth.TAU / peddleCount);
            q.multiply(droop);
            final float[] magicAngles = toEuler(q);
            final BulbBuilder peddleModel = bulb.createChild(24, 0);
            peddleModel.addBox(0.0F, 0.0F, 0.0F, 5.0F, 1.0F, 5.0F);
            peddleModel.setPosition(0.0F, 1.0F, 0.0F);
            peddleModel.setAngles(magicAngles[0], magicAngles[1], magicAngles[2]);
        }
    }
}
