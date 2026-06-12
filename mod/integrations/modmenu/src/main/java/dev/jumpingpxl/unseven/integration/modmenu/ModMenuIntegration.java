package dev.jumpingpxl.unseven.integration.modmenu;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.jumpingpxl.unseven.core.Unseven;
import dev.jumpingpxl.unseven.core.configuration.ConfigurationScreenBuilder;

public class ModMenuIntegration implements ModMenuApi {

  @Override
  public ConfigScreenFactory<?> getModConfigScreenFactory() {
    return parent -> new ConfigurationScreenBuilder(Unseven.get()).build(parent);
  }
}
