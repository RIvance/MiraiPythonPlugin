package org.ivance;

import net.mamoe.mirai.console.plugin.jvm.JavaPlugin;
import net.mamoe.mirai.console.plugin.jvm.JvmPluginDescriptionBuilder;
import net.mamoe.mirai.contact.Contact;
import net.mamoe.mirai.contact.User;
import net.mamoe.mirai.event.GlobalEventChannel;
import net.mamoe.mirai.event.events.MessageEvent;
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
    public void onEnable() {
        MiraiLogger logger = getLogger();

        // TODO: log enable info and warning
        loadHandlers();
        registerListeners();
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void loadHandlers() {
        MiraiLogger logger = getLogger();

        try {
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
        }
        catch (Exception exception) {
            logger.error("Fail to load message handlers, plugin \"org.ivance.MiraiPluginMain\" will be disabled");
            throw new RuntimeException("Fail to load message handlers");
        }
    }

    private void registerListeners() {
        GlobalEventChannel.INSTANCE.subscribeAlways(MessageEvent.class, (MessageEvent messageEvent) -> {
            String message = messageEvent.getMessage().contentToString();
            Contact contact = messageEvent.getSubject();
            User sender = messageEvent.getSender();

            commandHandlerMap.forEach((String command, Method method) -> {
                if (message.startsWith(command)) {
                    String[] arguments = message.split("\\s[\\s]*");
                    tryInvokeHandler(method, contact, sender, arguments);
                }
            });

            prefixedHandlerMap.forEach((String prefix, Method method) -> {
                if (message.startsWith(prefix)) {
                    String messageBody = message.substring(prefix.length()).trim();
                    tryInvokeHandler(method, contact, sender, messageBody);
                }
            });

            regexHandlerMap.forEach((String pattern, Method method) -> {
                if (message.matches(pattern)) {
                    tryInvokeHandler(method, contact, sender, message);
                }
            });
        });
    }

    private void tryInvokeHandler(Method method, Contact contact, User sender, Object argument) {
        MiraiLogger logger = getLogger();
        try {
            method.invoke(handlerInstancesMap.get(method), contact, sender, argument);
        } catch (ReflectiveOperationException exception) {
            logger.error("Cannot invoke handler \"" + method.getClass() + "." + method.getName() + "\"");
        } catch (Exception exception) {
            logger.warning(
                "Handler \"" + method.getClass() + "." + method.getName() +
                "\" throws exception:" + exception.getMessage()
            );
        }
    }
}
