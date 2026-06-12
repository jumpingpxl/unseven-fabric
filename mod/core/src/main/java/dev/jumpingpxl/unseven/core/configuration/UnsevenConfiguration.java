package dev.jumpingpxl.unseven.core.configuration;

import dev.isxander.yacl3.config.v2.api.SerialEntry;

import java.util.ArrayList;
import java.util.List;

public class UnsevenConfiguration {

  @SerialEntry
  private boolean enabled = true;

  @SerialEntry
  private boolean bypassWhileControlPressed = false;

  @SerialEntry
  private boolean blockSevenOnly = false;

  @SerialEntry
  private boolean ignoreOnlinePlayerNames = true;

  @SerialEntry
  private List<String> ignoredWords = new ArrayList<>();

  public boolean enabled() {
    return this.enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean bypassWhileControlPressed() {
    return this.bypassWhileControlPressed;
  }

  public void setBypassWhileControlPressed(boolean bypassWhileControlPressed) {
    this.bypassWhileControlPressed = bypassWhileControlPressed;
  }

  public boolean ignoreOnlinePlayerNames() {
    return this.ignoreOnlinePlayerNames;
  }

  public void setIgnoreOnlinePlayerNames(boolean ignoreOnlinePlayerNames) {
    this.ignoreOnlinePlayerNames = ignoreOnlinePlayerNames;
  }

  public boolean blockSevenOnly() {
    return this.blockSevenOnly;
  }

  public void setBlockSevenOnly(boolean blockSevenOnly) {
    this.blockSevenOnly = blockSevenOnly;
  }

  public List<String> ignoredWords() {
    return this.ignoredWords;
  }

  public void setIgnoredWords(List<String> ignoredWords) {
    this.ignoredWords = ignoredWords;
  }
}
