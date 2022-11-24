package me.paulf.fairylights.client.command;

import com.google.common.collect.ImmutableMap;
import com.google.gson.internal.UnsafeAllocator;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.RootCommandNode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author Paul Fulham
 * @version 1.0.0
 */
public final class ClientCommandProvider {
    private static final UnsafeAllocator ALLOCATOR = UnsafeAllocator.create();

    private final Commands commands;

    private final ImmutableMap<String, CommandBuilder> builders;

    private final Pattern chatPredicate;

    public ClientCommandProvider(final Commands commands, final ImmutableMap<String, CommandBuilder> builders, final Pattern chatPredicate) {
        this.commands = commands;
        this.builders = builders;
        this.chatPredicate = chatPredicate;
    }

    private void onKeyPressedEvent(final GuiScreenEvent.KeyboardKeyPressedEvent.Pre event) {
        if (event.getGui() instanceof ChatScreen) {
            final ClientPacketListener net = Minecraft.getInstance().getConnection();
            if (net == null) {
                return;
            }
            final RootCommandNode<SharedSuggestionProvider> root = net.getCommands().getRoot(); // Doesn't seem to be complaining...
            for (final Map.Entry<String, CommandBuilder> e : this.builders.entrySet()) {
                if (root.getChild(e.getKey()) == null) {
                    root.addChild(e.getValue().build(new SuggestionHelper()).build());
                }
            }
        }
    }

    private void onChatEvent(final ClientChatEvent event) {
        final String message = event.getMessage();
        if (this.chatPredicate.matcher(message).matches()) {
            event.setCanceled(true);
            Minecraft.getInstance().gui.getChat().addMessage(Component.nullToEmpty(message)); // eeeee - That was the suggested fix: Component.nullToEmpty(message)
            final Player user = Minecraft.getInstance().player;
            if (user != null) {
                this.commands.performCommand((CommandSourceStack) this.createSource(user), message); // This casting isn't scary at all noooo ....
            }
        }
    }

    private CommandSource createSource(final Entity entity) {
        //noinspection ConstantConditions
        return new CommandSource(new NoLoggingSource(entity), entity.position(), entity.get(), null, 4, entity.getName().getString(), entity.getDisplayName(), DummyServer.INSTANCE, entity);
    }
    // ^ Hopefully entity.position() is okay, there's a getter, but it requires a float that I'm not aware of.

    public void register(final IEventBus bus) {
        bus.addListener(this::onKeyPressedEvent);
        bus.addListener(this::onChatEvent);
    }

    private static <T> T instantiate(final Class<T> clazz) {
        try {
            return ALLOCATOR.newInstance(clazz);
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static final class Builder {
        private final List<CommandBuilder> builders = new ArrayList<>();

        public <S> Builder add(final CommandBuilder builder) {
            this.builders.add(builder);
            return this;
        }

        public ClientCommandProvider build() {
            final Commands commands = instantiate(Commands.class);
            final CommandDispatcher<CommandSource> dispatcher = new CommandDispatcher<>();
            final ImmutableMap.Builder<String, CommandBuilder> map = new ImmutableMap.Builder<>();
            for (final CommandBuilder builder : this.builders) {
                map.put(dispatcher.register(builder.build(new ExecutionHelper())).getName(), builder);
            }
            ObfuscationReflectionHelper.setPrivateValue(Commands.class, commands, dispatcher, "dispatcher");
            final ImmutableMap<String, CommandBuilder> builders = map.build();
            final Pattern pattern = Pattern.compile(
                String.format(
                    "^/(%s)(\\p{javaWhitespace}.*|$)",
                    builders.keySet().stream()
                        .map(Pattern::quote)
                        .collect(Collectors.joining("|"))
                ),
                Pattern.DOTALL
            );
            return new ClientCommandProvider(commands, builders, pattern);
        }
    }

    public interface CommandBuilder {
        <S> LiteralArgumentBuilder<S> build(Helper<S> helper);
    }

    public interface Helper<S> {
        <T extends ArgumentBuilder<S, T>> T executes(final T builder, final Command<CommandSource> command);
    }

    private static final class ExecutionHelper implements Helper<CommandSource> {
        @Override
        public <T extends ArgumentBuilder<CommandSource, T>> T executes(final T builder, final Command<CommandSource> command) {
            return builder.executes(command);
        }
    }

    private static final class SuggestionHelper implements Helper<ISuggestionProvider> {
        @Override
        public <T extends ArgumentBuilder<ISuggestionProvider, T>> T executes(final T builder, final Command<CommandSource> command) {
            return builder;
        }
    }

    private static final class DummyServer extends IntegratedServer {
        private static final DummyServer INSTANCE = instantiate(DummyServer.class);

        public DummyServer() {
            //noinspection ConstantConditions
            super(null, null, null, null, null, null, null, null, null, null, null);
        }

        @Override
        public IProfiler getProfiler() {
            return EmptyProfiler.INSTANCE;
        }
    }

    private static final class NoLoggingSource implements ICommandSource {
        private final Entity entity;

        public NoLoggingSource(final Entity entity) {
            this.entity = entity;
        }

        @Override
        public void sendMessage(final ITextComponent component, final UUID sender) {
            this.entity.sendMessage(component, sender);
        }

        @Override
        public boolean shouldReceiveFeedback() {
            return true;
        }

        @Override
        public boolean shouldReceiveErrors() {
            return true;
        }

        @Override
        public boolean allowLogging() {
            return false;
        }
    }
}
