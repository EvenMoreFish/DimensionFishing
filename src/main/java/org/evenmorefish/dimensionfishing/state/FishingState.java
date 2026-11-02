package org.evenmorefish.dimensionfishing.state;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerFishEvent;
import org.evenmorefish.dimensionfishing.common.TrackedHook;
import org.evenmorefish.dimensionfishing.state.impl.fishing.LavaFishingState;
import org.evenmorefish.dimensionfishing.state.impl.fishing.NoneFishingState;
import org.evenmorefish.dimensionfishing.state.impl.fishing.VoidFishingState;
import org.evenmorefish.dimensionfishing.util.ParticleFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public interface FishingState {

    NoneFishingState NONE = new NoneFishingState();
    LavaFishingState LAVA = new LavaFishingState();
    VoidFishingState VOID = new VoidFishingState();

    void playBiteSound(@NonNull Player player);

    void playSwallowSound(@NonNull Player player);

    @NonNull ParticleFactory getLureParticles();

    boolean callEvent(@NonNull TrackedHook hook, @NonNull PlayerFishEvent originalEvent);

    boolean checkPermission(@NonNull Player player);

    boolean checkWorld(@NonNull World hookWorld);

    default @Nullable World parseWorld(@NonNull String name) {
        if (name.contains(":")) {
            NamespacedKey key = NamespacedKey.fromString(name);
            if (key != null) {
                return Bukkit.getWorld(key);
            }
        }
        return Bukkit.getWorld(name);
    }

}
