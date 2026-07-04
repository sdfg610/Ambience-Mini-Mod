package me.molybdenum.ambience_mini.v1_20_1;

import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;

public class Utils {
    public static boolean isCombatableEntity(Entity entity, Player player) {
        return (entity instanceof Monster || entity instanceof NeutralMob)
                && !(entity instanceof TamableAnimal tam && player != null && player.getUUID() == tam.getOwnerUUID())
                && entity.isAlive();
    }
}
