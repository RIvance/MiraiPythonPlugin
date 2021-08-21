package org.ivance.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Classes with the annotation <code>Scannable</code> or the annotation extended from <code>Scannable</code>
 *  are able to be scanned by the static method of class <code>Reflect</code>
 * {@link Reflect org.ivance.reflect.Reflect}.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Scannable {
}
