package org.evenmorefish.dimensionfishing.util;

import com.destroystokyo.paper.ParticleBuilder;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Vibration;
import org.bukkit.block.data.BlockData;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.dimensionfishing.DimensionFishing;
import org.jspecify.annotations.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * Builds particles from a config file.
 */
public class ParticleFactory {

    private final List<ParticleBuilder> particles = new ArrayList<>();

    public ParticleFactory() {}

    public ParticleFactory(@NonNull List<Map<?, ?>> mapList) {
        loadParticles(mapList);
    }

    private void loadParticles(List<Map<?, ?>> mapList) {
        this.particles.clear();
        mapList.forEach(map -> {
            Particle particle = fetchParticle(map.get("particle"));
            if (particle == null) {
                DimensionFishing.getInstance().getLogger().warning("Particle was not configured properly.");
                return;
            }
            if (isInvalidParticleData(particle)) {
                DimensionFishing.getInstance().getLogger().warning("Particle " + particle + " cannot be used in DimensionFishing.");
                return;
            }
            ParticleBuilder builder = particle.builder();

            applyAmount(map.get("amount"), builder);
            applyColor(map.get("color"), builder);
            applyColorTransition(map.get("color-transition"), builder);
            applyExtra(map.get("extra"), builder);
            applyDataType(map.get("data"), builder);

            this.particles.add(builder);
        });
    }

    public void show(@NonNull Location location) {
        Iterator<ParticleBuilder> iterator = particles.iterator();
        while (iterator.hasNext()) {
            ParticleBuilder particle = iterator.next();
            try {
                particle.clone().location(location).spawn();
            } catch (IllegalArgumentException exception) {
                DimensionFishing.getInstance().getLogger().warning("Failed to spawn particle. It could be missing required data. It will no longer be spawned.");
                iterator.remove();
            }
        }
    }

    private @Nullable Particle fetchParticle(@Nullable Object name) {
        if (name == null) {
            return null;
        }
        try {
            return Particle.valueOf(name.toString().toUpperCase());
        } catch (IllegalArgumentException exception) {
            DimensionFishing.getInstance().getLogger().warning(name + " is not a valid particle.");
            return null;
        }
    }

    private void applyColor(@Nullable Object color, @NonNull ParticleBuilder builder) {
        if (color == null) {
            return;
        }
        Color finalColor = fetchColorFromHex(color.toString());
        if (finalColor == null) {
            return;
        }
        try {
            builder.color(finalColor);
        } catch (IllegalStateException exception) {
            DimensionFishing.getInstance().getLogger().warning("Color is not supported on this particle.");
        }
    }

    private void applyColorTransition(@Nullable Object transition, @NonNull ParticleBuilder builder) {
        if (transition == null) {
            return;
        }
        if (builder.particle().getDataType() != Particle.DustTransition.class) {
            DimensionFishing.getInstance().getLogger().warning("Color Transition is not supported on this particle.");
            return;
        }
        String string = transition.toString();
        String[] split = string.split(",");
        if (split.length != 3) {
            DimensionFishing.getInstance().getLogger().warning(string + " is not a valid transition.");
            return;
        }
        Color from = fetchColorFromHex(split[0]);
        Color to = fetchColorFromHex(split[1]);
        if (from == null || to == null) {
            return;
        }
        try {
            float size = Float.parseFloat(split[2]);
            builder.colorTransition(from, to, size);
        } catch (NumberFormatException exception) {
            DimensionFishing.getInstance().getLogger().warning(split[2] + " is not a valid float value.");
        }
    }

    private void applyAmount(@Nullable Object amount, @NonNull ParticleBuilder builder) {
        if (amount == null) {
            builder.count(1);
            return;
        }
        try {
            builder.count(Integer.parseInt(amount.toString()));
        } catch (NumberFormatException exception) {
            DimensionFishing.getInstance().getLogger().warning(amount + " is not a valid amount.");
        }
    }

    private void applyExtra(@Nullable Object extra, @NonNull ParticleBuilder builder) {
        if (extra == null) {
            return;
        }
        try {
            builder.extra(Double.parseDouble(extra.toString()));
        } catch (NumberFormatException exception) {
            DimensionFishing.getInstance().getLogger().warning(extra + " is not a valid double value.");
        }
    }

    private @Nullable Color fetchColorFromHex(@NonNull String hex) {
        try {
            java.awt.Color awtColor = java.awt.Color.decode(hex);
            int r = awtColor.getRed();
            int g = awtColor.getGreen();
            int b = awtColor.getBlue();
            return Color.fromRGB(r, g, b);
        } catch (NumberFormatException exception) {
            DimensionFishing.getInstance().getLogger().warning(hex + " is not a valid color.");
            return null;
        }
    }

    private void applyDataType(@Nullable Object object, @NonNull ParticleBuilder builder) {
        if (object == null) {
            return;
        }
        String string = object.toString();
        Class<?> clazz = builder.particle().getDataType();

        // Big ugly if chain incoming :D
        try {
            if (clazz == Float.class) {
                builder.data(Float.parseFloat(string));
            } else if (clazz == Integer.class) {
                builder.data(Integer.parseInt(string));
            } else if (clazz == BlockData.class) {
                Material material = Material.valueOf(string.toUpperCase());
                if (material.isBlock()) {
                    builder.data(material.createBlockData());
                }
            } else if (clazz == ItemStack.class) {
                Material material = Material.valueOf(string.toUpperCase());
                if (material.isItem()) {
                    builder.data(ItemStack.of(material));
                }
            }
        } catch (IllegalArgumentException exception) {
            DimensionFishing.getInstance().getLogger().log(Level.WARNING, string + " is not valid particle data.", exception);
        }
    }

    /**
     * Checks for invalid particle data classes.
     * If it cannot be configured, we return false.
     */
    private boolean isInvalidParticleData(@NonNull Particle particle) {
        Class<?> clazz = particle.getDataType();
        // Both of these require locations and those can't be configured in an understandable way.
        return (clazz == Vibration.class || clazz.getName().equals("org.bukkit.Particle.Trail"));
    }

}
