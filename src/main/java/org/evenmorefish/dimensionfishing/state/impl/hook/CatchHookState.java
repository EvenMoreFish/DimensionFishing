package org.evenmorefish.dimensionfishing.state.impl.hook;

import org.evenmorefish.dimensionfishing.common.TrackedHook;
import org.evenmorefish.dimensionfishing.state.HookState;
import org.jspecify.annotations.NonNull;

public class CatchHookState implements HookState {

    private int catchTime = 40;

    @Override
    public HookState tick(@NonNull TrackedHook hook) {
        catchTime--;
        return catchTime <= 0 ? null : this;
    }

}
