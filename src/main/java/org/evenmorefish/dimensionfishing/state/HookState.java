package org.evenmorefish.dimensionfishing.state;

import org.evenmorefish.dimensionfishing.common.TrackedHook;
import org.jspecify.annotations.NonNull;

import java.util.Random;

public interface HookState {

    Random RANDOM = new Random();

    HookState tick(@NonNull TrackedHook hook);

}
