package org.evenmorefish.dimensionfishing.integration;

import com.sk89q.worldguard.bukkit.event.entity.DamageEntityEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.evenmorefish.dimensionfishing.DimensionFishing;
import org.evenmorefish.dimensionfishing.util.Keys;

public class WorldGuardIntegration implements Listener {

    /**
     * Listens to the internal {@link DamageEntityEvent} to allow control over bypass.
     */
    @EventHandler
    public void onDamageEntity(DamageEntityEvent event) {
        if (!event.getEntity().getPersistentDataContainer().has(Keys.STAND_KEY)) {
            return;
        }
        if (!DimensionFishing.getInstance().getConfigProvider().enableWorldGuardBypass()) {
            return;
        }
        event.setAllowed(true);
    }

}
