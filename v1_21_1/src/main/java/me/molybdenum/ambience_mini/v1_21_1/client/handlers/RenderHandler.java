package me.molybdenum.ambience_mini.v1_21_1.client.handlers;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.PoseStack;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.v1_21_1.AmbienceMini;
import me.molybdenum.ambience_mini.v1_21_1.client.core.render.area.AreaRenderer;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.NotNull;


@EventBusSubscriber(modid = Common.MOD_ID, value={Dist.CLIENT})
public class RenderHandler
{
    private static final Window window = Minecraft.getInstance().getWindow();
    private static AreaRenderer renderer;


    static {
        AmbienceMini.registerOnClientCoreInitListener(
                () -> renderer = AmbienceMini.clientCore.areaRenderer
        );
    }


    @SubscribeEvent
    public static void onRenderLevelState(RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRIPWIRE_BLOCKS || renderer.isViewHidden())
            return;

        Vec3 viewFrom = event.getCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-viewFrom.x, -viewFrom.y, -viewFrom.z);

        renderer.setup(poseStack.last(), event.getFrustum());
        renderer.tickAndRender(viewFrom, event.getCamera().getXRot(), event.getCamera().getYRot());

        poseStack.popPose();
    }


    public static void renderAreaOverlay(@NotNull GuiGraphics guiGraphics, @NotNull DeltaTracker ignored) {
        renderer.setup(guiGraphics.pose().last(), null);
        renderer.renderAreaInfoOverlay(window.getGuiScaledWidth(), window.getGuiScaledHeight());
    }
}
