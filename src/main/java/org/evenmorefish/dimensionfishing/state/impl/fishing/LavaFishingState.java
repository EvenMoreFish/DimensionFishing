package org.evenmorefish.dimensionfishing.state.impl.fishing;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.dimensionfishing.DimensionFishing;
import org.evenmorefish.dimensionfishing.common.TrackedHook;
import org.evenmorefish.dimensionfishing.events.LavaFishCaughtEvent;
import org.evenmorefish.dimensionfishing.events.VoidFishCaughtEvent;
import org.evenmorefish.dimensionfishing.state.FishingState;
import org.evenmorefish.dimensionfishing.util.DirectionUtil;
import org.evenmorefish.dimensionfishing.util.ParticleFactory;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class LavaFishingState implements FishingState {

    @Override
    public void playBiteSound(@NonNull Player player) {
        player.playSound(DimensionFishing.getInstance().getConfigProvider().getLavaFishingBiteSound());
    }

    @Override
    public void playSwallowSound(@NonNull Player player) {
        player.playSound(DimensionFishing.getInstance().getConfigProvider().getLavaFishingSwallowSound());
    }

    @Override
    public @NonNull ParticleFactory getLureParticles() {
        return DimensionFishing.getInstance().getConfigProvider().getLavaFishingLureParticles();
    }

    @Override
    public boolean callEvent(@NonNull TrackedHook hook) {
        LavaFishCaughtEvent event = new LavaFishCaughtEvent(hook);
        event.callEvent();
        if (event.isCancelled()) {
            return false;
        }
        ItemStack caughtItem = event.getCaughtItem();
        if (caughtItem != null && !caughtItem.isEmpty()) {
            DirectionUtil.launchToPlayer(caughtItem, hook.getFishHook().getLocation(), hook.getPlayer());
        }
        return true;
    }

    @Override
    public boolean checkPermission(@NonNull Player player) {
        String permission = DimensionFishing.getInstance().getConfigProvider().getLavaFishingPermission();
        return permission == null || player.hasPermission(permission);
    }

    @Override
    public boolean checkWorld(@NonNull World hookWorld) {
        List<String> worlds = DimensionFishing.getInstance().getConfigProvider().getLavaAllowedWorlds();
        if (worlds.isEmpty()) {
            return hookWorld.getEnvironment() == World.Environment.NETHER;
        }
        return worlds.stream()
            .map(this::parseWorld)
            .anyMatch(hookWorld::equals);
    }


}
