package me.molybdenum.ambience_mini.client.core.render.area;

import com.mojang.blaze3d.vertex.*;
import me.molybdenum.ambience_mini.client.core.render.drawer.Drawer;
import me.molybdenum.ambience_mini.engine.client.core.areas.AreaHelper;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.Cube;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.BaseAreaRenderer;
import me.molybdenum.ambience_mini.engine.client.core.util.BaseNotification;
import me.molybdenum.ambience_mini.engine.shared.areas.Area;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public class AreaRenderer extends BaseAreaRenderer<Vec3, BlockPos, Screen>
{
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
