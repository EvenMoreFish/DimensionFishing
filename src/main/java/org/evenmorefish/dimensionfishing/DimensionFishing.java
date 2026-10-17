package org.evenmorefish.dimensionfishing;

import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import io.papermc.paper.plugin.configuration.PluginMeta;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.evenmorefish.dimensionfishing.common.FishingListener;
import org.evenmorefish.dimensionfishing.common.HookManager;
import org.evenmorefish.dimensionfishing.config.DimensionFishingConfigProvider;
import org.evenmorefish.dimensionfishing.integration.WorldGuardIntegration;
import org.evenmorefish.dimensionfishing.state.FishingState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Logger;

public class DimensionFishing {

    private static DimensionFishing INSTANCE;

    private final JavaPlugin plugin;
    private final Metrics metrics;
    private final Logger logger;
    private final DimensionFishingConfigProvider configProvider;

    private boolean hasMcMMODependency;
    private boolean hasWorldGuardDependency;

    public DimensionFishing(@NotNull JavaPlugin plugin, @NotNull DimensionFishingConfigProvider configProvider) {
        if (INSTANCE != null) {
            throw new UnsupportedOperationException(getClass().getName() + " has already been assigned!");
        }
        INSTANCE = this;
        this.plugin = plugin;
        this.configProvider = configProvider;
        this.logger = Logger.getLogger("DimensionFishing via " + plugin.getName());
        this.metrics = new Metrics(plugin, 32045);
        setupMetrics();

        try {
            Class.forName("com.gmail.nossr50.mcMMO");
            this.hasMcMMODependency = true;
        } catch (ClassNotFoundException ex) {
            this.hasMcMMODependency = false;
        }
        try {
            Class.forName("com.sk89q.worldguard.bukkit.WorldGuardPlugin");
            hasWorldGuardDependency = true;
        } catch (ClassNotFoundException ignore) {
            hasWorldGuardDependency = false;
        }
    }

    public static @NotNull DimensionFishing getInstance() {
        if (INSTANCE == null) {
            throw new IllegalStateException(DimensionFishing.class.getSimpleName() + " has not been assigned!");
        }
        return INSTANCE;
    }

    private void setupMetrics() {
        this.metrics.addCustomChart(
            new SimplePie("typesEnabled", () -> {
                boolean lavaEnabled = configProvider.isLavaEnabled();
                boolean voidEnabled = configProvider.isVoidEnabled();
                if (lavaEnabled && voidEnabled) {
                    return "Both";
                } else if (lavaEnabled) {
                    return "Lava";
                } else if (voidEnabled) {
                    return "Void";
                }
                return "None";
            })
        );
    }

    public @NotNull JavaPlugin getPlugin() {
        return this.plugin;
    }

    public @NotNull DimensionFishingConfigProvider getConfigProvider() {
        return this.configProvider;
    }

    public void load() {}

    public void enable() {
        HookManager.getInstance().load();
        Bukkit.getPluginManager().registerEvents(new FishingListener(), plugin);

        if (hasWorldGuardDependency) {
            Bukkit.getPluginManager().registerEvents(new WorldGuardIntegration(), plugin);
        }
    }

    public void disable() {
        HookManager.getInstance().shutdown();
    }

    public void reload(@Nullable CommandSender sender) {
        if (sender instanceof Player player) {
            FishingState.LAVA.getLureParticles().show(player.getLocation());
            FishingState.VOID.getLureParticles().show(player.getLocation());
        }
    }

    public Logger getLogger() {
        return this.logger;
    }

    public boolean hasMcMMODependency() {
        return this.hasMcMMODependency;
    }

    public boolean hasWorldGuardDependency() {
        return this.hasWorldGuardDependency;
    }

}
