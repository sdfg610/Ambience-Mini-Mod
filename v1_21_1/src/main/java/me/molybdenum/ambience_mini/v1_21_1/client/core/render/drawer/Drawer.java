package me.molybdenum.ambience_mini.v1_21_1.client.core.render.drawer;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import me.molybdenum.ambience_mini.engine.client.core.render.Color;
import me.molybdenum.ambience_mini.engine.client.core.render.drawer.BaseDrawer;
import me.molybdenum.ambience_mini.engine.shared.Common;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector2i;
import me.molybdenum.ambience_mini.engine.shared.utils.vectors.Vector3i;
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
    private PoseStack.Pose normalPose;
    private BufferBuilder builder = null;
    private final MultiBufferSource.BufferSource buffer = mc.renderBuffers().bufferSource();


    public void setup(PoseStack.Pose pose) {
        this.pose = pose.pose();
        this.normalPose = pose;
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

        builder = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR_NORMAL);
    }

    @Override
    protected void drawLine(Vector3i first, Vector3i last, Color color, int alpha) {
        var size = getNormalSize(first, last);
        builder.addVertex(pose, first.x(), first.y(), first.z())
                .setColor(color.r, color.g, color.b, alpha)
                .setNormal(normalPose, size.first(), size.second(), size.third());

        builder.addVertex(pose, last.x(), last.y(), last.z())
                .setColor(color.r, color.g, color.b, alpha)
                .setNormal(normalPose, size.first(), size.second(), size.third());
    }

    @Override
    protected void endLineBuilder() {
        BufferUploader.drawWithShader(builder.buildOrThrow());
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
        RenderSystem.disableCull();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        builder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
    }

    @Override
    protected void drawQuad(Vector3i p1, Vector3i p2, Vector3i p3, Vector3i p4, Color color, int alpha) {
        builder.addVertex(pose, p1.x(), p1.y(), p1.z())
                .setColor(color.r, color.g, color.b, alpha);

        builder.addVertex(pose, p2.x(), p2.y(), p2.z())
                .setColor(color.r, color.g, color.b, alpha);

        builder.addVertex(pose, p3.x(), p3.y(), p3.z())
                .setColor(color.r, color.g, color.b, alpha);

        builder.addVertex(pose, p4.x(), p4.y(), p4.z())
                .setColor(color.r, color.g, color.b, alpha);
    }

    @Override
    protected void endQuadBuilder() {
        BufferUploader.drawWithShader(builder.buildOrThrow());
        builder = null;
    }


    // -----------------------------------------------------------------------------------------------------------------
    // Text
    @Override
    protected void beginTextBuilder() { }

    @Override
    protected void drawText(String text, Vector2i position, Color color, int alpha) {
        mc.font.drawInBatch(text, (float)position.x(), (float)position.y(), color.toABGR32(alpha), false, pose, buffer, Font.DisplayMode.NORMAL, 0, 15728880);
    }

    @Override
    protected void endTextBuilder() {
        buffer.endBatch();
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
