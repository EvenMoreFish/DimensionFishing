package org.evenmorefish.dimensionfishing.config;

import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.evenmorefish.dimensionfishing.util.ParticleFactory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

/**
 * Provides all configs for DimensionFishing.
 */
public interface DimensionFishingConfigProvider {

    boolean isLavaEnabled();

    boolean isVoidEnabled();

    @NonNull List<String> getLavaAllowedWorlds();

    @NonNull List<String> getVoidAllowedWorlds();

    @NonNull Sound getLavaFishingSwallowSound();

    @NonNull Sound getVoidFishingSwallowSound();

    @NonNull Sound getLavaFishingBiteSound();

    @NonNull Sound getVoidFishingBiteSound();

    @Nullable String getLavaFishingPermission();

    @Nullable String getVoidFishingPermission();

    @NonNull ParticleFactory getLavaFishingLureParticles();

    @NonNull ParticleFactory getVoidFishingLureParticles();

    @NonNull Predicate<Player> getLavaPredicate();

    @NonNull Predicate<Player> getVoidPredicate();

    boolean enableWorldGuardBypass();
    
}
