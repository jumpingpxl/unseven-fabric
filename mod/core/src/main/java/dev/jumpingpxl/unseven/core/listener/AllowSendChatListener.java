package dev.jumpingpxl.unseven.core.listener;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.Window;
import dev.jumpingpxl.unseven.core.BlockedCommandRegistry.BlockedCommand;
import dev.jumpingpxl.unseven.core.BlockedCommandRegistry.ConsumeType;
import dev.jumpingpxl.unseven.core.Unseven;
import dev.jumpingpxl.unseven.core.configuration.UnsevenConfiguration;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents.AllowChat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import org.jspecify.annotations.NonNull;

import java.util.Collection;
import java.util.Locale;

public class AllowSendChatListener implements AllowChat {

  private final Unseven unseven;

  public AllowSendChatListener(Unseven unseven) {
    this.unseven = unseven;
  }

  @Override
  public boolean allowSendChatMessage(@NonNull String message) {
    if (message.isEmpty() || message.charAt(0) != '7') {
      return true;
    }

    // message has content and starts with a seven, check if enabled
    UnsevenConfiguration configuration = this.unseven.configuration();
    if (!configuration.enabled()) {
      return true;
    }

    // if bypass is enabled and either lcontrol or rcontrol is pressed, allow
    if (configuration.bypassWhileControlPressed()) {
      Window window = Minecraft.getInstance().getWindow();
      if (InputConstants.isKeyDown(window, InputConstants.KEY_LCONTROL)
          || InputConstants.isKeyDown(window, InputConstants.KEY_RCONTROL)) {
        return true;
      }
    }

    // if block seven only is disabled and message only consists of 7, allow
    if (!configuration.blockSevenOnly() && message.length() == 1) {
      return true;
    }

    // Get first word of message and lowercase it
    String firstWord = this.getFirstWord(message);
    String possibleCommand = firstWord.toLowerCase(Locale.ROOT);

    // if ignore online player names is enabled and possible command is player name, allow
    if (configuration.ignoreOnlinePlayerNames() && this.isPlayerName(possibleCommand)) {
      return true;
    }

    // loop through ignored words and check if one matches
    for (String ignoredWord : configuration.ignoredWords()) {
      // if possible command is ignored word, allow
      if (ignoredWord.equals(possibleCommand)) {
        return true;
      }
    }

    BlockedCommand blockedCommand = this.unseven.registry().register(message);

    // build message
    Component component = Component.translatable(
            "unseven.blocked.message",
            Component.literal(firstWord)
                .withStyle(ChatFormatting.YELLOW)
                .withStyle(style -> style.withHoverEvent(new HoverEvent.ShowText(
                    Component.literal(message).withStyle(ChatFormatting.YELLOW)
                )))
        )
        .append(Component.literal("\n  "))
        .append(Component.translatable(
            "unseven.blocked.message.action",
            Component.translatable("unseven.blocked.message.sendAsMessage")
                .withStyle(ChatFormatting.RED, ChatFormatting.BOLD)
                .withStyle(style -> style.withClickEvent(
                    new ClickEvent.RunCommand(blockedCommand.buildCommand(ConsumeType.MESSAGE))
                )),
            Component.translatable("unseven.blocked.message.sendAsComand")
                .withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD)
                .withStyle(style -> style.withClickEvent(
                    new ClickEvent.RunCommand(blockedCommand.buildCommand(ConsumeType.COMMAND))
                ))
        ));

    // display message
    this.unseven.displayMessage(component);

    // block message send
    return false;
  }

  /**
   * Lighweight method to get everything up to the first space
   */
  private String getFirstWord(String message) {
    int spaceIndex = message.indexOf(' ');
    if (spaceIndex == -1) {
      return message;
    }

    return message.substring(0, spaceIndex);
  }

  /**
   * @param lowercaseWord lowercase string to check against online player names
   * @return whether the supplied string matches a player's name
   */
  private boolean isPlayerName(String lowercaseWord) {
    ClientPacketListener connection = Minecraft.getInstance().getConnection();
    if (connection == null) {
      return false;
    }

    Collection<PlayerInfo> onlinePlayers = connection.getOnlinePlayers();
    for (PlayerInfo onlinePlayer : onlinePlayers) {
      String name = onlinePlayer.getProfile().name();

      // ignore any player whose name is definitely no match resulting in fewer calls on toLowerCase
      if (name.isEmpty() || name.charAt(0) != '7') {
        continue;
      }

      if (lowercaseWord.equals(name.toLowerCase(Locale.ROOT))) {
        return true;
      }
    }

    return false;
  }
}
