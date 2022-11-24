package me.paulf.fairylights.server.capability;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.server.fastener.Fastener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

public final class CapabilityHandler {
    private CapabilityHandler() {}

    public static final ResourceLocation FASTENER_ID = new ResourceLocation(FairyLights.ID, "fastener");

    @CapabilityInject(Fastener.class)
    public static Capability<Fastener<?>> FASTENER_CAP = null;

    public static void register() {
        CapabilityManager.INSTANCE.register(Fastener.class, new FastenerStorage<>(), () -> {
            throw new UnsupportedOperationException();
        });
    }

    public static class FastenerStorage<T extends Fastener<?>> implements IStorage<T> {
        @Override
        public CompoundTag writeNBT(final Capability<T> capability, final T instance, final Direction side) {
            return instance.serializeNBT();
        }

        @Override
        public void readNBT(final Capability<T> capability, final T instance, final Direction side, final Tag nbt) {
            instance.deserializeNBT((CompoundTag) nbt);
        }
    }
}
