package me.paulf.fairylights.server;

import me.paulf.fairylights.FairyLights;
import me.paulf.fairylights.client.tutorial.ClippyController;
import me.paulf.fairylights.server.capability.CapabilityHandler;
import me.paulf.fairylights.server.config.FLConfig;
import me.paulf.fairylights.server.fastener.BlockView;
import me.paulf.fairylights.server.fastener.CreateBlockViewEvent;
import me.paulf.fairylights.server.fastener.RegularBlockView;
import me.paulf.fairylights.server.item.LightVariant;
import me.paulf.fairylights.server.jingle.JingleLibrary;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.INBT;
import net.minecraft.resources.IResourceManagerReloadListener;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.PacketDistributor;

import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class ServerProxy {
    public ServerProxy() {
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, FLConfig.GENERAL_SPEC);
    }

    /**
     * <pre>
     * |\   /|    __     __     __     __
     *  \|_|/    /  \   /  \   /  \   /  \
     *  /. .\   |%%%%| |@@@@| |####| |$$$$|
     * =\_Y_/=   \__/   \__/   \__/   \__/
     * </pre>
     */
    public void initEggs() {
        MinecraftForge.EVENT_BUS.<FMLServerAboutToStartEvent>addListener(e -> {
            final MinecraftServer server = e.getServer();
            server.getResourceManager().addReloadListener((IResourceManagerReloadListener) mgr -> JingleLibrary.loadAll(server));
        });
    }

    @SuppressWarnings("ConstantConditions")
    public void initHandlers() {
        MinecraftForge.EVENT_BUS.register(new ServerEventHandler());
        CapabilityHandler.register();
        CapabilityManager.INSTANCE.register(LightVariant.class,  new Capability.IStorage<LightVariant>() {
            @Override
            public INBT writeNBT(final Capability<LightVariant> capability, final LightVariant instance, final Direction side) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void readNBT(final Capability<LightVariant> capability, final LightVariant instance, final Direction side, final INBT nbt) {
                throw new UnsupportedOperationException();
            }
        }, () -> {
            throw new UnsupportedOperationException();
        });
        new ClippyController().init();
    }

    protected <M> BiConsumer<M, Supplier<NetworkEvent.Context>> clientConsumer(final Supplier<Supplier<BiConsumer<M, Supplier<NetworkEvent.Context>>>> consumer) {
        return (msg, ctx) -> ctx.get().setPacketHandled(true);
    }

    public void initRenders() {}

    public void initRendersLate() {}

    public static void sendToPlayersWatchingChunk(final Object message, final World world, final BlockPos pos) {
        FairyLights.network.send(PacketDistributor.TRACKING_CHUNK.with(() -> world.getChunkAt(pos)), message);
    }

    public static void sendToPlayersWatchingEntity(final Object message, final World world, final Entity entity) {
        FairyLights.network.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), message);
    }

    public static BlockView buildBlockView() {
        final CreateBlockViewEvent evt = new CreateBlockViewEvent(new RegularBlockView());
        MinecraftForge.EVENT_BUS.post(evt);
        return evt.getView();
    }

    public void initIntegration() {
		/*if (Loader.isModLoaded(ValkyrienWarfareMod.MODID)) {
			final Class<?> vw;
			try {
				vw = Class.forName("ValkyrienWarfare");
			} catch (final ClassNotFoundException e) {
				throw new AssertionError(e);
			}
			MinecraftForge.EVENT_BUS.register(vw);
		}*/
    }
}
