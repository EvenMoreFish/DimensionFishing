package org.evenmorefish.dimensionfishing.state.impl.hook;

import org.bukkit.entity.FishHook;
import org.evenmorefish.dimensionfishing.common.TrackedHook;
import org.evenmorefish.dimensionfishing.state.HookState;
import org.jspecify.annotations.NonNull;

import java.util.Random;

public class WaitHookState implements HookState {

    private static final Random RANDOM = new Random();

    private int waitTime;

    public WaitHookState(@NonNull TrackedHook hook, int lureLevel) {
        this.waitTime = fetchWaitTime(hook.getFishHook(), lureLevel);
    }

    @Override
    public HookState tick(@NonNull TrackedHook hook) {
        waitTime--;
        return waitTime <= 0 ? new LureHookState(hook) : this;
    }

    private int fetchWaitTime(@NonNull FishHook hook, int lureLevel) {
        int ticksPerLureLevel = 100;
        int reduceTicks = (lureLevel * ticksPerLureLevel);
        int maxWaitTime = Math.max(0, hook.getMaxWaitTime() - reduceTicks);
        int minWaitTime = Math.max(0, hook.getMinWaitTime() - reduceTicks);

        return (minWaitTime == maxWaitTime) ? minWaitTime : RANDOM.nextInt(minWaitTime, maxWaitTime);
    }

}
