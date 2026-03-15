package com.swalrus.tris;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;

@Config(name = "tris")
public class TrisConfig implements ConfigData {
    @ConfigEntry.Gui.Tooltip
    @ConfigEntry.Gui.EnumHandler(option = ConfigEntry.Gui.EnumHandler.EnumDisplayOption.DROPDOWN)
    public LlmProvider provider = LlmProvider.GOOGLE_GEMINI;

    @ConfigEntry.Gui.Tooltip
    public String model = "gemini-2.0-flash";

    /** API key. For Ollama, leave empty to use http://localhost:11434, or enter a custom base URL. */
    @ConfigEntry.Gui.Tooltip
    public String apiKey = "";

    @ConfigEntry.Gui.Tooltip
    public String systemPrompt = "";

    public static TrisConfig get() {
        return AutoConfig.getConfigHolder(TrisConfig.class).getConfig();
    }
}
