package org.evenmorefish.dimensionfishing.common;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.datatypes.player.McMMOPlayer;
import com.gmail.nossr50.locale.LocaleLoader;
import com.gmail.nossr50.skills.fishing.FishingManager;
import com.gmail.nossr50.util.player.UserManager;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.evenmorefish.dimensionfishing.DimensionFishing;
import org.evenmorefish.dimensionfishing.config.DimensionFishingConfigProvider;
import org.evenmorefish.dimensionfishing.events.LavaFishCaughtEvent;
import org.evenmorefish.dimensionfishing.events.VoidFishCaughtEvent;
import org.evenmorefish.dimensionfishing.state.FishingState;
import org.evenmorefish.dimensionfishing.state.HookState;
import org.evenmorefish.dimensionfishing.state.impl.fishing.NoneFishingState;
import org.evenmorefish.dimensionfishing.state.impl.hook.CatchHookState;
import org.evenmorefish.dimensionfishing.state.impl.hook.WaitHookState;
import org.evenmorefish.dimensionfishing.util.Keys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.Random;

public class TrackedHook {

    private static final Random RANDOM = new Random();

    private final FishHook hook;
    private final Player player;

    private FishingState fishingState = FishingState.NONE;
    private HookState hookState;
    private ArmorStand stand;
    private boolean shouldCustomTick;
    private final int voidRequiredLevel;

    // The duration of ticks the hook should be "pulled" by a fish for.
    private boolean pulled = false;
    private int pullTime = 5;

    private final boolean canLava;
    private final boolean canVoid;

    public TrackedHook(@NotNull Player player, @NotNull FishHook hook, int lureLevel) {
        this.player = player;
        this.hook = hook;
        this.voidRequiredLevel = player.getLocation().getBlockY() - RANDOM.nextInt(4, 6);

        this.hookState = new WaitHookState(this, lureLevel);

        this.canLava = canLavaFish();
        this.canVoid = canVoidFish();
        this.shouldCustomTick = (this.canLava || this.canVoid);
    }

    public @NotNull FishHook getFishHook() {
        return this.hook;
    }

    public @NotNull Player getPlayer() {
        return this.player;
    }

    public @Nullable ArmorStand getStand() {
        return this.stand;
    }

    public @NotNull FishingState getFishingState() {
        return this.fishingState;
    }

    public @NotNull HookState getHookState() {
        return this.hookState;
    }

    public void tick() {
        if (!shouldCustomTick) {
            return;
        }
        if (hook.getOwnerUniqueId() == null || !hook.isValid() || hook.isOnGround() || isHookedToNormalEntity()) {
            this.shouldCustomTick = false;
            return;
        }

        if (pulled) {
            if (pullTime > 0) {
                pullTime--;
            } else {
                setPulled(false);
            }
        }

        if (fishingState instanceof NoneFishingState) {
            if (isLava()) {
                attachToStand();
                this.fishingState = FishingState.LAVA;
            } else if (isVoid()) {
                attachToStand();
                this.fishingState = FishingState.VOID;
            } else if (isWater()) {
                // If in water, vanilla will handle it.
                this.shouldCustomTick = false;
            }
            return;
        }

        this.hookState = hookState.tick(this);
        if (hookState == null) {
            invalidate();
        }
    }

    public void reel() {
        this.shouldCustomTick = false;
        // Store states as they are removed when #invalidate is called
        HookState hookState = this.hookState;
        FishingState fishingState = this.fishingState;
        this.invalidate();
        if (!(hookState instanceof CatchHookState)) {
            return;
        }
        if (checkMcMMOOverfishing()) {
            return;
        }
        if (!fishingState.callEvent(this)) {
            fishingState.playSwallowSound(player);
            invalidate();
        }
    }

    private boolean isHookedToNormalEntity() {
        Entity hooked = hook.getHookedEntity();
        if (hooked == null) {
            return false;
        }
        return !hooked.equals(stand);
    }

