package me.molybdenum.ambience_mini.v1_19_2.client.core.render.area;

import com.mojang.blaze3d.vertex.PoseStack;
import me.molybdenum.ambience_mini.engine.client.core.locations.AreaHelper;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.BaseAreaRenderer;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.Cube;
import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.core.areas.Area;
import me.molybdenum.ambience_mini.v1_19_2.client.core.render.drawer.Drawer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;


public class AreaRenderer extends BaseAreaRenderer<Vec3, BlockPos, Screen>
{
    private final Minecraft mc = Minecraft.getInstance();
    private final Drawer drawer;
    private Frustum frustum;


    public AreaRenderer(Drawer drawer) {
        super(drawer);
        this.drawer = drawer;
    }

    public void setup(PoseStack.Pose pose, Frustum frustum) {
        this.drawer.setup(pose);
        this.frustum = frustum;
    }


    @Override
    protected boolean isVisible(Cube cube) {
        var from = cube.p;
        var to = cube.pXYZ;
        return frustum.isVisible(new AABB(from.x(), from.y(), from.z(), to.x(), to.y(), to.z()));
    }


    @Override
    protected String getDimensionID() {
        assert mc.level != null;
        return mc.level.dimension().location().toString();
    }

    @Override
    public BlockPos getAirJustBeforeLookedAtBlockIfInRange(Vec3 from, Vec3 to) {
        assert mc.level != null;
        BlockHitResult hit = mc.level.clip(new ClipContext(
                from, to,
                ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE,
                null
        ));
        return hit.getType() == HitResult.Type.BLOCK ? new BlockPos(hit.getDirection().getNormal().offset(hit.getBlockPos())) : null;
    }


    @Override
    protected Screen createAreaScreen(Area selectedArea, BaseNotification<?> notification, AreaHelper areaHelper) {
        return new AreaScreen(new AreaScreenSymbiote(
                selectedArea, drawer, notification, this, areaHelper
        ));
    }

    @Override
    protected void openScreen(Screen screen) {
        Minecraft.getInstance().setScreen(screen);
    }
}
