package me.molybdenum.ambience_mini.handlers;

import com.mojang.blaze3d.vertex.*;
import me.molybdenum.ambience_mini.AmbienceMini;
import me.molybdenum.ambience_mini.core.area.Renderer;
import me.molybdenum.ambience_mini.core.state.LevelState;
import me.molybdenum.ambience_mini.engine.Common;
import me.molybdenum.ambience_mini.engine.core.areas.Point;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = Common.MOD_ID, value={Dist.CLIENT})
public class RenderHandler
{
    private static final LevelState level = AmbienceMini.core.levelState;
    private static final Renderer renderer = new Renderer();

    @SubscribeEvent
    public static void onRenderLayerPost(RenderLevelStageEvent event)
    {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES)
            return;

        Vec3 viewFrom = event.getCamera().getPosition();
        BlockPos blockPos = level.getAirJustBeforeLookedAtBlockIfInRange(viewFrom, event.getCamera().getXRot(), event.getCamera().getYRot(), Common.AREA_SELECTION_RANGE);
        if (blockPos == null)
            return;

        PoseStack poseStack = event.getPoseStack();
        poseStack.pushPose();
        poseStack.translate(-viewFrom.x, -viewFrom.y, -viewFrom.z);

        renderer.setPoseAndNormal(poseStack.last());
        renderer.renderPointerBox(new Point(blockPos.getX(), blockPos.getY(), blockPos.getZ()));

        poseStack.popPose();
    }
}
