package me.paulf.fairylights.server.feature.light;

import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class TorchLightBehavior implements BrightnessLightBehavior {
    private final double offset;

    private float value = 1.0F;

    private boolean powered = true;

    public TorchLightBehavior(final double offset) {
        this.offset = offset;
    }

    @Override
    public float getBrightness(final float delta) {
        return this.value;
    }

    @Override
    public void power(final boolean powered, final boolean now, final Light<?> light) {
        this.powered = powered;
        this.value = this.powered ? 1.0F : 0.0F;
    }

    @Override
    public void tick(final Level world, final Vec3 origin, final Light<?> light) {
        if (world.rand.nextFloat() < 0.08F) {
            this.createParticles(world, origin, light);
        }
    }

    @Override
    public void animateTick(final World world, final Vector3d origin, final Light<?> light) {
        this.createParticles(world, origin, light);
    }

    private void createParticles(final World world, final Vector3d origin, final Light<?> light) {
        if (this.powered) {
            final Vector3d p = light.getTransformedPoint(origin, new Vector3d(0.0D, -this.offset, 0.0D));
            final double x = p.getX();
            final double y = p.getY();
            final double z = p.getZ();
            world.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
            world.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);
        }
    }
}
