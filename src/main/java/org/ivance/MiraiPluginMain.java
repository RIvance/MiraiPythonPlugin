package org.ivance;

import lombok.val;
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
import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ConfigurationBuilder;

import java.lang.reflect.Method;
import java.util.*;
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
        loadHandlers();
        registerListeners();
        logger.info("PythonPlugin is enabled");
        logger.warning("This plugin uses a simple sandbox to prevent file access, " +
                "but it is not 100 percent safe, please be careful when use this " +
                "plugin and make sure that the bot service is running in a docker"
        );
    }

    private void loadHandlers() {
        MiraiLogger logger = getLogger();

        try {
            Reflections reflections = new Reflections(
                new ConfigurationBuilder().forPackages("org.ivance.handler").setScanners(
                    new TypeAnnotationsScanner(),
                    new SubTypesScanner(),
                    new MethodAnnotationsScanner()
                )
            );

            logger.info("Scanning handler classes");
            Set<Class<?>> handlerClasses = reflections.getTypesAnnotatedWith(HandlerSingleton.class);
            logger.info("Handler classes found: " + Arrays.toString(handlerClasses.toArray()));

            BiConsumer<Method, Class<?>> tryCreateInstanceFunction = (Method method, Class<?> clazz) -> {
                if (handlerInstancesMap.containsKey(method)) {
                    handlerInstancesMap.put(method, handlerInstancesMap.get(method));
                } else {
                    try {
                        handlerInstancesMap.put(method, clazz.getConstructor().newInstance());
                        logger.info("Handler loaded: \"" + method.getName() + "\"");
                    } catch (ReflectiveOperationException exception) {
                        logger.error("Fail to load handler \"" + method.getName() + "\"");
                    }
                }
            };

            for (Class<?> clazz : handlerClasses) {
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
        MiraiLogger logger = getLogger();

        GlobalEventChannel.INSTANCE.subscribeAlways(MessageEvent.class, (MessageEvent messageEvent) -> {
            String message = messageEvent.getMessage().contentToString();
            Contact contact = messageEvent.getSubject();
            User sender = messageEvent.getSender();

            commandHandlerMap.forEach((String command, Method method) -> {
                if (message.startsWith(command)) {
                    logger.info("Command detected: " + command);
                    String[] arguments = message.split("\\s[\\s]*");
                    logger.info("Invoking handler: " + method.getName());
                    tryInvokeHandler(method, contact, sender, arguments);
                }
            });

            prefixedHandlerMap.forEach((String prefix, Method method) -> {
                if (message.startsWith(prefix)) {
                    logger.info("Message prefix detected: " + prefix);
                    String messageBody = message.substring(prefix.length()).trim();
                    logger.info("Invoking handler: " + method.getName());
                    tryInvokeHandler(method, contact, sender, messageBody);
                }
            });

            regexHandlerMap.forEach((String pattern, Method method) -> {
                if (message.matches(pattern)) {
                    logger.info("Regex pattern detected: " + pattern);
                    logger.info("Invoking handler: " + method.getName());
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
