package me.paulf.fairylights.server.block.entity;

import me.paulf.fairylights.server.block.FLBlocks;
import me.paulf.fairylights.server.block.FastenerBlock;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.fastener.Fastener;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.util.LazyOptional;

public final class FastenerBlockEntity extends BlockEntity implements TickableBlockEntity {
    public FastenerBlockEntity() {
        super(FLBlockEntities.FASTENER.get());
    }

    @Override
    public AABB getRenderBoundingBox() {
        return this.getFastener().map(fastener -> fastener.getBounds().grow(1)).orElseGet(super::getRenderBoundingBox);
    }

    public Vector3d getOffset() {
        return FLBlocks.FASTENER.get().getOffset(this.getFacing(), 0.125F);
    }

    public Direction getFacing() {
        final BlockState state = this.world.getBlockState(this.pos);
        if (state.getBlock() != FLBlocks.FASTENER.get()) {
            return Direction.UP;
        }
        return state.get(FastenerBlock.FACING);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(this.pos, 0, this.getUpdateTag());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void onDataPacket(final NetworkManager net, final SUpdateTileEntityPacket pkt) {
        this.read(this.world.getBlockState(pkt.getPos()), pkt.getNbtCompound());
    }

    @Override
    public void setWorldAndPos(final World world, final BlockPos pos) {
        super.setWorldAndPos(world, pos);
        this.getFastener().ifPresent(fastener -> fastener.setWorld(world));
    }

    @Override
    public void tick() {
        this.getFastener().ifPresent(fastener -> {
            if (!this.world.isRemote && fastener.hasNoConnections()) {
                this.world.removeBlock(this.pos, false);
            } else if (!this.world.isRemote && fastener.update()) {
                this.markDirty();
                final BlockState state = this.world.getBlockState(this.pos);
                this.world.notifyBlockUpdate(this.pos, state, state, 3);
            }
        });
    }

    @Override
    public void remove() {
        this.getFastener().ifPresent(Fastener::remove);
        super.remove();
    }

    private LazyOptional<Fastener<?>> getFastener() {
        return this.getCapability(CapabilityHandler.FASTENER_CAP);
    }
}
