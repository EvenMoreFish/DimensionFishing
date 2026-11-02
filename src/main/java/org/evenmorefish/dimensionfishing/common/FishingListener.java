package org.evenmorefish.dimensionfishing.common;

import com.destroystokyo.paper.event.entity.EntityRemoveFromWorldEvent;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.dimensionfishing.util.Keys;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.Nullable;

public class FishingListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onFish(PlayerFishEvent event) {
        if (event.isCancelled()) {
            return;
        }
        switch (event.getState()) {
            case FISHING -> {
                int lureLevel = fetchLureLevel(event.getPlayer(), event.getHand());
                TrackedHook hook = new TrackedHook(event.getPlayer(), event.getHook(), lureLevel);
                if (!hook.isShouldCustomTick()) {
                    return;
                }
                HookManager.getInstance().trackHook(hook);
            }
            // This is used instead of CAUGHT_FISH as the hook is attached to an armor stand.
            case CAUGHT_ENTITY -> HookManager.getInstance().checkCaughtEntity(event);
        }
    }

    // Cleans up any armor stands if the hook is removed from the world.
    @EventHandler
    public void onHookRemoved(EntityRemoveFromWorldEvent event) {
        if (!(event.getEntity() instanceof FishHook hook)) {
            return;
        }
        Entity hooked = hook.getHookedEntity();
        if (hooked != null && hooked.getPersistentDataContainer().has(Keys.STAND_KEY)) {
            hooked.remove();
        }
    }

    private int fetchLureLevel(@NonNull Player player, @Nullable EquipmentSlot slot) {
        if (slot == null) {
            return 0;
        }
        ItemStack item = player.getInventory().getItem(slot);
        return item.getEnchantmentLevel(Enchantment.LURE);
    }

}
