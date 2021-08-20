package org.ivance;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

public class PluginMain extends JavaPlugin {
    public PluginMain() {
        super(new JvmPluginDescriptionBuilder("org.ivance.pythonplugin", "1.0").build());
    }

    @Override
    public void onEnable() {
        MiraiLogger logger = getLogger();
    }
}
