package me.molybdenum.ambience_mini.client.core.render.drawer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.molybdenum.ambience_mini.engine.client.core.render.Vector2i;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.areas.Vector3i;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL11;

public class Drawer extends BaseDrawer
{
    private final Minecraft mc = Minecraft.getInstance();

    private Matrix4f pose;
    private Matrix3f normal;
    private BufferBuilder builder = null;
    private MultiBufferSource.BufferSource buffer = null;


    public void setup(PoseStack.Pose pose) {
        this.pose = pose.pose();
        this.normal = pose.normal();
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Lines
    @Override
    protected void beginLineBuilder() {
        GL11.glEnable(GL11.GL_LINE_SMOOTH);
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getRendertypeLinesShader);
        RenderSystem.lineWidth(Common.AREA_LINE_WIDTH);

        builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
    }

    @Override
    protected void drawLine(Vector3i first, Vector3i last, Color color, int alpha) {
        var size = getNormalSize(first, last);
        builder.vertex(pose, first.x(), first.y(), first.z())
                .color(color.r, color.g, color.b, alpha)
                .normal(normal, size.first(), size.second(), size.third())
                .endVertex();

        builder.vertex(pose, last.x(), last.y(), last.z())
                .color(color.r, color.g, color.b, alpha)
                .normal(normal, size.first(), size.second(), size.third())
                .endVertex();
    }

    @Override
    protected void endLineBuilder() {
        BufferUploader.drawWithShader(builder.end());
        builder = null;
        GL11.glDisable(GL11.GL_LINE_SMOOTH);
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Quads
    @Override
    protected void beginQuadBuilder() {
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();   // TODO: Use cull to avoid overlapping faces being drawn? Cannot fix when separate areas overlap....
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        builder = Tesselator.getInstance().getBuilder();
        builder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }

    @Override
    protected void drawQuad(Vector3i p1, Vector3i p2, Vector3i p3, Vector3i p4, Color color, int alpha) {
        builder.vertex(pose, p1.x(), p1.y(), p1.z())
                .color(color.r, color.g, color.b, alpha)
                .endVertex();

        builder.vertex(pose, p2.x(), p2.y(), p2.z())
                .color(color.r, color.g, color.b, alpha)
                .endVertex();

        builder.vertex(pose, p3.x(), p3.y(), p3.z())
                .color(color.r, color.g, color.b, alpha)
                .endVertex();

        builder.vertex(pose, p4.x(), p4.y(), p4.z())
                .color(color.r, color.g, color.b, alpha)
                .endVertex();
    }

    @Override
    protected void endQuadBuilder() {
        BufferUploader.drawWithShader(builder.end());
        builder = null;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Text
    @Override
    protected void beginTextBuilder() {
        buffer = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
    }

    @Override
    protected void drawText(String text, Vector2i position, Color color, int alpha) {
        mc.font.drawInBatch(text, (float)position.x(), (float)position.y(), color.toABGR32(alpha), false, pose, buffer, Font.DisplayMode.NORMAL, 0, 15728880);
    }

    @Override
    protected void endTextBuilder() {
        buffer.endBatch();
        buffer = null;
    }



    @Override
    public int getTextWidth(String text) {
        return mc.font.width(text);
    }

    @Override
    public int getLineHeight() {
        return mc.font.lineHeight;
    }
}
