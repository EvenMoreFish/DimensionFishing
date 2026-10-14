package org.evenmorefish.dimensionfishing.state;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.evenmorefish.dimensionfishing.common.TrackedHook;
import org.evenmorefish.dimensionfishing.state.impl.fishing.LavaFishingState;
import org.evenmorefish.dimensionfishing.state.impl.fishing.NoneFishingState;
import org.evenmorefish.dimensionfishing.state.impl.fishing.VoidFishingState;
import org.evenmorefish.dimensionfishing.util.ParticleFactory;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@ApiStatus.Internal
public interface FishingState {

    NoneFishingState NONE = new NoneFishingState();
    LavaFishingState LAVA = new LavaFishingState();
    VoidFishingState VOID = new VoidFishingState();

    void playBiteSound(@NotNull Player player);

    void playSwallowSound(@NotNull Player player);

    @NotNull ParticleFactory getLureParticles();

    boolean callEvent(@NotNull TrackedHook hook);

    boolean checkPermission(@NotNull Player player);

    boolean checkWorld(@NotNull World hookWorld);

    default @Nullable World parseWorld(@NotNull String name) {
        if (name.contains(":")) {
            NamespacedKey key = NamespacedKey.fromString(name);
            if (key != null) {
                return Bukkit.getWorld(key);
            }
        }
        return Bukkit.getWorld(name);
    }

}
