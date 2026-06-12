package dev.jumpingpxl.unseven.core.configuration;

import dev.isxander.yacl3.api.ConfigCategory;
import dev.isxander.yacl3.api.ListOption;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.OptionDescription;
import dev.isxander.yacl3.api.YetAnotherConfigLib;
import dev.isxander.yacl3.api.YetAnotherConfigLib.Builder;
import dev.isxander.yacl3.api.controller.BooleanControllerBuilder;
import dev.isxander.yacl3.api.controller.ControllerBuilder;
import dev.isxander.yacl3.gui.controllers.string.IStringController;
import dev.jumpingpxl.unseven.core.Unseven;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Pattern;

public class ConfigurationScreenBuilder {

  private static final Pattern IGNORED_WORDS_PATTERN = Pattern.compile("^7[a-z0-9_]+$");
  private static final UnsevenConfiguration DEFAULT_CONFIG = new UnsevenConfiguration();
  private final Unseven unseven;
  private final Builder builder;

  public ConfigurationScreenBuilder(Unseven unseven) {
    this.unseven = unseven;
    this.builder = YetAnotherConfigLib.createBuilder()
        .title(Component.literal("Unseven Configuration"))
        .save(unseven::saveConfiguration)
        .category(this.createCategory());
  }

  private ConfigCategory createCategory() {
    ConfigCategory.Builder category = ConfigCategory.createBuilder()
        .name(Component.literal("Unseven Configuration"));

    category.option(this.option(
        ".enabled.name",
        UnsevenConfiguration::enabled,
        config -> config::setEnabled
    ).controller(this::booleanYesNoController).build());

    category.option(this.option(
            ".bypassWhileControlPressed.name",
            UnsevenConfiguration::bypassWhileControlPressed,
            config -> config::setBypassWhileControlPressed
        )
        .description(OptionDescription.of(
            this.translation(".bypassWhileControlPressed.description")
        ))
        .controller(this::booleanOnOffController)
        .build());

    category.option(this.option(
            ".blockSevenOnly.name",
            UnsevenConfiguration::blockSevenOnly,
            config -> config::setBlockSevenOnly
        )
        .description(OptionDescription.of(this.translation(".blockSevenOnly.description")))
        .controller(this::booleanOnOffController)
        .build());

    category.option(this.option(
            ".ignoreOnlinePlayerNames.name",
            UnsevenConfiguration::ignoreOnlinePlayerNames,
            config -> config::setIgnoreOnlinePlayerNames
        )
        .description(OptionDescription.of(this.translation(".ignoreOnlinePlayerNames.description")))
        .controller(this::booleanOnOffController)
        .build());

    category.option(ListOption.<String>createBuilder()
        .name(this.translation(".ignoredWords.name"))
        .binding(
            DEFAULT_CONFIG.ignoredWords(),
            () -> this.unseven.configuration().ignoredWords(),
            newValue -> {
              List<String> list = new ArrayList<>();
              // prevent duplicate entries
              for (String word : newValue) {
                String lowerCaseWord = word.toLowerCase(Locale.ROOT);

                // if entry is not yet in list and word is valid, add
                if (!list.contains(lowerCaseWord)
                    && IGNORED_WORDS_PATTERN.matcher(lowerCaseWord).matches()) {
                  list.add(word);
                }
              }

              this.unseven.configuration().setIgnoredWords(list);
            }
        )
        .description(OptionDescription.of(this.translation(".ignoredWords.description")))
        .controller(option -> () -> new UnsevenStringController(option))
        .initial("7")
        .build());

    return category.build();
  }

  public Screen build(Screen parent) {
    return this.builder.build().generateScreen(parent);
  }

  private ControllerBuilder<Boolean> booleanOnOffController(Option<Boolean> option) {
    return this.booleanController(option, "on/off");
  }

  private ControllerBuilder<Boolean> booleanYesNoController(Option<Boolean> option) {
    return this.booleanController(option, "yes/no");
  }

  private ControllerBuilder<Boolean> booleanController(Option<Boolean> option, String type) {
    return BooleanControllerBuilder.create(option)
        .formatValue(val -> val
            ? Component.translatable("unseven.booleanTypes." + type + ".true")
            : Component.translatable("unseven.booleanTypes." + type + ".false"))
        .coloured(true);
  }

  private <T> Option.Builder<T> option(
      String translationKey,
      Function<UnsevenConfiguration, T> getter,
      Function<UnsevenConfiguration, Consumer<T>> setter
  ) {
    return Option.<T>createBuilder()
        .name(this.translation(translationKey))
        .binding(
            getter.apply(DEFAULT_CONFIG),
            () -> getter.apply(this.unseven.configuration()),
            setter.apply(this.unseven.configuration())
        );
  }

  private Component translation(String key) {
    return Component.translatable("unseven.config" + key);
  }

  public record UnsevenStringController(Option<String> option)
      implements IStringController<String> {

    @Override
    public Option<String> option() {
      return this.option;
    }

    @Override
    public String getString() {
      return this.option().pendingValue();
    }

    @Override
    public void setFromString(String value) {
      this.option().requestSet(value);
    }

    @Override
    public boolean isInputValid(String input) {
      if (input.isEmpty()) {
        return true;
      }

      char c = input.charAt(0);
      if (c != '7') {
        return false;
      }

      return input.length() == 1
          || IGNORED_WORDS_PATTERN.matcher(input.toLowerCase(Locale.ROOT)).matches();
    }
  }
}
