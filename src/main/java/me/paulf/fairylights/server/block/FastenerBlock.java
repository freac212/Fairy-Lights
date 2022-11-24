package me.paulf.fairylights.server.block;

import me.paulf.fairylights.server.ServerEventHandler;
import me.paulf.fairylights.server.block.entity.FastenerBlockEntity;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.connection.HangingLightsConnection;
import me.paulf.fairylights.server.fastener.accessor.BlockFastenerAccessor;
import me.paulf.fairylights.server.jingle.JingleLibrary;
import net.minecraft.world.level.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.stream.Stream;

import net.minecraft.block.AbstractBlock;

public final class FastenerBlock extends DirectionalBlock {
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;

    private static final VoxelShape NORTH_AABB = Block.makeCuboidShape(6.0D, 6.0D, 12.0D, 10.0D, 10.0D, 16.0D);

    private static final VoxelShape SOUTH_AABB = Block.makeCuboidShape(6.0D, 6.0D, 0.0D, 10.0D, 10.0D, 4.0D);

    private static final VoxelShape WEST_AABB = Block.makeCuboidShape(12.0D, 6.0D, 6.0D, 16.0D, 10.0D, 10.0D);

    private static final VoxelShape EAST_AABB = Block.makeCuboidShape(0.0D, 6.0D, 6.0D, 4.0D, 10.0D, 10.0D);

    private static final VoxelShape DOWN_AABB = Block.makeCuboidShape(6.0D, 12.0D, 6.0D, 10.0D, 16.0D, 10.0D);

