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
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
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
    private TextDisplay display;
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

    /**
     * @return The invisible TextDisplay this hook is attached to.
     */
    public @Nullable TextDisplay getDisplay() {
        return this.display;
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
                attachToDisplay();
                this.fishingState = FishingState.LAVA;
            } else if (isVoid()) {
                attachToDisplay();
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
        setPulled(false);
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
        return !hooked.equals(display);
    }

    private boolean isLava() {
        if (!this.canLava || hook.getLocation().getBlock().getType() != Material.LAVA) {
            return false;
        }
        Material above = hook.getLocation().clone().add(0, 1, 0).getBlock().getType();
        Material above2 = hook.getLocation().clone().add(0, 2, 0).getBlock().getType();
        if (above == Material.LAVA && above2 == Material.LAVA) { // If in a lava pillar, invalidate.
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
        if (display != null) {
            display.remove();
        }
        hook.remove();
    }

    public boolean isShouldCustomTick() {
        return this.shouldCustomTick;
    }

    // ArmorStand shenanigans

    private void attachToDisplay() {
        if (this.display != null) {
            return;
        }

        Location displayLocation = hook.getLocation();
        while (displayLocation.getBlock().getType() == Material.LAVA) {
            displayLocation.setY(displayLocation.toCenterLocation().getY() + 0.6);
        }

        TextDisplay display = createNewDisplay(displayLocation);
        hook.setHookedEntity(display);
        display.addPassenger(hook);
        hook.setVisualFire(false);
        hook.setFireTicks(0);
        this.display = display;
    }

    private TextDisplay createNewDisplay(@NonNull Location location) {
        TextDisplay display = location.getWorld().spawn(location, TextDisplay.class);
        display.setInvisible(true);
        display.setInvulnerable(true);
        display.setVisualFire(false);
        display.setPersistent(false);
        display.setGravity(false);
        display.getPersistentDataContainer().set(
            Keys.STAND_KEY,
            PersistentDataType.BOOLEAN,
            true
        );

        return display;
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
        if (this.pulled == pulled) {
            return;
        }
        if (pulled) {
            display.teleport(display.getLocation().add(0, -0.3, 0));
            fishingState.playBiteSound(player);
        } else {
            display.teleport(display.getLocation().add(0, 0.3, 0));
        }
        this.pulled = pulled;
    }

}
