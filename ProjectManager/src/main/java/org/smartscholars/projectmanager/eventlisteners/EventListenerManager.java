package org.smartscholars.projectmanager.eventlisteners;

import net.dv8tion.jda.api.sharding.ShardManager;

import java.util.List;

public class EventListenerManager {

    private final ShardManager shardManager;
    private final List<IEvent> eventListeners;

    public EventListenerManager(ShardManager shardManager, List<IEvent> eventListeners) {
        this.shardManager = shardManager;
        this.eventListeners = eventListeners;
    }

    public void registerEventListeners() {
        eventListeners.forEach(shardManager::addEventListener);
    }
}
