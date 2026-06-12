package dev.jumpingpxl.unseven.core;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class BlockedCommandRegistry {

  private static final long LIFETIME = 30 * 1000; // 30 seconds
  private static final long PURGE_CYCLE = 5 * 1000; // 5 seconds

  private final Map<UUID, BlockedCommand> blockedCommands = new HashMap<>();
  private long nextPurge = 0;

  public BlockedCommand register(String command) {
    UUID uuid = UUID.randomUUID();
    BlockedCommand value = new BlockedCommand(command, uuid);
    this.blockedCommands.put(uuid, value);
    return value;
  }

  public void purge() {
    if (this.blockedCommands.isEmpty()) {
      return;
    }

    long time = System.currentTimeMillis();
    if (this.nextPurge > time) {
      return;
    }

    this.nextPurge = time + PURGE_CYCLE;
    this.blockedCommands.values().removeIf(command -> !command.isValid(time));
  }

  public BlockedCommand get(UUID uuid) {
    return this.blockedCommands.get(uuid);
  }

  public enum ConsumeType {
    MESSAGE,
    COMMAND
  }

  public static class BlockedCommand {

    private final UUID uuid;
    private final String command;
    private final long validUntil;
    private boolean consumed;

    public BlockedCommand(String command, UUID uuid) {
      this.command = command;
      this.uuid = uuid;
      this.validUntil = System.currentTimeMillis() + LIFETIME;
    }

    public boolean isValid(long time) {
      return this.validUntil > time;
    }

    public boolean isConsumed() {
      return this.consumed;
    }

    public String consume() {
      this.consumed = true;
      return this.command;
    }

    public String buildCommand(ConsumeType consumeType) {
      return "unseven " + consumeType + " " + this.uuid;
    }
  }
}
