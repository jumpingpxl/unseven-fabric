package dev.jumpingpxl.unseven.core.listener;

import dev.jumpingpxl.unseven.core.Unseven;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents.ModifyChat;
import org.jspecify.annotations.NonNull;

public class ModifySendChatListener implements ModifyChat {

  @Override
  public @NonNull String modifySendChatMessage(@NonNull String message) {
    if (message.indexOf(Unseven.BYPASS_PREFIX) != 0) {
      return message;
    }

    String bypassedMessage = message.substring(Unseven.BYPASS_PREFIX.length());
    if (!bypassedMessage.isEmpty() && bypassedMessage.charAt(0) == '7') {
      return bypassedMessage;
    }

    return message;
  }
}
