package me.paulf.fairylights.server.fastener.connection;

import me.paulf.fairylights.server.fastener.connection.type.Connection;
import me.paulf.fairylights.util.CatenaryUtils;
import me.paulf.fairylights.util.CubicBezier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

import java.util.NoSuchElementException;

public final class Catenary {
    private static final int MIN_VERTEX_COUNT = 8;

    private final int count;

    private final float yaw;

    private final float dx;

    private final float dz;

    private final float[] x;

    private final float[] y;

    private final float length;

    private Catenary(final int count, final float yaw, final float dx, final float dz, final float[] x, final float[] y, final float length) {
        this.count = count;
        this.yaw = yaw;
        this.dx = dx;
        this.dz = dz;
        this.x = x;
        this.y = y;
        this.length = length;
    }

    public int getCount() {
        return this.count;
    }

    public Vec3d getEnd() {
        return new Vec3d(this.x[this.count - 1] * this.dx, this.y[this.count - 1], this.x[this.count - 1] * this.dz);
    }

    public SegmentIterator iterator() {
        return this.iterator(false);
    }

    public SegmentIterator iterator(final boolean inclusive) {
        return new SegmentIterator() {
            private int index = -1;

            @Override
            public boolean next() {
                final int nextIndex = this.index + 1;
                if (inclusive ? nextIndex > Catenary.this.count : nextIndex >= Catenary.this.count) {
                    throw new NoSuchElementException();
                }
                this.index = nextIndex;
                return nextIndex + (inclusive ? 0 : 1) < Catenary.this.count;
            }

            private void checkIndex(final float t) {
                if (this.index + (inclusive && t == 0.0F ? 0 : 1) >= Catenary.this.count) {
                    throw new IllegalStateException();
                }
            }

            @Override
            public float getX(final float t) {
                this.checkIndex(t);
                if (t == 0.0F) {
                    return Catenary.this.x[this.index] * Catenary.this.dx;
                }
                if (t == 1.0F) {
                    return Catenary.this.x[this.index + 1] * Catenary.this.dx;
                }
                return MathHelper.lerp(t, Catenary.this.x[this.index], Catenary.this.x[this.index + 1]) * Catenary.this.dx;
            }

            @Override
            public float getY(final float t) {
                this.checkIndex(t);
                if (t == 0.0F) {
                    return Catenary.this.y[this.index];
                }
                if (t == 1.0F) {
                    return Catenary.this.y[this.index + 1];
                }
                return MathHelper.lerp(t, Catenary.this.y[this.index], Catenary.this.y[this.index + 1]);
            }

            @Override
            public float getZ(final float t) {
                this.checkIndex(t);
                if (t == 0.0F) {
                    return Catenary.this.x[this.index] * Catenary.this.dz;
                }
                if (t == 1.0F) {
                    return Catenary.this.x[this.index + 1] * Catenary.this.dz;
                }
                return MathHelper.lerp(t, Catenary.this.x[this.index], Catenary.this.x[this.index + 1]) * Catenary.this.dz;
            }

            @Override
            public Vec3d getPos() {
                return new Vec3d(Catenary.this.x[this.index] * Catenary.this.dx, Catenary.this.y[this.index], Catenary.this.x[this.index] * Catenary.this.dz);
            }

            @Override
            public float getYaw() {
                return Catenary.this.yaw;
            }

            @Override
            public float getPitch() {
                this.checkIndex(1.0F);
                if (inclusive) {
                    throw new IllegalStateException();
                }
                final float dx = Catenary.this.x[this.index + 1] - Catenary.this.x[this.index];
                final float dy = Catenary.this.y[this.index + 1] - Catenary.this.y[this.index];
                return (float) MathHelper.atan2(dy, dx);
            }

            @Override
            public float getLength() {
                this.checkIndex(1.0F);
                if (inclusive) {
                    throw new IllegalStateException();
                }
                final float dx = Catenary.this.x[this.index + 1] - Catenary.this.x[this.index];
                final float dy = Catenary.this.y[this.index + 1] - Catenary.this.y[this.index];
                return MathHelper.sqrt(dx * dx + dy * dy);
            }
        };
    }

    public interface SegmentIterator {
        boolean next();

        float getX(final float t);

        float getY(final float t);

        float getZ(final float t);

        Vec3d getPos();

        float getYaw();

        float getPitch();

        float getLength();
    }

    public float getLength() {
        return this.length;
    }

    public static Catenary from(final Vec3d direction, final CubicBezier bezier, final float slack) {
        final float dist = (float) direction.length();
        final float length;
        if (slack < 1e-2 || Math.abs(direction.x) < 1e-6 && Math.abs(direction.z) < 1e-6) {
            length = dist;
        } else {
            length = dist + (lengthFunc(bezier, dist) - dist) * slack;
        }
        return from(direction, length);
    }

    private static float lengthFunc(final CubicBezier bezier, final double length) {
        return bezier.eval(MathHelper.clamp((float) length / Connection.MAX_LENGTH, 0, 1)) * Connection.MAX_LENGTH;
    }

    public static Catenary from(final Vec3d dir, final float ropeLength) {
        final float angle = (float) MathHelper.atan2(dir.z, dir.x);
        final float endX = MathHelper.sqrt(dir.x * dir.x + dir.z * dir.z);
        final float endY = (float) dir.y;
        final float vx = MathHelper.cos(angle);
        final float vz = MathHelper.sin(angle);
        if (dir.length() > 2.0F * Connection.MAX_LENGTH) {
            return new Catenary(2, angle, vx, vz, new float[] { 0.0F, endX }, new float[] { 0.0F, endY }, MathHelper.sqrt(endX * endX + endY * endY));
        }
        final int count = Math.max((int) (ropeLength * CatenaryUtils.SEG_LENGTH), MIN_VERTEX_COUNT);
        final float[] x = new float[count];
        final float[] y = new float[count];
        CatenaryUtils.catenary(0.0F, 0.0F, endX, endY, ropeLength, count, x, y);
        float length = 0.0F;
        for (int i = 1; i < count; i++) {
            final float dx = x[i] - x[i - 1];
            final float dy = y[i] - y[i - 1];
            length += MathHelper.sqrt(dx * dx + dy * dy);
        }
        return new Catenary(count, angle, vx, vz, x, y, length);
    }
}