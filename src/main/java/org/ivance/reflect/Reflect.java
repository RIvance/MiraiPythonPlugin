package org.ivance.reflect;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

public class Reflect {

    /**
     * Scans all classes accessible with the annotation <code>Scannable</code>
     *  from the context class loader which belong to the given package and subpackages.
     * @param packageName The base package
     * @return The classes
     */
    @SuppressWarnings("rawtypes")
    public static List<Class> getScannableClasses(
            String packageName) throws ClassNotFoundException, IOException {
        return getScannableClasses(packageName, Scannable.class);
    }

    /**
     * Scans all classes accessible with subclass of the annotation <code>Scannable</code>
     *  from the context class loader which belong to the given package and subpackages.
     * @param packageName The base package
     * @return The classes
     */
    @SuppressWarnings("rawtypes")
    public static <ScannableExtended extends Scannable> List<Class> getScannableClasses(
        String packageName, Class<ScannableExtended> scannableAnnotation
    ) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        assert classLoader != null;

        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();

        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(resource.getFile()));
        }

        ArrayList<Class> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findScannableClasses(directory, packageName, scannableAnnotation));
        }
        return classes;
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     */
    @SuppressWarnings({"rawtypes", "unchecked"})
    private static <ScannableExtended extends Scannable> List<Class> findScannableClasses(
        File directory, String packageName, Class<ScannableExtended> scannableAnnotation
    ) throws ClassNotFoundException {

        List<Class> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }

        File[] files = directory.listFiles();
        if (files == null) {
            return new ArrayList<>();
        }

        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findScannableClasses(
                    file, packageName + "." + file.getName(), scannableAnnotation
                ));
            } else if (file.getName().endsWith(".class")) {
                Class clazz = Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6));
                if (clazz.isAnnotationPresent(scannableAnnotation)) {
                    classes.add(clazz);
                }
            }
        }
        return classes;
    }
}
