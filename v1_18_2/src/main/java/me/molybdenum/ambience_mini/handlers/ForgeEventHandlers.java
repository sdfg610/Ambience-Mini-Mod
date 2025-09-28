package me.molybdenum.ambience_mini.handlers;

import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.state.Screens;
import net.minecraft.client.gui.screens.*;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.realms.DisconnectedRealmsScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.client.event.ScreenOpenEvent;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = Common.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventHandlers
{
    public static Screens currentScreen = Screens.MAIN_MENU;


    //
    // Client events
    //
    @SubscribeEvent
    @OnlyIn(value = Dist.CLIENT)
    public static void onScreenChanged(final ScreenOpenEvent event) {
        Screen screen = event.getScreen();

        if (screen instanceof ProgressScreen || screen instanceof ConnectScreen || screen instanceof LevelLoadingScreen || screen instanceof ReceivingLevelScreen)
            currentScreen = Screens.JOINING;
        else if (screen instanceof DisconnectedScreen || screen instanceof DisconnectedRealmsScreen)
            currentScreen = Screens.DISCONNECTED;
        else if (screen instanceof PauseScreen)
            currentScreen = Screens.PAUSE;
        else if (screen instanceof WinScreen)
            currentScreen = Screens.CREDITS;
        else if (screen instanceof TitleScreen || screen instanceof JoinMultiplayerScreen || screen instanceof DirectJoinServerScreen || screen instanceof SelectWorldScreen || screen instanceof CreateWorldScreen) {
            currentScreen = Screens.MAIN_MENU;
        }
    }

    @SubscribeEvent
    @OnlyIn(value = Dist.CLIENT)
    public static void onDimensionChanged(final PlayerEvent.PlayerChangedDimensionEvent event) {
        // TODO: Pause music while changing dimensions to avoid small hiccup when entering directly into a village or boss fight.
    }



    //
    // Server events
    //
    @SubscribeEvent
    @OnlyIn(value = Dist.DEDICATED_SERVER)
    public static void onEntitySetAttackTargetEvent(final LivingChangeTargetEvent event) {

    }

    @SubscribeEvent
    @OnlyIn(value = Dist.DEDICATED_SERVER)
    public static void onPlayerAttackEvent(final AttackEntityEvent event) {

    }

    @SubscribeEvent
    @OnlyIn(value = Dist.DEDICATED_SERVER)
    public static void onEntityDeath(final LivingDeathEvent event) {

    }
}
