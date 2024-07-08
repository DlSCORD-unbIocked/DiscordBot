package org.smartscholars.projectmanager.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandInfo;
import org.smartscholars.projectmanager.commands.ICommand;

public class CustomClassLoader extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(CustomClassLoader.class);
    private final Map<String, Class<?>> loadedClasses = new HashMap<>();

    public CustomClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (loadedClasses.containsKey(name)) {
            return loadedClasses.get(name);
        }

        Class<?> clazz = super.loadClass(name, resolve); // Delegate to parent first
        if (clazz != null) {
            loadedClasses.put(name, clazz);
            return clazz;
        }

        logger.info("Attempting to load class: {}", name);
        try {
            String classPath = "ProjectManager/target/classes/" + name.replace('.', '/') + ".class";
            Path path = Paths.get(classPath);
            logger.info("Looking for class at: {}", path.toAbsolutePath());
            if (!Files.exists(path)) {
                logger.info("Class not found in custom path, delegating to parent: {}", name);
                return super.loadClass(name, resolve);
            }
            byte[] classBytes = Files.readAllBytes(path);
            clazz = defineClass(name, classBytes, 0, classBytes.length);
            if (resolve) {
                resolveClass(clazz);
            }
            loadedClasses.put(name, clazz);
            logger.info("Successfully loaded class with custom class loader: {}", name);
            return clazz;
        } catch (IOException e) {
            logger.error("Error loading class with custom class loader: {}", name, e);
            throw new ClassNotFoundException("Error loading class with custom class loader: " + name, e);
        }
    }

    public void reloadClass(String className) {
        try {
            // Use this instance to reload the class
            Class<?> reloadedClass = this.loadClass(className, true); // true to indicate reloading
            logger.info("Class reloaded: {}", reloadedClass.getName());

            // Removed commandInstances logic. It should be handled elsewhere, not in the class loader.

        } catch (ClassNotFoundException e) {
            logger.error("Failed to reload class: {}", className, e);
        }
    }
}