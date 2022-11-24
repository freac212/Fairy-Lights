package me.paulf.fairylights.server.entity;

import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.ByteBufOutputStream;
import me.paulf.fairylights.server.ServerProxy;
import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.item.ConnectionItem;
import me.paulf.fairylights.server.net.clientbound.UpdateEntityFastenerMessage;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.decoration.HangingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.IPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.core.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fml.common.registry.IEntityAdditionalSpawnData;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.io.IOException;

public final class FenceFastenerEntity extends HangingEntity implements IEntityAdditionalSpawnData {
    private int surfaceCheckTime;

    public FenceFastenerEntity(final EntityType<? extends FenceFastenerEntity> type, final Level world) {
        super(type, world);
    }

    public FenceFastenerEntity(final Level world) {
        this(FLEntities.FASTENER.get(), world);
    }

    public FenceFastenerEntity(final Level world, final BlockPos pos) {
        this(world);
        this.setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public int getWidthPixels() {
        return 9;
    }

    @Override
    public int getHeightPixels() {
        return 9;
    }

    @Override
    public float getEyeHeight(final Pose pose, final EntityDimensions size) {
        /*
         * Because this entity is inside of a block when
         * EntityLivingBase#canEntityBeSeen performs its
         * raytracing it will always return false during
         * NetHandlerPlayServer#processUseEntity, making
         * the player reach distance be limited at three
         * blocks as opposed to the standard six blocks.
         * EntityLivingBase#canEntityBeSeen will add the
         * value given by getEyeHeight to the y position
         * of the entity to calculate the end point from
         * which to raytrace to. Returning one lets most
         * interactions with a player succeed, typically
         * for breaking the connection or creating a new
         * connection. I hope you enjoy my line lengths.
         */
        return 1;
    }

    @SuppressWarnings("deprecation")
    @Override
    public float getBrightness() {
        final BlockPos pos = this.getPosition();
        if (this.world.isBlockPresent(pos)) {
            return this.world.getBrightness(pos);
        }
        return 0;
    }

    @Override
    public boolean isInRangeToRenderDist(final double distance) {
        return distance < 4096;
    }

    @Override
    public boolean isImmuneToExplosions() {
        return true;
    }

    @Override
    public boolean onValidSurface() {
        return !this.world.isBlockPresent(this.hangingPosition) || ConnectionItem.isFence(this.world.getBlockState(this.hangingPosition));
    }

    @Override
    public void remove() {
        this.getFastener().ifPresent(Fastener::remove);
        super.remove();
    }

    // Copy from super but remove() moved to after onBroken()
    @Override
    public boolean attackEntityFrom(final DamageSource source, final float amount) {
        if (this.isInvulnerableTo(source)) {
            return false;
        }
        if (!this.world.isRemote && this.isAlive()) {
            this.markVelocityChanged();
            this.onBroken(source.getTrueSource());
            this.remove();
        }
        return true;
    }

    @Override
    public boolean isNonBoss() {
        return false;
    }

    @Override
    public void onBroken(@Nullable final Entity breaker) {
        this.getFastener().ifPresent(fastener -> fastener.dropItems(this.world, this.hangingPosition));
        if (breaker != null) {
            this.world.playEvent(2001, this.hangingPosition, Block.getStateId(FLBlocks.FASTENER.get().getDefaultState()));
        }
    }

    @Override
    public void playPlaceSound() {
        final SoundType sound = FLBlocks.FASTENER.get().getSoundType(FLBlocks.FASTENER.get().getDefaultState(), this.world, this.getHangingPosition(), null);
        this.playSound(sound.getPlaceSound(), (sound.getVolume() + 1) / 2, sound.getPitch() * 0.8F);
    }

    @Override
    public SoundCategory getSoundCategory() {
        return SoundCategory.BLOCKS;
    }

    @Override
    public void setPosition(final double x, final double y, final double z) {
        super.setPosition(MathHelper.floor(x) + 0.5, MathHelper.floor(y) + 0.5, MathHelper.floor(z) + 0.5);
    }

    @Override
    public void updateFacingWithBoundingBox(final Direction facing) {}

    @Override
    protected void updateBoundingBox() {
        final double posX = this.hangingPosition.getX() + 0.5;
        final double posY = this.hangingPosition.getY() + 0.5;
        final double posZ = this.hangingPosition.getZ() + 0.5;
        this.setRawPosition(posX, posY, posZ);
        final float w = 3 / 16F;
        final float h = 3 / 16F;
        this.setBoundingBox(new AxisAlignedBB(posX - w, posY - h, posZ - w, posX + w, posY + h, posZ + w));
    }

    @Override
    public AxisAlignedBB getRenderBoundingBox() {
        return this.getFastener().map(fastener -> fastener.getBounds().grow(1)).orElseGet(super::getRenderBoundingBox);
    }

    @Override
    public void tick() {
        this.getFastener().ifPresent(fastener -> {
            if (!this.world.isRemote && (fastener.hasNoConnections() || this.checkSurface())) {
                this.onBroken(null);
                this.remove();
            } else if (fastener.update() && !this.world.isRemote) {
                final UpdateEntityFastenerMessage msg = new UpdateEntityFastenerMessage(this, fastener.serializeNBT());
                ServerProxy.sendToPlayersWatchingEntity(msg, this);
            }
        });
    }

    private boolean checkSurface() {
        if (this.surfaceCheckTime++ == 100) {
            this.surfaceCheckTime = 0;
            return !this.onValidSurface();
        }
        return false;
    }

    @Override
    public ActionResultType processInitialInteract(final PlayerEntity player, final Hand hand) {
        final ItemStack stack = player.getHeldItem(hand);
        if (stack.getItem() instanceof ConnectionItem) {
            if (this.world.isRemote) {
                player.swingArm(hand);
            } else {
                this.getFastener().ifPresent(fastener -> ((ConnectionItem) stack.getItem()).connect(stack, player, this.world, fastener));
            }
            return ActionResultType.SUCCESS;
        }
        return super.processInitialInteract(player, hand);
    }

    @Override
    public void writeAdditional(final CompoundNBT compound) {
        compound.put("pos", NBTUtil.writeBlockPos(this.hangingPosition));
    }

    @Override
    public void readAdditional(final CompoundNBT compound) {
        this.hangingPosition = NBTUtil.readBlockPos(compound.getCompound("pos"));
    }

    @Override
    public void writeSpawnData(final PacketBuffer buf) {
        this.getFastener().ifPresent(fastener -> {
            try {
                CompressedStreamTools.write(fastener.serializeNBT(), new ByteBufOutputStream(buf));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void readSpawnData(final PacketBuffer buf) {
        this.getFastener().ifPresent(fastener -> {
            try {
                fastener.deserializeNBT(CompressedStreamTools.read(new ByteBufInputStream(buf), new NBTSizeTracker(0x200000)));
            } catch (final IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private LazyOptional<Fastener<?>> getFastener() {
        return this.getCapability(CapabilityHandler.FASTENER_CAP);
    }

    public static FenceFastenerEntity create(final World world, final BlockPos fence) {
        final FenceFastenerEntity fastener = new FenceFastenerEntity(world, fence);
        fastener.forceSpawn = true;
        world.addEntity(fastener);
        fastener.playPlaceSound();
        return fastener;
    }

    @Nullable
    public static FenceFastenerEntity find(final World world, final BlockPos pos) {
        final HangingEntity entity = findHanging(world, pos);
        if (entity instanceof FenceFastenerEntity) {
            return (FenceFastenerEntity) entity;
        }
        return null;
    }

    @Nullable
    public static HangingEntity findHanging(final World world, final BlockPos pos) {
        for (final HangingEntity e : world.getEntitiesWithinAABB(HangingEntity.class, new AxisAlignedBB(pos).grow(2))) {
            if (e.getHangingPosition().equals(pos)) {
                return e;
            }
        }
        return null;
    }
}
