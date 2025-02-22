package me.paulf.fairylights.server.net;

import me.paulf.fairylights.server.fastener.Fastener;
import me.paulf.fairylights.server.fastener.FastenerType;
import me.paulf.fairylights.server.fastener.accessor.FastenerAccessor;
import me.paulf.fairylights.server.connection.Connection;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public abstract class ConnectionMessage implements Message {
    public BlockPos pos;

    public FastenerAccessor accessor;

    public UUID uuid;

    public ConnectionMessage() {}

    public ConnectionMessage(final Connection connection) {
        final Fastener<?> fastener = connection.getFastener();
        this.pos = fastener.getPos();
        this.accessor = fastener.createAccessor();
        this.uuid = connection.getUUID();
    }

    @Override
    public void encode(final FriendlyByteBuf buf) {
        buf.writeBlockPos(this.pos);
        buf.writeCompoundTag(FastenerType.serialize(this.accessor));
        buf.writeUniqueId(this.uuid);
    }

    @Override
    public void decode(final PacketBuffer buf) {
        this.pos = buf.readBlockPos();
        this.accessor = FastenerType.deserialize(Objects.requireNonNull(buf.readCompoundTag(), "tag"));
        this.uuid = buf.readUniqueId();
    }

    @SuppressWarnings("unchecked")
    public static <C extends Connection> Optional<C> getConnection(final ConnectionMessage message, final Predicate<? super Connection> typePredicate, final World world) {
        return message.accessor.get(world, false).map(Optional::of).orElse(Optional.empty()).flatMap(f -> (Optional<C>) f.get(message.uuid).filter(typePredicate));
    }
}
