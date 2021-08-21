package org.ivance;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.utils.MiraiLogger;

public class MiraiPluginMain extends JavaPlugin {
    public MiraiPluginMain() {
        super(new JvmPluginDescriptionBuilder("org.ivance.pythonplugin", "1.0").build());
    }

    @Override
    public void onEnable() {
        MiraiLogger logger = getLogger();
    }
}
