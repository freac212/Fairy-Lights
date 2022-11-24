package me.paulf.fairylights.server.net;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.Objects;

public class ClientMessageContext extends MessageContext {
    public ClientMessageContext(final NetworkEvent.Context context) {
        super(context);
    }

    @Override
    public LogicalSide getSide() {
        return LogicalSide.CLIENT;
    }

    public Minecraft getMinecraft() {
        return Minecraft.getInstance();
    }

    public ClientLevel getWorld() {
        return Objects.requireNonNull(this.getMinecraft().world);
    }

    public PlayerEntity getPlayer() {
        return Objects.requireNonNull(this.context.getSender());
    }
}
