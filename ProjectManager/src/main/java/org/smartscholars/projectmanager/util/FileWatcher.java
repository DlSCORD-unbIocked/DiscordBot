package org.smartscholars.projectmanager.util;

import net.dv8tion.jda.api.entities.Guild;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.smartscholars.projectmanager.commands.CommandManager;

import java.io.IOException;
import java.nio.file.*;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

public class FileWatcher implements Runnable {
    private final Logger logger = LoggerFactory.getLogger(FileWatcher.class);
    private final Path pathToWatch;
    private final CommandManager commandManager;
    private long lastReloadTime = 0;

    public FileWatcher(Path pathToWatch, CommandManager commandManager) {
        this.pathToWatch = pathToWatch;
        this.commandManager = commandManager;
    }

    @Override
    public void run() {
        if (!Files.exists(pathToWatch)) {
            logger.error("Path does not exist: {}", pathToWatch);
            return;
        }
        if (!Files.isDirectory(pathToWatch)) {
            logger.error("Path is not a directory: {}", pathToWatch);
            return;
        }
        try (WatchService watchService = FileSystems.getDefault().newWatchService()) {
            pathToWatch.register(watchService, ENTRY_MODIFY);
            while (!Thread.currentThread().isInterrupted()) {
                WatchKey key = watchService.take();
                for (WatchEvent<?> event : key.pollEvents()) {
                    Path changed = (Path) event.context();
                    if (changed.endsWith("commands.config")) {
                        long currentTime = System.currentTimeMillis();
                        long reloadDelay = 500;
                        if (currentTime - lastReloadTime > reloadDelay) {
                            Guild guild = CommandManager.getJda().getGuildById("1086425022245118033");
                            commandManager.reloadCommands(guild);
                            logger.info("Reloaded commands");
                            lastReloadTime = currentTime;
                        }
                    }
                }
                key.reset();
            }
        }
        catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Error while watching file", e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}