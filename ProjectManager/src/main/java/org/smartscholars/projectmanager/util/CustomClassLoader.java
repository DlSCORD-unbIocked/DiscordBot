package org.smartscholars.projectmanager.util;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CustomClassLoader extends ClassLoader {
    private static final Logger logger = LoggerFactory.getLogger(CustomClassLoader.class);
    private final Map<String, Class<?>> loadedClasses = new HashMap<>();
    private final Map<String, Long> classModificationTimes = new HashMap<>();

    public CustomClassLoader(ClassLoader parent) {
        super(parent);
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        try {
            String classPath = "ProjectManager/target/classes/" + name.replace('.', '/') + ".class";
            Path path = Paths.get(classPath);
            if (Files.exists(path)) {
                FileTime fileTime = Files.getLastModifiedTime(path);
                long lastModified = fileTime.toMillis();


                if (classModificationTimes.containsKey(name) && classModificationTimes.get(name) == lastModified) {
                    return loadedClasses.get(name);
                }
                else {
                    classModificationTimes.put(name, lastModified);
                }
            }
        } catch (IOException e) {
            throw new ClassNotFoundException("Failed to check class file modification time for " + name, e);
        }
        Class<?> clazz = super.loadClass(name, resolve);
        loadedClasses.put(name, clazz);
        return clazz;
    }

    public void reloadClass(String className) {
        try {
            Class<?> reloadedClass = this.loadClass(className, true);
            logger.info("Class reloaded: {}", reloadedClass.getName());

        } catch (ClassNotFoundException e) {
            logger.error("Failed to reload class: {}", className, e);
        }
    }
}