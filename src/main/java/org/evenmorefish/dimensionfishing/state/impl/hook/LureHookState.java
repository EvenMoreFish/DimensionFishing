package org.evenmorefish.dimensionfishing.state.impl.hook;

import org.bukkit.Location;
import org.evenmorefish.dimensionfishing.common.TrackedHook;
import org.evenmorefish.dimensionfishing.state.HookState;
import org.evenmorefish.dimensionfishing.util.DirectionUtil;
import org.evenmorefish.dimensionfishing.util.ParticleFactory;
import org.jspecify.annotations.NonNull;

public class LureHookState implements HookState {

    private int lureTime;

    private final Location hookLocation;
    private final Location startLocation;
    private Location currentLocation;
    private final double distance;
    private final int yaw;

    private final ParticleFactory particles;

    public LureHookState(@NonNull TrackedHook hook) {
        this.lureTime = RANDOM.nextInt(hook.getFishHook().getMinLureTime(), hook.getFishHook().getMaxLureTime());
        this.hookLocation = hook.getFishHook().getLocation().clone();
        this.distance = RANDOM.nextDouble(4D, 6D);
        this.yaw = RANDOM.nextInt(0, 359);

        Location hookLocation = hook.getFishHook().getLocation().clone();
        hookLocation.setYaw(this.yaw);
        this.startLocation = DirectionUtil.forwardFlat(hookLocation, distance);
        this.currentLocation = this.startLocation;

        this.particles = hook.getFishingState().getLureParticles();
    }

    @Override
    public HookState tick(@NonNull TrackedHook hook) {
        if (particles != null) {
            particles.show(currentLocation);
        }
        updateLocation();

        lureTime--;
        if (lureTime <= 0) {
            hook.setPulled(true);
            return new CatchHookState();
        } else {
            return this;
        }
    }

    public int getRemainingLureTime() {
        return lureTime;
    }

    private void updateLocation() {
        if (lureTime <= 0) {
            return;
        }
        Location hookLocation = this.hookLocation.clone();
        hookLocation.setYaw(this.yaw);
        this.currentLocation = DirectionUtil.forwardFlat(hookLocation, lureTime / distance).add(0, 0.1, 0);
    }

}
