package org.evenmorefish.dimensionfishing.util;

import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public class DirectionUtil {

    public static Location forwardFlat(@NotNull Location location, double distance) {
        Location loc = location.clone();
        loc.setPitch(0);
        Vector vector = loc.getDirection().multiply(distance);
        return location.clone().add(vector);
    }

    public static void launchToPlayer(@NonNull ItemStack item, @NonNull Location startLocation, @NonNull Player player) {
        Location endLocation = player.getEyeLocation().clone();
        if (!startLocation.getWorld().equals(endLocation.getWorld())) {
            return;
        }
        Item entity = startLocation.getWorld().dropItem(startLocation, item);
        double xa = endLocation.getX() - startLocation.getX();
        double ya = endLocation.getY() - startLocation.getY();
        double za = endLocation.getZ() - startLocation.getZ();
        double speed = 0.1;

        Vector vector = new Vector(xa * speed, ya * speed + Math.sqrt(Math.sqrt(xa * xa + ya * ya + za * za)) * 0.08, za * speed);
        entity.setVelocity(vector);
        entity.setOwner(player.getUniqueId());
    }

}
