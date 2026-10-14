package org.evenmorefish.dimensionfishing.state.impl.fishing;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.evenmorefish.dimensionfishing.common.TrackedHook;
import org.evenmorefish.dimensionfishing.state.FishingState;
import org.evenmorefish.dimensionfishing.util.ParticleFactory;
import org.jspecify.annotations.NonNull;

public class NoneFishingState implements FishingState {

    private final ParticleFactory lureParticles = new ParticleFactory();

    @Override
    public void playBiteSound(@NonNull Player player) {}

    @Override
    public void playSwallowSound(@NonNull Player player) {}

    @Override
    public @NonNull ParticleFactory getLureParticles() {
        return this.lureParticles;
    }

    @Override
    public boolean callEvent(@NonNull TrackedHook hook) {
        return true;
    }

    @Override
    public boolean checkPermission(@NonNull Player player) {
        return true;
    }

    @Override
    public boolean checkWorld(@NonNull World hookWorld) {
        return true;
    }

}
