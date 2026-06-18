package dev.jumpingpxl.unseven.core.listener;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import dev.jumpingpxl.unseven.core.BlockedCommandRegistry.BlockedCommand;
import dev.jumpingpxl.unseven.core.BlockedCommandRegistry.ConsumeType;
import dev.jumpingpxl.unseven.core.Unseven;
import dev.jumpingpxl.unseven.core.configuration.ConfigurationScreenBuilder;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import java.util.UUID;

public class ClientCommandRegistrationListener implements ClientCommandRegistrationCallback {

  private final Unseven unseven;

  public ClientCommandRegistrationListener(Unseven unseven) {
    this.unseven = unseven;
  }

  @Override
  public void register(
      CommandDispatcher<FabricClientCommandSource> dispatcher,
      CommandBuildContext buildContext
  ) {
    dispatcher.register(
        ClientCommands.literal("unseven")
            .executes(this::openSettingsFromCommand)
            .then(ClientCommands.argument("internal_arguments", StringArgumentType.greedyString())
                .executes(this::executeBlockedCommand))
    );
  }

  private int openSettingsFromCommand(CommandContext<FabricClientCommandSource> context) {
    Minecraft.getInstance().schedule(() -> Minecraft.getInstance().gui.setScreen(
        new ConfigurationScreenBuilder(this.unseven).build(null)
    ));

    return 1;
  }

  private int executeBlockedCommand(CommandContext<FabricClientCommandSource> context) {
    String internalArguments = context.getArgument("internal_arguments", String.class);
    String[] arguments = internalArguments.split(" ");

    // if there aren't 2 arguments, act like the "main" command
    if (arguments.length != 2) {
      return this.openSettingsFromCommand(context);
    }

    ConsumeType type;
    UUID uuid;
    try {
      type = ConsumeType.valueOf(arguments[0]);
      uuid = UUID.fromString(arguments[1]);
    } catch (Exception e) {
      // if either the consume type or uuid cannot be parsed, act like the "main" command
      return this.openSettingsFromCommand(context);
    }

    BlockedCommand blockedCommand = this.unseven.registry().get(uuid);
    if (blockedCommand == null) {
      this.displayError(Component.translatable("unseven.action.message.error.expired"));
    } else if (blockedCommand.isConsumed()) {
      this.displayError(Component.translatable("unseven.action.message.error.consumed"));
    } else {
      ClientPacketListener connection = Minecraft.getInstance().getConnection();
      if (connection != null) {
        switch (type) {
          case COMMAND -> connection.sendCommand(blockedCommand.consume().substring(1));
          case MESSAGE -> connection.sendChat(Unseven.BYPASS_PREFIX + blockedCommand.consume());
        }
      }
    }

    return 1;
  }

  private void displayError(MutableComponent component) {
    this.unseven.displayMessage(Component.translatable(
        "unseven.action.message.error",
        component.withStyle(ChatFormatting.RED)
    ).withStyle(ChatFormatting.DARK_RED));
  }
}
