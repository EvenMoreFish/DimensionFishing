package org.evenmorefish.dimensionfishing.common;

import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.scheduler.BukkitTask;
import org.evenmorefish.dimensionfishing.DimensionFishing;
import org.evenmorefish.dimensionfishing.util.Keys;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NonNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class HookManager {

    private static final HookManager instance = new HookManager();
    private BukkitTask task;
    private final Map<UUID, TrackedHook> trackedHooks = new HashMap<>();

    private HookManager() {}

    public static @NonNull HookManager getInstance() {
        return instance;
    }

    public void load() {
        this.task = Bukkit.getScheduler().runTaskTimer(
            DimensionFishing.getInstance().getPlugin(),
            () -> {
                Iterator<TrackedHook> iterator = trackedHooks.values().iterator();
                while (iterator.hasNext()) {
                    TrackedHook hook = iterator.next();
                    hook.tick();
                    if (!hook.isShouldCustomTick()) {
                        iterator.remove();
                    }
                }
            },
            1L,
            1L
        );
    }

    public void shutdown() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void trackHook(@NonNull TrackedHook hook) {
        this.trackedHooks.put(hook.getFishHook().getUniqueId(), hook);
    }

    public void checkCaughtEntity(@NonNull PlayerFishEvent event) {
        Entity entity = event.getCaught();
        if (entity == null) {
            return;
        }
        if (entity.getPersistentDataContainer().has(Keys.STAND_KEY)) {
            TrackedHook tracked = trackedHooks.remove(event.getHook().getUniqueId());
            if (tracked == null) {
                return;
            }
            tracked.reel(event);
            event.setCancelled(true);
        }
    }

}