    private static final VoxelShape UP_AABB = Block.makeCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D);

    public FastenerBlock(final AbstractBlock.Properties properties) {
        super(properties);
        this.setDefaultState(this.stateContainer.getBaseState()
            .with(FACING, Direction.NORTH)
            .with(TRIGGERED, false)
        );
    }

    @Override
    protected void fillStateContainer(final StateContainer.Builder<Block, BlockState> builder) {
        builder.add(FACING, TRIGGERED);
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState rotate(final BlockState state, final Rotation rot) {
        return state.with(FACING, rot.rotate(state.get(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState mirror(final BlockState state, final Mirror mirrorIn) {
        return state.with(FACING, mirrorIn.mirror(state.get(FACING)));
    }

    @SuppressWarnings("deprecation")
    @Override
    public VoxelShape getShape(final BlockState state, final IBlockReader worldIn, final BlockPos pos, final ISelectionContext context) {
        switch (state.get(FACING)) {
            case NORTH:
                return NORTH_AABB;
            case SOUTH:
                return SOUTH_AABB;
            case WEST:
                return WEST_AABB;
            case EAST:
                return EAST_AABB;
            case DOWN:
                return DOWN_AABB;
            case UP:
            default:
                return UP_AABB;
        }
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Override
    public TileEntity createTileEntity(final BlockState state, final IBlockReader world) {
        return new FastenerBlockEntity();
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced(final BlockState state, final World world, final BlockPos pos, final BlockState newState, final boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            final TileEntity entity = world.getTileEntity(pos);
            if (entity instanceof FastenerBlockEntity) {
                entity.getCapability(CapabilityHandler.FASTENER_CAP).ifPresent(f -> f.dropItems(world, pos));
            }
            super.onReplaced(state, world, pos, newState, isMoving);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean isValidPosition(final BlockState state, final IWorldReader world, final BlockPos pos) {
        final Direction facing = state.get(FACING);
        final BlockPos attachedPos = pos.offset(facing.getOpposite());
        final BlockState attachedState = world.getBlockState(attachedPos);
        return attachedState.getBlock().isIn(BlockTags.LEAVES) || attachedState.isSolidSide(world, attachedPos, facing) || facing == Direction.UP && attachedState.isIn(BlockTags.WALLS);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(final BlockItemUseContext context) {
        BlockState result = this.getDefaultState();
        final IWorldReader world = context.getWorld();
        final BlockPos pos = context.getPos();
        for (final Direction dir : context.getNearestLookingDirections()) {
            result = result.with(FACING, dir.getOpposite());
            if (result.isValidPosition(world, pos)) {
                return result;
            }
        }
        return null;
    }

    @SuppressWarnings("deprecation")
    @Override
    public BlockState updatePostPlacement(final BlockState state, final Direction facing, final BlockState facingState, final IWorld world, final BlockPos currentPos, final BlockPos facingPos) {
        if (facing.getOpposite() == state.get(FACING) && !state.isValidPosition(world, currentPos)) {
            return Blocks.AIR.getDefaultState();
        }
        return super.updatePostPlacement(state, facing, facingState, world, currentPos, facingPos);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onBlockAdded(final BlockState state, final World world, final BlockPos pos, final BlockState oldState, final boolean isMoving) {
        if (oldState.getBlock() != state.getBlock()) {
            if (world.isBlockPowered(pos.offset(state.get(FACING).getOpposite()))) {
                world.setBlockState(pos, state.with(TRIGGERED, true), 3);
            }
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public void neighborChanged(final BlockState state, final World world, final BlockPos pos, final Block blockIn, final BlockPos fromPos, final boolean isMoving) {
        if (state.isValidPosition(world, pos)) {
            final boolean receivingPower = world.isBlockPowered(pos);
            final boolean isPowered = state.get(TRIGGERED);
            if (receivingPower && !isPowered) {
                world.getPendingBlockTicks().scheduleTick(pos, this, 2);
                world.setBlockState(pos, state.with(TRIGGERED, true), 4);
            } else if (!receivingPower && isPowered) {
                world.setBlockState(pos, state.with(TRIGGERED, false), 4);
            }
        } else {
            final TileEntity entity = world.getTileEntity(pos);
            spawnDrops(state, world, pos, entity);
            world.removeBlock(pos, false);
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean hasComparatorInputOverride(final BlockState state) {
        return true;
    }

    @SuppressWarnings("deprecation")
    @Override
    public int getComparatorInputOverride(final BlockState state, final World world, final BlockPos pos) {
        final TileEntity entity = world.getTileEntity(pos);
        if (entity == null) return super.getComparatorInputOverride(state, world, pos);
        return entity.getCapability(CapabilityHandler.FASTENER_CAP).map(f -> f.getAllConnections().stream()).orElse(Stream.empty())
            .filter(HangingLightsConnection.class::isInstance)
            .map(HangingLightsConnection.class::cast)
            .mapToInt(c -> (int) Math.ceil(c.getJingleProgress() * 15))
            .max().orElse(0);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void tick(final BlockState state, final ServerWorld world, final BlockPos pos, final Random random) {
        this.jingle(world, pos);
    }

    private void jingle(final World world, final BlockPos pos) {
        final TileEntity entity = world.getTileEntity(pos);
        if (!(entity instanceof FastenerBlockEntity)) {
            return;
        }
        entity.getCapability(CapabilityHandler.FASTENER_CAP).ifPresent(fastener -> fastener.getAllConnections().stream()
            .filter(HangingLightsConnection.class::isInstance)
            .map(HangingLightsConnection.class::cast)
            .filter(conn -> conn.canCurrentlyPlayAJingle() && conn.isDestination(new BlockFastenerAccessor(fastener.getPos())) && world.getBlockState(fastener.getPos()).get(TRIGGERED))
            .findFirst().ifPresent(conn -> ServerEventHandler.tryJingle(world, conn, JingleLibrary.RANDOM))
        );
    }

    public Vector3d getOffset(final Direction facing, final float offset) {
        return getFastenerOffset(facing, offset);
    }

    public static Vector3d getFastenerOffset(final Direction facing, final float offset) {
        double x = offset, y = offset, z = offset;
        switch (facing) {
            case DOWN:
                y += 0.75F;
            case UP:
                x += 0.375F;
                z += 0.375F;
                break;
            case WEST:
                x += 0.75F;
            case EAST:
                z += 0.375F;
                y += 0.375F;
                break;
            case NORTH:
                z += 0.75F;
            case SOUTH:
                x += 0.375F;
                y += 0.375F;
        }
        return new Vector3d(x, y, z);
    }
}
