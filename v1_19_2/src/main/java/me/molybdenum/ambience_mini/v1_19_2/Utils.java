package me.molybdenum.ambience_mini.v1_19_2;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Monster;

public class Utils {
    private static final Minecraft mc = Minecraft.getInstance();

    public static boolean isCombatableEntity(Entity entity) {
        return (entity instanceof Monster || entity instanceof NeutralMob)
                && !(entity instanceof TamableAnimal tam && mc.player != null && mc.player.getUUID() == tam.getOwnerUUID())
                && entity.isAlive();
    }
}
