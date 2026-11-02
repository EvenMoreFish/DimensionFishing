package org.evenmorefish.dimensionfishing.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.dimensionfishing.DimensionFishing;
import org.evenmorefish.dimensionfishing.common.TrackedHook;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.Nullable;

public class VoidFishCaughtEvent extends PlayerEvent implements Cancellable {

    private static final HandlerList handlers = new HandlerList();
    private final TrackedHook hook;
    private final PlayerFishEvent originalEvent;

    private boolean cancel;
    private ItemStack caughtItem = null;

    @ApiStatus.Internal
    public VoidFishCaughtEvent(@NonNull TrackedHook hook, @NonNull PlayerFishEvent originalEvent) {
        super(hook.getPlayer());
        this.hook = hook;
        this.originalEvent = originalEvent;
    }

    public @Nullable ItemStack getCaughtItem() {
        return this.caughtItem;
    }

    public void setCaughtItem(@Nullable ItemStack item) {
        this.caughtItem = item;
    }

    public @NonNull TrackedHook getTrackedHook() {
        return this.hook;
    }

    public @NonNull PlayerFishEvent getOriginalEvent() {
        return this.originalEvent;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public @NonNull HandlerList getHandlers() {
        return handlers;
    }

    public static boolean hasListeners() {
        if (handlers.getRegisteredListeners().length == 0) {
            DimensionFishing.getInstance().getLogger().warning("There are no listeners for VoidFishCaughtEvent.");
            return false;
        }
        return true;
    }

    @Override
    public boolean isCancelled() {
        return cancel;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancel = cancel;
    }

}
