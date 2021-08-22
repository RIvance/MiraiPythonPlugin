package org.ivance;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.utils.MiraiLogger;
import org.ivance.annotation.MessageHandlerSingleton;
import org.ivance.annotation.PrefixedMessageHandler;
import org.ivance.annotation.RegexMessageHandler;
import org.ivance.reflect.Reflect;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MiraiPluginMain extends JavaPlugin {

    private final Map<String, Method> prefixedMessageHandlerMap = new HashMap<>();
    private final Map<String, Method> regexMessageHandlerMap = new HashMap<>();
    private final Map<Method, Object> messageHandlersInstanceMap = new HashMap<>();

    public MiraiPluginMain() {
        super(new JvmPluginDescriptionBuilder("org.ivance.pythonplugin", "1.0").build());
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void onEnable() {
        MiraiLogger logger = getLogger();

        // TODO: log enable info and warning
        try {
            /* load message handlers begin */
            List<Class> messageHandlerClasses = Reflect.getAnnotatedClasses(
                "org.ivance.message", MessageHandlerSingleton.class
            );

            for (Class clazz : messageHandlerClasses) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(PrefixedMessageHandler.class)) {
                        messageHandlersInstanceMap.put(method, clazz.getConstructor().newInstance());
                        for (String prefix : method.getAnnotation(PrefixedMessageHandler.class).prefix()) {
                            prefixedMessageHandlerMap.put(prefix, method);
                        }
                    }
                    else if (method.isAnnotationPresent(RegexMessageHandler.class)) {
                        messageHandlersInstanceMap.put(method, clazz.getConstructor().newInstance());
                        for (String pattern : method.getAnnotation(RegexMessageHandler.class).pattern()) {
                            regexMessageHandlerMap.put(pattern, method);
                        }
                    }
                }
            }
            /* load message handlers end */
        }
        catch (Exception exception) {
            logger.error("Fail to load message handlers, plugin \"org.ivance.MiraiPluginMain\" will be disabled");
            throw new RuntimeException("Fail to load message handlers");
        }

        // TODO: register listener
    }
}
