package me.molybdenum.ambience_mini.client.core;


import me.molybdenum.ambience_mini.client.NilMusicManager;
import me.molybdenum.ambience_mini.client.core.networking.ClientNetworkManager;
import me.molybdenum.ambience_mini.client.core.setup.ClientConfig;
import me.molybdenum.ambience_mini.client.core.setup.KeyBindings;
import me.molybdenum.ambience_mini.client.core.state.CombatState;
import me.molybdenum.ambience_mini.client.core.state.LevelState;
import me.molybdenum.ambience_mini.client.core.state.PlayerState;
import me.molybdenum.ambience_mini.client.core.state.ScreenState;
import me.molybdenum.ambience_mini.client.core.util.Notification;
import me.molybdenum.ambience_mini.client.core.render.area.AreaRenderer;
import me.molybdenum.ambience_mini.engine.client.core.BaseClientCore;
import me.molybdenum.ambience_mini.engine.client.core.areas.ClientAreaManager;
import me.molybdenum.ambience_mini.engine.client.core.setup.ServerSetup;
import me.molybdenum.ambience_mini.engine.client.core.util.ClientNameCache;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import org.slf4j.Logger;

public class ClientCore extends BaseClientCore<
        BlockPos, Vec3, BlockState, Entity, KeyMapping, Component,
        Notification, ClientNetworkManager, AreaRenderer, ClientConfig, KeyBindings, PlayerState, LevelState, ScreenState, CombatState >
{
    private static final String OBF_MC_MUSIC_MANAGER = "f_91044_";
    private static final Minecraft mc = Minecraft.getInstance();


    public ClientCore(
            Logger logger,
            ClientNameCache nameCache,
            Notification notification,
            ClientNetworkManager networkManager,
            ClientAreaManager areaManager,
            AreaRenderer renderer,
            ServerSetup serverSetup,
            ClientConfig clientConfig,
            KeyBindings keyBindings,
            PlayerState playerState,
            LevelState levelState,
            ScreenState screenState,
            CombatState combatState
    ) {
        super(logger, nameCache, notification, networkManager, areaManager, renderer, serverSetup, clientConfig, keyBindings, playerState, levelState, screenState, combatState);
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
