package org.smartscholars.projectmanager.util;

import org.slf4j.Logger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.io.IOException;
import org.slf4j.LoggerFactory;

public class CustomClassLoader extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(CustomClassLoader.class);

    public CustomClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // Check if the class is from the Java standard library
        if (name.startsWith("java.") || name.startsWith("javax.") || name.startsWith("jdk.")) {
            return super.loadClass(name, resolve);
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
            Class<?> clazz = defineClass(name, classBytes, 0, classBytes.length);
            if (resolve) {
                resolveClass(clazz);
            }
            logger.info("Successfully loaded class with custom class loader: {}", name);
            return clazz;
        } catch (IOException e) {
            logger.error("Error loading class with custom class loader: {}", name, e);
            throw new ClassNotFoundException("Error loading class with custom class loader: " + name, e);
        } catch (NoClassDefFoundError e) {
            logger.error("Dependency not found for: {}", name, e);
            throw new ClassNotFoundException("Dependency not found for: " + name, e);
        }
    }
}