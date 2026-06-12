package dev.jumpingpxl.unseven.core;

import dev.isxander.yacl3.config.v2.api.ConfigClassHandler;
import dev.isxander.yacl3.config.v2.api.serializer.GsonConfigSerializerBuilder;
import dev.jumpingpxl.unseven.core.configuration.UnsevenConfiguration;
import dev.jumpingpxl.unseven.core.listener.AllowSendChatListener;
import dev.jumpingpxl.unseven.core.listener.ClientCommandRegistrationListener;
import dev.jumpingpxl.unseven.core.listener.ModifySendChatListener;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Unseven implements ClientModInitializer {

  public static final String BYPASS_PREFIX = "$%%unseven-bypass%%$ ";
  private static final Component PREFIX = Component.literal("[").withStyle(ChatFormatting.GRAY)
      .append(Component.literal("Unseven").withStyle(ChatFormatting.LIGHT_PURPLE))
      .append(Component.literal("] "));
  private static Unseven instance;
  private final ConfigClassHandler<UnsevenConfiguration> configHandler;
  private final BlockedCommandRegistry registry = new BlockedCommandRegistry();

  public Unseven() {
    setInstance(this);
    this.configHandler = this.createConfigurationHandler();
    this.configHandler.load();
  }

  public static @NotNull Unseven get() {
    Objects.requireNonNull(instance, "Unseven has not been initialized");
    return instance;
  }

  static void setInstance(@NotNull Unseven instance) {
    if (Unseven.instance != null) {
      throw new IllegalStateException("Unseven has already been initialized");
    }

    Objects.requireNonNull(instance, "Unseven has not been initialized");
    Unseven.instance = instance;
  }

  @Override
  public void onInitializeClient() {
    ClientSendMessageEvents.ALLOW_CHAT.register(new AllowSendChatListener(this));
    ClientCommandRegistrationCallback.EVENT.register(new ClientCommandRegistrationListener(this));
    ClientSendMessageEvents.MODIFY_CHAT.register(new ModifySendChatListener());
    ClientTickEvents.END_CLIENT_TICK.register(_ -> this.registry.purge());
  }

  public UnsevenConfiguration configuration() {
    return this.configHandler.instance();
  }

  public void saveConfiguration() {
    this.configHandler.save();
  }

  public BlockedCommandRegistry registry() {
    return this.registry;
  }

  public void displayMessage(Component component) {
    Minecraft.getInstance().gui.getChat().addClientSystemMessage(
        Component.empty().withStyle(ChatFormatting.GRAY).append(PREFIX).append(component)
    );
  }

  private ConfigClassHandler<UnsevenConfiguration> createConfigurationHandler() {
    return ConfigClassHandler.createBuilder(UnsevenConfiguration.class)
        .id(Identifier.fromNamespaceAndPath("unseven", "config"))
        .serializer(config -> GsonConfigSerializerBuilder.create(config)
            .setPath(FabricLoader.getInstance().getConfigDir().resolve("unseven.json"))
            .build())
        .build();
  }
}
