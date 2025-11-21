package me.molybdenum.ambience_mini.core;


import me.molybdenum.ambience_mini.NilMusicManager;
import me.molybdenum.ambience_mini.core.setup.ClientConfig;
import me.molybdenum.ambience_mini.core.setup.KeyBindings;
import me.molybdenum.ambience_mini.core.state.CombatState;
import me.molybdenum.ambience_mini.core.state.LevelState;
import me.molybdenum.ambience_mini.core.state.PlayerState;
import me.molybdenum.ambience_mini.core.state.ScreenState;
import me.molybdenum.ambience_mini.core.util.Notification;
import me.molybdenum.ambience_mini.engine.core.BaseCore;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

public class Core extends BaseCore<
        BlockPos, Vec3, BlockState, Entity, KeyMapping, Component,
        Notification, ClientConfig, KeyBindings, PlayerState, LevelState, ScreenState, CombatState >
{
    private static final String OBF_MC_MUSIC_MANAGER = "f_91044_";
    private static final Minecraft mc = Minecraft.getInstance();


    public Core(Logger logger) {
        super(logger);
    }


    @Override
    public boolean isFocused() {
        return mc.isWindowActive();
    }

    @Override
    protected void disableNativeMusicManager() {
        Minecraft mc = Minecraft.getInstance();
        ObfuscationReflectionHelper.setPrivateValue(
                Minecraft.class, mc, new NilMusicManager(mc), OBF_MC_MUSIC_MANAGER
        );
    }
}
