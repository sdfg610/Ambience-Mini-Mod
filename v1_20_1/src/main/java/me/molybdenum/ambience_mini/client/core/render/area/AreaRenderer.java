package me.molybdenum.ambience_mini.client.core.render.area;

import com.mojang.blaze3d.vertex.*;
import me.molybdenum.ambience_mini.client.core.render.drawer.Drawer;
import me.molybdenum.ambience_mini.client.core.util.Notification;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.Cube;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.BaseAreaRenderer;
import me.molybdenum.ambience_mini.engine.client.core.render.areas.IAreaScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;


public class AreaRenderer extends BaseAreaRenderer<Vec3, BlockPos>
{
    private final Notification notification;
    private final Drawer drawer;
    private Frustum frustum;


    public AreaRenderer(Notification notification, Drawer drawer) {
        super(drawer);
        this.notification = notification;
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
    protected IAreaScreen<EditBox, Checkbox, Button> createAndShowAreaScreen() {
        AreaScreen screen = new AreaScreen(this, new AreaScreenSymbiote(notification, this, getSelectedArea()));
        Minecraft.getInstance().setScreen(screen);
        return screen;
    }
}