    private boolean isLava() {
        if (!this.canLava || hook.getLocation().getBlock().getType() != Material.LAVA) {
            return false;
        }
        if (hook.getLocation().clone().add(0, 1, 0).getBlock().getType() == Material.LAVA) { // If the block above is lava, invalidate.
            invalidate();
            return false;
        }
        return true;
    }

    private boolean isWater() {
        return hook.getLocation().getBlock().getType() == Material.WATER;
    }

    private boolean isVoid() {
        return this.canVoid && hook.getLocation().getBlockY() <= voidRequiredLevel;
    }

    private boolean canLavaFish() {
        DimensionFishingConfigProvider config = DimensionFishing.getInstance().getConfigProvider();
        return config.isLavaEnabled()
            && LavaFishCaughtEvent.hasListeners()
            && config.getLavaPredicate().test(player)
            && FishingState.LAVA.checkPermission(player)
            && FishingState.LAVA.checkWorld(hook.getWorld());
    }

    private boolean canVoidFish() {
        DimensionFishingConfigProvider config = DimensionFishing.getInstance().getConfigProvider();
        return config.isVoidEnabled()
            && VoidFishCaughtEvent.hasListeners()
            && config.getVoidPredicate().test(player)
            && FishingState.VOID.checkPermission(player)
            && FishingState.VOID.checkWorld(hook.getWorld());
    }

    public void invalidate() {
        this.shouldCustomTick = false;
        this.fishingState = null;
        this.hookState = null;
        if (stand != null) {
            stand.remove();
        }
        hook.remove();
    }

    public boolean isShouldCustomTick() {
        return this.shouldCustomTick;
    }

    // ArmorStand shenanigans

    private void attachToStand() {
        if (this.stand != null) {
            return;
        }
        ArmorStand stand = createNewStand(hook.getLocation());
        hook.setHookedEntity(stand);
        stand.addPassenger(hook);
        hook.setVisualFire(false);
        hook.setFireTicks(0);
        this.stand = stand;
    }

    private ArmorStand createNewStand(@NonNull Location location) {
        ArmorStand stand = location.getWorld().spawn(location, ArmorStand.class);
        stand.setInvisible(true);
        stand.setInvulnerable(true);
        stand.setVisualFire(false);
        stand.setPersistent(false);
        stand.setGravity(false);
        stand.setAI(false);
        stand.getPersistentDataContainer().set(
            Keys.STAND_KEY,
            PersistentDataType.BOOLEAN,
            true
        );

        // Set a small scale so the hook doesn't float.
        AttributeInstance scale = stand.getAttribute(Attribute.GENERIC_SCALE);
        if (scale != null) {
            scale.setBaseValue(0.1);
        }

        return stand;
    }

    private boolean checkMcMMOOverfishing() {
        if (!Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            return false;
        }
        if (!DimensionFishing.getInstance().hasMcMMODependency()) {
            return false;
        }
        if (!ExperienceConfig.getInstance().isFishingExploitingPrevented()) {
            return false;
        }
        McMMOPlayer mmoPlayer = UserManager.getPlayer(player);
        if (mmoPlayer == null) {
            return false;
        }
        FishingManager manager = mmoPlayer.getFishingManager();
        manager.processExploiting(this.hook.getLocation().toVector());
        if (manager.isExploitingFishing()) {
            // This was taken directly from mcMMO's source code.
            player.sendMessage(LocaleLoader.getString(
                "Fishing.ScarcityTip",
                ExperienceConfig.getInstance().getFishingExploitingOptionMoveRange())
            );
            return true;
        }
        return false;
    }

    public void setPulled(boolean pulled) {
        if (pulled) {
            stand.teleport(stand.getLocation().add(0, -0.3, 0));
            fishingState.playBiteSound(player);
        } else {
            stand.teleport(stand.getLocation().add(0, 0.3, 0));
        }
        this.pulled = pulled;
    }

}
