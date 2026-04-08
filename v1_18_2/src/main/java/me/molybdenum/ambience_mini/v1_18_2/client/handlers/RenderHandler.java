package me.molybdenum.ambience_mini.v1_18_2.client.handlers;

import com.mojang.blaze3d.vertex.PoseStack;
import me.molybdenum.ambience_mini.v1_18_2.AmbienceMini;
import me.molybdenum.ambience_mini.v1_18_2.client.core.render.area.AreaRenderer;
import me.molybdenum.ambience_mini.engine.shared.Common;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.gui.ForgeIngameGui;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Common.MOD_ID, value={Dist.CLIENT})
public class RenderHandler
{
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

    public static void renderAreaOverlay(ForgeIngameGui ignored1, PoseStack poseStack, float ignored2, int screenWidth, int screenHeight) {
        renderer.setup(poseStack.last(), null);
        renderer.renderAreaInfoOverlay(screenWidth, screenHeight);
    }
}
