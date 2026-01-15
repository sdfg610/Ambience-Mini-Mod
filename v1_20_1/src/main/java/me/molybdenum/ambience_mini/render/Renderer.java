package me.molybdenum.ambience_mini.render;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.molybdenum.ambience_mini.engine.render.BaseRenderer;
import me.molybdenum.ambience_mini.engine.core.areas.Vector3;
import net.minecraft.client.renderer.GameRenderer;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;


public class Renderer extends BaseRenderer<BufferBuilder>
{
    private Matrix4f pose;
    private Matrix3f normal;


    public void setPoseAndNormal(PoseStack.Pose pose) {
        this.pose = pose.pose();
        this.normal = pose.normal();
    }


    @Override
    protected BufferBuilder setupLineBuilder() {
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(3f);

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
        return builder;
    }

    @Override
    protected BufferBuilder setupRectangleBuilder() {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        BufferBuilder builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        return builder;
    }

    @Override
    protected void renderAndTearDown(BufferBuilder builder) {
        BufferUploader.drawWithShader(builder.end());

        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }


    @Override
    protected void drawLine(BufferBuilder builder, Vector3 first, Vector3 last) {
        int alpha = getAlpha();
        var sizes = getNormalSize(first, last);
        builder.vertex(pose, first.x(), first.y(), first.z())
                .color(red, green, blue, alpha)
                .normal(normal, sizes.first(), sizes.second(), sizes.third())
                .endVertex();

        builder.vertex(pose, last.x(), last.y(), last.z())
                .color(red, green, blue, alpha)
                .normal(normal, sizes.first(), sizes.second(), sizes.third())
                .endVertex();
    }

    @Override
    protected void drawRectangle(BufferBuilder builder, Vector3 corner1, Vector3 corner2, Vector3 corner3, Vector3 corner4) {
        int alpha = getAlpha() / 2;
        builder.vertex(pose, corner1.x(), corner1.y(), corner1.z())
                .color(red, green, blue, alpha)
                .endVertex();

        builder.vertex(pose, corner2.x(), corner2.y(), corner2.z())
                .color(red, green, blue, alpha)
                .endVertex();

        builder.vertex(pose, corner3.x(), corner3.y(), corner3.z())
                .color(red, green, blue, alpha)
                .endVertex();

        builder.vertex(pose, corner4.x(), corner4.y(), corner4.z())
                .color(red, green, blue, alpha)
                .endVertex();
    }
}
