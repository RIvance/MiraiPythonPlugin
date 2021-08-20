package org.ivance.utils;

import org.jetbrains.annotations.NotNull;

public class ArrayUtils {
    @SafeVarargs
    public static<Type> Type[] merge(@NotNull Type[] ... arrays) {
        int mergedLength = 0;
        for (Type[] array : arrays) {
            if (array != null) {
                mergedLength += array.length;
            }
        }

        @SuppressWarnings("unchecked")
        Type[] mergedArray = (Type[]) new Object[mergedLength];

        int destPos = 0;
        for (Type[] array : arrays) {
            if (array != null) {
                System.arraycopy(array, 0, mergedArray, destPos, array.length);
                destPos += array.length;
            }
        }

        return mergedArray;
    }
}
