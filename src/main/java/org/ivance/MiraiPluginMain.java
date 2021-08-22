package org.ivance;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.utils.MiraiLogger;
import org.ivance.annotation.CommandHandler;
import org.ivance.annotation.HandlerSingleton;
import org.ivance.annotation.PrefixedHandler;
import org.ivance.annotation.RegexHandler;
import org.ivance.reflect.Reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MiraiPluginMain extends JavaPlugin {

    private final Map<String, Method> commandHandlerMap = new HashMap<>();
    private final Map<String, Method> prefixedHandlerMap = new HashMap<>();
    private final Map<String, Method> regexHandlerMap = new HashMap<>();

    private final Map<Method, Object> handlerInstancesMap = new HashMap<>();

    public MiraiPluginMain() {
        super(new JvmPluginDescriptionBuilder("org.ivance.pythonplugin", "1.0").build());
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public void onEnable() {
        MiraiLogger logger = getLogger();

        // TODO: log enable info and warning

        // Load message handlers
        try {
            /* load message handlers begin */
            List<Class> messageHandlerClasses = Reflect.getAnnotatedClasses(
                "org.ivance.handler", HandlerSingleton.class
            );

            BiConsumer<Method, Class> tryCreateInstanceFunction = (Method method, Class clazz) -> {
                if (handlerInstancesMap.containsKey(method)) {
                    handlerInstancesMap.put(method, handlerInstancesMap.get(method));
                } else {
                    try {
                        handlerInstancesMap.put(method, clazz.getConstructor().newInstance());
                    } catch (ReflectiveOperationException exception) {
                        logger.error("Fail to load handler \"" + method.getName() + "\"");
                    }
                }
            };

            for (Class clazz : messageHandlerClasses) {
                for (Method method : clazz.getDeclaredMethods()) {
                    // CommandHandler
                    if (method.isAnnotationPresent(CommandHandler.class)) {
                        tryCreateInstanceFunction.accept(method, clazz);
                        for (String prefix : method.getAnnotation(CommandHandler.class).command()) {
                            commandHandlerMap.put(prefix, method);
                        }
                    }
                    // PrefixedHandler
                    else if (method.isAnnotationPresent(PrefixedHandler.class)) {
                        tryCreateInstanceFunction.accept(method, clazz);
                        for (String prefix : method.getAnnotation(PrefixedHandler.class).prefix()) {
                            prefixedHandlerMap.put(prefix, method);
                        }
                    }
                    // RegexHandler
                    else if (method.isAnnotationPresent(RegexHandler.class)) {
                        tryCreateInstanceFunction.accept(method, clazz);
                        for (String pattern : method.getAnnotation(RegexHandler.class).pattern()) {
                            regexHandlerMap.put(pattern, method);
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
        // Register Listener
    }
}
